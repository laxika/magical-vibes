package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MemoriesReturning.class, GrizzlyBears.class})
class MemoriesReturningTest extends BaseCardTest {

    @Test
    @DisplayName("Alternates controller hand picks and opponent bottom picks")
    void alternatesHandAndBottomPicks() {
        Card firstHand = new GrizzlyBears();
        Card firstBottom = new GrizzlyBears();
        Card secondHand = new GrizzlyBears();
        Card secondBottom = new GrizzlyBears();
        Card finalHand = new GrizzlyBears();
        Card untouched = new GrizzlyBears();
        harness.setLibrary(player1, List.of(firstHand, firstBottom, secondHand, secondBottom,
                finalHand, untouched));
        harness.setHand(player1, List.of(new MemoriesReturning()));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player2, List.of(firstHand.getId())))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not your turn");
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
        Card only = new GrizzlyBears();
        harness.setLibrary(player1, List.of(only));
        Card spell = new MemoriesReturning();
        harness.setGraveyard(player1, List.of(spell));
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 7);

        harness.castFlashback(player1, 0);
        harness.passBothPriorities();
        assertChoice(player1, List.of(only));

        harness.handleMultipleCardsChosen(player1, List.of(only.getId()));

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .contains(spell);
        assertThat(gd.interaction.activeInteraction()).isNull();
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
