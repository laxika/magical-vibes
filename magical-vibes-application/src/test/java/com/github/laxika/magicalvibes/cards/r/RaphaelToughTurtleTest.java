package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RaphaelToughTurtle.class, GrizzlyBears.class, Forest.class})
class RaphaelToughTurtleTest extends BaseCardTest {

    @Test
    @DisplayName("Another creature entering deals 1 damage to a target opponent")
    void anotherCreatureEnteringDealsDamageToOpponent() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new RaphaelToughTurtle());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("The triggered ability can target only an opponent")
    void triggeredAbilityTargetsOnlyOpponent() {
        harness.addToBattlefield(player1, new RaphaelToughTurtle());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice.validIds()).contains(player2.getId()).doesNotContain(player1.getId());
    }

    @Test
    @DisplayName("Raphael does not trigger for its own entry")
    void doesNotTriggerForItsOwnEntry() {
        harness.castFromHand(player1, new RaphaelToughTurtle(), "{1}{R}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("An opponent's creature entering does not trigger Raphael")
    void opponentCreatureEnteringDoesNotTrigger() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new RaphaelToughTurtle());

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Each subsequent creature entry triggers Raphael again")
    void triggersForEachCreatureEntry() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new RaphaelToughTurtle());

        for (int i = 0; i < 2; i++) {
            harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
            harness.passBothPriorities();
            harness.handlePermanentChosen(player1, player2.getId());
            harness.passBothPriorities();
            harness.assertLife(player2, 19 - i);
        }
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Raphael's trigger resolves after Raphael dies")
    void triggerResolvesAfterSourceLeavesBattlefield() {
        harness.setLife(player2, 20);
        var raphael = harness.addToBattlefieldAndReturn(player1, new RaphaelToughTurtle());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());

        raphael.setMarkedDamage(3);
        harness.runStateBasedActions();
        harness.assertInGraveyard(player1, "Raphael, Tough Turtle");
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("A land entering does not trigger Raphael")
    void noncreatureEnteringDoesNotTrigger() {
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new RaphaelToughTurtle());
        harness.setHand(player1, java.util.List.of(new Forest()));

        harness.playLand(player1, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        harness.assertLife(player2, 20);
    }
}
