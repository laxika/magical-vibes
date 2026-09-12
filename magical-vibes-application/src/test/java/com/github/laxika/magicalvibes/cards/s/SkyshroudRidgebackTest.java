package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SkyshroudRidgeback.class)
class SkyshroudRidgebackTest extends BaseCardTest {

    @Test
    @DisplayName("Skyshroud Ridgeback enters with two fade counters")
    void entersWithFadeCounters() {
        harness.castFromHand(player1, new SkyshroudRidgeback(), "{G}");
        harness.passBothPriorities();

        Permanent ridgeback = findPermanent(player1, "Skyshroud Ridgeback");
        assertThat(ridgeback.getCounterCount(CounterType.FADE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Fading removes one fade counter during its controller's upkeep")
    void removesFadeCounterAtUpkeep() {
        Permanent ridgeback = addCreatureReady(player1, new SkyshroudRidgeback());
        ridgeback.setCounterCount(CounterType.FADE, 2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(ridgeback.getCounterCount(CounterType.FADE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Skyshroud Ridgeback");
    }

    @Test
    @DisplayName("Fading removes the last fade counter without sacrificing Skyshroud Ridgeback")
    void removesLastFadeCounterWithoutSacrificing() {
        Permanent ridgeback = addCreatureReady(player1, new SkyshroudRidgeback());
        ridgeback.setCounterCount(CounterType.FADE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(ridgeback.getCounterCount(CounterType.FADE)).isZero();
        harness.assertOnBattlefield(player1, "Skyshroud Ridgeback");
    }

    @Test
    @DisplayName("Fading does not remove a fade counter during an opponent's upkeep")
    void doesNotRemoveFadeCounterDuringOpponentsUpkeep() {
        Permanent ridgeback = addCreatureReady(player1, new SkyshroudRidgeback());
        ridgeback.setCounterCount(CounterType.FADE, 2);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(ridgeback.getCounterCount(CounterType.FADE)).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Skyshroud Ridgeback");
    }

    @Test
    @DisplayName("Fading sacrifices Skyshroud Ridgeback when it has no fade counters")
    void sacrificesWithoutFadeCounters() {
        addCreatureReady(player1, new SkyshroudRidgeback());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Skyshroud Ridgeback");
    }
}
