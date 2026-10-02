package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.HornedTurtle;
import com.github.laxika.magicalvibes.cards.r.ReachOfShadows;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AbzanBeastmaster.class, GrizzlyBears.class, HillGiant.class,
        HornedTurtle.class, ReachOfShadows.class})
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

    @Test
    @DisplayName("Triggers even when the toughness condition is initially false")
    void triggersWithoutGreatestToughness() {
        harness.addToBattlefield(player1, new AbzanBeastmaster());
        harness.addToBattlefield(player2, new HornedTurtle());

        advanceToUpkeep(player1);

        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Draws when removing the opponent's toughest creature makes the condition true")
    void drawsWhenConditionBecomesTrueBeforeResolution() {
        harness.addToBattlefield(player1, new AbzanBeastmaster());
        harness.addToBattlefield(player2, new HornedTurtle());
        harness.setHand(player1, List.of(new ReachOfShadows()));
        harness.addMana(player1, ManaColor.BLACK, 5);

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.castAndResolveInstant(player1, 0, findPermanent(player2, "Horned Turtle").getId());
        harness.assertInGraveyard(player2, "Horned Turtle");
        int handSize = gd.playerHands.get(player1.getId()).size();
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }

    @Test
    @DisplayName("Does not draw if the controller loses their toughest creature before resolution")
    void noDrawWhenConditionBecomesFalseBeforeResolution() {
        harness.addToBattlefield(player1, new AbzanBeastmaster());
        harness.addToBattlefield(player1, new HornedTurtle());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player2, List.of(new ReachOfShadows()));
        harness.addMana(player2, ManaColor.BLACK, 5);
        int handSize = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.castAndResolveInstant(player2, 0, findPermanent(player1, "Horned Turtle").getId());
        harness.assertInGraveyard(player1, "Horned Turtle");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
    }

    @Test
    @DisplayName("The trigger still draws after Beastmaster leaves if another creature qualifies")
    void drawsAfterSourceLeavesBattlefield() {
        harness.addToBattlefield(player1, new AbzanBeastmaster());
        harness.addToBattlefield(player1, new HornedTurtle());
        harness.addToBattlefield(player2, new HillGiant());
        harness.setHand(player2, List.of(new ReachOfShadows()));
        harness.addMana(player2, ManaColor.BLACK, 5);
        int handSize = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.castAndResolveInstant(player2, 0, findPermanent(player1, "Abzan Beastmaster").getId());
        harness.assertInGraveyard(player1, "Abzan Beastmaster");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }

    @Test
    @DisplayName("Multiple creatures tied for greatest toughness still draw only one card")
    void drawsOnlyOneCardForMultipleQualifyingCreatures() {
        harness.addToBattlefield(player1, new AbzanBeastmaster());
        harness.addToBattlefield(player1, new HornedTurtle());
        harness.addToBattlefield(player1, new HornedTurtle());
        harness.addToBattlefield(player2, new HornedTurtle());
        int handSize = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }
}
