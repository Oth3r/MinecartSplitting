package one.oth3r.minecartsplitting.mixin;

import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.vehicle.minecart.AbstractMinecart;
import net.minecraft.world.item.*;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

@Mixin(MinecartItem.class)
public abstract class MinecartItemMixin extends Item {

    @Shadow @Final private EntityType<? extends AbstractMinecart> type;

    public MinecartItemMixin(Properties settings) {
        super(settings);
    }

    @Override
    public @Nullable ItemStackTemplate getCraftingRemainder(ItemStack stack) {
        // don't empty if its already empty
        if (this.type.equals(EntityType.MINECART)) return super.getCraftingRemainder(stack);
        // return the mine cart
        return ItemStackTemplate.fromNonEmptyStack(Items.MINECART.getDefaultInstance());
    }

}