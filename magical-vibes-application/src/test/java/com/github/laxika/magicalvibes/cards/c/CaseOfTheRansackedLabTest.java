package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.Divination;
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

@CardUsed({CaseOfTheRansackedLab.class, Divination.class, GrizzlyBears.class, Shock.class})
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

    private void castShocks(int count) {
        harness.setHand(player1, List.of(new Shock(), new Shock(), new Shock(), new Shock()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        for (int i = 0; i < count; i++) {
            harness.addMana(player1, ManaColor.RED, 1);
            harness.castInstant(player1, 0, player2.getId());
            harness.passBothPriorities();
        }
    }

    private void resolveEndStepTriggers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
