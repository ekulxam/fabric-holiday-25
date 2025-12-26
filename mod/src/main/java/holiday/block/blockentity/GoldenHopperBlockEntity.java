package holiday.block.blockentity;

import net.minecraft.block.BlockState;
import net.minecraft.block.entity.BlockEntityType;
import net.minecraft.block.entity.HopperBlockEntity;
import net.minecraft.text.Text;
import net.minecraft.util.math.BlockPos;

public class GoldenHopperBlockEntity extends HopperBlockEntity {

    public GoldenHopperBlockEntity(BlockPos pos, BlockState state) {
        super(pos, state);
    }

    @Override
    protected Text getContainerName() {
        return Text.translatable("block.holiday-server-mod.golden_hopper");
    }

    @Override
    public BlockEntityType<?> getType() {
        return HolidayServerBlockEntities.GOLDEN_HOPPER_BLOCK_ENTITY;
    }
}
