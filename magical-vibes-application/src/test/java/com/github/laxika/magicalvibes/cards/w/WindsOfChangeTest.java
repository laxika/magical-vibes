package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CosisTrickster;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WindsOfChange.class, Mountain.class, Forest.class})
class WindsOfChangeTest extends BaseCardTest {

    @Test
    @DisplayName("Each player draws the same number of cards as they had in hand")
    void wheelPreservesHandSize() {
        harness.setHand(player1, List.of(new WindsOfChange(), new Mountain(), new Forest()));
        harness.setHand(player2, List.of(new Mountain(), new Forest()));

        fillLibrary(player1, 10);
        fillLibrary(player2, 10);

        harness.addMana(player1, ManaColor.RED, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        // Player1 had 2 cards remaining after casting Winds of Change, so draws 2.
        // Player2 had 2 cards, so draws 2.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Player with empty hand draws zero cards")
    void emptyHandDrawsZero() {
        harness.setHand(player2, List.of());

        fillLibrary(player1, 10);
        fillLibrary(player2, 10);

        harness.castFromHand(player1, new WindsOfChange(), new WindsOfChange().getManaCost());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Original hand cards are shuffled into library, not kept")
    void handCardsGoIntoLibrary() {
        harness.setHand(player2, List.of(new Forest()));

        fillLibrary(player1, 10);
        fillLibrary(player2, 10);

        int libraryBefore = gd.playerDecks.get(player2.getId()).size();

        harness.castFromHand(player1, new WindsOfChange(), new WindsOfChange().getManaCost());
        harness.passBothPriorities();

        // Player2 shuffled 1 card in and drew 1: hand size stays 1, library size unchanged.
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(libraryBefore);
    }

    @Test
    void shufflesIntoEmptyLibraryBeforeDrawing() {
        harness.setHand(player2, List.of(new Forest()));
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of());

        harness.castFromHand(player1, new WindsOfChange(), new WindsOfChange().getManaCost());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId()))
                .anyMatch(card -> card instanceof Forest);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @CardUsed(CosisTrickster.class)
    void emptyHandStillShufflesLibraryForShuffleTriggers() {
        Permanent trickster = harness.addToBattlefieldAndReturn(player1, new CosisTrickster());
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, List.of(new Mountain()));

        harness.castFromHand(player1, new WindsOfChange(), new WindsOfChange().getManaCost());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.MayAbilityChoice.class);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(trickster.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    private void fillLibrary(Player player, int count) {
        harness.setLibrary(player, IntStream.range(0, count)
                .mapToObj(index -> (Card) new Mountain())
                .toList());
    }
}
