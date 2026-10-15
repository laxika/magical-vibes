package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AegisTurtle;
import com.github.laxika.magicalvibes.cards.c.ChandraBoldPyromancer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SnapdaxApexOfTheHunt.class, GrizzlyBears.class, ChandraBoldPyromancer.class, AegisTurtle.class})
class SnapdaxApexOfTheHuntTest extends BaseCardTest {

    @Test
    void mutatingDealsFourDamageToOpponentsCreatureAndGainsFourLife() {
        Permanent snapdax = addCreatureReady(player1, new SnapdaxApexOfTheHunt());
        Permanent bear = addCreatureReady(player2, new GrizzlyBears());
        harness.setLife(player1, 20);

        triggerMutation(snapdax);
        harness.handlePermanentChosen(player1, bear.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        harness.assertLife(player1, 24);
    }

    @Test
    void mutatingDealsFourDamageToOpponentsPlaneswalkerAndGainsFourLife() {
        Permanent snapdax = addCreatureReady(player1, new SnapdaxApexOfTheHunt());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ChandraBoldPyromancer());
        planeswalker.setCounterCount(CounterType.LOYALTY, 10);
        harness.setLife(player1, 20);

        triggerMutation(snapdax);
        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(6);
        harness.assertLife(player1, 24);
    }

    @Test
    void mutatingCannotTargetOwnCreature() {
        Permanent snapdax = addCreatureReady(player1, new SnapdaxApexOfTheHunt());
        Permanent ownBear = addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player2, new GrizzlyBears());

        triggerMutation(snapdax);

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, ownBear.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid permanent");
    }

    @Test
    void noLegalMutationTargetDoesNotGainLife() {
        Permanent snapdax = addCreatureReady(player1, new SnapdaxApexOfTheHunt());
        addCreatureReady(player1, new AegisTurtle());
        harness.setLife(player1, 20);

        triggerMutation(snapdax);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
    }

    @Test
    void mutationTargetLeavingBattlefieldPreventsLifeGain() {
        Permanent snapdax = addCreatureReady(player1, new SnapdaxApexOfTheHunt());
        Permanent turtle = addCreatureReady(player2, new AegisTurtle());
        harness.setLife(player1, 20);

        triggerMutation(snapdax);
        harness.handlePermanentChosen(player1, turtle.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, turtle));
        resolveAllTriggers();

        harness.assertLife(player1, 20);
        harness.assertInHand(player2, "Aegis Turtle");
    }

    @Test
    void mutationTargetBecomingControlledByAbilityControllerPreventsDamageAndLifeGain() {
        Permanent snapdax = addCreatureReady(player1, new SnapdaxApexOfTheHunt());
        Permanent turtle = addCreatureReady(player2, new AegisTurtle());
        harness.setLife(player1, 20);

        triggerMutation(snapdax);
        harness.handlePermanentChosen(player1, turtle.getId());
        gd.playerBattlefields.get(player2.getId()).remove(turtle);
        gd.playerBattlefields.get(player1.getId()).add(turtle);
        resolveAllTriggers();

        assertThat(turtle.getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
    }

    @Test
    void mutationAbilityResolvesAfterSnapdaxLeavesBattlefield() {
        Permanent snapdax = addCreatureReady(player1, new SnapdaxApexOfTheHunt());
        Permanent turtle = addCreatureReady(player2, new AegisTurtle());
        harness.setLife(player1, 20);

        triggerMutation(snapdax);
        harness.handlePermanentChosen(player1, turtle.getId());
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, snapdax));
        resolveAllTriggers();

        assertThat(turtle.getMarkedDamage()).isEqualTo(4);
        harness.assertLife(player1, 24);
    }

    @Test
    void normalCastingDoesNotDealDamageOrGainLife() {
        Permanent turtle = addCreatureReady(player2, new AegisTurtle());
        harness.setHand(player1, List.of(new SnapdaxApexOfTheHunt()));
        harness.setLife(player1, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        harness.assertOnBattlefield(player1, "Snapdax, Apex of the Hunt");
        assertThat(turtle.getMarkedDamage()).isZero();
        harness.assertLife(player1, 20);
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void canCastForMutateCostUsingBlackHybridMana() {
        assertCanCastForMutateCost(ManaColor.BLACK);
    }

    @Test
    void canCastForMutateCostUsingRedHybridMana() {
        assertCanCastForMutateCost(ManaColor.RED);
    }

    @Test
    void unblockedSnapdaxDealsDamageInBothCombatDamageSteps() {
        addCreatureReady(player1, new SnapdaxApexOfTheHunt());
        harness.setLife(player2, 20);

        declareAttackers(List.of(0));
        resolveCombat();

        harness.assertLife(player2, 14);
    }

    private void assertCanCastForMutateCost(ManaColor hybridColor) {
        Permanent turtle = addCreatureReady(player1, new AegisTurtle());
        harness.setHand(player1, List.of(new SnapdaxApexOfTheHunt()));
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.addMana(player1, hybridColor, 1);
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castWithAlternateCost(player1, 0, turtle.getId());

        assertThat(gd.stack).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void triggerMutation(Permanent snapdax) {
        harness.inMutationScope(() -> harness.getTriggerCollectionService().checkMutateTriggers(
                gd, snapdax, List.of(snapdax.getCard()), player1.getId()));
        harness.inMutationScope(() -> harness.getTriggerCollectionService().processNextSelfTriggeredAbilityTarget(gd));
    }
}
