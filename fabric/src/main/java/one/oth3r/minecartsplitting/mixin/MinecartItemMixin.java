package one.oth3r.minecartsplitting.mixin;

import net.minecraft.entity.vehicle.AbstractMinecartEntity;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.item.MinecartItem;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MinecartItem.class)
public abstract class MinecartItemMixin extends Item {

    @Shadow @Final private AbstractMinecartEntity.Type type;

    public MinecartItemMixin(Settings settings) {
        super(settings);
    }

    @Override
    public ItemStack getRecipeRemainder(ItemStack stack) {
        // don't empty if its already empty
        if (this.type.equals(AbstractMinecartEntity.Type.RIDEABLE)) return super.getRecipeRemainder(stack);
        // return the mine cart
        return Items.MINECART.getDefaultStack();
    }

}