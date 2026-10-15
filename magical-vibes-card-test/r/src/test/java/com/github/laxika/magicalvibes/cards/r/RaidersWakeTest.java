package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;

import com.github.laxika.magicalvibes.cards.d.Distress;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.s.Sift;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RaidersWake.class, Distress.class, GrizzlyBears.class, Sift.class})
class RaidersWakeTest extends BaseCardTest {

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
        harness.withAutoStop(gd.currentStep, () -> harness.handleCardChosen(player1, 0));

        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        // Raiders' Wake trigger: player2 loses 2 life
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does NOT trigger when controller discards")
    void doesNotTriggerOnControllerDiscard() {
        harness.addToBattlefield(player1, new RaidersWake());
        harness.setLife(player1, 20);

        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));

        harness.setHand(player1, List.of(new Sift()));
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLUE, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.handleCardChosen(player1, 0);

        // Controller's life should be unchanged
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
    }

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
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThat(((PendingInteraction.PermanentChoice) gd.interaction.activeInteraction()).validPermanentIds())
                .containsExactly(player2.getId());

        // Select opponent as target
        harness.handlePermanentChosen(player1, player2.getId());

        // Triggered ability is now on the stack — resolve it
        harness.passBothPriorities();

        // Opponent should be prompted to discard
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        assertThat(((PendingInteraction.HandChoice) gd.interaction.activeInteraction()).playerId()).isEqualTo(player2.getId());

        harness.handleCardChosen(player2, 0);

        // Grizzly Bears should have been discarded to graveyard
        harness.assertInGraveyard(player2, "Grizzly Bears");
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
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();

        // Grizzly Bears should still be in hand (not forced to discard)
        harness.assertInHand(player2, "Grizzly Bears");
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
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNull();
    }

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

        // Opponent discards; the life loss waits for its own trigger to resolve.
        harness.withAutoStop(TurnStep.END_STEP, () -> harness.handleCardChosen(player2, 0));
        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

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
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("no cards to discard"));
    }

    @Test
    @DisplayName("Each Wake creates a separate life-loss trigger for an opponent's discard")
    void multipleWakesCreateSeparateLifeLossTriggers() {
        harness.addToBattlefield(player1, new RaidersWake());
        harness.addToBattlefield(player1, new RaidersWake());
        harness.setHand(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Distress()));
        harness.setLife(player2, 20);
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.BLACK, 2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.withAutoStop(gd.currentStep, () -> harness.handleCardChosen(player1, 0));

        harness.assertLife(player2, 20);
        assertThat(gd.stack).hasSize(2);
        harness.withAutoStop(gd.currentStep, () -> harness.passBothPriorities());
        harness.assertLife(player2, 18);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        harness.assertLife(player2, 16);
    }

    private void markAttackedThisTurn() {
        gd.playersDeclaredAttackersThisTurn.add(player1.getId());
    }
}
