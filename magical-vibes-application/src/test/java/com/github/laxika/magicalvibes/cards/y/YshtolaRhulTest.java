package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YshtolaRhul.class, GrizzlyBears.class})
class YshtolaRhulTest extends BaseCardTest {

    @Test
    @DisplayName("Flickers a creature and creates one additional end step")
    void flickersCreatureAndCreatesAdditionalEndStep() {
        harness.addToBattlefield(player1, new YshtolaRhul());
        harness.addToBattlefield(player1, new GrizzlyBears());
        UUID originalBearsId = harness.getPermanentId(player1, "Grizzly Bears");

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        assertThat(gd.endStepsThisTurn).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, originalBearsId);
        harness.passBothPriorities();

        Permanent returnedBears = findPermanent(player1, "Grizzly Bears");
        assertThat(returnedBears.getId()).isNotEqualTo(originalBearsId);
        assertThat(gd.endStepsThisTurn).isEqualTo(2);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        assertThat(gd.currentStep).isEqualTo(TurnStep.END_STEP);
        harness.handlePermanentChosen(player1, returnedBears.getId());
        harness.passBothPriorities();

        assertThat(gd.additionalEndStepsPending).isZero();
        harness.passUntil(TurnStep.CLEANUP);
        assertThat(gd.currentStep).isEqualTo(TurnStep.CLEANUP);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's end step")
    void doesNotTriggerDuringOpponentsEndStep() {
        harness.addToBattlefield(player1, new YshtolaRhul());
        harness.addToBattlefield(player1, new GrizzlyBears());

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }
}
