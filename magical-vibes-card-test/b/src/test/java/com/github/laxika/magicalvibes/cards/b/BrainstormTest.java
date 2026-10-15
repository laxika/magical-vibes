package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.a.Abundance;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Brainstorm.class, Island.class, Abundance.class})
class BrainstormTest extends BaseCardTest {

    private List<Card> fiveCards() {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < 5; i++) {
            cards.add(new Island());
        }
        return cards;
    }

    private void castBrainstorm(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.castFromHand(player1, new Brainstorm(), "{U}");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Draws three cards, then asks which two hand cards to put on top")
    void drawsThreeThenPrompts() {
        List<Card> library = fiveCards();
        castBrainstorm(library);

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(library.get(0), library.get(1), library.get(2));
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class);
    }

    @Test
    @DisplayName("Requires exactly two cards when at least two cards are available")
    void requiresExactlyTwoCardsWhenAvailable() {
        castBrainstorm(fiveCards());

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOfSatisfying(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class, choice -> {
                    assertThat(choice.minCount()).isEqualTo(2);
                    assertThat(choice.maxCount()).isEqualTo(2);
                });
    }

    @Test
    @DisplayName("Choosing two cards puts them on top of the library, first chosen on top, no top/bottom prompt")
    void putsChosenCardsOnTop() {
        List<Card> library = fiveCards();
        castBrainstorm(library);

        Card drawn0 = library.get(0);
        Card drawn1 = library.get(1);
        Card drawn2 = library.get(2);

        harness.handleMultipleCardsChosen(player1, List.of(drawn0.getId(), drawn1.getId()));

        // No destination prompt for Brainstorm — the cards go straight to the top.
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawn2);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(drawn0, drawn1, library.get(3), library.get(4));
    }

    @Test
    @DisplayName("Can put a card that was already in hand on top")
    void choosesFromEntireHand() {
        List<Card> library = fiveCards();
        Card alreadyInHand = new Island();
        harness.setLibrary(player1, library);

        harness.setHand(player1, List.of(new Brainstorm(), alreadyInHand));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0);

        harness.handleMultipleCardsChosen(player1, List.of(alreadyInHand.getId(), library.get(0).getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(library.get(1), library.get(2));
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(alreadyInHand, library.get(0), library.get(3), library.get(4));
    }

    @Test
    @DisplayName("Completes the return choice before losing after the library runs out")
    void completesReturnChoiceBeforeLosingWhenLibraryRunsOut() {
        Card libraryCard = new Island();
        Card alreadyInHand = new Island();
        harness.setLibrary(player1, List.of(libraryCard));

        harness.setHand(player1, List.of(new Brainstorm(), alreadyInHand));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0);

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class);
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);

        harness.handleMultipleCardsChosen(player1, List.of(alreadyInHand.getId(), libraryCard.getId()));

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(alreadyInHand, libraryCard);
    }

    @Test
    @DisplayName("Requires every available card before losing after a short draw")
    void requiresEveryAvailableCardWhenLibraryIsShort() {
        Card libraryCard = new Island();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.castFromHand(player1, new Brainstorm(), "{U}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction())
                .isInstanceOfSatisfying(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class, choice -> {
                    assertThat(choice.minCount()).isEqualTo(1);
                    assertThat(choice.maxCount()).isEqualTo(1);
                });
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);

        harness.handleMultipleCardsChosen(player1, List.of(libraryCard.getId()));

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
    }

    @Test
    @DisplayName("Does not open a return choice when the library is empty")
    void doesNotOpenReturnChoiceWithEmptyLibrary() {
        harness.setLibrary(player1, List.of());
        harness.castFromHand(player1, new Brainstorm(), "{U}");
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Can put the chosen cards on top in reverse draw order")
    void putsCardsOnTopInChosenOrder() {
        List<Card> library = fiveCards();
        castBrainstorm(library);

        harness.handleMultipleCardsChosen(player1, List.of(library.get(2).getId(), library.get(0).getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(library.get(1));
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(library.get(2), library.get(0), library.get(3), library.get(4));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Drawing exactly the remaining three cards does not cause a loss")
    void drawingExactlyThreeRemainingCardsDoesNotLose() {
        List<Card> library = List.of(new Island(), new Island(), new Island());
        castBrainstorm(library);

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        harness.handleMultipleCardsChosen(player1, List.of(library.get(1).getId(), library.get(0).getId()));

        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(library.get(2));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(library.get(1), library.get(0));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Cannot return the same card twice instead of two distinct cards")
    void rejectsDuplicateCardSelection() {
        List<Card> library = fiveCards();
        castBrainstorm(library);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(library.get(0).getId(), library.get(0).getId())))
                .isInstanceOf(IllegalStateException.class);

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(library.get(0), library.get(1), library.get(2));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(library.get(3), library.get(4));
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOf(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(library.get(0).getId(), library.get(1).getId()));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(library.get(2));
    }

    @Test
    @DisplayName("Finishes all optional draw replacements before choosing cards to return")
    void completesDrawReplacementsBeforeReturningCards() {
        List<Card> library = fiveCards();
        Card alreadyInHand = new Island();
        harness.addToBattlefield(player1, new Abundance());
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new Brainstorm(), alreadyInHand));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castAndResolveInstant(player1, 0);

        for (int i = 0; i < 3; i++) {
            assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
            harness.handleMayAbilityChosen(player1, false);
        }

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(alreadyInHand, library.get(0), library.get(1), library.get(2));
        assertThat(gd.interaction.activeInteraction())
                .isInstanceOfSatisfying(PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class, choice -> {
                    assertThat(choice.minCount()).isEqualTo(2);
                    assertThat(choice.maxCount()).isEqualTo(2);
                });
        harness.handleMultipleCardsChosen(player1, List.of(alreadyInHand.getId(), library.get(2).getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(library.get(0), library.get(1));
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(alreadyInHand, library.get(2), library.get(3), library.get(4));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
