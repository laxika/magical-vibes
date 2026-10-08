package com.github.laxika.magicalvibes.cards.v;

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

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ValorsReach.class, GrizzlyBears.class})
class ValorsReachTest extends BaseCardTest {

    private PlanechaseService planar;

    @BeforeEach
    void preparePlane() {
        planar = GameTestEngineContext.get().getBean(PlanechaseService.class);
        gd.planechase = new PlanechaseState();
        gd.planechase.controllerId = player1.getId();
        gd.planechase.faceUp.add(new PlanarObject(new ValorsReach(), gd.nextTimestamp()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
    }

    @Test
    void exactlyTwoAttackersGainDoubleStrike() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, first, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void otherAttackerCountsDoNotGainDoubleStrike() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, attacker, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void threeAttackersDoNotGainDoubleStrike() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        Permanent third = addCreatureReady(player1, new GrizzlyBears());

        declareAttackers(List.of(0, 1, 2));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, first, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, second, Keyword.DOUBLE_STRIKE)).isFalse();
        assertThat(gqs.hasKeyword(gd, third, Keyword.DOUBLE_STRIKE)).isFalse();
    }

    @Test
    void originalAttackerStillGainsDoubleStrikeAfterLeavingCombat() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1));
            first.setAttacking(false);
            harness.passBothPriorities();
        });

        assertThat(gqs.hasKeyword(gd, first, Keyword.DOUBLE_STRIKE)).isTrue();
        assertThat(gqs.hasKeyword(gd, second, Keyword.DOUBLE_STRIKE)).isTrue();
    }

    @Test
    void chaosDoesNotAddCombatWhenAllChosenTargetsBecomeIllegal() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        first.tap();
        second.tap();

        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.inMutationScope(() -> planar.chaos(gd));
            harness.passBothPriorities();
            harness.handlePermanentChosen(player1, first.getId());
            harness.handlePermanentChosen(player1, second.getId());
            gd.playerBattlefields.get(player1.getId()).removeAll(List.of(first, second));
            resolveAllTriggers();
        });

        assertThat(gd.additionalCombatMainPhasePairs).isZero();
    }

    @Test
    void chaosOutsideMainPhaseUntapsWithoutAddingCombat() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        first.tap();
        second.tap();
        harness.forceStep(TurnStep.BEGINNING_OF_COMBAT);

        harness.withAutoStop(TurnStep.BEGINNING_OF_COMBAT, () -> {
            harness.inMutationScope(() -> planar.chaos(gd));
            harness.passBothPriorities();
            harness.handlePermanentChosen(player1, first.getId());
            harness.handlePermanentChosen(player1, second.getId());
            harness.passBothPriorities();
        });

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(gd.additionalCombatMainPhasePairs).isZero();
    }

    @Test
    void chaosWithNoCreaturesStillAddsCombatDuringMainPhase() {
        harness.withAutoStop(TurnStep.PRECOMBAT_MAIN, () -> {
            harness.inMutationScope(() -> planar.chaos(gd));
            harness.passBothPriorities();
            resolveAllTriggers();
        });

        assertThat(gd.additionalCombatMainPhasePairs).isEqualTo(1);
    }

    @Test
    void creaturesPutOntoBattlefieldAttackingDoNotGainDoubleStrike() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());

        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS, () -> {
            declareAttackers(List.of(0, 1));
            Permanent laterAttacker = addCreatureReady(player1, new GrizzlyBears());
            laterAttacker.setAttacking(true);
            resolveAllTriggers();

            assertThat(gqs.hasKeyword(gd, first, Keyword.DOUBLE_STRIKE)).isTrue();
            assertThat(gqs.hasKeyword(gd, second, Keyword.DOUBLE_STRIKE)).isTrue();
            assertThat(gqs.hasKeyword(gd, laterAttacker, Keyword.DOUBLE_STRIKE)).isFalse();
        });
    }

    @Test
    void chaosUntapsUpToTwoControlledCreaturesAndAddsCombatDuringMainPhase() {
        Permanent first = addCreatureReady(player1, new GrizzlyBears());
        Permanent second = addCreatureReady(player1, new GrizzlyBears());
        Permanent third = addCreatureReady(player1, new GrizzlyBears());
        first.tap();
        second.tap();
        third.tap();

        harness.inMutationScope(() -> planar.chaos(gd));
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, first.getId());
        harness.handlePermanentChosen(player1, second.getId());
        resolveAllTriggers();

        assertThat(first.isTapped()).isFalse();
        assertThat(second.isTapped()).isFalse();
        assertThat(third.isTapped()).isTrue();
        assertThat(gd.additionalCombatMainPhasePairs).isEqualTo(1);
    }
}
