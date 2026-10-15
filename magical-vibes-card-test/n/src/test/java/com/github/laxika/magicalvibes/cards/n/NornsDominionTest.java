package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.planar.PlanarObject;
import com.github.laxika.magicalvibes.model.planar.PlanechaseState;
import com.github.laxika.magicalvibes.service.planar.PlanechaseService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({NornsDominion.class, GrizzlyBears.class, Forest.class})
class NornsDominionTest extends BaseCardTest {

    private PlanechaseService planar;
    private TriggerCollectionService triggers;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        triggers = GameTestEngineContext.get().getBean(TriggerCollectionService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new NornsDominion(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void planeswalkingAwayDestroysUnmarkedNonlandsThenRemovesAllFateCounters() {
        Permanent markedCreature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        markedCreature.setCounterCount(CounterType.FATE, 1);
        Permanent unmarkedCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        Permanent markedLand = harness.addToBattlefieldAndReturn(player2, new Forest());
        markedLand.setCounterCount(CounterType.FATE, 1);

        harness.inMutationScope(() -> planar.planeswalk(gd));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(markedCreature);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(markedLand)
                .doesNotContain(unmarkedCreature);
        assertThat(markedCreature.getCounterCount(CounterType.FATE)).isZero();
        assertThat(markedLand.getCounterCount(CounterType.FATE)).isZero();
    }

    @Test
    void chaosMayPutAFateCounterOnTargetPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> triggers.processNextSpellTargetTrigger(gd));

        PendingInteraction.PermanentChoice targetChoice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(targetChoice.validIds()).contains(target.getId());
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.FATE)).isEqualTo(1);
    }

    @Test
    void chaosCanBeDeclinedWithoutRemovingExistingFateCounters() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        target.setCounterCount(CounterType.FATE, 2);

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> triggers.processNextSpellTargetTrigger(gd));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.FATE)).isEqualTo(2);
    }

    @Test
    void chaosCanPutAnotherFateCounterOnAnOpponentsLand() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new Forest());
        target.setCounterCount(CounterType.FATE, 1);

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.inMutationScope(() -> triggers.processNextSpellTargetTrigger(gd));
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(target.getCounterCount(CounterType.FATE)).isEqualTo(2);
    }

    @Test
    void departureRemovesEveryFateCounterButPreservesOtherCounterTypes() {
        Permanent marked = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        marked.setCounterCount(CounterType.FATE, 3);
        marked.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        Permanent unmarked = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        unmarked.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        Permanent land = harness.addToBattlefieldAndReturn(player1, new Forest());

        harness.inMutationScope(() -> planar.planeswalk(gd));
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).contains(marked);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(land).doesNotContain(unmarked);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(unmarked.getCard());
        assertThat(marked.getCounterCount(CounterType.FATE)).isZero();
        assertThat(marked.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }
}
