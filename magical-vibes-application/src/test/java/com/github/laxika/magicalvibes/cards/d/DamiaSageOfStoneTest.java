package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DamiaSageOfStone.class, GrizzlyBears.class})
class DamiaSageOfStoneTest extends BaseCardTest {

    private List<Card> bears(int count) {
        return Stream.generate(GrizzlyBears::new).limit(count).map(Card.class::cast).toList();
    }

    @Test
    @DisplayName("Draws enough cards during upkeep to reach seven cards in hand")
    void drawsUpToSevenCards() {
        harness.addToBattlefield(player1, new DamiaSageOfStone());
        harness.setHand(player1, bears(4));
        harness.setLibrary(player1, bears(5));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
    }

    @Test
    @DisplayName("Does not trigger with seven cards in hand")
    void doesNotTriggerAtSevenCards() {
        harness.addToBattlefield(player1, new DamiaSageOfStone());
        harness.setHand(player1, bears(7));
        harness.setLibrary(player1, bears(2));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
    }

    @Test
    @DisplayName("Rechecks the hand-size condition and amount at resolution")
    void rechecksHandSizeAtResolution() {
        harness.addToBattlefield(player1, new DamiaSageOfStone());
        harness.setHand(player1, bears(4));
        harness.setLibrary(player1, bears(5));

        advanceToUpkeep(player1);
        harness.setHand(player1, bears(6));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
    }

    @Test
    @DisplayName("Does not draw if the hand reaches seven cards before resolution")
    void doesNotDrawWhenHandReachesSevenBeforeResolution() {
        harness.addToBattlefield(player1, new DamiaSageOfStone());
        harness.setHand(player1, bears(6));
        harness.setLibrary(player1, bears(2));

        advanceToUpkeep(player1);
        harness.setHand(player1, bears(7));
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
    }

    @Test
    @DisplayName("Skips the controller's draw step")
    void skipsControllerDrawStep() {
        harness.setLibrary(player1, bears(1));
        harness.addToBattlefield(player1, new DamiaSageOfStone());

        harness.forceActivePlayer(player1);
        gd.turnNumber = 2;
        harness.forceStep(com.github.laxika.magicalvibes.model.TurnStep.UPKEEP);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
