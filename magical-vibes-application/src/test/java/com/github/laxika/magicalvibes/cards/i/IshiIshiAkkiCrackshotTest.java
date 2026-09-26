package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GoblinCohort;
import com.github.laxika.magicalvibes.cards.h.HeedTheMists;
import com.github.laxika.magicalvibes.cards.k.KamiOfFalseHope;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IshiIshiAkkiCrackshot.class, HeedTheMists.class, KamiOfFalseHope.class, GoblinCohort.class})
class IshiIshiAkkiCrackshotTest extends BaseCardTest {

    /** Player1 controls Ishi-Ishi; it is player2's (the opponent's) turn. */
    private void setUpOpponentTurn() {
        harness.addToBattlefield(player1, new IshiIshiAkkiCrackshot());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Opponent's Arcane spell: Ishi-Ishi deals 2 damage to that player")
    void opponentArcaneDealsDamage() {
        setUpOpponentTurn();
        harness.castFromHand(player2, new HeedTheMists(), "{3}{U}{U}");

        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 2);
    }

    @Test
    @DisplayName("Opponent's Spirit creature spell also triggers the damage")
    void opponentSpiritDealsDamage() {
        setUpOpponentTurn();
        harness.castFromHand(player2, new KamiOfFalseHope(), "{W}");

        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 2);
    }

    @Test
    @DisplayName("Opponent's non-Spirit, non-Arcane spell does not trigger")
    void opponentUnrelatedSpellDoesNotTrigger() {
        setUpOpponentTurn();
        harness.castFromHand(player2, new GoblinCohort(), "{R}");

        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    @Test
    @DisplayName("Controller's own Arcane spell does not trigger")
    void ownArcaneDoesNotTrigger() {
        harness.addToBattlefield(player1, new IshiIshiAkkiCrackshot());
        harness.castFromHand(player1, new HeedTheMists(), "{3}{U}{U}");

        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());

        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLifeBefore);
    }
}
