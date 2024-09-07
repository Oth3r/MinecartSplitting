package one.oth3r.minecartsplitting;

import net.fabricmc.fabric.api.datagen.v1.DataGeneratorEntrypoint;
import net.fabricmc.fabric.api.datagen.v1.FabricDataGenerator;
import net.fabricmc.fabric.api.datagen.v1.FabricDataOutput;
import net.fabricmc.fabric.api.datagen.v1.provider.FabricRecipeProvider;
import net.minecraft.data.server.recipe.RecipeJsonProvider;
import net.minecraft.data.server.recipe.ShapelessRecipeJsonBuilder;
import net.minecraft.item.Items;
import net.minecraft.recipe.book.RecipeCategory;
import net.minecraft.util.Identifier;

import java.util.function.Consumer;

public class ModDataGenerator implements DataGeneratorEntrypoint {
	@Override
	public void onInitializeDataGenerator(FabricDataGenerator fabricDataGenerator) {
		FabricDataGenerator.Pack pack = fabricDataGenerator.createPack();
		pack.addProvider(RecipeProvider::new);
	}

	private static class RecipeProvider extends FabricRecipeProvider {


		public RecipeProvider(FabricDataOutput output) {
			super(output);
		}

		@Override
		public void generate(Consumer<RecipeJsonProvider> exporter) {
			ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, Items.TNT).input(Items.TNT_MINECART)
					.criterion(hasItem(Items.TNT_MINECART), conditionsFromItem(Items.TNT_MINECART))
					.offerTo(exporter, Identifier.of(MinecartSplitting.MOD_ID,getRecipeName(Items.TNT_MINECART)));

			ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, Items.CHEST).input(Items.CHEST_MINECART)
					.criterion(hasItem(Items.CHEST_MINECART), conditionsFromItem(Items.CHEST_MINECART))
					.offerTo(exporter, Identifier.of(MinecartSplitting.MOD_ID,getRecipeName(Items.CHEST_MINECART)));

			ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, Items.HOPPER).input(Items.HOPPER_MINECART)
					.criterion(hasItem(Items.HOPPER_MINECART), conditionsFromItem(Items.HOPPER_MINECART))
					.offerTo(exporter, Identifier.of(MinecartSplitting.MOD_ID,getRecipeName(Items.HOPPER_MINECART)));

			ShapelessRecipeJsonBuilder.create(RecipeCategory.MISC, Items.FURNACE).input(Items.FURNACE_MINECART)
					.criterion(hasItem(Items.FURNACE_MINECART), conditionsFromItem(Items.FURNACE_MINECART))
					.offerTo(exporter, Identifier.of(MinecartSplitting.MOD_ID,getRecipeName(Items.FURNACE_MINECART)));
		}
	}
}
