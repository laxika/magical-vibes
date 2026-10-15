package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CinderPyromancer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Stensia.class, CinderPyromancer.class, GrizzlyBears.class})
class StensiaTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new Stensia(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void putsACounterOnAControlledCreatureAfterItDealsDamageToAnOpponent() {
        Permanent pyromancer = addCreatureReady(player1, new CinderPyromancer());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(pyromancer.getCounters().getOrDefault(
                com.github.laxika.magicalvibes.model.CounterType.PLUS_ONE_PLUS_ONE, 0)).isEqualTo(1);
    }

    @Test
    void putsACounterOnAnOpponentsCreatureAfterItDealsDamageToThePlanarController() {
        Permanent pyromancer = addCreatureReady(player2, new CinderPyromancer());
        harness.forceActivePlayer(player2);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(pyromancer.getCounters().getOrDefault(
                com.github.laxika.magicalvibes.model.CounterType.PLUS_ONE_PLUS_ONE, 0)).isEqualTo(1);
    }

    @Test
    void chaosGrantsTheTapDamageAbilityUntilEndOfTurn() {
        Permanent creature = addCreatureReady(player1, new GrizzlyBears());
        harness.setLife(player2, 20);

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(creature.isTapped()).isTrue();
    }

    @Test
    void putsACounterOnAnOpponentsCreatureThatDamagesItsOwnController() {
        Permanent pyromancer = addCreatureReady(player2, new CinderPyromancer());

        harness.activateAbility(player2, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        assertThat(pyromancer.getCounters().getOrDefault(
                com.github.laxika.magicalvibes.model.CounterType.PLUS_ONE_PLUS_ONE, 0)).isEqualTo(1);
    }

    @Test
    void onlyTheFirstDamageToAnyPlayerInATurnGetsACounter() {
        Permanent pyromancer = addCreatureReady(player1, new CinderPyromancer());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        pyromancer.untap();
        harness.activateAbility(player1, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
        assertThat(pyromancer.getCounters().getOrDefault(
                com.github.laxika.magicalvibes.model.CounterType.PLUS_ONE_PLUS_ONE, 0)).isEqualTo(1);
    }

    @Test
    void damageBeforeStensiaBecomesFaceUpStillCountsAsTheFirstDamageThisTurn() {
        gd.planechase.faceUp.clear();
        Permanent pyromancer = addCreatureReady(player1, new CinderPyromancer());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        gd.planechase.faceUp.add(new PlanarObject(new Stensia(), gd.nextTimestamp()));
        pyromancer.untap();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 18);
        assertThat(pyromancer.getCounters().getOrDefault(
                com.github.laxika.magicalvibes.model.CounterType.PLUS_ONE_PLUS_ONE, 0)).isZero();
    }

    @Test
    void eachCreatureCanGetItsOwnCounterInTheSameTurn() {
        Permanent first = addCreatureReady(player1, new CinderPyromancer());
        Permanent second = addCreatureReady(player1, new CinderPyromancer());

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, null, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getCounters().getOrDefault(
                com.github.laxika.magicalvibes.model.CounterType.PLUS_ONE_PLUS_ONE, 0)).isEqualTo(1);
        assertThat(second.getCounters().getOrDefault(
                com.github.laxika.magicalvibes.model.CounterType.PLUS_ONE_PLUS_ONE, 0)).isEqualTo(1);
    }

    @Test
    void chaosDoesNotGrantTheAbilityToOpponentsOrCreaturesEnteringAfterResolution() {
        addCreatureReady(player2, new GrizzlyBears());
        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();
        addCreatureReady(player1, new GrizzlyBears());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void chaosAbilityCannotTargetACreature() {
        addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());
        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponent.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void chaosAbilityExpiresAtEndOfTurn() {
        addCreatureReady(player1, new GrizzlyBears());
        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Permanent has no activated ability");
    }

    @Test
    void theSameCreatureCanGetAnotherCounterOnTheNextTurn() {
        Permanent pyromancer = addCreatureReady(player1, new CinderPyromancer());
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passUntil(player2, TurnStep.UPKEEP);
        pyromancer.untap();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(pyromancer.getCounters().getOrDefault(
                com.github.laxika.magicalvibes.model.CounterType.PLUS_ONE_PLUS_ONE, 0)).isEqualTo(2);
    }
}
