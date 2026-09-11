package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.PendingInteraction;
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

@CardUsed({RavensRun.class, GiantSpider.class})
class RavensRunTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new RavensRun(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void allCreaturesHaveWither() {
        Permanent ownCreature = harness.addToBattlefieldAndReturn(player1, new GiantSpider());
        Permanent opposingCreature = harness.addToBattlefieldAndReturn(player2, new GiantSpider());

        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.WITHER)).isTrue();
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.WITHER)).isTrue();
    }

    @Test
    void witherTurnsCombatDamageIntoMinusCounters() {
        Permanent attacker = addCreatureReady(player1, new GiantSpider());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new GiantSpider());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        assertThat(blocker.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    void chaosPlacesOneTwoAndThreeCountersOnDifferentTargetCreatures() {
        Permanent first = addCreatureReady(player1, new GiantSpider());
        Permanent second = addCreatureReady(player2, new GiantSpider());
        Permanent third = addCreatureReady(player2, new GiantSpider());

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .processNextETBTokenMultiTargetTrigger(gd));

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(first.getId(), second.getId(), third.getId());

        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        harness.handlePermanentChosen(player1, third.getId());
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        assertThat(second.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(2);
        assertThat(third.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(3);
    }

    @Test
    void chaosCannotChooseTheSameCreatureMoreThanOnce() {
        Permanent first = addCreatureReady(player1, new GiantSpider());
        addCreatureReady(player2, new GiantSpider());
        addCreatureReady(player2, new GiantSpider());

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> harness.getTriggerCollectionService()
                .processNextETBTokenMultiTargetTrigger(gd));
        harness.handlePermanentChosen(player1, first.getId());

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, first.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
