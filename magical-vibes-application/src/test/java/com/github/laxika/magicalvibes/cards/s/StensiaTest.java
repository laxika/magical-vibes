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
        Permanent pyromancer = harness.addToBattlefieldAndReturn(player1, new CinderPyromancer());
        pyromancer.setSummoningSick(false);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(pyromancer.getCounters().getOrDefault(
                com.github.laxika.magicalvibes.model.CounterType.PLUS_ONE_PLUS_ONE, 0)).isEqualTo(1);
    }

    @Test
    void putsACounterOnAnOpponentsCreatureAfterItDealsDamageToThePlanarController() {
        Permanent pyromancer = harness.addToBattlefieldAndReturn(player2, new CinderPyromancer());
        pyromancer.setSummoningSick(false);
        harness.forceActivePlayer(player2);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(pyromancer.getCounters().getOrDefault(
                com.github.laxika.magicalvibes.model.CounterType.PLUS_ONE_PLUS_ONE, 0)).isEqualTo(1);
    }

    @Test
    void chaosGrantsTheTapDamageAbilityUntilEndOfTurn() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        creature.setSummoningSick(false);
        harness.setLife(player2, 20);

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(creature.isTapped()).isTrue();
    }
}
