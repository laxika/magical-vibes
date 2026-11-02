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
    @EnumSource(EffectSlot.class)
    void everySlotIsALegacyTrigger(EffectSlot slot) {
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
            "ON_SELF_LOSES_CONTROL, BATTLEFIELD",
            "GRAVEYARD_ON_DAY_NIGHT_CHANGE, GRAVEYARD"
    })
    void functionsFromTheZoneItsNameStartsWith(EffectSlot slot, Zone zone) {
        assertThat(new LegacySlotTrigger(slot).defaultZones()).isEqualTo(AbilityZones.of(zone));
    }
}
