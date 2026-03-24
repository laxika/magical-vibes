package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.RaidConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardPutIntoHandAndLoseLifeEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RuinRaiderTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has RaidConditionalEffect wrapping RevealTopCardPutIntoHandAndLoseLifeEffect on CONTROLLER_END_STEP_TRIGGERED")
    void hasCorrectEffectConfiguration() {
        RuinRaider card = new RuinRaider();

        assertThat(card.getEffects(EffectSlot.CONTROLLER_END_STEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.CONTROLLER_END_STEP_TRIGGERED).getFirst())
                .isInstanceOf(RaidConditionalEffect.class);
        RaidConditionalEffect raid =
                (RaidConditionalEffect) card.getEffects(EffectSlot.CONTROLLER_END_STEP_TRIGGERED).getFirst();
        assertThat(raid.wrapped()).isInstanceOf(RevealTopCardPutIntoHandAndLoseLifeEffect.class);
    }

    // ===== Raid trigger: reveal top card, draw, lose life =====

    @Test
    @DisplayName("When raid met, reveals top card, puts it into hand, and loses life equal to mana value")
    void raidMetRevealsAndDrawsAndLosesLife() {
        harness.addToBattlefield(player1, new RuinRaider());
        harness.setHand(player1, List.of());
        Card topCard = new GrizzlyBears(); // MV 2
        gd.playerDecks.get(player1.getId()).addFirst(topCard);
        harness.setLife(player1, 20);

        markAttackedThisTurn();
        advanceToEndStep();

        // Resolve the raid trigger
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(topCard.getId()));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("When raid met, revealing a land (mana value 0) causes no life loss")
    void raidMetRevealingLandCausesNoLifeLoss() {
        harness.addToBattlefield(player1, new RuinRaider());
        harness.setHand(player1, List.of());
        Card topCard = new Forest(); // MV 0
        gd.playerDecks.get(player1.getId()).addFirst(topCard);
        harness.setLife(player1, 20);

        markAttackedThisTurn();
        advanceToEndStep();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getId().equals(topCard.getId()));
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("When raid met, card is removed from the top of the library")
    void raidMetCardRemovedFromLibrary() {
        harness.addToBattlefield(player1, new RuinRaider());
        harness.setHand(player1, List.of());
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);
        int deckSizeBefore = gd.playerDecks.get(player1.getId()).size();

        markAttackedThisTurn();
        advanceToEndStep();

        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckSizeBefore - 1);
        assertThat(gd.playerDecks.get(player1.getId()))
                .noneMatch(c -> c.getId().equals(topCard.getId()));
    }

    @Test
    @DisplayName("When raid not met, no end step trigger fires")
    void raidNotMetNoTrigger() {
        harness.addToBattlefield(player1, new RuinRaider());
        harness.setHand(player1, List.of());
        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.setLife(player1, 20);

        // Do NOT mark attacked this turn
        advanceToEndStep();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does not trigger on opponent's end step even if controller attacked")
    void doesNotTriggerOnOpponentEndStep() {
        harness.addToBattlefield(player1, new RuinRaider());
        harness.setHand(player1, List.of());
        gd.playerDecks.get(player1.getId()).addFirst(new GrizzlyBears());
        int handBefore = gd.playerHands.get(player1.getId()).size();
        harness.setLife(player1, 20);

        markAttackedThisTurn();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        gs.advanceStep(gd);

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("Does nothing when library is empty and raid is met")
    void doesNothingWhenLibraryEmpty() {
        harness.addToBattlefield(player1, new RuinRaider());
        harness.setHand(player1, List.of());
        gd.playerDecks.get(player1.getId()).clear();
        harness.setLife(player1, 20);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        markAttackedThisTurn();
        advanceToEndStep();

        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId()).size()).isEqualTo(handBefore);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    // ===== Helpers =====

    private void markAttackedThisTurn() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
    }

    private void advanceToEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to END_STEP
    }
}
