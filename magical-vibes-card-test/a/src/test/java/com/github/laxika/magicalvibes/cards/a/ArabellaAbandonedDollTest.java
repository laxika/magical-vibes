package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.u.Unsummon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

@CardUsed({ArabellaAbandonedDoll.class, GrizzlyBears.class, HillGiant.class, Unsummon.class})
class ArabellaAbandonedDollTest extends BaseCardTest {

    @Test
    void attackingDealsDamageAndGainsLifeForSmallCreaturesYouControl() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);
        addCreatureReady(player1, new ArabellaAbandonedDoll());
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new HillGiant());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 17);
    }

    @Test
    void countsOnlyControlledCreaturesIncludingNonattackingCreatures() {
        harness.setLife(player1, 10);
        addCreatureReady(player1, new ArabellaAbandonedDoll());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        resolveAllTriggers();

        harness.assertLife(player1, 12);
        harness.assertLife(player2, 18);
    }

    @Test
    void countsCurrentPowerWhenTriggerResolves() {
        harness.setLife(player1, 10);
        Permanent arabella = addCreatureReady(player1, new ArabellaAbandonedDoll());
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent giant = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        arabella.setPowerModifier(2);
        bears.setPowerModifier(1);
        giant.setPowerModifier(-4);
        resolveAllTriggers();

        harness.assertLife(player1, 11);
        harness.assertLife(player2, 19);
    }

    @Test
    void resolvesAfterArabellaLeavesAndDoesNotCountItsLastKnownPower() {
        harness.setLife(player1, 10);
        Permanent arabella = addCreatureReady(player1, new ArabellaAbandonedDoll());
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new Unsummon()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        harness.castAndResolveInstant(player1, 0, arabella.getId());
        resolveAllTriggers();

        harness.assertInHand(player1, "Arabella, Abandoned Doll");
        harness.assertLife(player1, 11);
        harness.assertLife(player2, 19);
    }

    @Test
    void dealsNoDamageAndGainsNoLifeWhenNoControlledCreatureQualifies() {
        harness.setLife(player1, 10);
        Permanent arabella = addCreatureReady(player1, new ArabellaAbandonedDoll());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> declareAttackers(List.of(0)));
        arabella.setPowerModifier(2);
        resolveAllTriggers();

        harness.assertLife(player1, 10);
        harness.assertLife(player2, 20);
    }
}
