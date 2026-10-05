package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.e.ElspethKnightErrant;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.MetallicSliver;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({LavabellySliver.class, GrizzlyBears.class, MetallicSliver.class, ElspethKnightErrant.class})
class LavabellySliverTest extends BaseCardTest {

    @Test
    void sliverEnteringUnderYourControlDealsDamageAndGainsLife() {
        harness.addToBattlefield(player1, new LavabellySliver());
        harness.setLife(player1, 19);
        harness.castFromHand(player1, new MetallicSliver(), "{1}");
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void LavabellySliverTriggersForItself() {
        harness.setLife(player1, 19);
        harness.castFromHand(player1, new LavabellySliver(), "{1}{R}{W}");
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void nonSliversDoNotGainTheAbility() {
        harness.addToBattlefield(player1, new LavabellySliver());
        harness.setLife(player1, 19);
        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    void abilityCanTargetAPlaneswalker() {
        harness.addToBattlefield(player1, new LavabellySliver());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ElspethKnightErrant());
        planeswalker.setCounterCount(CounterType.LOYALTY, 3);
        harness.castFromHand(player1, new MetallicSliver(), "{1}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validIds()).contains(planeswalker.getId());
        harness.handlePermanentChosen(player1, planeswalker.getId());
        harness.passBothPriorities();

        assertThat(planeswalker.getCounterCount(CounterType.LOYALTY)).isEqualTo(2);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
    }

    @Test
    void opposingSliversDoNotGainTheAbility() {
        harness.addToBattlefield(player1, new LavabellySliver());
        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new MetallicSliver(), "{1}");
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void abilityCanTargetItsController() {
        harness.castFromHand(player1, new LavabellySliver(), "{1}{R}{W}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void multipleLavabellySliversGrantSeparateIndependentlyTargetedTriggers() {
        harness.addToBattlefield(player1, new LavabellySliver());
        harness.addToBattlefield(player1, new LavabellySliver());
        harness.castFromHand(player1, new MetallicSliver(), "{1}");
        harness.passBothPriorities();

        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)).isNotNull();
        harness.handlePermanentChosen(player1, player1.getId());
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    void illegalPlaneswalkerTargetPreventsLifeGain() {
        harness.addToBattlefield(player1, new LavabellySliver());
        Permanent planeswalker = harness.addToBattlefieldAndReturn(player2, new ElspethKnightErrant());
        harness.castFromHand(player1, new MetallicSliver(), "{1}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, planeswalker.getId());

        planeswalker.setCounterCount(CounterType.LOYALTY, 0);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Elspeth, Knight-Errant");
        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    void triggerStillResolvesAfterLavabellySliverDies() {
        Permanent lavabelly = harness.addToBattlefieldAndReturn(player1, new LavabellySliver());
        harness.castFromHand(player1, new MetallicSliver(), "{1}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player2.getId());

        lavabelly.setMarkedDamage(2);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Lavabelly Sliver");
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }

    @Test
    void triggerStillResolvesAfterEnteringSliverDies() {
        harness.addToBattlefield(player1, new LavabellySliver());
        Permanent enteringSliver = harness.enterBattlefieldAndReturn(player1, new MetallicSliver());
        harness.handlePermanentChosen(player1, player2.getId());

        enteringSliver.setMarkedDamage(1);
        harness.runStateBasedActions();
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Metallic Sliver");
        harness.assertLife(player1, 21);
        harness.assertLife(player2, 19);
    }
}
