package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.u.UnauthorizedExit;
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

@CardUsed({CaseOfTheRansackedLab.class, Divination.class, GrizzlyBears.class, Shock.class, UnauthorizedExit.class})
class CaseOfTheRansackedLabTest extends BaseCardTest {

    @Test
    @DisplayName("Instant and sorcery spells you cast cost one less")
    void reducesInstantAndSorceryCosts() {
        harness.addToBattlefield(player1, new CaseOfTheRansackedLab());
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.castSorcery(player1, 0, 0);

        assertThat(gd.stack).anyMatch(entry -> entry.getCard().getName().equals("Divination"));
    }

    @Test
    @DisplayName("Solves at the beginning of the end step after four instant or sorcery spells")
    void solvesAfterFourInstantOrSorcerySpells() {
        Permanent casePermanent = harness.addToBattlefieldAndReturn(player1, new CaseOfTheRansackedLab());

        castShocks(4);
        resolveEndStepTriggers();

        assertThat(casePermanent.isSolved()).isTrue();
    }

    @Test
    @DisplayName("Does not solve before four instant or sorcery spells")
    void doesNotSolveBeforeFourInstantOrSorcerySpells() {
        Permanent casePermanent = harness.addToBattlefieldAndReturn(player1, new CaseOfTheRansackedLab());

        castShocks(3);
        resolveEndStepTriggers();

        assertThat(casePermanent.isSolved()).isFalse();
    }

    @Test
    @DisplayName("The solved Case draws a card whenever you cast an instant or sorcery spell")
    void solvedCaseDrawsOnInstantOrSorceryCast() {
        harness.addToBattlefield(player1, new CaseOfTheRansackedLab());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        castShocks(4);
        resolveEndStepTriggers();

        harness.setHand(player1, List.of(new Shock()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("The solved Case does not draw for a creature spell")
    void solvedCaseDoesNotDrawOnCreatureCast() {
        harness.addToBattlefield(player1, new CaseOfTheRansackedLab());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        castShocks(4);
        resolveEndStepTriggers();

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.forceActivePlayer(player1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    void reducesInstantGenericCost() {
        harness.addToBattlefield(player1, new CaseOfTheRansackedLab());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        harness.setHand(player1, List.of(new UnauthorizedExit()));
        harness.setLibrary(player1, List.of());
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castAndResolveInstant(player1, 0, target.getId());

        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    void doesNotReduceColoredManaCost() {
        harness.addToBattlefield(player1, new CaseOfTheRansackedLab());
        harness.setHand(player1, List.of(new Shock()));

        assertThatThrownBy(() -> harness.castInstant(player1, 0, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotReduceOpponentSpellCost() {
        harness.addToBattlefield(player1, new CaseOfTheRansackedLab());
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player2, List.of(new Divination()));
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.castSorcery(player2, 0, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void doesNotReduceCreatureSpellCost() {
        harness.addToBattlefield(player1, new CaseOfTheRansackedLab());
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        assertThatThrownBy(() -> harness.castCreature(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void opponentSpellsDoNotCountTowardSolving() {
        Permanent casePermanent = harness.addToBattlefieldAndReturn(player2, new CaseOfTheRansackedLab());
        castShocks(4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(casePermanent.isSolved()).isFalse();
    }

    @Test
    void doesNotSolveAtOpponentEndStep() {
        Permanent casePermanent = harness.addToBattlefieldAndReturn(player1, new CaseOfTheRansackedLab());
        castShocks(4);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.stack).isEmpty();
        assertThat(casePermanent.isSolved()).isFalse();
    }

    @Test
    void countsSpellsCastBeforeCaseEnteredBattlefield() {
        castShocks(4);
        Permanent casePermanent = harness.addToBattlefieldAndReturn(player1, new CaseOfTheRansackedLab());

        resolveEndStepTriggers();

        assertThat(casePermanent.isSolved()).isTrue();
    }

    @Test
    void creatureSpellDoesNotCountTowardSolving() {
        Permanent casePermanent = harness.addToBattlefieldAndReturn(player1, new CaseOfTheRansackedLab());
        castShocks(3);
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        resolveEndStepTriggers();

        assertThat(casePermanent.isSolved()).isFalse();
    }

    @Test
    void fourthSpellDuringEndStepDoesNotSolveCaseThatTurn() {
        Permanent casePermanent = harness.addToBattlefieldAndReturn(player1, new CaseOfTheRansackedLab());
        castShocks(3);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        assertThat(gd.stack).isEmpty();
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(casePermanent.isSolved()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void unsolvedCaseDoesNotDrawOnSpellCast() {
        harness.addToBattlefield(player1, new CaseOfTheRansackedLab());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castAndResolveInstant(player1, 0, player2.getId());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void solvedCaseDrawsForSorceryBeforeSorceryResolves() {
        harness.addToBattlefield(player1, new CaseOfTheRansackedLab());
        castShocks(4);
        resolveEndStepTriggers();
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, List.of(new GrizzlyBears(), new GrizzlyBears(), new GrizzlyBears()));
        harness.setHand(player1, List.of(new Divination()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    void solvedCaseDoesNotDrawForOpponentSpell() {
        harness.addToBattlefield(player1, new CaseOfTheRansackedLab());
        castShocks(4);
        resolveEndStepTriggers();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castAndResolveInstant(player2, 0, player1.getId());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    void pendingDrawStillResolvesAfterSolvedCaseLeavesBattlefield() {
        Permanent casePermanent = harness.addToBattlefieldAndReturn(player1, new CaseOfTheRansackedLab());
        castShocks(4);
        resolveEndStepTriggers();
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new Shock()));
        harness.setHand(player2, List.of(new UnauthorizedExit()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player2, ManaColor.BLUE, 1);
        harness.addMana(player2, ManaColor.COLORLESS, 1);
        harness.castInstant(player1, 0, player2.getId());

        harness.castAndResolveInstant(player2, 0, casePermanent.getId());
        harness.assertNotOnBattlefield(player1, "Case of the Ransacked Lab");
        harness.passBothPriorities();

        harness.assertInHand(player1, "Grizzly Bears");
        harness.passBothPriorities();
    }

    private void castShocks(int count) {
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock(), new Shock()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        for (int i = 0; i < count; i++) {
            harness.addMana(player1, ManaColor.RED, 1);
            harness.castAndResolveInstant(player1, 0, player2.getId());
        }
    }

    private void resolveEndStepTriggers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.passBothPriorities();
    }
}
