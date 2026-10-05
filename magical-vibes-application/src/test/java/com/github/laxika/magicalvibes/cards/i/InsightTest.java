package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.b.BayouDragonfly;
import com.github.laxika.magicalvibes.cards.c.Counterspell;
import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.f.Firefly;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.MirrisGuile;
import com.github.laxika.magicalvibes.cards.s.SegmentedWurm;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BayouDragonfly.class, Counterspell.class, Disenchant.class, Firefly.class,
        Forest.class, Insight.class, MirrisGuile.class, SegmentedWurm.class})
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

        harness.castFromHand(player2, new BayouDragonfly(), "{1}{G}");

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

        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castFromHand(player2, new MirrisGuile(), "{G}");

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

        harness.castFromHand(player2, new Firefly(), "{3}{R}");

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

        harness.castFromHand(player1, new BayouDragonfly(), "{1}{G}");

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

    @Test
    @DisplayName("A multicolored green spell draws exactly one card")
    void multicoloredGreenSpellDrawsOneCard() {
        setUpOpponentTurn();
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castFromHand(player2, new SegmentedWurm(), "{3}{R}{G}");

        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Each Insight triggers independently for the same spell")
    void multipleInsightsEachDrawOneCard() {
        setUpOpponentTurn();
        harness.addToBattlefield(player1, new Insight());
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.castFromHand(player2, new BayouDragonfly(), "{1}{G}");

        assertThat(gd.stack).hasSize(3);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 2);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Destroying Insight in response does not stop its draw trigger")
    void drawTriggerSurvivesSourceDestruction() {
        setUpOpponentTurn();
        var insightId = harness.getPermanentId(player1, "Insight");
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.castFromHand(player2, new BayouDragonfly(), "{1}{G}");

        harness.setHand(player2, List.of(new Disenchant()));
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player2, 0, insightId);

        harness.assertNotOnBattlefield(player1, "Insight");
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
    }

    @Test
    @DisplayName("Countering the green spell does not stop Insight's draw trigger")
    void drawTriggerSurvivesCounteringSpell() {
        setUpOpponentTurn();
        var greenSpell = new BayouDragonfly();
        harness.castFromHand(player2, greenSpell, "{1}{G}");

        harness.setHand(player1, List.of(new Counterspell()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.castAndResolveInstant(player1, 0, greenSpell.getId());

        harness.assertInGraveyard(player2, "Bayou Dragonfly");
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).isEmpty();
    }
}
