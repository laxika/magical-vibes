package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Zone;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.EnumSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LegacySlotTriggerTest {

    @ParameterizedTest
    @EnumSource(value = EffectSlot.class, names = {"SPELL", "STATIC", "COMMAND_ZONE_STATIC"})
    void slotsOfSpellAndStaticAbilitiesAreNotTriggers(EffectSlot slot) {
        assertThatThrownBy(() -> new LegacySlotTrigger(slot)).isInstanceOf(IllegalArgumentException.class);
    }

    @ParameterizedTest
    @EnumSource(value = EffectSlot.class, mode = EnumSource.Mode.EXCLUDE,
            names = {"SPELL", "STATIC", "COMMAND_ZONE_STATIC"})
    void everyOtherSlotIsALegacyTrigger(EffectSlot slot) {
        LegacySlotTrigger trigger = new LegacySlotTrigger(slot);

        assertThat(trigger.slot()).isEqualTo(slot);
        assertThat(trigger.defaultZones().zones()).hasSize(1);
    }

    @Test
    void needsASlot() {
        assertThatThrownBy(() -> new LegacySlotTrigger(null)).isInstanceOf(NullPointerException.class);
    }

    @ParameterizedTest
    @CsvSource({
            "ON_DEATH, BATTLEFIELD",
            "ON_TAP, BATTLEFIELD",
            "GRAVEYARD_UPKEEP_TRIGGERED, GRAVEYARD",
            "GRAVEYARD_DRAW_REPLACEMENT, GRAVEYARD",
            "COMMAND_ZONE_UPKEEP_TRIGGERED, COMMAND",
            "EXILE_ON_CONTROLLER_CASTS_SPELL, EXILE",
            "EXILED_UPKEEP_TRIGGERED, EXILE",
            "HAND_ON_CONTROLLER_CASTS_SPELL, HAND"
    })
    void functionsFromTheZoneItsNameStartsWith(EffectSlot slot, Zone zone) {
        assertThat(new LegacySlotTrigger(slot).defaultZones()).isEqualTo(AbilityZones.of(zone));
    }
}
