package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.action.DelayedGraveyardToBattlefieldUnderControl;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class LifelineTest extends BaseCardTest {

    private void shockBearsWithSurvivingCreature() {
        harness.addToBattlefield(player1, new Lifeline());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        UUID bearsId = harness.getPermanentId(player2, "Grizzly Bears");
        harness.castInstant(player1, 0, bearsId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void advanceToEndStep() {
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        gs.advanceStep(gd);
    }

    @Test
    @DisplayName("A dead creature returns to its owner's control at the next end step")
    void returnsDeadCreatureUnderOwnersControl() {
        shockBearsWithSurvivingCreature();

        assertThat(gd.getDelayedActions(DelayedGraveyardToBattlefieldUnderControl.class)).hasSize(1);
        harness.assertInGraveyard(player2, "Grizzly Bears");

        advanceToEndStep();

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        harness.assertNotOnBattlefield(player1, "Grizzly Bears");
        harness.assertNotInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Lifeline does not trigger when no other creature is on the battlefield")
    void doesNotTriggerWithoutAnotherCreature() {
        harness.addToBattlefield(player1, new Lifeline());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        assertThat(gd.getDelayedActions(DelayedGraveyardToBattlefieldUnderControl.class)).isEmpty();
        advanceToEndStep();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("The other-creature condition is checked again when Lifeline's trigger resolves")
    void checksAnotherCreatureAgainOnResolution() {
        harness.addToBattlefield(player1, new Lifeline());
        harness.addToBattlefield(player2, new LlanowarElves());
        harness.addToBattlefield(player2, new GrizzlyBears());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Grizzly Bears"));
        harness.passBothPriorities();

        harness.castInstant(player1, 0, harness.getPermanentId(player2, "Llanowar Elves"));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.getDelayedActions(DelayedGraveyardToBattlefieldUnderControl.class)).isEmpty();
        harness.assertInGraveyard(player2, "Grizzly Bears");
    }
}
