package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.f.Fog;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HornedTurtle;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Insight.class, GrizzlyBears.class, HornedTurtle.class, Fog.class, Forest.class})
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
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");

        // Draw trigger sits on top of the creature spell.
        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Insight");

        harness.passBothPriorities(); // resolve the draw trigger

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Opponent's green noncreature spell: you draw a card")
    void opponentGreenNoncreatureSpellDrawsCard() {
        setUpOpponentTurn();
        harness.castFromHand(player2, new Fog(), "{G}");

        int handBefore = gd.playerHands.get(player1.getId()).size();

        assertThat(gd.stack).hasSize(2);
        assertThat(gd.stack.getLast().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(gd.stack.getLast().getCard().getName()).isEqualTo("Insight");

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Opponent's non-green spell does not trigger")
    void opponentNonGreenSpellDoesNotTrigger() {
        setUpOpponentTurn();
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castFromHand(player2, new HornedTurtle(), "{2}{U}");

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
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");

        // Only the creature spell — no triggered ability.
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);

        harness.passBothPriorities();

        // Hand size unchanged aside from the cast spell leaving hand.
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Opponent's land play does not trigger")
    void opponentLandPlayDoesNotTrigger() {
        setUpOpponentTurn();
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.setHand(player2, List.of(new Forest()));
        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
    }
}
