package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.f.FanaticalFirebrand;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CacklingProwler.class, Shock.class, FanaticalFirebrand.class})
class CacklingProwlerTest extends BaseCardTest {

    @Test
    @DisplayName("Morbid puts a +1/+1 counter on Cackling Prowler at your end step")
    void morbidPutsCounterAtEndStep() {
        Permanent prowler = harness.addToBattlefieldAndReturn(player1, new CacklingProwler());
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(prowler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Cackling Prowler does not get a counter when no creature died this turn")
    void morbidDoesNothingWithoutCreatureDeath() {
        Permanent prowler = harness.addToBattlefieldAndReturn(player1, new CacklingProwler());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);

        assertThat(prowler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Ward {2} counters an opponent's spell when they cannot pay")
    void wardCountersUnpaidSpell() {
        Permanent prowler = harness.addToBattlefieldAndReturn(player1, new CacklingProwler());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, prowler.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Shock");
        harness.assertOnBattlefield(player1, "Cackling Prowler");
    }

    @Test
    void payingWardAllowsOpponentsSpellToResolve() {
        Permanent prowler = harness.addToBattlefieldAndReturn(player1, new CacklingProwler());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castInstant(player2, 0, prowler.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, true);
        resolveAllTriggers();

        assertThat(prowler.getMarkedDamage()).isEqualTo(2);
        assertThat(gd.playerManaPools.get(player2.getId()).getTotal()).isZero();
        harness.assertOnBattlefield(player1, "Cackling Prowler");
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void decliningAffordableWardCountersSpell() {
        Permanent prowler = harness.addToBattlefieldAndReturn(player1, new CacklingProwler());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.castInstant(player2, 0, prowler.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player2, false);
        resolveAllTriggers();

        assertThat(prowler.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Shock");
    }

    @Test
    void wardDoesNotTriggerForControllersSpell() {
        Permanent prowler = harness.addToBattlefieldAndReturn(player1, new CacklingProwler());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, prowler.getId());
        resolveAllTriggers();

        assertThat(prowler.getMarkedDamage()).isEqualTo(2);
        harness.assertInGraveyard(player1, "Shock");
    }

    @Test
    void wardCountersOpponentsActivatedAbility() {
        Permanent prowler = harness.addToBattlefieldAndReturn(player1, new CacklingProwler());
        addCreatureReady(player2, new FanaticalFirebrand());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, null, prowler.getId());
        resolveAllTriggers();

        assertThat(prowler.getMarkedDamage()).isZero();
        harness.assertInGraveyard(player2, "Fanatical Firebrand");
        harness.assertOnBattlefield(player1, "Cackling Prowler");
    }

    @Test
    void multipleActualCreatureDeathsGiveOnlyOneCounter() {
        Permanent prowler = harness.addToBattlefieldAndReturn(player1, new CacklingProwler());
        addCreatureReady(player1, new FanaticalFirebrand());
        addCreatureReady(player1, new FanaticalFirebrand());

        harness.activateAbility(player1, 1, null, player2.getId());
        resolveAllTriggers();
        harness.activateAbility(player1, 1, null, player2.getId());
        resolveAllTriggers();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(prowler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void morbidDoesNotTriggerAtOpponentsEndStep() {
        Permanent prowler = harness.addToBattlefieldAndReturn(player1, new CacklingProwler());
        gd.creatureDeathCountThisTurn.merge(player2.getId(), 1, Integer::sum);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(prowler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void creatureDyingAfterEndStepBeginsDoesNotTriggerMorbid() {
        Permanent prowler = harness.addToBattlefieldAndReturn(player1, new CacklingProwler());
        addCreatureReady(player1, new FanaticalFirebrand());
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(player1, TurnStep.END_STEP);

        harness.activateAbility(player1, 1, null, player2.getId());
        resolveAllTriggers();

        harness.assertInGraveyard(player1, "Fanatical Firebrand");
        assertThat(prowler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
