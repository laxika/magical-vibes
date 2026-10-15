package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.p.Panopticon;
import com.github.laxika.magicalvibes.cards.s.StrionicResonator;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
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
        advanceToUpkeep(player1);

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
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(source.getCounters()).containsEntry(CounterType.SCROLL, 10);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(beforeHand + 10);
        assertThat(gd.planechase.faceUp).extracting(PlanarObject::getId).containsExactly(source.getId());
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(gd.planechase.faceUp).extracting(object -> object.getCard().getName())
                .containsExactly("Panopticon");
    }

    @Test
    void upkeepOnTheOtherPlayersTurnBenefitsThatPlayer() {
        source.getCounters().put(CounterType.SCROLL, 2);

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(source.getCounters()).containsEntry(CounterType.SCROLL, 3);
        assertThat(gd.getLife(player2.getId())).isEqualTo(23);
        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
    }

    @Test
    void pendingChaosUsesTheCounterCountBeforePlaneswalkingAway() {
        source.getCounters().put(CounterType.SCROLL, 2);
        int beforeHand = gd.playerHands.get(player1.getId()).size();
        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> planar.planeswalk(gd));

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        assertThat(gd.playerHands.get(player1.getId())).hasSize(beforeHand + 1);

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(beforeHand + 3);
    }

    @Test
    @CardUsed(StrionicResonator.class)
    void copiedThresholdTriggerStillPlaneswalksAfterCounterCountDrops() {
        harness.addToBattlefield(player1, new StrionicResonator());
        source.getCounters().put(CounterType.SCROLL, 9);
        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        assertThat(gd.stack).hasSize(1);
        var originalTriggerId = gd.stack.getLast().getTargetableId();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, originalTriggerId);
        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));
        assertThat(gd.stack).hasSize(2);
        source.getCounters().put(CounterType.SCROLL, 9);

        harness.inMutationScope(() -> harness.getStackResolutionService().resolveTopOfStack(gd));

        assertThat(gd.planechase.faceUp).extracting(object -> object.getCard().getName())
                .containsExactly("Panopticon");
        assertThat(gd.stack).anyMatch(entry -> entry.getTargetableId().equals(originalTriggerId));
    }
}
