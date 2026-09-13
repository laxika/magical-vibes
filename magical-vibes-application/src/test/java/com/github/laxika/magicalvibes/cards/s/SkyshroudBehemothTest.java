package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(SkyshroudBehemoth.class)
class SkyshroudBehemothTest extends BaseCardTest {

    @Test
    @DisplayName("Skyshroud Behemoth enters tapped with two fade counters")
    void entersTappedWithFadeCounters() {
        harness.castFromHand(player1, new SkyshroudBehemoth(), "{5}{G}{G}");
        harness.passBothPriorities();

        Permanent behemoth = findPermanent(player1, "Skyshroud Behemoth");
        assertThat(behemoth.isTapped()).isTrue();
        assertThat(behemoth.getCounterCount(CounterType.FADE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Fading removes one fade counter during Skyshroud Behemoth's controller's upkeep")
    void removesFadeCounterAtUpkeep() {
        Permanent behemoth = addCreatureReady(player1, new SkyshroudBehemoth());
        behemoth.setCounterCount(CounterType.FADE, 2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(behemoth.getCounterCount(CounterType.FADE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Skyshroud Behemoth");
    }

    @Test
    @DisplayName("Fading removes the last fade counter without sacrificing Skyshroud Behemoth")
    void removesLastFadeCounterWithoutSacrificing() {
        Permanent behemoth = addCreatureReady(player1, new SkyshroudBehemoth());
        behemoth.setCounterCount(CounterType.FADE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(behemoth.getCounterCount(CounterType.FADE)).isZero();
        harness.assertOnBattlefield(player1, "Skyshroud Behemoth");
    }

    @Test
    @DisplayName("Fading does not remove a fade counter during an opponent's upkeep")
    void doesNotRemoveFadeCounterDuringOpponentsUpkeep() {
        Permanent behemoth = addCreatureReady(player1, new SkyshroudBehemoth());
        behemoth.setCounterCount(CounterType.FADE, 1);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(behemoth.getCounterCount(CounterType.FADE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Skyshroud Behemoth");
    }

    @Test
    @DisplayName("Fading sacrifices Skyshroud Behemoth when it has no fade counters")
    void sacrificesWithoutFadeCounters() {
        addCreatureReady(player1, new SkyshroudBehemoth());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Skyshroud Behemoth");
    }
}
