package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TriumphOfFerocity.class, GrizzlyBears.class, HillGiant.class, GiantGrowth.class})
class TriumphOfFerocityTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card when you control the creature with the greatest power")
    void drawsWhenYouHaveGreatestPower() {
        harness.addToBattlefield(player1, new TriumphOfFerocity());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());
        int handSize = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }

    @Test
    @DisplayName("Draws a card on a tie for greatest power")
    void drawsOnTie() {
        harness.addToBattlefield(player1, new TriumphOfFerocity());
        harness.addToBattlefield(player1, new HillGiant());
        harness.addToBattlefield(player2, new HillGiant());
        int handSize = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }

    @Test
    @DisplayName("No draw when an opponent's creature has strictly greater power")
    void noDrawWhenOpponentHasBiggerCreature() {
        harness.addToBattlefield(player1, new TriumphOfFerocity());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        int handSize = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
    }

    @Test
    @DisplayName("No draw when you control no creatures")
    void noDrawWithoutCreatures() {
        harness.addToBattlefield(player1, new TriumphOfFerocity());
        harness.addToBattlefield(player2, new GrizzlyBears());
        int handSize = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new TriumphOfFerocity());
        harness.addToBattlefield(player1, new HillGiant());
        int handSize = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
    }

    @Test
    @DisplayName("Triggers even when neither player controls a creature")
    void triggersWithoutAnyCreatures() {
        harness.addToBattlefield(player1, new TriumphOfFerocity());
        int handSize = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
    }

    @Test
    @DisplayName("Draws when the controller's creature becomes greatest in response")
    void drawsWhenPowerConditionBecomesTrueBeforeResolution() {
        harness.addToBattlefield(player1, new TriumphOfFerocity());
        var bear = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player1, List.of(new GiantGrowth()));

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player1, 0, bear.getId());

        assertThat(gd.stack).hasSize(1);
        int handSize = gd.playerHands.get(player1.getId()).size();
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }

    @Test
    @DisplayName("Does not draw when an opponent's creature becomes greatest in response")
    void noDrawWhenPowerConditionBecomesFalseBeforeResolution() {
        harness.addToBattlefield(player1, new TriumphOfFerocity());
        harness.addToBattlefield(player1, new HillGiant());
        var bear = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player2, List.of(new GiantGrowth()));
        int handSize = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.addMana(player2, ManaColor.GREEN, 1);
        harness.castAndResolveInstant(player2, 0, bear.getId());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
    }

    @Test
    @DisplayName("Draws when only the controller has a creature")
    void drawsWithoutOpposingCreatures() {
        harness.addToBattlefield(player1, new TriumphOfFerocity());
        harness.addToBattlefield(player1, new GrizzlyBears());
        int handSize = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }
}
