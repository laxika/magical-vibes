package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class InsightTest extends BaseCardTest {

    /** Player1 controls Insight; it is player2's (the opponent's) turn. */
    private void setUpOpponentTurn() {
        harness.addToBattlefield(player1, new Insight());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Opponent's green spell: you draw a card")
    void opponentGreenSpellDrawsCard() {
        setUpOpponentTurn();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castCreature(player2, 0);

        // Draw trigger sits on top of the creature spell.
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Insight");

        harness.passBothPriorities(); // resolve the draw trigger

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Opponent's non-green spell does not trigger")
    void opponentNonGreenSpellDoesNotTrigger() {
        setUpOpponentTurn();
        harness.setHand(player2, List.of(new HillGiant()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castCreature(player2, 0);

        // Only the creature spell is on the stack — no triggered ability.
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }

    @Test
    @DisplayName("Controller's own green spell does not trigger (only opponents' casts count)")
    void ownGreenSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new Insight());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castCreature(player1, 0);

        // Only the creature spell — no triggered ability.
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);

        harness.passBothPriorities();

        // Hand size unchanged aside from the cast spell leaving hand.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore - 1);
    }
}
