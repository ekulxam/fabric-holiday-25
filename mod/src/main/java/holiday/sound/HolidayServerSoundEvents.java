package holiday.sound;

import holiday.CommonEntrypoint;
import net.minecraft.registry.Registries;
import net.minecraft.registry.Registry;
import net.minecraft.sound.SoundEvent;
import net.minecraft.util.Identifier;

public final class HolidayServerSoundEvents {
    public static final SoundEvent ITEM_HOPPER_MITE_EAT = register("item.hopper_mite.eat");

    private HolidayServerSoundEvents() {
    }

    public static void register() {
        return;
    }

    public static SoundEvent register(String path) {
        Identifier id = CommonEntrypoint.identifier(path);
        SoundEvent sound = SoundEvent.of(id);

        Registry.register(Registries.SOUND_EVENT, id, sound);
        return sound;
    }
}
