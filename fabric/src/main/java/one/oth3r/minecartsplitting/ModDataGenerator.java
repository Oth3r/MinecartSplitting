package one.oth3r.minecartsplitting;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.CachedOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.PackOutput;
import net.minecraft.data.recipes.RecipeOutput;
import net.minecraft.world.item.Items;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.resources.ResourceKey;
import net.minecraft.core.registries.Registries;
import net.minecraft.core.HolderLookup;
import net.minecraft.resources.Identifier;
import org.jspecify.annotations.NonNull;

import java.util.concurrent.CompletableFuture;

public class ModDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
		pack.addProvider(RecipeProvider::new);
		pack.addProvider(CompatRecipeProvider::new);
	}

	private static class CompatRecipeProvider implements DataProvider {
		private final PackOutput.PathProvider recipePathProvider;
		private final PackOutput.PathProvider recipeAdvancementPathProvider;

		public CompatRecipeProvider(FabricPackOutput output) {
			this.recipePathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "recipe");
			this.recipeAdvancementPathProvider = output.createPathProvider(PackOutput.Target.DATA_PACK, "advancement/recipes/misc");
		}

		@Override
		public @NonNull CompletableFuture<?> run(@NonNull CachedOutput output) {
			return CompletableFuture.allOf(
					compatShapelessMinecartSplit(output, "grated_hopper_minecart", "hoppergadgetry:grated_hopper_minecart", "hoppergadgetry:grated_hopper"),
					compatShapelessMinecartSplit(output, "chute_minecart", "hoppergadgetry:chute_minecart", "hoppergadgetry:chute")
			);
		}

		private CompletableFuture<?> compatShapelessMinecartSplit(CachedOutput output, String recipeName, String ingredientId, String resultId) {
			Identifier recipeId = Identifier.fromNamespaceAndPath(MinecartSplitting.MOD_ID, recipeName);

			CompletableFuture<?> recipe = DataProvider.saveStable(output,
					shapelessCompatRecipe(ingredientId, resultId),
					recipePathProvider.json(recipeId));
			CompletableFuture<?> advancement = DataProvider.saveStable(output,
					recipeAdvancement(recipeName, ingredientId, resultId, recipeId.toString()),
					recipeAdvancementPathProvider.json(recipeId));

			return CompletableFuture.allOf(recipe, advancement);
		}

		private static JsonObject shapelessCompatRecipe(String ingredientId, String resultId) {
			JsonObject recipe = new JsonObject();
			recipe.add("fabric:load_conditions", registryContainsItems(ingredientId, resultId));
			recipe.addProperty("type", "minecraft:crafting_shapeless");
			recipe.addProperty("category", "misc");

			JsonArray ingredients = new JsonArray();
			ingredients.add(ingredientId);
			recipe.add("ingredients", ingredients);

			JsonObject result = new JsonObject();
			result.addProperty("id", resultId);
			recipe.add("result", result);

			return recipe;
		}

		private static JsonObject recipeAdvancement(String criterionName, String ingredientId, String resultId, String recipeId) {
			JsonObject advancement = new JsonObject();
			advancement.add("fabric:load_conditions", registryContainsItems(ingredientId, resultId));
			advancement.addProperty("parent", "minecraft:recipes/root");

			JsonObject criteria = new JsonObject();
			criteria.add("has_" + criterionName, inventoryChangedCriterion(ingredientId));
			criteria.add("has_the_recipe", recipeUnlockedCriterion(recipeId));
			advancement.add("criteria", criteria);

			JsonArray requirementGroup = new JsonArray();
			requirementGroup.add("has_the_recipe");
			requirementGroup.add("has_" + criterionName);

			JsonArray requirements = new JsonArray();
			requirements.add(requirementGroup);
			advancement.add("requirements", requirements);

			JsonObject rewards = new JsonObject();
			JsonArray recipes = new JsonArray();
			recipes.add(recipeId);
			rewards.add("recipes", recipes);
			advancement.add("rewards", rewards);

			return advancement;
		}

		private static JsonObject inventoryChangedCriterion(String itemId) {
			JsonObject criterion = new JsonObject();
			criterion.addProperty("trigger", "minecraft:inventory_changed");

			JsonObject conditions = new JsonObject();
			JsonArray items = new JsonArray();
			JsonObject item = new JsonObject();
			item.addProperty("items", itemId);
			items.add(item);
			conditions.add("items", items);
			criterion.add("conditions", conditions);

			return criterion;
		}

		private static JsonObject recipeUnlockedCriterion(String recipeId) {
			JsonObject criterion = new JsonObject();
			criterion.addProperty("trigger", "minecraft:recipe_unlocked");

			JsonObject conditions = new JsonObject();
			conditions.addProperty("recipe", recipeId);
			criterion.add("conditions", conditions);

			return criterion;
		}

		private static JsonArray registryContainsItems(String... itemIds) {
			JsonArray conditions = new JsonArray();
			JsonObject condition = new JsonObject();
			condition.addProperty("condition", "fabric:registry_contains");
			condition.addProperty("registry", "minecraft:item");

			JsonArray values = new JsonArray();
			for (String itemId : itemIds) {
				values.add(itemId);
			}
			condition.add("values", values);
			conditions.add(condition);

			return conditions;
		}

		@Override
		public @NonNull String getName() {
			return "Minecart Splitting compatibility recipes";
		}
	}

	private static class RecipeProvider extends FabricRecipeProvider {
		public RecipeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
			super(output, registriesFuture);
		}

		@Override
		protected net.minecraft.data.recipes.@NonNull RecipeProvider createRecipeProvider(HolderLookup.@NonNull Provider wrapperLookup, @NonNull RecipeOutput recipeExporter) {
			return new net.minecraft.data.recipes.RecipeProvider(wrapperLookup, recipeExporter) {
				@Override
				public void buildRecipes() {
					shapeless(RecipeCategory.MISC, Items.TNT).requires(Items.TNT_MINECART)
							.unlockedBy(getHasName(Items.TNT_MINECART), has(Items.TNT_MINECART))
							.save(output, ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(MinecartSplitting.MOD_ID, getSimpleRecipeName(Items.TNT_MINECART))));

					shapeless(RecipeCategory.MISC, Items.CHEST).requires(Items.CHEST_MINECART)
							.unlockedBy(getHasName(Items.CHEST_MINECART), has(Items.CHEST_MINECART))
							.save(output, ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(MinecartSplitting.MOD_ID, getSimpleRecipeName(Items.CHEST_MINECART))));

					shapeless(RecipeCategory.MISC, Items.HOPPER).requires(Items.HOPPER_MINECART)
							.unlockedBy(getHasName(Items.HOPPER_MINECART), has(Items.HOPPER_MINECART))
							.save(output, ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(MinecartSplitting.MOD_ID, getSimpleRecipeName(Items.HOPPER_MINECART))));

					shapeless(RecipeCategory.MISC, Items.FURNACE).requires(Items.FURNACE_MINECART)
							.unlockedBy(getHasName(Items.FURNACE_MINECART), has(Items.FURNACE_MINECART))
							.save(output, ResourceKey.create(Registries.RECIPE, Identifier.fromNamespaceAndPath(MinecartSplitting.MOD_ID, getSimpleRecipeName(Items.FURNACE_MINECART))));
				}
			};
		}

		@Override
		public @NonNull String getName() {
			return "Minecart Splitting recipes";
		}
	}
}
