package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.t.TurnToFrog;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.stream.Stream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DamiaSageOfStone.class, GrizzlyBears.class, TurnToFrog.class})
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
        harness.forceStep(TurnStep.UPKEEP);
        int handBefore = gd.playerHands.get(player1.getId()).size();

        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void drawsSevenFromEmptyHand() {
        harness.addToBattlefield(player1, new DamiaSageOfStone());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, bears(8));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void doesNotTriggerAboveSevenCards() {
        harness.addToBattlefield(player1, new DamiaSageOfStone());
        harness.setHand(player1, bears(8));

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotTriggerDuringOpponentsUpkeepOrSkipTheirDraw() {
        harness.addToBattlefield(player1, new DamiaSageOfStone());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, bears(2));
        gd.turnNumber = 2;

        advanceToUpkeep(player2);
        assertThat(gd.stack).isEmpty();
        harness.passUntilWithNoAttackers(player2, TurnStep.DRAW);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
    }

    @Test
    void skipsEntireDrawStepIncludingPriority() {
        harness.addToBattlefield(player1, new DamiaSageOfStone());
        harness.setHand(player1, bears(7));
        harness.setLibrary(player1, bears(2));
        gd.turnNumber = 2;
        advanceToUpkeep(player1);

        harness.withAutoStop(TurnStep.DRAW, () ->
                harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, harness::passBothPriorities));

        assertThat(gd.currentStep).isEqualTo(TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(7);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @CardUsed({TurnToFrog.class})
    void drawsNormallyAfterLosingAllAbilitiesDuringUpkeep() {
        var damia = harness.addToBattlefieldAndReturn(player1, new DamiaSageOfStone());
        harness.setHand(player1, bears(7));
        harness.setLibrary(player1, bears(2));
        harness.setHand(player2, List.of(new TurnToFrog()));
        gd.turnNumber = 2;
        advanceToUpkeep(player1);
        harness.addMana(player2, ManaColor.BLUE, 2);

        harness.castInstant(player2, 0, damia.getId());
        harness.withAutoStop(TurnStep.UPKEEP, harness::passBothPriorities);
        harness.passUntilWithNoAttackers(player1, TurnStep.DRAW);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(8);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
