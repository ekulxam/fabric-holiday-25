package holiday.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.llamalad7.mixinextras.sugar.Local;
import holiday.block.HolidayServerBlocks;
import holiday.block.blockentity.GoldenHopperBlockEntity;
import holiday.block.blockentity.HolidayServerBlockEntities;
import net.minecraft.block.entity.BlockEntityType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.*;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import holiday.item.HopperMiteItem;
import holiday.tag.HolidayServerItemTags;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

@Mixin(HopperBlockEntity.class)
public class HopperBlockEntityMixin {
    @Inject(
            method = "serverTick",
            at = @At("HEAD")
    )
    private static void applyHopperMiteEffects(World world, BlockPos pos, BlockState state, HopperBlockEntity blockEntity, CallbackInfo ci) {
        HopperMiteItem.applyEffectsTo(world, pos, blockEntity);
    }

    @Inject(
            method = "canInsert",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void preventInsertingHopperTrapped(Inventory inventory, ItemStack stack, int slot, Direction side, CallbackInfoReturnable<Boolean> ci) {
        if (stack.isIn(HolidayServerItemTags.HOPPER_TRAPPED)) {
            ci.setReturnValue(false);
        }
    }

    @Inject(
            method = "canExtract",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void preventExtractingHopperTrapped(Inventory hopperInventory, Inventory fromInventory, ItemStack stack, int slot, Direction facing, CallbackInfoReturnable<Boolean> ci) {
        if (stack.isIn(HolidayServerItemTags.HOPPER_TRAPPED)) {
            ci.setReturnValue(false);
        }
    }

    @Inject(
            method = "transfer(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/inventory/Inventory;Lnet/minecraft/item/ItemStack;ILnet/minecraft/util/math/Direction;)Lnet/minecraft/item/ItemStack;",
            at = @At("HEAD"),
            cancellable = true
    )
    private static void preventTransferringMiteFood(Inventory from, Inventory to, ItemStack stack, int slot, Direction side, CallbackInfoReturnable<ItemStack> ci) {
        if (from != null && HopperMiteItem.isFood(from, stack)) {
            ci.setReturnValue(stack);
        }
    }

    @ModifyArg(
        method = "<init>",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/LootableContainerBlockEntity;<init>(Lnet/minecraft/block/entity/BlockEntityType;Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/BlockState;)V")
    )
    private static BlockEntityType<?> goldenHopperMoment(BlockEntityType<?> blockEntityType, BlockPos pos, BlockState state) {
        if (state.isOf(HolidayServerBlocks.GOLDEN_HOPPER)) return HolidayServerBlockEntities.GOLDEN_HOPPER_BLOCK_ENTITY;
        return blockEntityType;
    }

    @WrapOperation(
        method = "insert",
        at = @At(value = "INVOKE", target = "Lnet/minecraft/block/entity/HopperBlockEntity;removeStack(II)Lnet/minecraft/item/ItemStack;")
    )
    private static ItemStack fastGolden(HopperBlockEntity instance, int slot, int amount, Operation<ItemStack> original) {
        if (instance instanceof GoldenHopperBlockEntity) return original.call(instance, slot, instance.getStack(slot).getCount());
        return original.call(instance, slot, amount);
    }
}
