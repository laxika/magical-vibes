package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.g.GluttonousGuest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaseOfThePilferedProof.class, CaseFileAuditor.class, GluttonousGuest.class})
class CaseOfThePilferedProofTest extends BaseCardTest {

    @Test
    @DisplayName("A Detective that enters under your control gets a +1/+1 counter")
    void detectiveEnteringGetsCounter() {
        addThreeDetectives();
        solveAtEndStep();
        resolveAllStack();

        harness.setHand(player1, List.of(new CaseFileAuditor()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castCreature(player1, 0);
        resolveAllStack();

        Permanent tracker = findPermanent(player1, "Case File Auditor");
        assertThat(tracker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The solved Case adds a Clue when a token is created")
    void solvedCaseAddsClueToTokenCreation() {
        addThreeDetectives();
        solveAtEndStep();
        resolveAllStack();

        harness.setHand(player1, List.of(new GluttonousGuest()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castCreature(player1, 0);
        resolveAllStack();

        assertThat(findPermanents(player1, "Blood")).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    @DisplayName("An unsolved Case does not add a Clue to token creation")
    void unsolvedCaseDoesNotAddClueToTokenCreation() {
        harness.addToBattlefield(player1, new CaseOfThePilferedProof());

        harness.setHand(player1, List.of(new GluttonousGuest()));
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castCreature(player1, 0);
        resolveAllStack();

        assertThat(findPermanents(player1, "Blood")).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
    }

    private void addThreeDetectives() {
        harness.addToBattlefield(player1, new CaseOfThePilferedProof());
        harness.addToBattlefield(player1, new CaseFileAuditor());
        harness.addToBattlefield(player1, new CaseFileAuditor());
        harness.addToBattlefield(player1, new CaseFileAuditor());
    }

    private void solveAtEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
        gd.stack.clear();
        gd.playerBattlefields.get(player1.getId()).subList(1, 4).clear();
        harness.clearPriorityPassed();
    }

    private void resolveAllStack() {
        while (!gd.stack.isEmpty()) {
            harness.passBothPriorities();
        }
    }
}
