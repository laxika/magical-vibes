package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ToxicAbomination.class)
class ToxicAbominationTest extends BaseCardTest {

    @Test
    @DisplayName("ETB causes its controller to lose 2 life")
    void etbLosesTwoLife() {
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castFromHand(player1, new ToxicAbomination(), "{1}{B}");
        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Toxic Abomination");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 2);
    }

    @Test
    @DisplayName("ETB does not affect the opponent")
    void etbDoesNotAffectOpponent() {
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castFromHand(player1, new ToxicAbomination(), "{1}{B}");
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    @Test
    @DisplayName("Life loss waits for the enter trigger to resolve")
    void lifeLossWaitsForTriggerResolution() {
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castFromHand(player1, new ToxicAbomination(), "{1}{B}");
        harness.assertLife(player1, lifeBefore);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Toxic Abomination");
        assertThat(gd.stack).hasSize(1);
        harness.assertLife(player1, lifeBefore);

        resolveAllTriggers();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, lifeBefore - 2);
        harness.assertLife(player2, opponentLifeBefore);
    }
}
