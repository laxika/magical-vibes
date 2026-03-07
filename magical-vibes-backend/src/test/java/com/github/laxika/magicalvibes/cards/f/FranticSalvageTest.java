package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.PutTargetCardsFromGraveyardOnTopOfLibraryEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FranticSalvageTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Frantic Salvage has correct effects")
    void hasCorrectEffects() {
        FranticSalvage card = new FranticSalvage();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(2);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0))
                .isInstanceOf(PutTargetCardsFromGraveyardOnTopOfLibraryEffect.class);
        PutTargetCardsFromGraveyardOnTopOfLibraryEffect effect =
                (PutTargetCardsFromGraveyardOnTopOfLibraryEffect) card.getEffects(EffectSlot.SPELL).get(0);
        assertThat(effect.filter()).isInstanceOf(CardTypePredicate.class);
        assertThat(((CardTypePredicate) effect.filter()).cardType()).isEqualTo(CardType.ARTIFACT);
        assertThat(card.getEffects(EffectSlot.SPELL).get(1)).isInstanceOf(DrawCardEffect.class);
    }

    // ===== Casting with artifact cards in graveyard =====

    @Test
    @DisplayName("Casting with artifact cards in graveyard prompts for target selection")
    void castingWithArtifactsInGraveyardPromptsTargetSelection() {
        harness.setGraveyard(player1, List.of(new LeoninScimitar(), new FountainOfYouth()));
        harness.setHand(player1, List.of(new FranticSalvage()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_GRAVEYARD_CHOICE);
        assertThat(gd.interaction.multiSelection().multiGraveyardPlayerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.multiSelection().multiGraveyardMaxCount()).isEqualTo(2);
        assertThat(gd.interaction.multiSelection().multiGraveyardValidCardIds()).hasSize(2);

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
        gd.playerDecks.get(player1.getId()).addFirst(topCard);
        harness.setHand(player1, List.of(new FranticSalvage()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0);

        // Select both artifacts
        List<UUID> validIds = new ArrayList<>(gd.interaction.multiSelection().multiGraveyardValidCardIds());
        harness.handleMultipleGraveyardCardsChosen(player1, validIds);

        // Spell should be on the stack
        assertThat(gd.stack).hasSize(1);

        // Resolve spell
        harness.passBothPriorities();

        // Artifacts should be on top of library, not in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Leonin Scimitar"))
                .noneMatch(c -> c.getName().equals("Fountain of Youth"));

        // Artifacts are on top of the library (before the draw happened, so one was drawn)
        // The draw picks one of the cards just placed on top
        assertThat(gd.playerHands.get(player1.getId())).isNotEmpty();

        // Log should mention putting cards on top
        assertThat(gd.gameLog).anyMatch(entry -> entry.contains("on top of their library from graveyard"));
    }

    @Test
    @DisplayName("Selecting one artifact out of two works correctly")
    void selectingOneOfTwoArtifacts() {
        Card artifact1 = new LeoninScimitar();
        Card artifact2 = new FountainOfYouth();
        harness.setGraveyard(player1, List.of(artifact1, artifact2));
        harness.setHand(player1, List.of(new FranticSalvage()));
        harness.addMana(player1, ManaColor.WHITE, 4);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.castInstant(player1, 0);

        // Select only one artifact
        List<UUID> validIds = new ArrayList<>(gd.interaction.multiSelection().multiGraveyardValidCardIds());
        harness.handleMultipleGraveyardCardsChosen(player1, List.of(validIds.getFirst()));

        harness.passBothPriorities();

        // One artifact still in graveyard, one was moved. Frantic Salvage also goes to graveyard after resolving.
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Frantic Salvage"));

        // Drew a card (hand was emptied by casting, then drew 1)
        // The spell was removed from hand, so handSizeBefore - 1 + 1 draw = handSizeBefore
        // But wait, the artifact that was put on top might have been drawn
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1); // drew 1 card
    }

    @Test
    @DisplayName("Selecting zero targets with artifacts available still draws a card")
    void selectingZeroTargetsStillDraws() {
        harness.setGraveyard(player1, List.of(new LeoninScimitar()));
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);
        harness.setHand(player1, List.of(new FranticSalvage()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0);

        // Select zero targets
        harness.handleMultipleGraveyardCardsChosen(player1, List.of());

        harness.passBothPriorities();

        // Artifact still in graveyard (not moved). Frantic Salvage also goes to graveyard after resolving.
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Leonin Scimitar"))
                .anyMatch(c -> c.getName().equals("Frantic Salvage"));

        // Drew the Grizzly Bears from top of library
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Casting with no artifact cards in graveyard =====

    @Test
    @DisplayName("Casting with no artifact cards in graveyard skips target prompt and still draws")
    void castingWithNoArtifactsSkipsPromptAndDraws() {
        // Only non-artifact cards in graveyard
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);
        harness.setHand(player1, List.of(new FranticSalvage()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0);

        // No graveyard prompt — spell goes directly on stack
        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        // Drew a card
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));

        // Non-artifact card still in graveyard (untouched). Frantic Salvage also goes to graveyard.
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"))
                .anyMatch(c -> c.getName().equals("Frantic Salvage"));
    }

    @Test
    @DisplayName("Casting with empty graveyard skips target prompt and still draws")
    void castingWithEmptyGraveyardSkipsPromptAndDraws() {
        Card topCard = new GrizzlyBears();
        gd.playerDecks.get(player1.getId()).addFirst(topCard);
        harness.setHand(player1, List.of(new FranticSalvage()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0);

        // No graveyard prompt — spell goes directly on stack
        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        // Drew a card
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    // ===== Only artifact cards are selectable =====

    @Test
    @DisplayName("Only artifact cards appear as valid targets, not creature cards")
    void onlyArtifactCardsAreValidTargets() {
        Card artifact = new LeoninScimitar();
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(artifact, creature));
        harness.setHand(player1, List.of(new FranticSalvage()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0);

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_GRAVEYARD_CHOICE);
        // Only the artifact should be valid
        assertThat(gd.interaction.multiSelection().multiGraveyardValidCardIds()).hasSize(1);
        assertThat(gd.interaction.multiSelection().multiGraveyardValidCardIds()).contains(artifact.getId());
    }

    // ===== Cards placed on top of library (draw verification) =====

    @Test
    @DisplayName("Card placed on top of library is drawn by the subsequent draw effect")
    void cardPlacedOnTopIsDrawn() {
        Card artifact = new LeoninScimitar();
        harness.setGraveyard(player1, List.of(artifact));
        harness.setHand(player1, List.of(new FranticSalvage()));
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.castInstant(player1, 0);

        List<UUID> validIds = new ArrayList<>(gd.interaction.multiSelection().multiGraveyardValidCardIds());
        harness.handleMultipleGraveyardCardsChosen(player1, validIds);

        harness.passBothPriorities();

        // The artifact was placed on top, then drawn. Only Frantic Salvage is in graveyard.
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()).getFirst().getName()).isEqualTo("Frantic Salvage");
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Leonin Scimitar"));
    }
}
