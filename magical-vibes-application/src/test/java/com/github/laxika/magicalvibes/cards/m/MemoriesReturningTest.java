package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MemoriesReturning.class, Island.class})
class MemoriesReturningTest extends BaseCardTest {

    @Test
    @DisplayName("Alternates controller hand picks and opponent bottom picks")
    void alternatesHandAndBottomPicks() {
        Card firstHand = new Island();
        Card firstBottom = new Island();
        Card secondHand = new Island();
        Card secondBottom = new Island();
        Card finalHand = new Island();
        Card untouched = new Island();
        harness.setLibrary(player1, List.of(firstHand, firstBottom, secondHand, secondBottom,
                finalHand, untouched));
        harness.setHand(player1, List.of(new MemoriesReturning()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player2, List.of(firstHand.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not your turn");
        assertChoice(player1, List.of(firstHand, firstBottom, secondHand, secondBottom, finalHand));

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(firstHand.getId(), secondHand.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(untouched.getId())))
                .isInstanceOf(IllegalStateException.class);
        assertChoice(player1, List.of(firstHand, firstBottom, secondHand, secondBottom, finalHand));

        harness.handleMultipleCardsChosen(player1, List.of(firstHand.getId()));
        assertChoice(player2, List.of(firstBottom, secondHand, secondBottom, finalHand));

        harness.handleMultipleCardsChosen(player2, List.of(firstBottom.getId()));
        assertChoice(player1, List.of(secondHand, secondBottom, finalHand));

        harness.handleMultipleCardsChosen(player1, List.of(secondHand.getId()));
        assertChoice(player2, List.of(secondBottom, finalHand));

        harness.handleMultipleCardsChosen(player2, List.of(secondBottom.getId()));

        assertThat(gd.playerHands.get(player1.getId()))
                .containsExactly(firstHand, secondHand, finalHand);
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(untouched, firstBottom, secondBottom);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Flashback resolves the same effect and exiles Memories Returning")
    void flashbackResolvesAndExilesSpell() {
        Card only = new Island();
        harness.setLibrary(player1, List.of(only));
        Card spell = new MemoriesReturning();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castAndResolveFlashback(player1, 0, null);
        assertChoice(player1, List.of(only));

        harness.handleMultipleCardsChosen(player1, List.of(only.getId()));

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .contains(spell);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(only);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).doesNotContain(spell);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @ParameterizedTest
    @ValueSource(ints = {2, 3, 4})
    @DisplayName("A short library follows the same alternating choices without reusing bottomed cards")
    void shortLibraryAlternatesOnlyOriginallyRevealedCards(int count) {
        List<Card> cards = List.of(new Island(), new Island(), new Island(), new Island());
        harness.setLibrary(player1, cards.subList(0, count));
        Card spell = new MemoriesReturning();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);
        assertChoice(player1, cards.subList(0, count));
        harness.handleMultipleCardsChosen(player1, List.of(cards.get(0).getId()));
        assertChoice(player2, cards.subList(1, count));
        harness.handleMultipleCardsChosen(player2, List.of(cards.get(1).getId()));
        if (count >= 3) {
            assertChoice(player1, cards.subList(2, count));
            harness.handleMultipleCardsChosen(player1, List.of(cards.get(2).getId()));
        }
        if (count == 4) {
            assertChoice(player2, List.of(cards.get(3)));
            harness.handleMultipleCardsChosen(player2, List.of(cards.get(3).getId()));
        }

        assertThat(gd.playerHands.get(player1.getId())).containsExactlyElementsOf(
                count == 2 ? List.of(cards.get(0)) : List.of(cards.get(0), cards.get(2)));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(
                count == 4 ? List.of(cards.get(1), cards.get(3)) : List.of(cards.get(1)));
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library resolves without choices or a failed draw")
    void emptyLibraryResolvesWithoutChoices() {
        harness.setLibrary(player1, List.of());
        Card spell = new MemoriesReturning();
        harness.setHand(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 4);

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(spell);
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    private void assertChoice(com.github.laxika.magicalvibes.model.Player player,
            List<Card> cards) {
        PendingInteraction.LibraryRevealChoice choice = gd.interaction
                .activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.playerId()).isEqualTo(player.getId());
        assertThat(choice.allCards()).containsExactlyElementsOf(cards);
        assertThat(choice.minCount()).isEqualTo(1);
        assertThat(choice.maxCount()).isEqualTo(1);
    }
}
