package one.oth3r.minecartsplitting;

import com.google.gson.JsonArray;
import com.google.gson.JsonObject;
import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.DataOutput;
import net.minecraft.data.DataProvider;
import net.minecraft.data.DataWriter;
import net.minecraft.data.recipe.RecipeExporter;
import net.minecraft.data.recipe.RecipeGenerator;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.registry.RegistryKey;
import net.minecraft.registry.RegistryKeys;
import net.minecraft.registry.RegistryWrapper;
import net.minecraft.util.Identifier;

import java.util.concurrent.CompletableFuture;

public class ModDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
		pack.addProvider(RecipeProvider::new);
		pack.addProvider(CompatRecipeProvider::new);
	}

	private static class CompatRecipeProvider implements DataProvider {
		private final DataOutput.PathResolver recipePathResolver;
		private final DataOutput.PathResolver advancementPathResolver;

		public CompatRecipeProvider(FabricDataOutput output) {
			this.recipePathResolver = output.getResolver(RegistryKeys.RECIPE);
			this.advancementPathResolver = output.getResolver(RegistryKeys.ADVANCEMENT);
		}

		@Override
		public CompletableFuture<?> run(DataWriter writer) {
			return CompletableFuture.allOf(
					compatShapelessMinecartSplit(writer, "grated_hopper_minecart", "hoppergadgetry:grated_hopper_minecart", "hoppergadgetry:grated_hopper"),
					compatShapelessMinecartSplit(writer, "chute_minecart", "hoppergadgetry:chute_minecart", "hoppergadgetry:chute")
			);
		}

		private CompletableFuture<?> compatShapelessMinecartSplit(DataWriter writer, String recipeName, String ingredientId, String resultId) {
			Identifier recipeId = Identifier.of(MinecartSplitting.MOD_ID, recipeName);
			Identifier advancementId = Identifier.of(MinecartSplitting.MOD_ID, "recipes/misc/" + recipeName);

			CompletableFuture<?> recipe = DataProvider.writeToPath(
					writer,
					shapelessCompatRecipe(ingredientId, resultId),
					recipePathResolver.resolveJson(recipeId)
			);
			CompletableFuture<?> advancement = DataProvider.writeToPath(
					writer,
					recipeAdvancement(recipeName, ingredientId, resultId, recipeId.toString()),
					advancementPathResolver.resolveJson(advancementId)
			);

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
			result.addProperty("count", 1);
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
		public String getName() {
			return "Minecart Splitting compatibility recipes";
		}
	}

	private static class RecipeProvider extends FabricRecipeProvider {

		public RecipeProvider(FabricDataOutput output, CompletableFuture<RegistryWrapper.WrapperLookup> registriesFuture) {
			super(output, registriesFuture);
		}

		@Override
		protected RecipeGenerator getRecipeGenerator(RegistryWrapper.WrapperLookup wrapperLookup, RecipeExporter recipeExporter) {
			return new RecipeGenerator(wrapperLookup, recipeExporter) {
				@Override
				public void generate() {
					createShapeless(RecipeCategory.MISC, Items.TNT).input(Items.TNT_MINECART)
							.criterion(hasItem(Items.TNT_MINECART), conditionsFromItem(Items.TNT_MINECART))
							.offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE,Identifier.of(MinecartSplitting.MOD_ID,getRecipeName(Items.TNT_MINECART))));

					createShapeless(RecipeCategory.MISC, Items.CHEST).input(Items.CHEST_MINECART)
							.criterion(hasItem(Items.CHEST_MINECART), conditionsFromItem(Items.CHEST_MINECART))
							.offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE,Identifier.of(MinecartSplitting.MOD_ID,getRecipeName(Items.CHEST_MINECART))));

					createShapeless(RecipeCategory.MISC, Items.HOPPER).input(Items.HOPPER_MINECART)
							.criterion(hasItem(Items.HOPPER_MINECART), conditionsFromItem(Items.HOPPER_MINECART))
							.offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE,Identifier.of(MinecartSplitting.MOD_ID,getRecipeName(Items.HOPPER_MINECART))));

					createShapeless(RecipeCategory.MISC, Items.FURNACE).input(Items.FURNACE_MINECART)
							.criterion(hasItem(Items.FURNACE_MINECART), conditionsFromItem(Items.FURNACE_MINECART))
							.offerTo(exporter, RegistryKey.of(RegistryKeys.RECIPE,Identifier.of(MinecartSplitting.MOD_ID,getRecipeName(Items.FURNACE_MINECART))));
				}
			};
		}

		@Override
		public String getName() {
			return "Minecart Splitting recipes";
		}
	}
}
