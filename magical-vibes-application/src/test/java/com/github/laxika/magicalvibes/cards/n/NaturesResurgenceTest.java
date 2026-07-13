package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NaturesResurgenceTest extends BaseCardTest {

    @Test
    @DisplayName("Each player draws a card for each creature card in their own graveyard")
    void eachPlayerDrawsForOwnGraveyard() {
        List<Card> p1Graveyard = new ArrayList<>();
        p1Graveyard.add(new GrizzlyBears());
        p1Graveyard.add(new GrizzlyBears());
        harness.setGraveyard(player1, p1Graveyard);

        List<Card> p2Graveyard = new ArrayList<>();
        p2Graveyard.add(new GrizzlyBears());
        p2Graveyard.add(new GrizzlyBears());
        p2Graveyard.add(new GrizzlyBears());
        harness.setGraveyard(player2, p2Graveyard);

        int p1DeckBefore = gd.playerDecks.get(player1.getId()).size();
        int p2DeckBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new NaturesResurgence()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(p1DeckBefore - 2);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(p2DeckBefore - 3);
    }

    @Test
    @DisplayName("Only creature cards count; non-creature cards are ignored")
    void onlyCreatureCardsCount() {
        List<Card> p1Graveyard = new ArrayList<>();
        p1Graveyard.add(new GrizzlyBears());
        p1Graveyard.add(new GiantGrowth());
        p1Graveyard.add(new GiantGrowth());
        harness.setGraveyard(player1, p1Graveyard);

        int p1DeckBefore = gd.playerDecks.get(player1.getId()).size();

        harness.setHand(player1, List.of(new NaturesResurgence()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(p1DeckBefore - 1);
    }

    @Test
    @DisplayName("A player with no creature cards in their graveyard draws nothing")
    void playerWithoutCreatureCardsDrawsNothing() {
        harness.setGraveyard(player1, List.of(new GrizzlyBears()));
        harness.setGraveyard(player2, List.of(new GiantGrowth()));

        int p2DeckBefore = gd.playerDecks.get(player2.getId()).size();

        harness.setHand(player1, List.of(new NaturesResurgence()));
        harness.setHand(player2, List.of());
        harness.addMana(player1, ManaColor.GREEN, 4);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(p2DeckBefore);
    }
}
