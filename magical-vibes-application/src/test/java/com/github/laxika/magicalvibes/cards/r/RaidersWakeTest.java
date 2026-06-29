package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.d.Distress;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.RaidConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPlayerDiscardsEffect;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RaidersWakeTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has LoseLifeEffect(2) on ON_OPPONENT_DISCARDS")
    void hasDiscardTriggerEffect() {
        RaidersWake card = new RaidersWake();

        assertThat(card.getEffects(EffectSlot.ON_OPPONENT_DISCARDS)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.ON_OPPONENT_DISCARDS).getFirst())
                .isInstanceOf(LoseLifeEffect.class);
        LoseLifeEffect lifeLoss = (LoseLifeEffect) card.getEffects(EffectSlot.ON_OPPONENT_DISCARDS).getFirst();
        assertThat(lifeLoss.amount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Has RaidConditionalEffect wrapping TargetPlayerDiscardsEffect(1) on CONTROLLER_END_STEP_TRIGGERED")
    void hasRaidEndStepDiscardEffect() {
        RaidersWake card = new RaidersWake();

        assertThat(card.getEffects(EffectSlot.CONTROLLER_END_STEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.CONTROLLER_END_STEP_TRIGGERED).getFirst())
                .isInstanceOf(RaidConditionalEffect.class);
        RaidConditionalEffect raid =
                (RaidConditionalEffect) card.getEffects(EffectSlot.CONTROLLER_END_STEP_TRIGGERED).getFirst();
        assertThat(raid.wrapped()).isInstanceOf(TargetPlayerDiscardsEffect.class);
        TargetPlayerDiscardsEffect discard = (TargetPlayerDiscardsEffect) raid.wrapped();
        assertThat(discard.amount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Has PlayerPredicateTargetFilter restricting to opponents")
    void hasOpponentTargetFilter() {
        RaidersWake card = new RaidersWake();

        assertThat(card.getTargetFilter()).isInstanceOf(PlayerPredicateTargetFilter.class);
    }

    // ===== Discard trigger: opponent loses 2 life =====

    @Test
    @DisplayName("Opponent loses 2 life when they discard via Distress")
    void opponentLosesLifeOnDiscard() {
        harness.addToBattlefield(player1, new RaidersWake());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setLife(player2, 20);

        harness.setHand(player1, List.of(new Distress()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Player1 chooses card from player2's revealed hand
        harness.handleCardChosen(player1, 0);

        // Raiders' Wake trigger: player2 loses 2 life
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does NOT trigger when controller discards")
    void doesNotTriggerOnControllerDiscard() {
        harness.addToBattlefield(player1, new RaidersWake());
        harness.setLife(player1, 20);

        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());
        gd.playerDecks.get(player1.getId()).add(new GrizzlyBears());

        harness.setHand(player1, List.of(new com.github.laxika.magicalvibes.cards.s.Sift()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        // Controller's life should be unchanged
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

    // ===== Raid trigger: target opponent discards at end step =====

    @Test
    @DisplayName("When raid met, target opponent discards a card at end step")
    void raidMetOpponentDiscards() {
        harness.addToBattlefield(player1, new RaidersWake());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        markAttackedThisTurn();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Advance to end step — raid trigger fires, needs target selection
        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.PERMANENT_CHOICE);

        // Select opponent as target
        harness.handlePermanentChosen(player1, player2.getId());

        // Triggered ability is now on the stack — resolve it
        harness.passBothPriorities();

        // Opponent should be prompted to discard
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.DISCARD_CHOICE);
        assertThat(gd.interaction.cardChoice().playerId()).isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        // Grizzly Bears should have been discarded to graveyard
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("When raid not met, no end step trigger fires")
    void raidNotMetNoTrigger() {
        harness.addToBattlefield(player1, new RaidersWake());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));

        // Do NOT mark attacked this turn
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passBothPriorities();

        // No targeting prompt — raid condition not met
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.PERMANENT_CHOICE);

        // Grizzly Bears should still be in hand (not forced to discard)
        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Does not trigger on opponent's end step even if controller attacked")
    void doesNotTriggerOnOpponentEndStep() {
        harness.addToBattlefield(player1, new RaidersWake());

        markAttackedThisTurn();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);

        gs.advanceStep(gd);

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.PERMANENT_CHOICE);
    }

    // ===== Both abilities interact: raid forces discard, which triggers life loss =====

    @Test
    @DisplayName("Raid-forced discard also triggers the life loss ability")
    void raidDiscardTriggersLifeLoss() {
        harness.addToBattlefield(player1, new RaidersWake());
        harness.setHand(player2, new ArrayList<>(List.of(new GrizzlyBears())));
        harness.setLife(player2, 20);

        markAttackedThisTurn();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Advance to end step — raid trigger fires
        harness.passBothPriorities();

        // Select opponent as target
        harness.handlePermanentChosen(player1, player2.getId());

        // Resolve triggered ability
        harness.passBothPriorities();

        // Opponent discards
        harness.handleCardChosen(player2, 0);

        // Discard triggered the life loss: opponent should lose 2 life
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Raid does nothing when opponent has empty hand")
    void raidDoesNothingWithEmptyHand() {
        harness.addToBattlefield(player1, new RaidersWake());
        harness.setHand(player2, new ArrayList<>());
        harness.setLife(player2, 20);

        markAttackedThisTurn();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        // Advance to end step — raid trigger fires
        harness.passBothPriorities();

        // Select opponent as target
        harness.handlePermanentChosen(player1, player2.getId());

        // Resolve triggered ability
        harness.passBothPriorities();

        // No discard possible — no life loss either
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.gameLog).anyMatch(log -> log.contains("no cards to discard"));
    }

    // ===== Helpers =====

    private void markAttackedThisTurn() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
    }
}
