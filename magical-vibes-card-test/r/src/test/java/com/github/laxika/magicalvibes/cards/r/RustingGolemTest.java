package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RustingGolem.class})
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
    @DisplayName("Does not lose a fade counter during an opponent's upkeep")
    void doesNotFadeDuringOpponentsUpkeep() {
        Permanent golem = addCreatureReady(player1, new RustingGolem());
        golem.setCounterCount(CounterType.FADE, 5);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(golem.getCounterCount(CounterType.FADE)).isEqualTo(5);
        harness.assertOnBattlefield(player1, "Rusting Golem");
    }

    @Test
    @DisplayName("Dies as a 0/0 after fading removes its last fade counter")
    void diesWhenLastFadeCounterIsRemoved() {
        addCreatureReady(player1, new RustingGolem()).setCounterCount(CounterType.FADE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Rusting Golem");
        harness.assertInGraveyard(player1, "Rusting Golem");
    }

    @Test
    @DisplayName("Sacrifices itself during upkeep when it has no fade counters")
    void sacrificesWithoutFadeCounters() {
        Permanent golem = addCreatureReady(player1, new RustingGolem());
        golem.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Rusting Golem");
    }

    @Test
    @DisplayName("A boosted Golem survives removal of its last fade counter until the next upkeep")
    void survivesLastCounterRemovalWhenBoostedThenSacrificesNextUpkeep() {
        Permanent golem = addCreatureReady(player1, new RustingGolem());
        golem.setCounterCount(CounterType.FADE, 1);
        golem.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Rusting Golem");
        assertThat(golem.getCounterCount(CounterType.FADE)).isZero();
        assertThat(gqs.getEffectivePower(gd, golem)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, golem)).isEqualTo(1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Rusting Golem");
        harness.assertInGraveyard(player1, "Rusting Golem");
    }

    @Test
    @DisplayName("Power and toughness track fade counters immediately and ignore unrelated counters")
    void tracksFadeCountersOutsideUpkeep() {
        Permanent golem = addCreatureReady(player1, new RustingGolem());
        golem.setCounterCount(CounterType.FADE, 3);
        golem.setCounterCount(CounterType.CHARGE, 2);

        assertThat(gqs.getEffectivePower(gd, golem)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, golem)).isEqualTo(3);

        golem.setCounterCount(CounterType.FADE, 6);

        assertThat(gqs.getEffectivePower(gd, golem)).isEqualTo(6);
        assertThat(gqs.getEffectiveToughness(gd, golem)).isEqualTo(6);
    }
}
