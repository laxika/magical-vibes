package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Doppelgang;
import com.github.laxika.magicalvibes.cards.g.GraniteWitness;
import com.github.laxika.magicalvibes.cards.n.NoviceInspector;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CaseOfThePilferedProof.class, CaseFileAuditor.class, ColdCaseCracker.class,
        NoviceInspector.class, GraniteWitness.class, Doppelgang.class})
class CaseOfThePilferedProofTest extends BaseCardTest {

    @Test
    @DisplayName("A Detective that enters under your control gets a +1/+1 counter")
    void detectiveEnteringGetsCounter() {
        addThreeDetectives();
        solveAtEndStep();

        harness.setLibrary(player1, List.of());
        harness.setHand(player1, List.of(new CaseFileAuditor()));
        harness.addMana(player1, ManaColor.WHITE, 3);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent tracker = findPermanent(player1, "Case File Auditor");
        assertThat(tracker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("The solved Case adds a Clue when a token is created")
    void solvedCaseAddsClueToTokenCreation() {
        addThreeDetectives();
        solveAtEndStep();

        castInspector(player1);

        assertThat(findPermanents(player1, "Clue")).hasSize(2);
    }

    @Test
    @DisplayName("An unsolved Case does not add a Clue to token creation")
    void unsolvedCaseDoesNotAddClueToTokenCreation() {
        harness.addToBattlefield(player1, new CaseOfThePilferedProof());

        castInspector(player1);

        assertThat(findPermanents(player1, "Clue")).hasSize(1);
        assertThat(findPermanent(player1, "Novice Inspector")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void eachSolvedCaseAddsItsOwnClue() {
        addThreeDetectives();
        harness.addToBattlefield(player1, new CaseOfThePilferedProof());
        solveAtEndStep();
        assertThat(findPermanents(player1, "Case of the Pilfered Proof"))
                .allMatch(Permanent::isSolved);

        castInspector(player1);

        assertThat(findPermanents(player1, "Clue")).hasSize(3);
    }

    @Test
    void solvedCaseAddsClueWhenTokenCopyIsCreated() {
        addThreeDetectives();
        solveAtEndStep();
        Permanent detective = findPermanent(player1, "Cold Case Cracker");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Doppelgang()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, 1, List.of(detective.getId()));
        resolveAllTriggers();

        assertThat(findPermanents(player1, "Cold Case Cracker")).hasSize(4);
        assertThat(findPermanents(player1, "Clue")).hasSize(1);
    }

    @Test
    void solvedCaseDoesNotModifyOpponentsTokenCreationOrDetective() {
        addThreeDetectives();
        solveAtEndStep();

        castInspector(player2);

        assertThat(findPermanents(player2, "Clue")).hasSize(1);
        assertThat(findPermanents(player1, "Clue")).isEmpty();
        assertThat(findPermanent(player2, "Novice Inspector")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void faceDownDetectiveEnteringDoesNotGetCounter() {
        harness.addToBattlefield(player1, new CaseOfThePilferedProof());
        harness.setHand(player1, List.of(new GraniteWitness()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        resolveAllTriggers();

        Permanent witness = findPermanent(player1, "Granite Witness");
        assertThat(witness.isFaceDown()).isTrue();
        assertThat(witness.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void detectiveTurnedFaceUpGetsCounter() {
        harness.addToBattlefield(player1, new CaseOfThePilferedProof());
        Permanent witness = harness.addToBattlefieldAndReturn(player1, new GraniteWitness());
        witness.setFaceDown(2, 2, Set.of(CardType.CREATURE));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.turnFaceUp(player1, 1);
        harness.handlePermanentChosen(player1, witness.getId());
        resolveAllTriggers();
        harness.handleMayAbilityChosen(player1, false);
        resolveAllTriggers();

        assertThat(witness.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void twoDetectivesDoNotSolveCase() {
        harness.addToBattlefield(player1, new CaseOfThePilferedProof());
        harness.addToBattlefield(player1, new ColdCaseCracker());
        harness.addToBattlefield(player1, new ColdCaseCracker());

        solveAtEndStep();

        assertThat(findPermanent(player1, "Case of the Pilfered Proof").isSolved()).isFalse();
    }

    @Test
    void opponentsEndStepDoesNotSolveCase() {
        addThreeDetectives();
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player2, TurnStep.END_STEP);
        resolveAllTriggers();

        assertThat(findPermanent(player1, "Case of the Pilfered Proof").isSolved()).isFalse();
    }

    @Test
    void solveConditionIsCheckedAgainOnResolution() {
        addThreeDetectives();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        assertThat(gd.stack).hasSize(1);
        gd.playerBattlefields.get(player1.getId()).remove(findPermanent(player1, "Cold Case Cracker"));

        resolveAllTriggers();

        assertThat(findPermanent(player1, "Case of the Pilfered Proof").isSolved()).isFalse();
    }

    @Test
    void solvedCaseStaysSolvedAfterDetectivesLeave() {
        addThreeDetectives();
        solveAtEndStep();
        gd.playerBattlefields.get(player1.getId()).removeIf(p -> p.getCard() instanceof ColdCaseCracker);

        castInspector(player1);

        assertThat(findPermanent(player1, "Case of the Pilfered Proof").isSolved()).isTrue();
        assertThat(findPermanents(player1, "Clue")).hasSize(2);
    }

    private void castInspector(Player player) {
        harness.forceActivePlayer(player);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player, List.of(new NoviceInspector()));
        harness.addMana(player, ManaColor.WHITE, 1);
        harness.castCreature(player, 0);
        resolveAllTriggers();
    }

    private void addThreeDetectives() {
        harness.addToBattlefield(player1, new CaseOfThePilferedProof());
        harness.addToBattlefield(player1, new ColdCaseCracker());
        harness.addToBattlefield(player1, new ColdCaseCracker());
        harness.addToBattlefield(player1, new ColdCaseCracker());
    }

    private void solveAtEndStep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);
        resolveAllTriggers();
    }
}
