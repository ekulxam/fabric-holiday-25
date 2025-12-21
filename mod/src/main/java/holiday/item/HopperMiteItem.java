package holiday.item;

import java.util.HashSet;
import java.util.Set;
import java.util.function.Consumer;

import holiday.component.HolidayServerDataComponentTypes;
import holiday.sound.HolidayServerSoundEvents;
import net.minecraft.component.type.TooltipDisplayComponent;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.inventory.Inventory;
import net.minecraft.inventory.StackReference;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.tooltip.TooltipType;
import net.minecraft.registry.entry.RegistryEntry;
import net.minecraft.screen.slot.Slot;
import net.minecraft.sound.SoundCategory;
import net.minecraft.text.Text;
import net.minecraft.util.ClickType;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.BlockPos;
import net.minecraft.world.World;

public class HopperMiteItem extends Item {
    public HopperMiteItem(Settings settings) {
        super(settings);
    }

    @Override
    public boolean onClicked(ItemStack stack, ItemStack otherStack, Slot slot, ClickType clickType, PlayerEntity player, StackReference cursorStackReference) {
        if (clickType == ClickType.RIGHT) {
            if (otherStack.isEmpty() || stack.getItem() == otherStack.getItem()) {
                stack.remove(HolidayServerDataComponentTypes.MITE_FOOD);
            } else {
                stack.set(HolidayServerDataComponentTypes.MITE_FOOD, otherStack.getItem().getRegistryEntry());
            }

            return true;
        }

        return super.onClicked(stack, otherStack, slot, clickType, player, cursorStackReference);
    }

    @Override
    public void appendTooltip(ItemStack stack, TooltipContext context, TooltipDisplayComponent displayComponent, Consumer<Text> textConsumer, TooltipType type) {
        RegistryEntry<Item> item = stack.get(HolidayServerDataComponentTypes.MITE_FOOD);

        if (item != null) {
            Text name = item.value().getName();
            textConsumer.accept(Text.translatable("item.holiday-server-mod.hopper_mite.tooltip", name).formatted(Formatting.GRAY));
        }
    }

    private static void playEatSound(World world, BlockPos pos) {
        world.playSound(null, pos, HolidayServerSoundEvents.ITEM_HOPPER_MITE_EAT, SoundCategory.BLOCKS, 0.2f, 1);
    }

    public static void applyEffectsTo(World world, BlockPos pos, Inventory inventory) {
        int size = inventory.size();
        Set<Item> items = new HashSet<>(size);

        for (int slot = 0; slot < size; slot++) {
            ItemStack stack = inventory.getStack(slot);
            RegistryEntry<Item> item = stack.get(HolidayServerDataComponentTypes.MITE_FOOD);

            if (item != null) {
                items.add(item.value());
            }
        }

        boolean eaten = false;

        for (int slot = 0; slot < size; slot++) {
            ItemStack stack = inventory.getStack(slot);

            if (items.contains(stack.getItem())) {
                stack.decrement(1);
                eaten = true;
            }
        }

        if (eaten) {
            HopperMiteItem.playEatSound(world, pos);
        }
    }
}
