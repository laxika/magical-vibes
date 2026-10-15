package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ThunderBrute.class})
class ThunderBruteTest extends BaseCardTest {

    @Test
    @DisplayName("The opponent pays tribute and Thunder Brute enters with three +1/+1 counters")
    void opponentPaysTribute() {
        Permanent brute = castThunderBrute();

        harness.handleMayAbilityChosen(player2, true);

        assertThat(brute.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, brute, Keyword.HASTE)).isFalse();
    }

    @Test
    @DisplayName("Declining tribute gives Thunder Brute haste until end of turn")
    void opponentDeclinesTribute() {
        Permanent brute = castThunderBrute();

        harness.handleMayAbilityChosen(player2, false);
        harness.passBothPriorities();

        assertThat(brute.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gqs.hasKeyword(gd, brute, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Haste is granted only when the unpaid-tribute trigger resolves")
    void hasteWaitsForTriggerResolution() {
        Permanent brute = castThunderBrute();

        harness.handleMayAbilityChosen(player2, false);

        assertThat(gd.stack).hasSize(1);
        assertThat(gqs.hasKeyword(gd, brute, Keyword.HASTE)).isFalse();

        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        assertThat(gqs.hasKeyword(gd, brute, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Haste from unpaid tribute expires at cleanup")
    void hasteExpiresAtCleanup() {
        Permanent brute = castThunderBrute();
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        assertThat(gqs.hasKeyword(gd, brute, Keyword.HASTE)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, brute, Keyword.HASTE)).isFalse();
        assertThat(brute.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Paid tribute creates no haste trigger and its counters persist after cleanup")
    void paidTributeDoesNotTriggerAndCountersPersist() {
        Permanent brute = castThunderBrute();
        harness.handleMayAbilityChosen(player2, true);

        assertThat(gd.stack).isEmpty();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(brute.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(gqs.hasKeyword(gd, brute, Keyword.HASTE)).isFalse();
    }

    private Permanent castThunderBrute() {
        harness.setHand(player1, java.util.List.of(new ThunderBrute()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        return findPermanent(player1, "Thunder Brute");
    }
}
