package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.a.AbbeyGriffin;
import com.github.laxika.magicalvibes.cards.b.BumpInTheNight;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.cards.g.GroundSeal;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MemorysJourney.class, AbbeyGriffin.class, BumpInTheNight.class, GroundSeal.class})
class MemorysJourneyTest extends BaseCardTest {

    @Test
    @DisplayName("Casting targeting self with cards in graveyard prompts for target selection")
    void castingTargetingSelfPromptsForGraveyardSelection() {
        Card card1 = new AbbeyGriffin();
        Card card2 = new BumpInTheNight();
        harness.setGraveyard(player1, List.of(card1, card2));
        harness.setHand(player1, List.of(new MemorysJourney()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).maxCount()).isEqualTo(2); // min(3, 2 cards)
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds()).hasSize(2);

        // Spell is NOT yet on the stack
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Selecting targets and resolving shuffles cards from graveyard into library")
    void selectingTargetsShufflesIntoLibrary() {
        Card card1 = new AbbeyGriffin();
        Card card2 = new BumpInTheNight();
        harness.setGraveyard(player1, List.of(card1, card2));
        int libSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new MemorysJourney()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player1.getId());

        // Select both cards
        List<UUID> validIds = new ArrayList<>(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        harness.handleMultipleCardsChosen(player1, validIds);

        // Spell should be on the stack
        assertThat(gd.stack).hasSize(1);

        // Resolve
        harness.passBothPriorities();

        // Cards should no longer be in graveyard (only Memory's Journey itself goes to graveyard)
        harness.assertNotInGraveyard(player1, "Abbey Griffin");
        harness.assertNotInGraveyard(player1, "Bump in the Night");
        harness.assertInGraveyard(player1, "Memory's Journey");

        // Library should have gained 2 cards
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libSizeBefore + 2);
    }

    @Test
    @DisplayName("Selecting one of three cards works correctly")
    void selectingOneOfThreeCards() {
        Card card1 = new AbbeyGriffin();
        Card card2 = new BumpInTheNight();
        Card card3 = new AbbeyGriffin();
        harness.setGraveyard(player1, List.of(card1, card2, card3));
        int libSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new MemorysJourney()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player1.getId());

        // Select only one card
        List<UUID> validIds = new ArrayList<>(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        harness.handleMultipleCardsChosen(player1, List.of(validIds.getFirst()));

        harness.passBothPriorities();

        // Two cards still in graveyard + Memory's Journey itself
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);

        // Library gained 1 card
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libSizeBefore + 1);
    }

    @Test
    @DisplayName("Selecting zero targets with cards available still resolves")
    void selectingZeroTargets() {
        harness.setGraveyard(player1, List.of(new AbbeyGriffin()));
        int libSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.setHand(player1, List.of(new MemorysJourney()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player1.getId());

        // Select zero targets
        harness.handleMultipleCardsChosen(player1, List.of());

        harness.passBothPriorities();

        // Abbey Griffin still in graveyard + Memory's Journey
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(2);

        // Library unchanged
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libSizeBefore);
    }

    @Test
    @DisplayName("Can target opponent's graveyard and shuffle their cards into their library")
    void canTargetOpponentGraveyard() {
        Card card1 = new AbbeyGriffin();
        Card card2 = new BumpInTheNight();
        harness.setGraveyard(player2, List.of(card1, card2));
        int opponentLibSizeBefore = gd.playerDecks.get(player2.getId()).size();
        harness.setHand(player1, List.of(new MemorysJourney()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player2.getId());

        // Should prompt caster for card selection from opponent's graveyard
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds()).hasSize(2);

        // Select both
        List<UUID> validIds = new ArrayList<>(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        harness.handleMultipleCardsChosen(player1, validIds);

        harness.passBothPriorities();

        // Opponent's graveyard should be empty
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();

        // Opponent's library gained 2 cards
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentLibSizeBefore + 2);

        // Memory's Journey goes to caster's graveyard, not opponent's
        harness.assertInGraveyard(player1, "Memory's Journey");
    }

    @Test
    @DisplayName("Casting with empty target player graveyard puts spell on stack directly")
    void castingWithEmptyGraveyardPutsOnStack() {
        harness.setHand(player1, List.of(new MemorysJourney()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player1.getId());

        // No graveyard prompt — spell goes directly on stack
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        // Memory's Journey goes to graveyard after resolving
        harness.assertInGraveyard(player1, "Memory's Journey");
    }

    @Test
    @DisplayName("Max targets is capped at 3 even with more cards in graveyard")
    void maxTargetsCappedAtThree() {
        Card card1 = new AbbeyGriffin();
        Card card2 = new BumpInTheNight();
        Card card3 = new AbbeyGriffin();
        Card card4 = new BumpInTheNight();
        harness.setGraveyard(player1, List.of(card1, card2, card3, card4));
        harness.setHand(player1, List.of(new MemorysJourney()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player1.getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).maxCount()).isEqualTo(3);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds()).hasSize(4);
    }

    @Test
    @DisplayName("Flashback from graveyard shuffles target cards into target player's library")
    void flashbackShufflesCardsIntoLibrary() {
        Card card1 = new AbbeyGriffin();
        Card card2 = new BumpInTheNight();
        // Memory's Journey in caster's graveyard + two cards in opponent's graveyard
        harness.setGraveyard(player1, List.of(new MemorysJourney()));
        harness.setGraveyard(player2, List.of(card1, card2));
        int opponentLibSizeBefore = gd.playerDecks.get(player2.getId()).size();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castFlashback(player1, 0, player2.getId());

        // Memory's Journey removed from graveyard
        harness.assertNotInGraveyard(player1, "Memory's Journey");

        // Should prompt for graveyard card selection from opponent's graveyard
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds()).hasSize(2);

        // Select both
        List<UUID> validIds = new ArrayList<>(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        harness.handleMultipleCardsChosen(player1, validIds);

        // Spell on stack with flashback flag
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().isCastWithFlashback()).isTrue();

        harness.passBothPriorities();

        // Opponent's graveyard empty, library gained cards
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentLibSizeBefore + 2);

        // Memory's Journey is exiled (flashback disposition)
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Memory's Journey"));
    }

    @Test
    @DisplayName("Flashback targeting own graveyard works (card is already removed before targeting)")
    void flashbackTargetingOwnGraveyard() {
        Card card1 = new AbbeyGriffin();
        Card card2 = new BumpInTheNight();
        harness.setGraveyard(player1, List.of(new MemorysJourney(), card1, card2));
        int libSizeBefore = gd.playerDecks.get(player1.getId()).size();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castFlashback(player1, 0, player1.getId());

        // Memory's Journey removed from graveyard, only card1 and card2 should be valid targets
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MultiGraveyardChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds()).hasSize(2);

        List<UUID> validIds = new ArrayList<>(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        harness.handleMultipleCardsChosen(player1, validIds);

        harness.passBothPriorities();

        // Both cards shuffled into library
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(libSizeBefore + 2);

        // Memory's Journey exiled
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Memory's Journey"));
    }

    @Test
    @DisplayName("Flashback with empty target graveyard puts spell on stack directly")
    void flashbackWithEmptyGraveyardPutsOnStack() {
        harness.setGraveyard(player1, List.of(new MemorysJourney()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castFlashback(player1, 0, player2.getId());

        // No graveyard prompt — spell goes directly on stack
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).hasSize(1);
        assertThat(gd.stack.getFirst().isCastWithFlashback()).isTrue();

        harness.passBothPriorities();

        // Memory's Journey is exiled
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Memory's Journey"));
    }

    @Test
    @DisplayName("Cannot cast flashback without enough mana")
    void flashbackFailsWithoutMana() {
        harness.setGraveyard(player1, List.of(new MemorysJourney()));

        assertThatThrownBy(() -> harness.castFlashback(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Resolution logs mention shuffling cards from graveyard into library")
    void resolutionLogsCorrectly() {
        Card card1 = new AbbeyGriffin();
        harness.setGraveyard(player1, List.of(card1));
        harness.setHand(player1, List.of(new MemorysJourney()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, player1.getId());

        List<UUID> validIds = new ArrayList<>(gd.interaction.activeInteraction(PendingInteraction.MultiGraveyardChoice.class).validCardIds());
        harness.handleMultipleCardsChosen(player1, validIds);

        harness.passBothPriorities();

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(entry -> entry.contains("shuffles") && entry.contains("from graveyard into their library"));
    }

    @Test
    @DisplayName("Only targets still in the graveyard are shuffled on resolution")
    void removedGraveyardTargetIsNotShuffled() {
        Card remaining = new AbbeyGriffin();
        Card removed = new BumpInTheNight();
        harness.setGraveyard(player2, List.of(remaining, removed));
        harness.setHand(player1, List.of(new MemorysJourney()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int librarySize = gd.playerDecks.get(player2.getId()).size();

        harness.castInstant(player1, 0, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of(remaining.getId(), removed.getId()));
        harness.setGraveyard(player2, List.of(remaining));
        harness.setHand(player2, List.of(removed));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySize + 1).contains(remaining);
        harness.assertInHand(player2, "Bump in the Night");
        harness.assertInGraveyard(player1, "Memory's Journey");
    }

    @Test
    @DisplayName("Graveyard targets protected before resolution are not shuffled")
    void graveyardTargetsBecomingUntargetableAreNotShuffled() {
        Card target = new AbbeyGriffin();
        harness.setGraveyard(player2, List.of(target));
        harness.setHand(player1, List.of(new MemorysJourney()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int librarySize = gd.playerDecks.get(player2.getId()).size();

        harness.castInstant(player1, 0, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of(target.getId()));
        harness.addToBattlefield(player2, new GroundSeal());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(target);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(librarySize);
        harness.assertInGraveyard(player1, "Memory's Journey");
    }


    @Test
    @DisplayName("All three selected cards are shuffled, leaving unselected cards behind")
    void shufflesThreeSelectedCards() {
        Card first = new AbbeyGriffin();
        Card second = new BumpInTheNight();
        Card third = new AbbeyGriffin();
        Card unselected = new BumpInTheNight();
        harness.setGraveyard(player2, List.of(first, second, third, unselected));
        harness.setHand(player1, List.of(new MemorysJourney()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        int librarySize = gd.playerDecks.get(player2.getId()).size();

        harness.castInstant(player1, 0, player2.getId());
        harness.handleMultipleCardsChosen(player1, List.of(first.getId(), second.getId(), third.getId()));
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(unselected);
        assertThat(gd.playerDecks.get(player2.getId()))
                .hasSize(librarySize + 3).contains(first, second, third).doesNotContain(unselected);
        harness.assertInGraveyard(player1, "Memory's Journey");
    }

    @Test
    @DisplayName("Flashback permits selecting zero cards and still exiles the spell")
    void flashbackWithZeroSelectedCards() {
        Card unselected = new AbbeyGriffin();
        harness.setGraveyard(player1, List.of(new MemorysJourney(), unselected));
        harness.addMana(player1, ManaColor.GREEN, 1);
        int librarySize = gd.playerDecks.get(player1.getId()).size();

        harness.castFlashback(player1, 0, player1.getId());
        harness.handleMultipleCardsChosen(player1, List.of());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(unselected);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(librarySize);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Memory's Journey"));
    }

}
