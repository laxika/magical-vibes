package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.c.CrawWurm;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({WolverineFierceFighter.class, GrizzlyBears.class, Shock.class, CrawWurm.class})
class WolverineFierceFighterTest extends BaseCardTest {

    @Test
    void entersAndFightsUpToOneOtherCreature() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.castFromHand(player1, new WolverineFierceFighter(), "{2}{R}{G}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        Permanent wolverine = findPermanent(player1, "Wolverine, Fierce Fighter");
        assertThat(wolverine.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void entersWithoutAValidFightTarget() {
        harness.castFromHand(player1, new WolverineFierceFighter(), "{2}{R}{G}");
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }

        harness.assertOnBattlefield(player1, "Wolverine, Fierce Fighter");
    }

    @Test
    void healsPreviouslyMarkedDamageBeforeRecordingNoncombatDamage() {
        Permanent wolverine = harness.addToBattlefieldAndReturn(player2, new WolverineFierceFighter());
        wolverine.setMarkedDamage(3);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, wolverine.getId());

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(wolverine);
        assertThat(wolverine.getMarkedDamage()).isEqualTo(2);
        assertThat(wolverine.isDamagedByDeathtouch()).isFalse();
    }

    @Test
    void healsPreviouslyMarkedDamageBeforeRecordingCombatDamage() {
        Permanent wolverine = addCreatureReady(player1, new WolverineFierceFighter());
        wolverine.setMarkedDamage(3);
        Permanent bears = addCreatureReady(player2, new GrizzlyBears());
        wolverine.setAttacking(true);
        bears.setBlocking(true);
        bears.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(bears);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(wolverine);
        assertThat(wolverine.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void separateDamageEventsHealEarlierDamage() {
        Permanent wolverine = harness.addToBattlefieldAndReturn(player2, new WolverineFierceFighter());
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 3);

        for (int i = 0; i < 3; i++) {
            harness.castAndResolveInstant(player1, 0, wolverine.getId());
            assertThat(wolverine.getMarkedDamage()).isEqualTo(2);
            harness.assertOnBattlefield(player2, "Wolverine, Fierce Fighter");
        }
    }

    @Test
    void canFightAnotherCreatureControlledByItsController() {
        Permanent bears = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.castFromHand(player1, new WolverineFierceFighter(), "{2}{R}{G}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Grizzly Bears");
        assertThat(findPermanent(player1, "Wolverine, Fierce Fighter").getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void healingDoesNotPreventLethalFightDamage() {
        Permanent wurm = harness.addToBattlefieldAndReturn(player2, new CrawWurm());
        harness.castFromHand(player1, new WolverineFierceFighter(), "{2}{R}{G}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, wurm.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player1, "Wolverine, Fierce Fighter");
        assertThat(wurm.getMarkedDamage()).isEqualTo(3);
        harness.assertOnBattlefield(player2, "Craw Wurm");
    }

    @Test
    void simultaneousBlockerDamageIsNotHealedBetweenSources() {
        Permanent wolverine = addCreatureReady(player1, new WolverineFierceFighter());
        wolverine.setMarkedDamage(3);
        Permanent first = addCreatureReady(player2, new GrizzlyBears());
        Permanent second = addCreatureReady(player2, new GrizzlyBears());
        wolverine.setAttacking(true);
        first.setBlocking(true);
        first.addBlockingTarget(0);
        second.setBlocking(true);
        second.addBlockingTarget(0);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(first.getId(), 2, second.getId(), 1));

        harness.assertOnBattlefield(player1, "Wolverine, Fierce Fighter");
        assertThat(wolverine.getMarkedDamage()).isEqualTo(4);
    }

    @Test
    void canChooseNoFightTargetEvenWhenAnotherCreatureExists() {
        Permanent bears = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.castFromHand(player1, new WolverineFierceFighter(), "{2}{R}{G}");
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(bears.getMarkedDamage()).isZero();
        assertThat(findPermanent(player1, "Wolverine, Fierce Fighter").getMarkedDamage()).isZero();
    }
}
