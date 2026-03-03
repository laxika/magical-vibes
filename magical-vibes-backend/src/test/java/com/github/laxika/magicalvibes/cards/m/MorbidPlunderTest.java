package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.ReturnTargetCardsFromGraveyardToHandEffect;
import com.github.laxika.magicalvibes.model.filter.CardTypePredicate;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class MorbidPlunderTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Morbid Plunder has correct effects")
    void hasCorrectEffects() {
        MorbidPlunder card = new MorbidPlunder();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).getFirst())
                .isInstanceOf(ReturnTargetCardsFromGraveyardToHandEffect.class);
        ReturnTargetCardsFromGraveyardToHandEffect effect =
                (ReturnTargetCardsFromGraveyardToHandEffect) card.getEffects(EffectSlot.SPELL).getFirst();
        assertThat(effect.filter()).isInstanceOf(CardTypePredicate.class);
        assertThat(((CardTypePredicate) effect.filter()).cardType()).isEqualTo(CardType.CREATURE);
        assertThat(effect.maxTargets()).isEqualTo(2);
    }

    // ===== Casting with creature cards in graveyard =====

    @Test
    @DisplayName("Casting with creature cards in graveyard prompts for target selection")
    void castingWithCreaturesInGraveyardPromptsTargetSelection() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new LlanowarElves()));
        harness.setHand(player1, List.of(new MorbidPlunder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_GRAVEYARD_CHOICE);
        assertThat(gd.interaction.awaitingMultiGraveyardChoicePlayerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.awaitingMultiGraveyardChoiceMaxCount()).isEqualTo(2);
        assertThat(gd.interaction.awaitingMultiGraveyardChoiceValidCardIds()).hasSize(2);

        // Spell is NOT yet on the stack (waiting for target selection)
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Selecting two targets puts them in hand on resolution")
    void selectingTwoTargetsReturnsToHand() {
        Card creature1 = new GrizzlyBears();
        Card creature2 = new LlanowarElves();
        harness.setGraveyard(player1, List.of(creature1, creature2));
        harness.setHand(player1, List.of(new MorbidPlunder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 0);

        // Select both creatures
        List<UUID> validIds = new ArrayList<>(gd.interaction.awaitingMultiGraveyardChoiceValidCardIds());
        harness.handleMultipleGraveyardCardsChosen(player1, validIds);

        // Spell should be on the stack
        assertThat(gd.stack).hasSize(1);

        // Resolve spell
        harness.passBothPriorities();

        // Both creatures should be in hand, not in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"))
                .noneMatch(c -> c.getName().equals("Llanowar Elves"));

        // Morbid Plunder goes to graveyard after resolution
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Morbid Plunder"));

        // Both creatures should be in hand
        assertThat(gd.playerHands.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));

        // Log should mention returning from graveyard
        assertThat(gd.gameLog).anyMatch(entry -> entry.contains("from graveyard to hand"));
    }

    @Test
    @DisplayName("Selecting one of two creatures works correctly")
    void selectingOneOfTwoCreatures() {
        Card creature1 = new GrizzlyBears();
        Card creature2 = new LlanowarElves();
        harness.setGraveyard(player1, List.of(creature1, creature2));
        harness.setHand(player1, List.of(new MorbidPlunder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 0);

        // Select only one creature
        List<UUID> validIds = new ArrayList<>(gd.interaction.awaitingMultiGraveyardChoiceValidCardIds());
        harness.handleMultipleGraveyardCardsChosen(player1, List.of(validIds.getFirst()));

        harness.passBothPriorities();

        // One creature returned to hand, one still in graveyard (plus Morbid Plunder)
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Morbid Plunder"));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Selecting zero targets with creatures available resolves without returning anything")
    void selectingZeroTargetsReturnsNothing() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new MorbidPlunder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 0);

        // Select zero targets
        harness.handleMultipleGraveyardCardsChosen(player1, List.of());

        harness.passBothPriorities();

        // Creature still in graveyard, Morbid Plunder also in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"))
                .anyMatch(c -> c.getName().equals("Morbid Plunder"));

        // Hand should be empty
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    // ===== Casting with no creature cards in graveyard =====

    @Test
    @DisplayName("Casting with no creature cards in graveyard skips target prompt")
    void castingWithNoCreaturesSkipsPrompt() {
        // Only non-creature cards in graveyard
        harness.setGraveyard(player1, List.of(new LeoninScimitar()));
        harness.setHand(player1, List.of(new MorbidPlunder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 0);

        // No graveyard prompt — spell goes directly on stack
        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        // Non-creature card untouched in graveyard. Morbid Plunder also goes to graveyard.
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Leonin Scimitar"))
                .anyMatch(c -> c.getName().equals("Morbid Plunder"));
    }

    @Test
    @DisplayName("Casting with empty graveyard skips target prompt")
    void castingWithEmptyGraveyardSkipsPrompt() {
        harness.setHand(player1, List.of(new MorbidPlunder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 0);

        // No graveyard prompt — spell goes directly on stack
        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        // Only Morbid Plunder in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId()).getFirst().getName())
                .isEqualTo("Morbid Plunder");
    }

    // ===== Only creature cards are selectable =====

    @Test
    @DisplayName("Only creature cards appear as valid targets, not artifacts")
    void onlyCreatureCardsAreValidTargets() {
        Card creature = new GrizzlyBears();
        Card artifact = new LeoninScimitar();
        harness.setGraveyard(player1, List.of(creature, artifact));
        harness.setHand(player1, List.of(new MorbidPlunder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_GRAVEYARD_CHOICE);
        // Only the creature should be valid
        assertThat(gd.interaction.awaitingMultiGraveyardChoiceValidCardIds()).hasSize(1);
        assertThat(gd.interaction.awaitingMultiGraveyardChoiceValidCardIds()).contains(creature.getId());
    }

    // ===== Max targets capped at 2 =====

    @Test
    @DisplayName("Max targets is capped at 2 even with 3 creatures in graveyard")
    void maxTargetsCappedAtTwo() {
        Card creature1 = new GrizzlyBears();
        Card creature2 = new LlanowarElves();
        Card creature3 = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature1, creature2, creature3));
        harness.setHand(player1, List.of(new MorbidPlunder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_GRAVEYARD_CHOICE);
        // All 3 creatures should be valid choices
        assertThat(gd.interaction.awaitingMultiGraveyardChoiceValidCardIds()).hasSize(3);
        // But max selectable is 2
        assertThat(gd.interaction.awaitingMultiGraveyardChoiceMaxCount()).isEqualTo(2);
    }
}
