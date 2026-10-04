package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FranticSalvage.class, LeoninScimitar.class, FountainOfYouth.class, GrizzlyBears.class})
class FranticSalvageTest extends BaseCardTest {


    @Test
    @DisplayName("Casting with artifact cards in graveyard prompts for target selection")
    void castingWithArtifactsInGraveyardPromptsTargetSelection() {
        harness.setGraveyard(player1, List.of(new LeoninScimitar(), new FountainOfYouth()));
        harness.setHand(player1, List.of(new FranticSalvage()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).maxCount()).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds()).hasSize(2);

        // Spell is NOT yet on the stack (waiting for target selection)
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Selecting targets puts spell on stack, resolving puts cards on top of library and draws")
    void selectingTargetsResolvesCorrectly() {
        Card artifact1 = new LeoninScimitar();
        Card artifact2 = new FountainOfYouth();
        harness.setGraveyard(player1, List.of(artifact1, artifact2));
        // Put a card on top of library so we can verify draw
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new FranticSalvage()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0);

        // Select both artifacts
        List<UUID> validIds = new ArrayList<>(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        harness.handleMultipleCardsChosen(player1, validIds);

        // Spell should be on the stack
        assertThat(gd.stack).hasSize(1);

        // Resolve spell
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1)));

        // Artifacts should be on top of library, not in graveyard
        harness.assertNotInGraveyard(player1, "Leonin Scimitar");
        harness.assertNotInGraveyard(player1, "Fountain of Youth");

        // Artifacts are on top of the library (before the draw happened, so one was drawn)
        // The draw picks one of the cards just placed on top
        assertThat(gd.playerHands.get(player1.getId())).isNotEmpty();

        // Log should mention putting cards on top
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(entry -> entry.contains("on top of their library from graveyard"));
    }

    @Test
    @DisplayName("Selecting one artifact out of two works correctly")
    void selectingOneOfTwoArtifacts() {
        Card artifact1 = new LeoninScimitar();
        Card artifact2 = new FountainOfYouth();
        harness.setGraveyard(player1, List.of(artifact1, artifact2));
        harness.setHand(player1, List.of(new FranticSalvage()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0);

        // Select only one artifact
        List<UUID> validIds = new ArrayList<>(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        harness.handleMultipleCardsChosen(player1, List.of(validIds.getFirst()));

        harness.passBothPriorities();

        // One artifact still in graveyard, one was moved. Frantic Salvage also goes to graveyard after resolving.
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Frantic Salvage");

        // Drew a card (hand was emptied by casting, then drew 1)
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1); // drew 1 card
    }

    @Test
    @DisplayName("Selecting zero targets with artifacts available still draws a card")
    void selectingZeroTargetsStillDraws() {
        harness.setGraveyard(player1, List.of(new LeoninScimitar()));
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new FranticSalvage()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0);

        // Select zero targets
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.passBothPriorities();

        // Artifact still in graveyard (not moved). Frantic Salvage also goes to graveyard after resolving.
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Leonin Scimitar");
        harness.assertInGraveyard(player1, "Frantic Salvage");

        // Drew the Grizzly Bears from top of library
        harness.assertInHand(player1, "Grizzly Bears");
    }


    @Test
    @DisplayName("Casting with no artifact cards in graveyard skips target prompt and still draws")
    void castingWithNoArtifactsSkipsPromptAndDraws() {
        // Only non-artifact cards in graveyard
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new FranticSalvage()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0);

        // No graveyard prompt — spell goes directly on stack
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        // Drew a card
        harness.assertInHand(player1, "Grizzly Bears");

        // Non-artifact card still in graveyard (untouched). Frantic Salvage also goes to graveyard.
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Frantic Salvage");
    }

    @Test
    @DisplayName("Casting with empty graveyard skips target prompt and still draws")
    void castingWithEmptyGraveyardSkipsPromptAndDraws() {
        Card topCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(topCard));
        harness.setHand(player1, List.of(new FranticSalvage()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0);

        // No graveyard prompt — spell goes directly on stack
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        // Drew a card
        harness.assertInHand(player1, "Grizzly Bears");
    }


    @Test
    @DisplayName("Only artifact cards appear as valid targets, not creature cards")
    void onlyArtifactCardsAreValidTargets() {
        Card artifact = new LeoninScimitar();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(artifact, creature));
        harness.setHand(player1, List.of(new FranticSalvage()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        // Only the artifact should be valid
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds()).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds()).contains(artifact.getId());
    }


    @Test
    @DisplayName("Card placed on top of library is drawn by the subsequent draw effect")
    void cardPlacedOnTopIsDrawn() {
        Card artifact = new LeoninScimitar();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(new FranticSalvage()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0);

        List<UUID> validIds = new ArrayList<>(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        harness.handleMultipleCardsChosen(player1, validIds);

        harness.passBothPriorities();

        // The artifact was placed on top, then drawn. Only Frantic Salvage is in graveyard.
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()).getFirst().getName()).isEqualTo("Frantic Salvage");
        harness.assertInHand(player1, "Leonin Scimitar");
    }

    @Test
    @DisplayName("Controller chooses the order before drawing the top returned artifact")
    void choosesLibraryOrderBeforeDrawing() {
        Card first = new LeoninScimitar();
        Card second = new FountainOfYouth();
        Card originalTop = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setLibrary(player1, List.of(originalTop));
        harness.setHand(player1, List.of(new FranticSalvage()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        var reorder = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        int secondIndex = reorder.cards().indexOf(second);
        int firstIndex = reorder.cards().indexOf(first);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(secondIndex, firstIndex)));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first, originalTop);
        harness.assertInGraveyard(player1, "Frantic Salvage");
    }

    @Test
    @DisplayName("No card is drawn when every chosen target has left the graveyard")
    void allTargetsRemovedPreventsDraw() {
        Card artifact = new LeoninScimitar();
        Card originalTop = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setLibrary(player1, List.of(originalTop));
        harness.setHand(player1, List.of(new FranticSalvage()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(artifact.getId()));
        harness.setGraveyard(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(originalTop);
        harness.assertInGraveyard(player1, "Frantic Salvage");
    }

    @Test
    @DisplayName("A remaining legal target is returned and drawn when another target leaves")
    void oneTargetRemovedStillReturnsOtherAndDraws() {
        Card removed = new LeoninScimitar();
        Card remaining = new FountainOfYouth();
        Card originalTop = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(removed, remaining));
        harness.setLibrary(player1, List.of(originalTop));
        harness.setHand(player1, List.of(new FranticSalvage()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(removed.getId(), remaining.getId()));
        harness.setGraveyard(player1, List.of(remaining));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(originalTop);
        harness.assertInGraveyard(player1, "Frantic Salvage");
    }


    @Test
    @DisplayName("Artifacts in an opponent's graveyard cannot be targeted")
    void opponentsArtifactsAreNotTargets() {
        Card opponentArtifact = new LeoninScimitar();
        Card originalTop = new GrizzlyBears();
        harness.setGraveyard(player1, List.of());
        harness.setGraveyard(player2, List.of(opponentArtifact));
        harness.setLibrary(player1, List.of(originalTop));
        harness.setHand(player1, List.of(new FranticSalvage()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentArtifact);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(originalTop);
        harness.assertInGraveyard(player1, "Frantic Salvage");
    }

}
