package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class TimeSpiralTest extends BaseCardTest {

    @Test
    @DisplayName("Each player shuffles hand and graveyard away and draws seven")
    void eachPlayerShufflesAndDrawsSeven() {
        Card handCard = new GrizzlyBears();
        Card graveyardCard = new GrizzlyBears();
        harness.setHand(player1, List.of(new TimeSpiral()));
        harness.setHand(player2, List.of(handCard));
        gd.playerGraveyards.get(player2.getId()).add(graveyardCard);
        harness.setLibrary(player1, deckOf(20));
        harness.setLibrary(player2, deckOf(20));

        cast();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(7);
        assertThat(gd.playerGraveyards.get(player2.getId())).isEmpty();
        List<Card> libraryAndHand = new ArrayList<>(gd.playerDecks.get(player2.getId()));
        libraryAndHand.addAll(gd.playerHands.get(player2.getId()));
        assertThat(libraryAndHand).contains(handCard, graveyardCard).hasSize(22);
    }

    @Test
    @DisplayName("Untaps up to six lands you control")
    void untapsUpToSixLands() {
        harness.setHand(player1, List.of(new TimeSpiral()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, deckOf(20));
        harness.setLibrary(player2, deckOf(20));

        List<Permanent> lands = new ArrayList<>();
        for (int i = 0; i < 7; i++) {
            Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());
            land.tap();
            lands.add(land);
        }
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.tap();

        cast();
        List<UUID> chosenLands = lands.stream().limit(6).map(Permanent::getId).toList();
        harness.handleMultiplePermanentsChosen(player1, chosenLands);

        assertThat(lands.stream().filter(land -> !land.isTapped())).hasSize(6);
        assertThat(lands.stream().filter(Permanent::isTapped)).hasSize(1);
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Exiles Time Spiral after resolution")
    void exilesSpell() {
        harness.setHand(player1, List.of(new TimeSpiral()));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, deckOf(20));
        harness.setLibrary(player2, deckOf(20));

        cast();

        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(card -> card.getName().equals("Time Spiral"));
        harness.assertNotInGraveyard(player1, "Time Spiral");
    }

    private void cast() {
        harness.addMana(player1, ManaColor.BLUE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.forceActivePlayer(player1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private List<Card> deckOf(int count) {
        List<Card> deck = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            deck.add(new GrizzlyBears());
        }
        return deck;
    }
}
