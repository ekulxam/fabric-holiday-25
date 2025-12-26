package holiday.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.llamalad7.mixinextras.sugar.Share;
import com.llamalad7.mixinextras.sugar.ref.LocalBooleanRef;
import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.sugar.ref.LocalRef;
import com.mojang.serialization.Codec;
import holiday.pond.SpeedyHopperAccess;
import holiday.item.HopperMiteItem;
import holiday.tag.HolidayServerItemTags;
import net.minecraft.entity.EntityType;
import net.minecraft.entity.ItemEntity;
import net.minecraft.particle.ParticleTypes;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;
import net.minecraft.block.BlockState;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.item.ItemStack;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.Direction;
import net.minecraft.world.World;

@Mixin(HopperBlockEntity.class)
public abstract class HopperBlockEntityMixin implements SpeedyHopperAccess {
    @Unique
    private boolean speedy;

    @Unique
    private static final Codec<Boolean> SPEEDY_CODEC = Codec.BOOL;

    @Inject(
            method = "serverTick",
            at = @At("HEAD")
    )
    private static void tickHoppers(World world, BlockPos pos, BlockState state, HopperBlockEntity blockEntity, CallbackInfo ci) {
        HopperMiteItem.applyEffectsTo(world, pos, blockEntity);
        if (((SpeedyHopperAccess) blockEntity).fabricHoliday$isSpeedy()
            && world.getRandom().nextBetween(0, 100) == 0
            && world instanceof ServerWorld serverWorld
            && world.getBlockState(pos.up()).isAir()) {
            serverWorld.spawnParticles(ParticleTypes.SOUL, false, false, pos.getX() + 0.5, pos.getY() + 1, pos.getZ() + 0.5, 1, 0, 0, 0, 0.01);
        }
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

    @Inject(method = "extract(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/entity/ItemEntity;)Z", at = @At("HEAD"))
    private static void splitOverstackedItems(Inventory inventory, ItemEntity _itemEntity, CallbackInfoReturnable<Boolean> cir, @Local(argsOnly = true) LocalRef<ItemEntity> itemEntityLocalRef, @Share("split") LocalBooleanRef split, @Share("original") LocalRef<ItemEntity> original) {
        ItemEntity itemEntity = itemEntityLocalRef.get();
        original.set(itemEntity);
        ItemStack stack = itemEntity.getStack();
        int maxCount = stack.getMaxCount();
        int count = stack.getCount();
        if (count > maxCount) {
            ItemStack stack1 = stack.split(maxCount);
            ItemEntity fakeItemEntity = new ItemEntity(EntityType.ITEM, itemEntity.getEntityWorld());
            fakeItemEntity.setStack(stack1);
            itemEntityLocalRef.set(fakeItemEntity);
            split.set(true);
        }
    }

    @Inject(method = "extract(Lnet/minecraft/inventory/Inventory;Lnet/minecraft/entity/ItemEntity;)Z", at = @At("RETURN"))
    private static void mergeThemBackTogetherIfTakingFails(Inventory inventory, ItemEntity _itemEntity, CallbackInfoReturnable<Boolean> cir, @Share("split") LocalBooleanRef split, @Share("original") LocalRef<ItemEntity> original) {
        if (split.get() && !_itemEntity.getStack().isEmpty()) {
            original.get().getStack().setCount(original.get().getStack().getCount() + _itemEntity.getStack().getCount());
        }
    }

    @WrapMethod(
        method = "needsCooldown"
    )
    private boolean noCooldownIfSpeedy(Operation<Boolean> original) {
        if (this.speedy) return false;
        return original.call();
    }

    @Override
    public boolean fabricHoliday$isSpeedy() {
        return speedy;
    }

    @Override
    public void fabricHoliday$setSpeedy(boolean value) {
        this.speedy = value;
    }

    @Inject(
        method = "writeData",
        at = @At("TAIL")
    )
    private void writeSpeedy(WriteView view, CallbackInfo ci) {
        view.put("Speedy", SPEEDY_CODEC, speedy);
    }

    @Inject(
        method = "readData",
        at = @At("TAIL")
    )
    private void readSpeedy(ReadView view, CallbackInfo ci) {
        this.speedy = view.getBoolean("Speedy", false);
    }
}
