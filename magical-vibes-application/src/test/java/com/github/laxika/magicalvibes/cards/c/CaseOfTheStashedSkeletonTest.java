package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CaseOfTheStashedSkeleton.class, Murder.class, Shock.class})
class CaseOfTheStashedSkeletonTest extends BaseCardTest {

    @Test
    @DisplayName("The Skeleton is suspected before the Case's enter ability finishes resolving")
    void suspectsSkeletonDuringTokenCreation() {
        harness.enterBattlefieldAndReturn(player1, new CaseOfTheStashedSkeleton());

        harness.passBothPriorities();

        Permanent skeleton = findPermanent(player1, "Skeleton");
        assertThat(skeleton.isSuspected()).isTrue();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("An unsolved Case cannot activate its search ability")
    void cannotActivateUnsolvedCase() {
        addCaseAndResolveSkeleton();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
        harness.assertOnBattlefield(player1, "Case of the Stashed Skeleton");
    }

    @Test
    @DisplayName("A suspected Skeleton prevents the solve ability from triggering at all")
    void doesNotTriggerWithSuspectedSkeleton() {
        Permanent casePermanent = addCaseAndResolveSkeleton();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(casePermanent.isSolved()).isFalse();
    }

    @Test
    @DisplayName("An opponent's suspected Skeleton does not prevent solving")
    void ignoresOpponentsSuspectedSkeleton() {
        Permanent casePermanent = addCaseAndResolveSkeleton();
        destroySkeleton();
        harness.enterBattlefieldAndReturn(player2, new CaseOfTheStashedSkeleton());
        resolveAllTriggers();
        assertThat(findPermanent(player2, "Skeleton").isSuspected()).isTrue();

        resolveEndStepTriggers();

        assertThat(casePermanent.isSolved()).isTrue();
    }

    @Test
    @DisplayName("A second Case's suspected Skeleton also prevents solving")
    void checksAllControlledSkeletons() {
        Permanent casePermanent = addCaseAndResolveSkeleton();
        destroySkeleton();
        harness.enterBattlefieldAndReturn(player1, new CaseOfTheStashedSkeleton());
        resolveAllTriggers();

        resolveEndStepTriggers();

        assertThat(casePermanent.isSolved()).isFalse();
    }

    @Test
    @DisplayName("The solve condition is checked again when the ability resolves")
    void doesNotSolveIfSuspectedSkeletonAppearsInResponse() {
        Permanent casePermanent = addCaseAndResolveSkeleton();
        destroySkeleton();
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);

        harness.enterBattlefieldAndReturn(player1, new CaseOfTheStashedSkeleton());
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Skeleton").isSuspected()).isTrue();
        assertThat(casePermanent.isSolved()).isFalse();
    }

    @Test
    @DisplayName("Removing the Skeleton on an opponent's end step does not solve the Case")
    void doesNotSolveDuringOpponentsEndStep() {
        Permanent casePermanent = addCaseAndResolveSkeleton();
        destroySkeleton();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(casePermanent.isSolved()).isFalse();
    }

    @Test
    @DisplayName("Creates a suspected 2/1 Skeleton token")
    void createsSuspectedSkeleton() {
        harness.enterBattlefieldAndReturn(player1, new CaseOfTheStashedSkeleton());
        resolveAllTriggers();

        Permanent skeleton = findPermanent(player1, "Skeleton");
        assertThat(skeleton.isSuspected()).isTrue();
    }

    @Test
    @DisplayName("Does not solve while a suspected Skeleton remains")
    void doesNotSolveWhileSkeletonRemainsSuspected() {
        Permanent casePermanent = addCaseAndResolveSkeleton();

        resolveEndStepTriggers();

        assertThat(casePermanent.isSolved()).isFalse();
    }

    @Test
    @DisplayName("Solves at the beginning of the end step after the suspected Skeleton leaves")
    void solvesAfterSkeletonLeaves() {
        Permanent casePermanent = addCaseAndResolveSkeleton();
        destroySkeleton();

        resolveEndStepTriggers();

        assertThat(casePermanent.isSolved()).isTrue();
    }

    @Test
    @DisplayName("The solved Case searches the library for any card")
    void solvedCaseSearchesLibrary() {
        solveCase();
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new Murder()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Murder");
        harness.assertNotOnBattlefield(player1, "Case of the Stashed Skeleton");
        harness.assertInGraveyard(player1, "Case of the Stashed Skeleton");
    }

    @Test
    @DisplayName("The solved Case cannot activate during an opponent's turn")
    void cannotActivateDuringOpponentTurn() {
        solveCase();
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("sorcery speed");
    }

    private Permanent addCaseAndResolveSkeleton() {
        Permanent casePermanent = harness.enterBattlefieldAndReturn(player1, new CaseOfTheStashedSkeleton());
        resolveAllTriggers();
        return casePermanent;
    }

    private void destroySkeleton() {
        Permanent skeleton = findPermanent(player1, "Skeleton");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castAndResolveInstant(player1, 0, skeleton.getId());
    }

    private void solveCase() {
        addCaseAndResolveSkeleton();
        destroySkeleton();
        resolveEndStepTriggers();
    }

    private void resolveEndStepTriggers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passUntil(TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
