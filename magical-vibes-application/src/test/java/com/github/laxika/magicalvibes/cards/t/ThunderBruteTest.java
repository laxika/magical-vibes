package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
