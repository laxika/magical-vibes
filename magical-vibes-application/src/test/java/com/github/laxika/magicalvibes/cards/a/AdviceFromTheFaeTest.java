package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AdviceFromTheFaeTest extends BaseCardTest {

    private List<Card> setupTopFive() {
        List<Card> top = List.of(
                new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears(),
                new GrizzlyBears(), new GrizzlyBears());
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(top);
        harness.setHand(player1, List.of(new AdviceFromTheFae()));
        harness.addMana(player1, ManaColor.BLUE, 6);
        return top;
    }

    @Test
    @DisplayName("Controlling more creatures than each other player keeps two cards")
    void keepsTwoWhenControllingMoreCreatures() {
        addCreatureReady(player1, new GrizzlyBears()); // player1: 1 creature, player2: 0
        List<Card> top = setupTopFive();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(top.get(0).getId(), top.get(1).getId()));
        // The remaining three are ordered onto the bottom of the library.
        gs.handleLibraryCardsReordered(gd, player1, List.of(0, 1, 2));

        assertThat(gd.playerHands.get(player1.getId())).contains(top.get(0), top.get(1));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3)
                .containsExactlyInAnyOrder(top.get(2), top.get(3), top.get(4));
    }

    @Test
    @DisplayName("Not controlling more creatures keeps only one card")
    void keepsOneWhenNotControllingMore() {
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears()); // equal counts -> not "more"
        List<Card> top = setupTopFive();

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
        harness.handleMultipleCardsChosen(player1, List.of(top.get(0).getId()));
        // The remaining four are ordered onto the bottom of the library.
        gs.handleLibraryCardsReordered(gd, player1, List.of(0, 1, 2, 3));

        assertThat(gd.playerHands.get(player1.getId())).contains(top.get(0));
        assertThat(gd.playerHands.get(player1.getId()))
                .doesNotContain(top.get(1), top.get(2), top.get(3), top.get(4));
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4);
    }
}
