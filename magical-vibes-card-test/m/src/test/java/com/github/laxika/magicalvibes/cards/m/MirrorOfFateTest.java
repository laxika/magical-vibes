package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MirrorOfFate.class, RuneclawBear.class, LlanowarElves.class, LightningBolt.class})
class MirrorOfFateTest extends BaseCardTest {

    @Test
    @DisplayName("With no exiled cards, entire library is exiled and library is empty")
    void noExiledCardsExilesEntireLibrary() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new MirrorOfFate());

        // Set up a library with some cards
        Card bears = new RuneclawBear();
        Card elves = new LlanowarElves();
        harness.setLibrary(player1, List.of(bears, elves));
        harness.setHand(player1, List.of());

        // Ensure no exiled cards (already empty by default)

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Library should be empty
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();

        // Exiled cards should include the library cards
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Runeclaw Bear"))
                .anyMatch(c -> c.getName().equals("Llanowar Elves"));

        // Mirror of Fate should be sacrificed
        harness.assertNotOnBattlefield(player1, "Mirror of Fate");
        harness.assertInGraveyard(player1, "Mirror of Fate");
    }

    @Test
    @DisplayName("With exiled cards, player is prompted to choose up to 7")
    void withExiledCardsPromptsChoice() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new MirrorOfFate());
        harness.setHand(player1, List.of());

        // Put a card in exile
        Card exiledBears = new RuneclawBear();
        gd.addToExile(player1.getId(), exiledBears);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Should be awaiting mirror of fate choice
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MirrorOfFateChoice.class);
    }

    @Test
    @DisplayName("Choosing a single exiled card puts it on top without reorder step")
    void choosingSingleCardPutsOnTopDirectly() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new MirrorOfFate());
        harness.setHand(player1, List.of());

        // Set up library
        Card libraryCard = new LightningBolt();
        harness.setLibrary(player1, List.of(libraryCard));

        // Put one card in exile
        Card exiledBears = new RuneclawBear();
        gd.addToExile(player1.getId(), exiledBears);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Choose the single card
        harness.handleMultipleCardsChosen(player1, List.of(exiledBears.getId()));

        // No reorder step needed for single card — library should have the card on top
        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(1);
        assertThat(library.getFirst().getName()).isEqualTo("Runeclaw Bear");

        // The original library card should be in exile
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Lightning Bolt"));

        // Chosen card should no longer be in exile
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getId().equals(exiledBears.getId()));
    }

    @Test
    @DisplayName("Choosing multiple exiled cards triggers reorder step for ordering")
    void choosingMultipleCardsTriggersReorder() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new MirrorOfFate());
        harness.setHand(player1, List.of());

        // Set up library with a card
        Card libraryCard = new LightningBolt();
        harness.setLibrary(player1, List.of(libraryCard));

        // Put cards in exile
        Card exiledBears = new RuneclawBear();
        Card exiledElves = new LlanowarElves();
        gd.addToExile(player1.getId(), exiledBears);
        gd.addToExile(player1.getId(), exiledElves);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Choose both exiled cards
        harness.handleMultipleCardsChosen(player1,
                List.of(exiledBears.getId(), exiledElves.getId()));

        // Should be awaiting library reorder (player chooses the order)
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);

        // Cards should be removed from exile already
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .noneMatch(c -> c.getId().equals(exiledBears.getId()))
                .noneMatch(c -> c.getId().equals(exiledElves.getId()));

        // The original library card should be in exile
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Lightning Bolt"));

        // Complete reorder: put Elves on top, Bears second (order: [1, 0])
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        // Library should now have Elves on top, Bears second
        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(2);
        assertThat(library.get(0).getName()).isEqualTo("Llanowar Elves");
        assertThat(library.get(1).getName()).isEqualTo("Runeclaw Bear");
    }

    @Test
    @DisplayName("Player can choose zero exiled cards")
    void canChooseZeroCards() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new MirrorOfFate());
        harness.setHand(player1, List.of());

        Card libraryCard = new RuneclawBear();
        harness.setLibrary(player1, List.of(libraryCard));

        Card exiledCard = new LightningBolt();
        gd.addToExile(player1.getId(), exiledCard);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Choose nothing
        harness.handleMultipleCardsChosen(player1, List.of());

        // Library should be empty (all was exiled, nothing chosen)
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();

        // All cards should be in exile (original library card + original exiled card)
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Runeclaw Bear"))
                .anyMatch(c -> c.getName().equals("Lightning Bolt"));
    }

    @Test
    @DisplayName("Mirror of Fate goes to graveyard after activation (sacrifice cost)")
    void sacrificeGoesToGraveyard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new MirrorOfFate());
        harness.setHand(player1, List.of());

        // Exile zone is already empty by default

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player1, "Mirror of Fate");
        harness.assertInGraveyard(player1, "Mirror of Fate");
    }

    @Test
    @DisplayName("Cannot choose more than 7 exiled cards even if more exist")
    void maxSevenCards() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new MirrorOfFate());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of());

        // Put 10 cards in exile
        List<Card> exiledCards = new ArrayList<>();
        for (int i = 0; i < 10; i++) {
            Card c = new RuneclawBear();
            gd.addToExile(player1.getId(), c);
            exiledCards.add(c);
        }

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MirrorOfFateChoice.class);

        // Choose 7 of the 10
        List<UUID> chosen = exiledCards.stream().limit(7).map(Card::getId).toList();
        harness.handleMultipleCardsChosen(player1, chosen);

        // Should be awaiting library reorder for the 7 cards
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);

        // Complete reorder: keep original order [0, 1, 2, 3, 4, 5, 6]
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1, 2, 3, 4, 5, 6)));

        // Library should have 7 cards
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(7);

        // Remaining 3 should still be in exile
        assertThat(gd.getPlayerExiledCards(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Mirror of Fate only affects controller's library and exile zone")
    void doesNotAffectOpponent() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new MirrorOfFate());
        harness.setHand(player1, List.of());

        // Set up player2's library and exile
        Card opponentLibraryCard = new RuneclawBear();
        harness.setLibrary(player2, List.of(opponentLibraryCard));

        Card opponentExiledCard = new LightningBolt();
        gd.addToExile(player2.getId(), opponentExiledCard);

        // Player1 has no exiled cards (already empty by default)
        harness.setLibrary(player1, List.of());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        // Opponent's library and exile should be unchanged
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId()).get(0).getName()).isEqualTo("Runeclaw Bear");
        assertThat(gd.getPlayerExiledCards(player2.getId()))
                .anyMatch(c -> c.getName().equals("Lightning Bolt"));
    }

    @Test
    @DisplayName("Face-down exiled cards cannot be selected alongside face-up cards")
    void cannotChooseFaceDownCard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new MirrorOfFate());
        harness.setHand(player1, List.of());
        Card libraryCard = new LightningBolt();
        harness.setLibrary(player1, List.of(libraryCard));
        Card faceUp = new RuneclawBear();
        Card faceDown = new LlanowarElves();
        gd.addToExile(player1.getId(), faceUp);
        gd.addToExile(player1.getId(), faceDown, null, true);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(faceDown.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        harness.handleMultipleCardsChosen(player1, List.of(faceUp.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(faceUp);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(faceDown, libraryCard);
    }

    @Test
    @DisplayName("With only face-down exiled cards, library is exiled without a choice")
    void onlyFaceDownCardsExilesLibraryWithoutChoice() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new MirrorOfFate());
        harness.setHand(player1, List.of());
        Card libraryCard = new LightningBolt();
        harness.setLibrary(player1, List.of(libraryCard));
        Card faceDown = new RuneclawBear();
        gd.addToExile(player1.getId(), faceDown, null, true);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(faceDown, libraryCard);
    }

    @Test
    @DisplayName("Selecting the same exiled card twice is rejected without changing zones")
    void rejectsDuplicateChosenCard() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new MirrorOfFate());
        harness.setHand(player1, List.of());
        Card libraryCard = new LightningBolt();
        harness.setLibrary(player1, List.of(libraryCard));
        Card first = new RuneclawBear();
        Card second = new LlanowarElves();
        gd.addToExile(player1.getId(), first);
        gd.addToExile(player1.getId(), second);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(first.getId(), first.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(first, second);
        harness.handleMultipleCardsChosen(player1, List.of(first.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(first);
    }

    @Test
    @DisplayName("Selecting eight cards is rejected and a legal choice remains possible")
    void rejectsEightChosenCards() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new MirrorOfFate());
        harness.setHand(player1, List.of());
        Card libraryCard = new LightningBolt();
        harness.setLibrary(player1, List.of(libraryCard));
        List<Card> exiled = new ArrayList<>();
        for (int i = 0; i < 8; i++) {
            Card card = new RuneclawBear();
            gd.addToExile(player1.getId(), card);
            exiled.add(card);
        }

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, exiled.stream().map(Card::getId).toList()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyElementsOf(exiled);
        harness.handleMultipleCardsChosen(player1, List.of());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Cards in the library and cards owned by opponents cannot be selected")
    void rejectsCardsOutsideOwnedExile() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new MirrorOfFate());
        harness.setHand(player1, List.of());
        Card libraryCard = new LightningBolt();
        harness.setLibrary(player1, List.of(libraryCard));
        Card owned = new RuneclawBear();
        Card opponentOwned = new LlanowarElves();
        gd.addToExile(player1.getId(), owned);
        gd.addToExile(player2.getId(), opponentOwned);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(libraryCard.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(opponentOwned.getId())))
                .isInstanceOf(IllegalStateException.class);
        harness.handleMultipleCardsChosen(player1, List.of(owned.getId()));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(owned);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.getPlayerExiledCards(player2.getId())).containsExactly(opponentOwned);
    }

    @Test
    @DisplayName("Sacrifice is paid immediately and exiled cards are chosen only at resolution")
    void paysSacrificeBeforeChoosingCardsAtResolution() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addToBattlefield(player1, new MirrorOfFate());
        harness.setHand(player1, List.of());
        Card libraryCard = new LightningBolt();
        harness.setLibrary(player1, List.of(libraryCard));

        harness.activateAbility(player1, 0, null, null);

        harness.assertNotOnBattlefield(player1, "Mirror of Fate");
        harness.assertInGraveyard(player1, "Mirror of Fate");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        Card newlyExiled = new RuneclawBear();
        gd.addToExile(player1.getId(), newlyExiled);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(newlyExiled.getId()));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(newlyExiled);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(libraryCard);
    }
}
