package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.effect.ShuffleTargetCardsFromGraveyardIntoLibraryEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MemorysJourneyTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Has one SPELL effect: shuffle up to 3 target cards from graveyard into library")
    void hasCorrectEffects() {
        MemorysJourney card = new MemorysJourney();

        assertThat(card.getEffects(EffectSlot.SPELL)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.SPELL).get(0))
                .isInstanceOf(ShuffleTargetCardsFromGraveyardIntoLibraryEffect.class);

        ShuffleTargetCardsFromGraveyardIntoLibraryEffect effect =
                (ShuffleTargetCardsFromGraveyardIntoLibraryEffect) card.getEffects(EffectSlot.SPELL).get(0);
        assertThat(effect.maxTargets()).isEqualTo(3);
        assertThat(effect.filter()).isNull(); // any card
    }

    @Test
    @DisplayName("Has flashback cost {G}")
    void hasFlashbackCost() {
        MemorysJourney card = new MemorysJourney();

        FlashbackCast flashback = card.getCastingOption(FlashbackCast.class).orElseThrow();
        assertThat(flashback.getCost(ManaCastingCost.class).orElseThrow().manaCost()).isEqualTo("{G}");
    }

    @Test
    @DisplayName("Needs target (auto-derived from player-targeting effect)")
    void needsTarget() {
        MemorysJourney card = new MemorysJourney();

        assertThat(card.isNeedsTarget()).isTrue();
    }

    // ===== Casting normally — targeting own graveyard =====

    @Test
    @DisplayName("Casting targeting self with cards in graveyard prompts for target selection")
    void castingTargetingSelfPromptsForGraveyardSelection() {
        Card card1 = new GrizzlyBears();
        Card card2 = new LightningBolt();
        harness.setGraveyard(player1, List.of(card1, card2));
        harness.setHand(player1, List.of(new MemorysJourney()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player1.getId());

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_GRAVEYARD_CHOICE);
        assertThat(gd.interaction.multiSelection().multiGraveyardPlayerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.multiSelection().multiGraveyardMaxCount()).isEqualTo(2); // min(3, 2 cards)
        assertThat(gd.interaction.multiSelection().multiGraveyardValidCardIds()).hasSize(2);

        // Spell is NOT yet on the stack
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Selecting targets and resolving shuffles cards from graveyard into library")
    void selectingTargetsShufflesIntoLibrary() {
        Card card1 = new GrizzlyBears();
        Card card2 = new LightningBolt();
        harness.setGraveyard(player1, List.of(card1, card2));
        int libSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new MemorysJourney()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player1.getId());

        // Select both cards
        List<UUID> validIds = new ArrayList<>(gd.interaction.multiSelection().multiGraveyardValidCardIds());
        harness.handleMultipleGraveyardCardsChosen(player1, validIds);

        // Spell should be on the stack
        assertThat(gd.stack).hasSize(1);

        // Resolve
        harness.passBothPriorities();

        // Cards should no longer be in graveyard (only Memory's Journey itself goes to graveyard)
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Grizzly Bears"))
                .noneMatch(c -> c.getName().equals("Lightning Bolt"))
                .anyMatch(c -> c.getName().equals("Memory's Journey"));

        // Library should have gained 2 cards
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libSizeBefore + 2);
    }

    @Test
    @DisplayName("Selecting one of three cards works correctly")
    void selectingOneOfThreeCards() {
        Card card1 = new GrizzlyBears();
        Card card2 = new LightningBolt();
        Card card3 = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(card1, card2, card3));
        int libSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new MemorysJourney()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player1.getId());

        // Select only one card
        List<UUID> validIds = new ArrayList<>(gd.interaction.multiSelection().multiGraveyardValidCardIds());
        harness.handleMultipleGraveyardCardsChosen(player1, List.of(validIds.getFirst()));

        harness.passBothPriorities();

        // Two cards still in graveyard + Memory's Journey itself
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);

        // Library gained 1 card
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libSizeBefore + 1);
    }

    @Test
    @DisplayName("Selecting zero targets with cards available still resolves")
    void selectingZeroTargets() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        int libSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new MemorysJourney()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player1.getId());

        // Select zero targets
        harness.handleMultipleGraveyardCardsChosen(player1, List.of());

        harness.passBothPriorities();

        // Grizzly Bears still in graveyard + Memory's Journey
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);

        // Library unchanged
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libSizeBefore);
    }

    // ===== Casting targeting opponent's graveyard =====

    @Test
    @DisplayName("Can target opponent's graveyard and shuffle their cards into their library")
    void canTargetOpponentGraveyard() {
        Card card1 = new GrizzlyBears();
        Card card2 = new LightningBolt();
        harness.setGraveyard(player2, List.of(card1, card2));
        int opponentLibSizeBefore = gd.playerDecks.get(player2.getId()).size();
        harness.setHand(player1, List.of(new MemorysJourney()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player2.getId());

        // Should prompt caster for card selection from opponent's graveyard
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_GRAVEYARD_CHOICE);
        assertThat(gd.interaction.multiSelection().multiGraveyardValidCardIds()).hasSize(2);

        // Select both
        List<UUID> validIds = new ArrayList<>(gd.interaction.multiSelection().multiGraveyardValidCardIds());
        harness.handleMultipleGraveyardCardsChosen(player1, validIds);

        harness.passBothPriorities();

        // Opponent's graveyard should be empty
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();

        // Opponent's library gained 2 cards
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentLibSizeBefore + 2);

        // Memory's Journey goes to caster's graveyard, not opponent's
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Memory's Journey"));
    }

    // ===== No valid targets in graveyard =====

    @Test
    @DisplayName("Casting with empty target player graveyard puts spell on stack directly")
    void castingWithEmptyGraveyardPutsOnStack() {
        harness.setHand(player1, List.of(new MemorysJourney()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player1.getId());

        // No graveyard prompt — spell goes directly on stack
        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        // Memory's Journey goes to graveyard after resolving
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Memory's Journey"));
    }

    // ===== Max targets cap =====

    @Test
    @DisplayName("Max targets is capped at 3 even with more cards in graveyard")
    void maxTargetsCappedAtThree() {
        Card card1 = new GrizzlyBears();
        Card card2 = new LightningBolt();
        Card card3 = new GrizzlyBears();
        Card card4 = new LightningBolt();
        harness.setGraveyard(player1, List.of(card1, card2, card3, card4));
        harness.setHand(player1, List.of(new MemorysJourney()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player1.getId());

        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_GRAVEYARD_CHOICE);
        assertThat(gd.interaction.multiSelection().multiGraveyardMaxCount()).isEqualTo(3);
        assertThat(gd.interaction.multiSelection().multiGraveyardValidCardIds()).hasSize(4);
    }

    // ===== Flashback =====

    @Test
    @DisplayName("Flashback from graveyard shuffles target cards into target player's library")
    void flashbackShufflesCardsIntoLibrary() {
        Card card1 = new GrizzlyBears();
        Card card2 = new LightningBolt();
        // Memory's Journey in caster's graveyard + two cards in opponent's graveyard
        harness.setGraveyard(player1, List.of(new MemorysJourney()));
        harness.setGraveyard(player2, List.of(card1, card2));
        int opponentLibSizeBefore = gd.playerDecks.get(player2.getId()).size();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castFlashback(player1, 0, player2.getId());

        // Memory's Journey removed from graveyard
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .noneMatch(c -> c.getName().equals("Memory's Journey"));

        // Should prompt for graveyard card selection from opponent's graveyard
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_GRAVEYARD_CHOICE);
        assertThat(gd.interaction.multiSelection().multiGraveyardValidCardIds()).hasSize(2);

        // Select both
        List<UUID> validIds = new ArrayList<>(gd.interaction.multiSelection().multiGraveyardValidCardIds());
        harness.handleMultipleGraveyardCardsChosen(player1, validIds);

        // Spell on stack with flashback flag
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().isCastWithFlashback()).isTrue();

        harness.passBothPriorities();

        // Opponent's graveyard empty, library gained cards
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentLibSizeBefore + 2);

        // Memory's Journey is exiled (flashback disposition)
        assertThat(gd.playerExiledCards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Memory's Journey"));
    }

    @Test
    @DisplayName("Flashback targeting own graveyard works (card is already removed before targeting)")
    void flashbackTargetingOwnGraveyard() {
        Card card1 = new GrizzlyBears();
        Card card2 = new LightningBolt();
        harness.setGraveyard(player1, List.of(new MemorysJourney(), card1, card2));
        int libSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castFlashback(player1, 0, player1.getId());

        // Memory's Journey removed from graveyard, only card1 and card2 should be valid targets
        assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.MULTI_GRAVEYARD_CHOICE);
        assertThat(gd.interaction.multiSelection().multiGraveyardValidCardIds()).hasSize(2);

        List<UUID> validIds = new ArrayList<>(gd.interaction.multiSelection().multiGraveyardValidCardIds());
        harness.handleMultipleGraveyardCardsChosen(player1, validIds);

        harness.passBothPriorities();

        // Both cards shuffled into library
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libSizeBefore + 2);

        // Memory's Journey exiled
        assertThat(gd.playerExiledCards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Memory's Journey"));
    }

    @Test
    @DisplayName("Flashback with empty target graveyard puts spell on stack directly")
    void flashbackWithEmptyGraveyardPutsOnStack() {
        harness.setGraveyard(player1, List.of(new MemorysJourney()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castFlashback(player1, 0, player2.getId());

        // No graveyard prompt — spell goes directly on stack
        assertThat(gd.interaction.awaitingInputType()).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().isCastWithFlashback()).isTrue();

        harness.passBothPriorities();

        // Memory's Journey is exiled
        assertThat(gd.playerExiledCards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Memory's Journey"));
    }

    @Test
    @DisplayName("Cannot cast flashback without enough mana")
    void flashbackFailsWithoutMana() {
        harness.setGraveyard(player1, List.of(new MemorysJourney()));

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    // ===== Log verification =====

    @Test
    @DisplayName("Resolution logs mention shuffling cards from graveyard into library")
    void resolutionLogsCorrectly() {
        Card card1 = new GrizzlyBears();
        harness.setGraveyard(player1, List.of(card1));
        harness.setHand(player1, List.of(new MemorysJourney()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player1.getId());

        List<UUID> validIds = new ArrayList<>(gd.interaction.multiSelection().multiGraveyardValidCardIds());
        harness.handleMultipleGraveyardCardsChosen(player1, validIds);

        harness.passBothPriorities();

        assertThat(gd.gameLog)
                .anyMatch(entry -> entry.contains("shuffles") && entry.contains("from graveyard into their library"));
    }
}
