package com.github.laxika.magicalvibes.cards.w;

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

class WarmthTest extends BaseCardTest {

    /** Player1 controls Warmth; it is player2's (the opponent's) turn. */
    private void setUpOpponentTurn() {
        harness.addToBattlefield(player1, new Warmth());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    @DisplayName("Opponent's red spell: you gain 2 life")
    void opponentRedSpellGainsLife() {
        setUpOpponentTurn();
        harness.setHand(player2, List.of(new HillGiant()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 4);

        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castCreature(player2, 0);

        // Gain-life trigger sits on top of the creature spell.
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Warmth");

        harness.passBothPriorities(); // resolve the gain-life trigger

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLifeBefore + 2);
    }

    @Test
    @DisplayName("Opponent's non-red spell does not trigger")
    void opponentNonRedSpellDoesNotTrigger() {
        setUpOpponentTurn();
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castCreature(player2, 0);

        // Only the creature spell is on the stack — no triggered ability.
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLifeBefore);
    }

    @Test
    @DisplayName("Controller's own red spell does not trigger (only opponents' casts count)")
    void ownRedSpellDoesNotTrigger() {
        harness.addToBattlefield(player1, new Warmth());
        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        int controllerLifeBefore = gd.playerLifeTotals.get(player1.getId());

        harness.castCreature(player1, 0);

        // Only the creature spell — no triggered ability.
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);

        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLifeBefore);
    }
}
