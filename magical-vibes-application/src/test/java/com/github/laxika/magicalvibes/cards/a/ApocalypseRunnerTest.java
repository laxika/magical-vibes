package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({ApocalypseRunner.class, GrizzlyBears.class, HillGiant.class})
class ApocalypseRunnerTest extends BaseCardTest {

    @Test
    @DisplayName("Grants lifelink and unblockable to a qualifying creature")
    void grantsLifelinkAndUnblockable() {
        Permanent runner = addCreatureReady(player1, new ApocalypseRunner());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();
        assertThat(bears.isCantBeBlocked()).isTrue();
        assertThat(runner.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Temporary abilities wear off at cleanup")
    void temporaryAbilitiesWearOff() {
        addCreatureReady(player1, new ApocalypseRunner());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isFalse();
        assertThat(bears.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Only targets creatures you control with power 2 or less")
    void rejectsIllegalTargets() {
        addCreatureReady(player1, new ApocalypseRunner());
        Permanent opponentCreature = addCreatureReady(player2, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, opponentCreature.getId()))
                .isInstanceOf(IllegalStateException.class);

        Permanent largeCreature = addCreatureReady(player1, new HillGiant());
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, largeCreature.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Crew 3 animates Apocalypse Runner and taps the crew")
    void crewAnimatesVehicleAndTapsCrew() {
        Permanent runner = addCreatureReady(player1, new ApocalypseRunner());
        Permanent crew = addCreatureReady(player1, new HillGiant());

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, runner)).isTrue();
        assertThat(crew.isTapped()).isTrue();
    }

    @Test
    void targetBecomingTooPowerfulBeforeResolutionGetsNeitherBenefit() {
        addCreatureReady(player1, new ApocalypseRunner());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, bears.getId());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isFalse();
        assertThat(bears.isCantBeBlocked()).isFalse();
    }

    @Test
    void powerIncreasingAfterResolutionDoesNotRemoveBenefits() {
        addCreatureReady(player1, new ApocalypseRunner());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();
        assertThat(bears.isCantBeBlocked()).isTrue();
    }

    @Test
    void noncreatureVehicleCanUseTapAbilityWhileSummoningSick() {
        Permanent runner = harness.addToBattlefieldAndReturn(player1, new ApocalypseRunner());
        runner.setSummoningSick(true);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(runner.isTapped()).isTrue();
        assertThat(gqs.hasKeyword(gd, bears, Keyword.LIFELINK)).isTrue();
        assertThat(bears.isCantBeBlocked()).isTrue();
    }

    @Test
    void summoningSickCreatureCanCrewAndAnimationEndsAtCleanup() {
        Permanent runner = addCreatureReady(player1, new ApocalypseRunner());
        Permanent crew = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        crew.setSummoningSick(true);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(crew.isTapped()).isTrue();
        assertThat(runner.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, runner)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, runner)).isFalse();
    }

    @Test
    void cannotCrewWithInsufficientPower() {
        Permanent runner = addCreatureReady(player1, new ApocalypseRunner());
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(bears.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, runner)).isFalse();
    }
}
