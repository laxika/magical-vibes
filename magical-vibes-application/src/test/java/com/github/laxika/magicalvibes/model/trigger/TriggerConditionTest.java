package com.github.laxika.magicalvibes.model.trigger;

import com.github.laxika.magicalvibes.model.AbilityZones;
import com.github.laxika.magicalvibes.model.EffectSlot;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class TriggerConditionTest {

    @Test
    void functionsFromItsTriggersDefaultZonesUnlessGivenOthers() {
        LegacySlotTrigger trigger = new LegacySlotTrigger(EffectSlot.COMMAND_ZONE_UPKEEP_TRIGGERED);

        assertThat(new TriggerCondition(trigger).zones()).isEqualTo(AbilityZones.COMMAND);
        assertThat(new TriggerCondition(trigger, AbilityZones.EXILE).zones()).isEqualTo(AbilityZones.EXILE);
    }

    @Test
    void needsATriggerAndZones() {
        LegacySlotTrigger trigger = new LegacySlotTrigger(EffectSlot.ON_DEATH);

        assertThatThrownBy(() -> new TriggerCondition(null)).isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new TriggerCondition(null, AbilityZones.BATTLEFIELD))
                .isInstanceOf(NullPointerException.class);
        assertThatThrownBy(() -> new TriggerCondition(trigger, null)).isInstanceOf(NullPointerException.class);
    }
}
