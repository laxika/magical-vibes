package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({TahngarthsGlare.class, GrizzlyBears.class})
class TahngarthsGlareTest extends BaseCardTest {

    @Test
    void controllerReordersOpponentsLibraryThenOpponentReordersControllerLibrary() {
        List<Card> ownTopCards = cards(4);
        List<Card> opponentTopCards = cards(4);
        harness.setLibrary(player1, ownTopCards);
        harness.setLibrary(player2, opponentTopCards);
        harness.setHand(player1, List.of(new TahngarthsGlare()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.LibraryReorder firstReorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(firstReorder.playerId()).isEqualTo(player1.getId());
        assertThat(firstReorder.deckOwnerId()).isEqualTo(player2.getId());
        assertThat(firstReorder.cards()).containsExactlyElementsOf(opponentTopCards.subList(0, 3));

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 1, 0)));

        PendingInteraction.LibraryReorder secondReorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(secondReorder.playerId()).isEqualTo(player2.getId());
        assertThat(secondReorder.deckOwnerId()).isEqualTo(player1.getId());
        assertThat(secondReorder.cards()).containsExactlyElementsOf(ownTopCards.subList(0, 3));

        gs.handleInteractionAnswer(gd, player2, new InteractionAnswer.CardOrder(List.of(1, 2, 0)));

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(
                opponentTopCards.get(2), opponentTopCards.get(1), opponentTopCards.get(0), opponentTopCards.get(3));
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(
                ownTopCards.get(1), ownTopCards.get(2), ownTopCards.get(0), ownTopCards.get(3));
    }

    @Test
    void onlyAnOpponentCanBeTargeted() {
        harness.setHand(player1, List.of(new TahngarthsGlare()));
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.castSorcery(player1, 0, player1.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("opponent");
    }

    private List<Card> cards(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(index -> (Card) new GrizzlyBears())
                .toList();
    }
}
