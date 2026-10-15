package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.g.GroundSeal;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.l.LeoninScimitar;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MorbidPlunder.class, GrizzlyBears.class, LlanowarElves.class, LeoninScimitar.class, GroundSeal.class})
class MorbidPlunderTest extends BaseCardTest {

    // ===== Casting with creature cards in graveyard =====

    @Test
    @DisplayName("Casting with creature cards in graveyard prompts for target selection")
    void castingWithCreaturesInGraveyardPromptsTargetSelection() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears(), new LlanowarElves()));
        harness.setHand(player1, List.of(new MorbidPlunder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).maxCount()).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds()).hasSize(2);

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
        List<UUID> validIds = new ArrayList<>(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        harness.handleMultipleCardsChosen(player1, validIds);

        // Spell should be on the stack
        assertThat(gd.stack).hasSize(1);

        // Resolve spell
        harness.passBothPriorities();

        // Both creatures should be in hand, not in graveyard
        harness.assertNotInGraveyard(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player1, "Llanowar Elves");

        // Morbid Plunder goes to graveyard after resolution
        harness.assertInGraveyard(player1, "Morbid Plunder");

        // Both creatures should be in hand
        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInHand(player1, "Llanowar Elves");

        // Log should mention returning from graveyard
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(entry -> entry.contains("from graveyard to hand"));
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
        List<UUID> validIds = new ArrayList<>(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        harness.handleMultipleCardsChosen(player1, List.of(validIds.getFirst()));

        harness.passBothPriorities();

        // One creature returned to hand, one still in graveyard (plus Morbid Plunder)
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Morbid Plunder");

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
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.passBothPriorities();

        // Creature still in graveyard, Morbid Plunder also in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Grizzly Bears");
        harness.assertInGraveyard(player1, "Morbid Plunder");

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
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        // Non-creature card untouched in graveyard. Morbid Plunder also goes to graveyard.
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Leonin Scimitar");
        harness.assertInGraveyard(player1, "Morbid Plunder");
    }

    @Test
    @DisplayName("Casting with empty graveyard skips target prompt")
    void castingWithEmptyGraveyardSkipsPrompt() {
        harness.setHand(player1, List.of(new MorbidPlunder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 0);

        // No graveyard prompt — spell goes directly on stack
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        // Only Morbid Plunder in graveyard
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        harness.assertInGraveyard(player1, "Morbid Plunder");
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

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        // Only the creature should be valid
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds()).hasSize(1);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds()).contains(creature.getId());
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

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        // All 3 creatures should be valid choices
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds()).hasSize(3);
        // But max selectable is 2
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).maxCount()).isEqualTo(2);
    }

    @Test
    @DisplayName("Opponent's creature cards are not offered as targets")
    void opponentsCreaturesAreNotValidTargets() {
        Card ownCreature = new GrizzlyBears();
        Card opposingCreature = new LlanowarElves();
        harness.setGraveyard(player1, List.of(ownCreature));
        harness.setGraveyard(player2, List.of(opposingCreature));
        harness.setHand(player1, List.of(new MorbidPlunder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds())
                .containsExactly(ownCreature.getId());
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(opposingCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(ownCreature.getId()));
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Llanowar Elves");
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Casting with only opponent creatures in graveyards returns nothing")
    void onlyOpponentCreaturesReturnsNothing() {
        harness.setGraveyard(player2, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new MorbidPlunder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Morbid Plunder");
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Selecting three creatures is rejected and two can still be selected")
    void selectingMoreThanTwoTargetsIsRejected() {
        Card first = new GrizzlyBears();
        Card second = new LlanowarElves();
        Card third = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(first, second, third));
        harness.setHand(player1, List.of(new MorbidPlunder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castSorcery(player1, 0, 0);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(first.getId(), second.getId(), third.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(first, second);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(third);
        harness.assertInGraveyard(player1, "Morbid Plunder");
    }

    @Test
    @DisplayName("The same creature card cannot be selected twice")
    void duplicateTargetsAreRejected() {
        Card creature = new GrizzlyBears();
        Card otherCreature = new LlanowarElves();
        harness.setGraveyard(player1, List.of(creature, otherCreature));
        harness.setHand(player1, List.of(new MorbidPlunder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castSorcery(player1, 0, 0);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(creature.getId(), creature.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(creature.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(otherCreature);
        harness.assertInGraveyard(player1, "Morbid Plunder");
    }

    @Test
    @DisplayName("A remaining legal target returns when the other target leaves the graveyard")
    void remainingLegalTargetReturnsToHand() {
        Card removed = new GrizzlyBears();
        Card remaining = new LlanowarElves();
        harness.setGraveyard(player1, List.of(removed, remaining));
        harness.setHand(player1, List.of(new MorbidPlunder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(removed.getId(), remaining.getId()));

        harness.setGraveyard(player1, List.of(remaining));
        harness.setExile(player1, List.of(removed));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(remaining);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(removed);
        harness.assertInGraveyard(player1, "Morbid Plunder");
        harness.assertNotInGraveyard(player1, "Llanowar Elves");
    }

    @Test
    @DisplayName("No cards return when every selected target leaves the graveyard")
    void allTargetsGoneReturnsNothing() {
        Card first = new GrizzlyBears();
        Card second = new LlanowarElves();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new MorbidPlunder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));

        harness.setGraveyard(player1, List.of());
        harness.setExile(player1, List.of(first, second));
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(first, second);
        harness.assertInGraveyard(player1, "Morbid Plunder");
    }

    @Test
    @DisplayName("Ground Seal entering before resolution makes the selected targets illegal")
    void graveyardTargetingRestrictionAppearingBeforeResolutionStopsReturn() {
        Card first = new GrizzlyBears();
        Card second = new LlanowarElves();
        harness.setGraveyard(player1, List.of(first, second));
        harness.setHand(player1, List.of(new MorbidPlunder()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.castSorcery(player1, 0, 0);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId()));

        harness.addToBattlefield(player2, new GroundSeal());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(first, second);
        harness.assertInGraveyard(player1, "Morbid Plunder");
    }

    @Test
    @DisplayName("Ground Seal permits casting Morbid Plunder with zero targets")
    void graveyardTargetingRestrictionAllowsZeroTargets() {
        Card creature = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(creature));
        harness.addToBattlefield(player2, new GroundSeal());
        harness.setHand(player1, List.of(new MorbidPlunder()));
        harness.addMana(player1, ManaColor.BLACK, 3);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(creature);
        harness.assertInGraveyard(player1, "Morbid Plunder");
    }
}
