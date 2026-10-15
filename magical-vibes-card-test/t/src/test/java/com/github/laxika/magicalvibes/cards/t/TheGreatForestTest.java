package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
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

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TheGreatForest.class, GiantSpider.class, GoblinPiker.class, GrizzlyBears.class})
class TheGreatForestTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new TheGreatForest(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void allCreaturesAssignCombatDamageUsingToughness() {
        Permanent ownSpider = addCreatureReady(player1, new GiantSpider());
        Permanent opposingPiker = addCreatureReady(player2, new GoblinPiker());

        assertThat(gqs.getEffectiveCombatDamage(gd, ownSpider)).isEqualTo(4);
        assertThat(gqs.getEffectiveCombatDamage(gd, opposingPiker)).isEqualTo(1);
    }

    @Test
    void chaosBoostsAndGivesTrampleToPlanarControllersCreaturesUntilEndOfTurn() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();

        assertThat(gqs.getEffectivePower(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.TRAMPLE)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void attackingAndBlockingCreaturesDealToughnessDamage() {
        Permanent attacker = addCreatureReady(player1, new GiantSpider());
        Permanent blocker = addCreatureReady(player2, new GiantSpider());
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    void chaosTrampleUsesBoostedToughnessForCombatDamage() {
        harness.setLife(player2, 20);
        Permanent attacker = addCreatureReady(player1, new GiantSpider());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();
        attacker.setAttacking(true);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);

        harness.resolveCombatDamage();
        harness.handleCombatDamageAssigned(player1, 0,
                Map.of(blocker.getId(), 2, player2.getId(), 4));
        harness.runStateBasedActions();

        harness.assertLife(player2, 16);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(attacker);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
    }

    @Test
    void chaosAffectsCreaturesPresentAtResolutionButNotLaterArrivals() {
        harness.inMutationScope(() -> planar.chaos(gd));
        Permanent beforeResolution = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.passBothPriorities();
        Permanent afterResolution = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        assertThat(gqs.getEffectiveToughness(gd, beforeResolution)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, beforeResolution, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectiveToughness(gd, afterResolution)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, afterResolution, Keyword.TRAMPLE)).isFalse();
    }

    @Test
    void leavingPlaneEndsStaticEffectButDoesNotRemoveResolvedChaosBonus() {
        Permanent creature = addCreatureReady(player1, new GiantSpider());
        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();

        gd.planechase.faceUp.clear();

        assertThat(gqs.getEffectiveCombatDamage(gd, creature)).isEqualTo(2);
        assertThat(gqs.getEffectiveToughness(gd, creature)).isEqualTo(6);
        assertThat(gqs.hasKeyword(gd, creature, Keyword.TRAMPLE)).isTrue();
    }

    @Test
    void pendingChaosKeepsItsControllerWhenPlanarControlChanges() {
        Permanent ownCreature = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.inMutationScope(() -> planar.chaos(gd));
        gd.planechase.controllerId = player2.getId();

        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, ownCreature)).isEqualTo(4);
        assertThat(gqs.hasKeyword(gd, ownCreature, Keyword.TRAMPLE)).isTrue();
        assertThat(gqs.getEffectiveToughness(gd, opposingCreature)).isEqualTo(2);
        assertThat(gqs.hasKeyword(gd, opposingCreature, Keyword.TRAMPLE)).isFalse();
    }

}
