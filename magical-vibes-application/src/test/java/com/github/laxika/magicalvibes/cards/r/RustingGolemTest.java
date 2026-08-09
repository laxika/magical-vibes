package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RustingGolemTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with five fade counters and is 5/5")
    void entersWithFadeCountersAndMatchesTheirCount() {
        harness.setHand(player1, List.of(new RustingGolem()));
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent golem = findPermanent(player1, "Rusting Golem");
        assertThat(golem.getCounterCount(CounterType.FADE)).isEqualTo(5);
        assertThat(gqs.getEffectivePower(gd, golem)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, golem)).isEqualTo(5);
    }

    @Test
    @DisplayName("Loses one fade counter and becomes 4/4 during upkeep")
    void fadesDuringUpkeep() {
        Permanent golem = addCreatureReady(player1, new RustingGolem());
        golem.setCounterCount(CounterType.FADE, 5);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(golem.getCounterCount(CounterType.FADE)).isEqualTo(4);
        assertThat(gqs.getEffectivePower(gd, golem)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, golem)).isEqualTo(4);
    }

    @Test
    @DisplayName("Sacrifices itself during upkeep when it has no fade counters")
    void sacrificesWithoutFadeCounters() {
        Permanent golem = addCreatureReady(player1, new RustingGolem());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Rusting Golem");
    }
}
