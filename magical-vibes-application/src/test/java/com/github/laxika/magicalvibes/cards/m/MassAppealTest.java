package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DoomedTraveler;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

class MassAppealTest extends BaseCardTest {

    private void castMassAppeal() {
        harness.setLibrary(player1, new ArrayList<>(
                IntStream.range(0, 6).mapToObj(i -> (Card) new GrizzlyBears()).toList()));
        harness.setHand(player1, List.of(new MassAppeal()));
        harness.addMana(player1, ManaColor.BLUE, 3);

        harness.castAndResolveSorcery(player1, 0, 0);
    }

    @Test
    @DisplayName("Draws a card for each Human the caster controls")
    void drawsOnePerHuman() {
        harness.addToBattlefield(player1, new DoomedTraveler());
        harness.addToBattlefield(player1, new DoomedTraveler());

        castMassAppeal();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
    }

    @Test
    @DisplayName("Draws nothing when no Humans are controlled")
    void drawsNothingWithoutHumans() {
        harness.addToBattlefield(player1, new GrizzlyBears());

        castMassAppeal();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(6);
    }

    @Test
    @DisplayName("Humans controlled by the opponent are not counted")
    void ignoresOpponentHumans() {
        harness.addToBattlefield(player1, new DoomedTraveler());
        harness.addToBattlefield(player2, new DoomedTraveler());
        harness.addToBattlefield(player2, new DoomedTraveler());

        castMassAppeal();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5);
    }
}
