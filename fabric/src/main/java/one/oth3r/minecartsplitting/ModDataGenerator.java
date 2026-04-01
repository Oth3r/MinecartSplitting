package one.oth3r.minecartsplitting;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricPackOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
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
	}

	private static class RecipeProvider extends FabricRecipeProvider {
		public RecipeProvider(FabricPackOutput output, CompletableFuture<HolderLookup.Provider> registriesFuture) {
			super(output, registriesFuture);
		}

		@Override
		protected net.minecraft.data.recipes.@NonNull RecipeProvider createRecipeProvider(HolderLookup.@NonNull Provider wrapperLookup, RecipeOutput recipeExporter) {
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
		public String getName() {
			return "";
		}
	}
}
