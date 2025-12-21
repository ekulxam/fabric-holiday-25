package holiday.mixin;

import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import com.google.common.collect.BiMap;

import net.minecraft.loot.context.LootContextTypes;
import net.minecraft.util.Identifier;
import net.minecraft.util.context.ContextType;

@Mixin(LootContextTypes.class)
public interface LootContextTypesAccessor {
    @Accessor
    public static BiMap<Identifier, ContextType> getMAP() {
        throw new AssertionError();
    }
}
