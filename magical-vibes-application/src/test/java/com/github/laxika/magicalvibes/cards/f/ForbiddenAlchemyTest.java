package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.cards.w.WalkingCorpse;
import com.github.laxika.magicalvibes.cards.d.DeadWeight;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ForbiddenAlchemy.class, WalkingCorpse.class, DeadWeight.class})
class ForbiddenAlchemyTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Forbidden Alchemy puts it on the stack")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new ForbiddenAlchemy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castInstant(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.INSTANT_SPELL);
        assertThat(entry.getCard()).isInstanceOf(ForbiddenAlchemy.class);
        assertThat(entry.getControllerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Resolving enters library reveal choice state")
    void resolvingEntersRevealChoiceState() {
        Card chosen = new WalkingCorpse();
        harness.setLibrary(player1, List.of(chosen, new DeadWeight(), new WalkingCorpse(), new DeadWeight()));
        harness.setHand(player1, List.of(new ForbiddenAlchemy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
    }

    @Test
    @DisplayName("Forbidden Alchemy goes to graveyard after resolving")
    void goesToGraveyardAfterResolving() {
        Card card0 = new WalkingCorpse();
        harness.setLibrary(player1, List.of(card0, new DeadWeight(), new WalkingCorpse(), new DeadWeight()));

        harness.setHand(player1, List.of(new ForbiddenAlchemy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        GameData gd = harness.getGameData();
        // The spell only reaches the graveyard once its resolution finishes
        harness.handleMultipleCardsChosen(player1, List.of(card0.getId()));

        harness.assertInGraveyard(player1, "Forbidden Alchemy");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Choosing a card puts it in hand and the rest into graveyard")
    void choosingPutsOneInHandRestInGraveyard() {
        Card card0 = new WalkingCorpse();
        Card card1 = new DeadWeight();
        Card card2 = new WalkingCorpse();
        Card card3 = new DeadWeight();
        harness.setLibrary(player1, List.of(card0, card1, card2, card3));

        harness.setHand(player1, List.of(new ForbiddenAlchemy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        GameData gd = harness.getGameData();
        // Choose card1 (DeadWeight) for hand
        harness.handleMultipleCardsChosen(player1, List.of(card1.getId()));

        // Hand should contain the chosen card
        assertThat(gd.playerHands.get(player1.getId())).contains(card1);

        // The other 3 should be in the graveyard (plus Forbidden Alchemy itself)
        List<Card> graveyard = gd.playerGraveyards.get(player1.getId());
        assertThat(graveyard).contains(card0);
        assertThat(graveyard).contains(card2);
        assertThat(graveyard).contains(card3);
    }

    @Test
    @DisplayName("Choosing the first card puts it in hand and rest into graveyard")
    void choosingFirstCard() {
        Card card0 = new WalkingCorpse();
        Card card1 = new DeadWeight();
        Card card2 = new WalkingCorpse();
        Card card3 = new DeadWeight();
        harness.setLibrary(player1, List.of(card0, card1, card2, card3));

        harness.setHand(player1, List.of(new ForbiddenAlchemy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        GameData gd = harness.getGameData();
        harness.handleMultipleCardsChosen(player1, List.of(card0.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(card0);
        List<Card> graveyard = gd.playerGraveyards.get(player1.getId());
        assertThat(graveyard).contains(card1);
        assertThat(graveyard).contains(card2);
        assertThat(graveyard).contains(card3);
    }

    @Test
    @DisplayName("Choosing clears awaiting state")
    void choosingClearsAwaitingState() {
        Card card0 = new WalkingCorpse();
        Card card1 = new WalkingCorpse();
        Card card2 = new WalkingCorpse();
        Card card3 = new WalkingCorpse();
        harness.setLibrary(player1, List.of(card0, card1, card2, card3));

        harness.setHand(player1, List.of(new ForbiddenAlchemy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        GameData gd = harness.getGameData();
        harness.handleMultipleCardsChosen(player1, List.of(card0.getId()));

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Remaining cards do not stay in library")
    void remainingCardsNotInLibrary() {
        Card card0 = new WalkingCorpse();
        Card card1 = new DeadWeight();
        Card card2 = new WalkingCorpse();
        Card card3 = new DeadWeight();
        harness.setLibrary(player1, List.of(card0, card1, card2, card3));

        harness.setHand(player1, List.of(new ForbiddenAlchemy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        GameData gd = harness.getGameData();
        harness.handleMultipleCardsChosen(player1, List.of(card0.getId()));

        // Library should be empty (we only put 4 cards in it, all were taken)
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("With 1 card in library, it automatically goes to hand")
    void oneCardInLibrary() {
        GameData gd = harness.getGameData();
        Card singleCard = new WalkingCorpse();
        harness.setLibrary(player1, List.of(singleCard));

        harness.setHand(player1, List.of(new ForbiddenAlchemy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(singleCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("looks at the top card"));
    }

    @Test
    @DisplayName("With 2 cards in library, enters reveal choice with 2 cards")
    void twoCardsInLibrary() {
        GameData gd = harness.getGameData();
        Card cardA = new WalkingCorpse();
        Card cardB = new DeadWeight();
        harness.setLibrary(player1, List.of(cardA, cardB));

        harness.setHand(player1, List.of(new ForbiddenAlchemy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);

        // Choose cardA for hand
        harness.handleMultipleCardsChosen(player1, List.of(cardA.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(cardA);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(cardB);
    }

    @Test
    @DisplayName("With empty library, nothing happens")
    void emptyLibrary() {
        GameData gd = harness.getGameData();
        harness.setLibrary(player1, List.of());

        harness.setHand(player1, List.of(new ForbiddenAlchemy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("library is empty"));
    }

    @Test
    @DisplayName("Flashback from graveyard works correctly")
    void flashbackFromGraveyard() {
        Card card0 = new WalkingCorpse();
        Card card1 = new DeadWeight();
        Card card2 = new WalkingCorpse();
        Card card3 = new DeadWeight();
        harness.setLibrary(player1, List.of(card0, card1, card2, card3));

        harness.setGraveyard(player1, List.of(new ForbiddenAlchemy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castAndResolveFlashback(player1, 0, null);

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);

        harness.handleMultipleCardsChosen(player1, List.of(card0.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(card0);
        List<Card> graveyard = gd.playerGraveyards.get(player1.getId());
        assertThat(graveyard).contains(card1);
        assertThat(graveyard).contains(card2);
        assertThat(graveyard).contains(card3);
    }

    @Test
    @DisplayName("Flashback exiles the spell after resolving")
    void flashbackExilesAfterResolving() {
        Card card0 = new WalkingCorpse();
        Card card1 = new WalkingCorpse();
        Card card2 = new WalkingCorpse();
        Card card3 = new WalkingCorpse();
        harness.setLibrary(player1, List.of(card0, card1, card2, card3));

        harness.setGraveyard(player1, List.of(new ForbiddenAlchemy()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 6);

        harness.castAndResolveFlashback(player1, 0, null);

        GameData gd = harness.getGameData();
        harness.handleMultipleCardsChosen(player1, List.of(card0.getId()));

        // Should NOT be in graveyard
        harness.assertNotInGraveyard(player1, "Forbidden Alchemy");
        // Should be in exile
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getName().equals("Forbidden Alchemy"));
    }

    @Test
    @DisplayName("Game log records looking at cards")
    void gameLogRecordsLooking() {
        harness.setLibrary(player1, List.of(new WalkingCorpse(), new DeadWeight(), new WalkingCorpse(), new DeadWeight()));

        harness.setHand(player1, List.of(new ForbiddenAlchemy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        GameData gd = harness.getGameData();
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("looks at the top") && log.contains("4"));
        PendingInteraction.LibraryRevealChoice choice =
                (PendingInteraction.LibraryRevealChoice) gd.interaction.activeInteraction();
        harness.handleMultipleCardsChosen(player1, List.of(choice.validCardIds().getFirst()));
    }

    @Test
    @DisplayName("Game log records putting card in hand and rest in graveyard")
    void gameLogRecordsChoice() {
        Card card0 = new WalkingCorpse();
        harness.setLibrary(player1, List.of(card0, new DeadWeight(), new WalkingCorpse(), new DeadWeight()));

        harness.setHand(player1, List.of(new ForbiddenAlchemy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveInstant(player1, 0);

        GameData gd = harness.getGameData();
        harness.handleMultipleCardsChosen(player1, List.of(card0.getId()));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log ->
                log.contains("puts one card into their hand") && log.contains("graveyard"));
    }

    @Test
    @DisplayName("Choosing a card is mandatory when the library has cards")
    void cannotDeclineChoosingCard() {
        Card chosen = new WalkingCorpse();
        Card other = new DeadWeight();
        harness.setLibrary(player1, List.of(chosen, other));
        harness.setHand(player1, List.of(new ForbiddenAlchemy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);

        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(other);
    }

    @Test
    @DisplayName("Only the top four cards move and deeper cards retain their order")
    void deeperLibraryCardsRemainInOrder() {
        Card chosen = new DeadWeight();
        Card second = new WalkingCorpse();
        Card third = new WalkingCorpse();
        Card fourth = new DeadWeight();
        Card fifth = new WalkingCorpse();
        Card sixth = new DeadWeight();
        harness.setLibrary(player1, List.of(chosen, second, third, fourth, fifth, sixth));
        harness.setHand(player1, List.of(new ForbiddenAlchemy()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castAndResolveInstant(player1, 0);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(second, third, fourth);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fifth, sixth);
    }
}
