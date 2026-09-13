package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.p.Panopticon;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
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

@CardUsed({Aretopolis.class, Panopticon.class})
class AretopolisTest extends BaseCardTest {

    private PlanechaseService planar;
    private PlanarObject source;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.deck.add(new Panopticon());
        source = new PlanarObject(new Aretopolis(), gd.nextTimestamp());
        gd.planechase.faceUp.add(source);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void planeswalkingToAretopolisAddsScrollCounterAndGainsThatMuchLife() {
        int beforeLife = gd.getLife(player1.getId());
        harness.inMutationScope(() -> planar.trigger(
                gd, source, EffectSlot.PLANESWALK_TO_TRIGGERED, player1.getId()));
        harness.passBothPriorities();

        assertThat(source.getCounters()).containsEntry(CounterType.SCROLL, 1);
        assertThat(gd.getLife(player1.getId())).isEqualTo(beforeLife + 1);
    }

    @Test
    void upkeepAddsScrollCounterThenGainsLifeEqualToTheCount() {
        source.getCounters().put(CounterType.SCROLL, 2);
        harness.forceStep(TurnStep.UPKEEP);

        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(StepTriggerService.class)
                .handleUpkeepTriggers(gd));
        harness.passBothPriorities();

        assertThat(source.getCounters()).containsEntry(CounterType.SCROLL, 3);
        assertThat(gd.getLife(player1.getId())).isEqualTo(23);
    }

    @Test
    void chaosAddsScrollCountersThenDrawsThatManyCards() {
        source.getCounters().put(CounterType.SCROLL, 2);
        int beforeHand = gd.playerHands.get(player1.getId()).size();

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();

        assertThat(source.getCounters()).containsEntry(CounterType.SCROLL, 3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(beforeHand + 3);
    }

    @Test
    void planeswalksWhenTheScrollCounterThresholdIsReached() {
        source.getCounters().put(CounterType.SCROLL, 9);
        int beforeHand = gd.playerHands.get(player1.getId()).size();

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();

        assertThat(source.getCounters()).containsEntry(CounterType.SCROLL, 10);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(beforeHand + 10);
        assertThat(gd.planechase.faceUp).extracting(object -> object.getCard().getName())
                .containsExactly("Panopticon");
    }
}
