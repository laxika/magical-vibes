package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
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

@CardUsed({CaseOfTheStashedSkeleton.class, GrizzlyBears.class, Shock.class})
class CaseOfTheStashedSkeletonTest extends BaseCardTest {

    @Test
    @DisplayName("Creates a suspected 2/1 Skeleton token")
    void createsSuspectedSkeleton() {
        harness.enterBattlefieldAndReturn(player1, new CaseOfTheStashedSkeleton());
        resolveAllStack();

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
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        harness.assertInHand(player1, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Case of the Stashed Skeleton");
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
        resolveAllStack();
        return casePermanent;
    }

    private void destroySkeleton() {
        Permanent skeleton = findPermanent(player1, "Skeleton");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castInstant(player1, 0, skeleton.getId());
        harness.passBothPriorities();
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
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void resolveAllStack() {
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
