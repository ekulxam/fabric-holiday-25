package holiday.mixin;

import java.util.Iterator;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import holiday.loot.HolidayServerLootContextTypes;
import holiday.loot.HolidayServerLootTables;
import it.unimi.dsi.fastutil.objects.ObjectArrayList;
import net.minecraft.block.BlockState;
import net.minecraft.block.RedstoneTorchBlock;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.item.ItemStack;
import net.minecraft.loot.LootTable;
import net.minecraft.loot.context.LootContextParameters;
import net.minecraft.loot.context.LootWorldContext;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.util.Util;
import net.minecraft.util.math.BlockPos;
import net.minecraft.util.math.random.Random;

@Mixin(RedstoneTorchBlock.class)
public class RedstoneTorchBlockMixin {
    @Inject(
            method = "scheduledTick",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/server/world/ServerWorld;scheduleBlockTick(Lnet/minecraft/util/math/BlockPos;Lnet/minecraft/block/Block;I)V"
            )
    )
    private void tryAttractHopperMite(BlockState state, ServerWorld world, BlockPos pos, Random random, CallbackInfo ci) {
        world.getBlockEntity(pos.down(), BlockEntityType.HOPPER).ifPresent(hopper -> {
            LootTable lootTable = world.getServer().getReloadableRegistries().getLootTable(HolidayServerLootTables.HOPPER_MITE_ATTRACTION_GAMEPLAY);

            Iterable<ItemStack> items = lootTable.generateLoot(new LootWorldContext.Builder(world)
                    .add(LootContextParameters.ORIGIN, pos.toCenterPos())
                    .add(LootContextParameters.BLOCK_STATE, state)
                    .build(HolidayServerLootContextTypes.BURN_OUT));

            Iterator<ItemStack> iterator = items.iterator();

            int size = hopper.size();
            ObjectArrayList<Integer> slots = new ObjectArrayList<>(size);

            for (int slot = 0; slot < size; slot++) {
                if (hopper.getStack(slot).isEmpty()) {
                    slots.add(slot);
                }
            }

            if (!slots.isEmpty()) {
                Util.shuffle(slots, random);

                for (int slot : slots) {
                    if (iterator.hasNext()) {
                        hopper.setStack(slot, iterator.next());
                    }
                }
            }
        });
    }
}
