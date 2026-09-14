package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(PhyrexianProwler.class)
class PhyrexianProwlerTest extends BaseCardTest {

    @Test
    @DisplayName("Phyrexian Prowler enters with three fade counters")
    void entersWithFadeCounters() {
        harness.castFromHand(player1, new PhyrexianProwler(), "{3}{B}");
        harness.passBothPriorities();

        Permanent prowler = findPermanent(player1, "Phyrexian Prowler");
        assertThat(prowler.getCounterCount(CounterType.FADE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Fading removes one fade counter during its controller's upkeep")
    void removesFadeCounterAtUpkeep() {
        Permanent prowler = addCreatureReady(player1, new PhyrexianProwler());
        prowler.setCounterCount(CounterType.FADE, 2);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(prowler.getCounterCount(CounterType.FADE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Phyrexian Prowler");
    }

    @Test
    @DisplayName("Fading removes the last fade counter without sacrificing Phyrexian Prowler")
    void removesLastFadeCounterWithoutSacrificing() {
        Permanent prowler = addCreatureReady(player1, new PhyrexianProwler());
        prowler.setCounterCount(CounterType.FADE, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(prowler.getCounterCount(CounterType.FADE)).isZero();
        harness.assertOnBattlefield(player1, "Phyrexian Prowler");
    }

    @Test
    @DisplayName("Fading does not remove a fade counter during an opponent's upkeep")
    void doesNotRemoveFadeCounterDuringOpponentsUpkeep() {
        Permanent prowler = addCreatureReady(player1, new PhyrexianProwler());
        prowler.setCounterCount(CounterType.FADE, 1);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(prowler.getCounterCount(CounterType.FADE)).isEqualTo(1);
        harness.assertOnBattlefield(player1, "Phyrexian Prowler");
    }

    @Test
    @DisplayName("Fading sacrifices Phyrexian Prowler when it has no fade counters")
    void sacrificesWithoutFadeCounters() {
        addCreatureReady(player1, new PhyrexianProwler());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Phyrexian Prowler");
    }

    @Test
    @DisplayName("Removing a fade counter gives Phyrexian Prowler +1/+1 until end of turn")
    void removesCounterAndBoostsSelf() {
        Permanent prowler = addCreatureReady(player1, new PhyrexianProwler());
        prowler.setCounterCount(CounterType.FADE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(prowler.getCounterCount(CounterType.FADE)).isZero();
        assertThat(gqs.getEffectivePower(gd, prowler)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, prowler)).isEqualTo(4);
    }

    @Test
    @DisplayName("The activated ability cannot be used without a fade counter")
    void cannotActivateWithoutFadeCounter() {
        Permanent prowler = addCreatureReady(player1, new PhyrexianProwler());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(prowler.getCounterCount(CounterType.FADE)).isZero();
    }

    @Test
    @DisplayName("The activated ability's boost wears off at end of turn")
    void boostWearsOffAtEndOfTurn() {
        Permanent prowler = addCreatureReady(player1, new PhyrexianProwler());
        prowler.setCounterCount(CounterType.FADE, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, prowler)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, prowler)).isEqualTo(4);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, prowler)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, prowler)).isEqualTo(3);
    }
}
