package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.b.BoggartRamGang;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.h.HornedTurtle;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.service.turn.StepTriggerService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheGreatAerie.class, GrizzlyBears.class, HillGiant.class, HornedTurtle.class, BoggartRamGang.class})
class TheGreatAerieTest extends BaseCardTest {

    private PlanechaseService planar;
    private PlanarObject source;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        source = new PlanarObject(new TheGreatAerie(), gd.nextTimestamp());
        gd.planechase.faceUp.add(source);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void planeswalkingToTheGreatAerieBolstersTheLeastToughCreature() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent giant = addCreatureReady(player1, new HillGiant());

        harness.inMutationScope(() -> planar.trigger(
                gd, source, EffectSlot.PLANESWALK_TO_TRIGGERED, player1.getId()));
        harness.passBothPriorities();

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(giant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void bolstersTheLeastToughCreatureAtThePlanarControllersUpkeep() {
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        Permanent giant = addCreatureReady(player1, new HillGiant());

        harness.forceStep(TurnStep.UPKEEP);
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(StepTriggerService.class)
                .handleUpkeepTriggers(gd));
        harness.passBothPriorities();

        assertThat(bear.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(giant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void chaosMakesTheChosenCreaturesDealDamageEqualToTheirToughness() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new HornedTurtle());

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, ownCreature.getId());
        harness.handlePermanentChosen(player1, opposingCreature.getId());
        harness.passBothPriorities();

        assertThat(ownCreature.getMarkedDamage()).isEqualTo(4);
        assertThat(opposingCreature.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    void bolsterLetsTheControllerChooseAmongTiedCreatures() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());

        harness.inMutationScope(() -> planar.trigger(
                gd, source, EffectSlot.PLANESWALK_TO_TRIGGERED, player1.getId()));
        harness.passBothPriorities();
        harness.handleMultiplePermanentsChosen(player1, java.util.List.of(second.getId()));

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void bolsterDoesNothingWithoutCreaturesYouControl() {
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());

        harness.inMutationScope(() -> planar.trigger(
                gd, source, EffectSlot.PLANESWALK_TO_TRIGGERED, player1.getId()));
        harness.passBothPriorities();

        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    void upkeepBolstersTheNewActivePlayersCreatures() {
        Permanent own = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UPKEEP);

        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(StepTriggerService.class)
                .handleUpkeepTriggers(gd));
        harness.passBothPriorities();

        assertThat(own.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void chaosDoesNoDamageWhenOnlyOneCreatureIsChosen() {
        Permanent own = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new HornedTurtle());

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.handlePermanentChosen(player1, opponent.getId());
        harness.passBothPriorities();

        assertThat(own.getMarkedDamage()).isZero();
        assertThat(opponent.getMarkedDamage()).isZero();
    }

    @Test
    void chaosDoesNoDamageWhenOneTargetChangesController() {
        Permanent own = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponent = addCreatureReady(player2, new HornedTurtle());

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, own.getId());
        harness.handlePermanentChosen(player1, opponent.getId());
        gd.playerBattlefields.get(player2.getId()).remove(opponent);
        gd.playerBattlefields.get(player1.getId()).add(opponent);
        harness.passBothPriorities();

        assertThat(own.getMarkedDamage()).isZero();
        assertThat(opponent.getMarkedDamage()).isZero();
    }
    @Test
    void chaosUsesBothToughnessesBeforeWitherDamageIsDealt() {
        Permanent own = addCreatureReady(player1, new BoggartRamGang());
        Permanent opponent = addCreatureReady(player2, new HornedTurtle());

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, own.getId());
        harness.handlePermanentChosen(player1, opponent.getId());
        harness.passBothPriorities();

        assertThat(opponent.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
        harness.assertNotOnBattlefield(player1, "Boggart Ram-Gang");
        harness.assertInGraveyard(player1, "Boggart Ram-Gang");
        harness.assertOnBattlefield(player2, "Horned Turtle");
    }
}