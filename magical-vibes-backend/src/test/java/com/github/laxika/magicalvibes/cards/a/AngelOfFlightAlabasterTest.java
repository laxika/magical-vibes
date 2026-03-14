package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GhostWarden;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HowlingBanshee;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AngelOfFlightAlabasterTest extends BaseCardTest {

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Angel of Flight Alabaster has upkeep-triggered graveyard return effect")
    void hasCorrectEffects() {
        AngelOfFlightAlabaster card = new AngelOfFlightAlabaster();

        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.UPKEEP_TRIGGERED).getFirst())
                .isInstanceOf(ReturnCardFromGraveyardEffect.class);
    }

    // ===== Upkeep trigger: return Spirit from graveyard to hand =====

    @Test
    @DisplayName("Controller's upkeep returns Spirit from graveyard to hand")
    void upkeepReturnsSpiritFromGraveyardToHand() {
        harness.addToBattlefield(player1, new AngelOfFlightAlabaster());
        harness.setGraveyard(player1, List.of(new GhostWarden()));

        advanceToUpkeep(player1);

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);

        // Resolve trigger → graveyard choice prompt
        harness.passBothPriorities();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.GRAVEYARD_CHOICE);

        // Choose Spirit
        harness.handleGraveyardCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Ghost Warden"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Ghost Warden"));
    }

    @Test
    @DisplayName("Returns specific Spirit when multiple Spirits are in graveyard")
    void returnsSpecificSpiritFromGraveyard() {
        harness.addToBattlefield(player1, new AngelOfFlightAlabaster());
        harness.setGraveyard(player1, List.of(new GhostWarden(), new HowlingBanshee()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        // Choose Howling Banshee (index 1)
        harness.handleGraveyardCardChosen(player1, 1);

        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Howling Banshee"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Ghost Warden"));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Howling Banshee"));
    }

    @Test
    @DisplayName("No effect when graveyard is empty")
    void noEffectWithEmptyGraveyard() {
        harness.addToBattlefield(player1, new AngelOfFlightAlabaster());

        advanceToUpkeep(player1);

        // Trigger is on the stack
        assertThat(gd.stack).hasSize(1);

        // Resolve — should resolve without graveyard choice
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.GRAVEYARD_CHOICE);
    }

    @Test
    @DisplayName("No effect when graveyard has only non-Spirit cards")
    void noEffectWithOnlyNonSpiritsInGraveyard() {
        harness.addToBattlefield(player1, new AngelOfFlightAlabaster());
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.awaitingInputType()).isNotEqualTo(AwaitingInput.GRAVEYARD_CHOICE);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Upkeep trigger does NOT fire during opponent's upkeep")
    void upkeepTriggerDoesNotFireDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new AngelOfFlightAlabaster());
        harness.setGraveyard(player1, List.of(new GhostWarden()));

        advanceToUpkeep(player2);

        // No trigger should fire for player 1's graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Ghost Warden"));
    }
}
