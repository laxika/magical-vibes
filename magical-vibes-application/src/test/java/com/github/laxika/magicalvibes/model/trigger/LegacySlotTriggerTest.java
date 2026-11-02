package com.github.laxika.magicalvibes.model.trigger;

import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThatThrownBy;

class LegacySlotTriggerTest {

    @Test
    void needsASlot() {
        assertThatThrownBy(() -> new LegacySlotTrigger(null)).isInstanceOf(NullPointerException.class);
    }
}
