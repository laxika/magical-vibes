package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LanternKami;
import com.github.laxika.magicalvibes.cards.r.ReachThroughMists;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
        harness.setHand(player2, List.of(new ReachThroughMists()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castInstant(player2, 0);

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Ishi-Ishi, Akki Crackshot");

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 2);
    }

    @Test
    @DisplayName("Opponent's Spirit creature spell also triggers the damage")
    void opponentSpiritDealsDamage() {
        setUpOpponentTurn();
        harness.setHand(player2, List.of(new LanternKami()));
        harness.addMana(player2, ManaColor.WHITE, 1);

        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore - 2);
    }

    @Test
    @DisplayName("Opponent's non-Spirit, non-Arcane spell does not trigger")
    void opponentUnrelatedSpellDoesNotTrigger() {
        setUpOpponentTurn();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        int opponentLifeBefore = gd.playerLifeTotals.get(player2.getId());

        harness.castCreature(player2, 0);

        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(opponentLifeBefore);
    }

    @Test
    @DisplayName("Controller's own Arcane spell does not trigger")
    void ownArcaneDoesNotTrigger() {
        harness.addToBattlefield(player1, new IshiIshiAkkiCrackshot());
        harness.setHand(player1, List.of(new ReachThroughMists()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castInstant(player1, 0);

        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLifeBefore);
    }
}
