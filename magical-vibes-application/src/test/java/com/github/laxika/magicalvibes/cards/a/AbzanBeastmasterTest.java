package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.HornedTurtle;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AbzanBeastmasterTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when you control the creature with the greatest toughness")
    void drawsWhenYouHaveGreatestToughness() {
        harness.addToBattlefield(player1, new AbzanBeastmaster());
        harness.addToBattlefield(player1, new HornedTurtle());
        harness.addToBattlefield(player2, new HillGiant());
        int handSize = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }

    @Test
    @DisplayName("Draws a card on a tie for greatest toughness")
    void drawsOnTie() {
        harness.addToBattlefield(player1, new AbzanBeastmaster());
        harness.addToBattlefield(player1, new HornedTurtle());
        harness.addToBattlefield(player2, new HornedTurtle());
        int handSize = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }

    @Test
    @DisplayName("Does not draw when an opponent's creature has strictly greater toughness")
    void noDrawWhenOpponentHasGreaterToughness() {
        harness.addToBattlefield(player1, new AbzanBeastmaster());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HornedTurtle());
        int handSize = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
    }

    @Test
    @DisplayName("Draws a card when it is the only creature")
    void drawsWhenItIsTheOnlyCreature() {
        harness.addToBattlefield(player1, new AbzanBeastmaster());
        int handSize = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new AbzanBeastmaster());
        harness.addToBattlefield(player1, new HornedTurtle());
        int handSize = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
    }
}
