package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameLogEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AncestralKnowledge.class, Abeyance.class, Alms.class, Abjure.class, Abduction.class,
        AuraOfSilence.class})
class AncestralKnowledgeTest extends BaseCardTest {

    private List<Card> castAndLookAtFourCards() {
        Card c0 = new Abeyance();
        Card c1 = new Alms();
        Card c2 = new Abjure();
        Card c3 = new Abduction();
        return castAndLookAt(List.of(c0, c1, c2, c3));
    }

    private List<Card> castAndLookAt(List<Card> cards) {
        harness.setLibrary(player1, cards);
        harness.castFromHand(player1, new AncestralKnowledge(), "{1}{U}");
        harness.passBothPriorities(); // enchantment resolves, ETB trigger goes on the stack
        harness.passBothPriorities(); // ETB trigger resolves

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        return cards;
    }

    @Test
    @DisplayName("Exiles any number of the looked-at cards, then puts the rest back on top in the chosen order")
    void exilesAnyNumberRestOnTop() {
        List<Card> cards = castAndLookAtFourCards();

        harness.handleCardChosen(player1, 1);

        // The pick repeats over what is left instead of ending after one exile.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .contains(cards.get(1).getId(), cards.get(0).getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        List<Card> deckAfter = gd.playerDecks.get(player1.getId());
        assertThat(deckAfter).hasSize(2);
        assertThat(deckAfter.get(0).getId()).isEqualTo(cards.get(3).getId());
        assertThat(deckAfter.get(1).getId()).isEqualTo(cards.get(2).getId());
    }

    @Test
    @DisplayName("Exiling nothing puts every looked-at card back on top")
    void mayExileNothing() {
        List<Card> cards = castAndLookAtFourCards();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(3, 2, 1, 0)));

        List<Card> deckAfter = gd.playerDecks.get(player1.getId());
        assertThat(deckAfter).hasSize(4);
        assertThat(deckAfter.get(0).getId()).isEqualTo(cards.get(3).getId());
        assertThat(deckAfter.get(3).getId()).isEqualTo(cards.get(0).getId());
    }

    @Test
    @DisplayName("Exiling every looked-at card leaves the library empty")
    void mayExileAllCards() {
        List<Card> cards = castAndLookAtFourCards();

        for (int i = 0; i < cards.size(); i++) {
            harness.handleCardChosen(player1, 0);
            if (i < cards.size() - 1) {
                assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
            }
        }

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyElementsOf(cards.stream().map(Card::getId).toList());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Looks at ten cards when the library has at least ten cards")
    void looksAtTopTenCards() {
        List<Card> cards = castAndLookAt(List.of(
                new Abeyance(), new Alms(), new Abjure(), new Abduction(), new Abeyance(),
                new Alms(), new Abjure(), new Abduction(), new Abeyance(), new Alms()));

        harness.handleCardChosen(player1, 9);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(cards.get(9).getId());

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(8, 7, 6, 5, 4, 3, 2, 1, 0)));

        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(
                        cards.get(8).getId(), cards.get(7).getId(), cards.get(6).getId(),
                        cards.get(5).getId(), cards.get(4).getId(), cards.get(3).getId(),
                        cards.get(2).getId(), cards.get(1).getId(), cards.get(0).getId());
    }

    @Test
    @DisplayName("Leaving the battlefield shuffles the controller's library")
    void leavingBattlefieldShufflesLibrary() {
        Permanent knowledge = harness.addToBattlefieldAndReturn(player1, new AncestralKnowledge());

        harness.addToBattlefield(player2, new AuraOfSilence());
        harness.sacrificePermanent(player2, 0, knowledge.getId());
        harness.passBothPriorities(); // Aura of Silence's ability
        harness.passBothPriorities(); // leaves-the-battlefield shuffle trigger

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(knowledge);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains(player1.getUsername() + " shuffles their library."));
    }

    @Test
    @DisplayName("Paying cumulative upkeep keeps it and charges the next upkeep per age counter")
    void paysCumulativeUpkeepAndScalesWithAgeCounters() {
        Permanent knowledge = harness.addToBattlefieldAndReturn(player1, new AncestralKnowledge());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        assertThat(knowledge.getCounterCount(CounterType.AGE)).isEqualTo(1);

        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(knowledge);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(knowledge.getCounterCount(CounterType.AGE)).isEqualTo(2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(knowledge);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
    }

    @Test
    @DisplayName("Declining the cumulative upkeep sacrifices it and shuffles the library")
    void decliningCumulativeUpkeepSacrifices() {
        Permanent knowledge = harness.addToBattlefieldAndReturn(player1, new AncestralKnowledge());

        advanceToUpkeep(player1);
        harness.passBothPriorities(); // cumulative upkeep trigger
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(knowledge);
        harness.assertInGraveyard(player1, "Ancestral Knowledge");
    }

    @Test
    @DisplayName("An empty library finishes the enters ability without asking for a choice")
    void emptyLibraryNeedsNoChoice() {
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new AncestralKnowledge(), "{1}{U}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        harness.assertOnBattlefield(player1, "Ancestral Knowledge");
    }

    @Test
    @DisplayName("Exiling the top ten leaves deeper cards untouched and does not inspect them")
    void exilingTopTenLeavesDeeperCardsUntouched() {
        List<Card> cards = castAndLookAt(List.of(
                new Abeyance(), new Alms(), new Abjure(), new Abduction(), new Abeyance(),
                new Alms(), new Abjure(), new Abduction(), new Abeyance(), new Alms(),
                new Abjure(), new Abduction()));

        for (int i = 0; i < 10; i++) {
            harness.handleCardChosen(player1, 0);
        }

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyElementsOf(cards.subList(0, 10).stream().map(Card::getId).toList());
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(cards.get(10).getId(), cards.get(11).getId());
    }

    @Test
    @DisplayName("A single unexiled card returns to the top without a reorder prompt")
    void singleRemainingCardReturnsToTop() {
        List<Card> cards = castAndLookAtFourCards();

        for (int i = 0; i < 3; i++) {
            harness.handleCardChosen(player1, 0);
        }
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(cards.get(3).getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId)
                .containsExactly(cards.get(0).getId(), cards.get(1).getId(), cards.get(2).getId());
    }

    @Test
    @DisplayName("Declining upkeep shuffles only the controller's library after the sacrifice")
    void declinedUpkeepResolvesShuffleTrigger() {
        Permanent knowledge = harness.addToBattlefieldAndReturn(player1, new AncestralKnowledge());
        List<Card> cards = List.of(new Abeyance(), new Alms(), new Abjure());
        Card opponentCard = new Abduction();
        harness.setLibrary(player1, cards);
        harness.setLibrary(player2, List.of(opponentCard));
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(knowledge);
        harness.assertInGraveyard(player1, "Ancestral Knowledge");
        assertThat(gd.playerDecks.get(player1.getId()))
                .extracting(Card::getId)
                .containsExactlyInAnyOrderElementsOf(cards.stream().map(Card::getId).toList());
        assertThat(gd.playerDecks.get(player2.getId()))
                .extracting(Card::getId)
                .containsExactly(opponentCard.getId());
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains(player1.getUsername() + " shuffles their library."))
                .noneMatch(log -> log.contains(player2.getUsername() + " shuffles their library."));
    }

    @Test
    @DisplayName("The enters trigger still resolves after the enchantment leaves and its library is shuffled")
    void entersTriggerResolvesAfterSourceLeaves() {
        Card libraryCard = new Abeyance();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.castFromHand(player1, new AncestralKnowledge(), "{1}{U}");
        harness.passBothPriorities();
        harness.addToBattlefield(player1, new AuraOfSilence());

        harness.sacrificePermanent(player1, 1,
                harness.getPermanentId(player1, "Ancestral Knowledge"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Ancestral Knowledge");
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText))
                .anyMatch(log -> log.contains(player1.getUsername() + " shuffles their library."));

        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibrarySearch.class);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .extracting(Card::getId).containsExactly(libraryCard.getId());
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cumulative upkeep does not trigger during the opponent's upkeep")
    void noCumulativeUpkeepOnOpponentTurn() {
        Permanent knowledge = harness.addToBattlefieldAndReturn(player1, new AncestralKnowledge());

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(knowledge.getCounterCount(CounterType.AGE)).isZero();
        harness.assertOnBattlefield(player1, "Ancestral Knowledge");
    }
}
