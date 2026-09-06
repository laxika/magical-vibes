package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Prosperity.class})
class ProsperityTest extends BaseCardTest {

    @Test
    @DisplayName("X=3: each player draws 3 cards")
    void eachPlayerDrawsX() {
        harness.setHand(player1, List.of(new Prosperity()));
        harness.addMana(player1, ManaColor.BLUE, 4); // X=3: {3}{U} = 4
        int p1Before = gd.playerHands.get(player1.getId()).size() - 1; // spell leaves hand
        int p2Before = gd.playerHands.get(player2.getId()).size();

        harness.castAndResolveSorcery(player1, 0, 3);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(p1Before + 3);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(p2Before + 3);
    }

    @Test
    @DisplayName("X=0: no player draws")
    void xZeroDrawsNothing() {
        harness.setHand(player1, List.of(new Prosperity()));
        harness.addMana(player1, ManaColor.BLUE, 1); // X=0: {0}{U} = 1
        int p1Before = gd.playerHands.get(player1.getId()).size() - 1;
        int p2Before = gd.playerHands.get(player2.getId()).size();

        harness.castAndResolveSorcery(player1, 0, 0);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(p1Before);
        assertThat(gd.playerHands.get(player2.getId())).hasSize(p2Before);
    }

    @Test
    @DisplayName("Prosperity goes to graveyard and the stack empties")
    void resolvesToGraveyard() {
        harness.setHand(player1, List.of(new Prosperity()));
        harness.addMana(player1, ManaColor.BLUE, 3); // X=2

        harness.castAndResolveSorcery(player1, 0, 2);

        harness.assertInGraveyard(player1, "Prosperity");
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @CardUsed({PhyrexianWalker.class})
    @DisplayName("X=1: each player draws one card from their own library")
    void eachPlayerDrawsOneCardFromTheirOwnLibrary() {
        Prosperity player1Spell = new Prosperity();
        PhyrexianWalker player1Card = new PhyrexianWalker();
        PhyrexianWalker player2Card = new PhyrexianWalker();
        harness.setHand(player1, List.of(player1Spell));
        harness.setHand(player2, List.of());
        harness.setLibrary(player1, List.of(player1Card));
        harness.setLibrary(player2, List.of(player2Card));
        harness.addMana(player1, ManaColor.BLUE, 2); // X=1: {1}{U} = 2

        harness.castAndResolveSorcery(player1, 0, 1);

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(player1Card);
        assertThat(gd.playerHands.get(player2.getId())).containsExactly(player2Card);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }
}
