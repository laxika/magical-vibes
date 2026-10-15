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
        harness.passUntil(TurnStep.END_STEP);

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
        harness.passUntil(TurnStep.END_STEP);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Can flicker itself and trigger again in the additional end step")
    void canFlickerItself() {
        harness.addToBattlefield(player1, new YshtolaRhul());
        UUID originalId = harness.getPermanentId(player1, "Y'shtola Rhul");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        harness.handlePermanentChosen(player1, originalId);
        harness.passBothPriorities();

        UUID returnedId = harness.getPermanentId(player1, "Y'shtola Rhul");
        assertThat(returnedId).isNotEqualTo(originalId);
        assertThat(gd.endStepsThisTurn).isEqualTo(2);
        harness.handlePermanentChosen(player1, returnedId);
        harness.passBothPriorities();

        assertThat(harness.getPermanentId(player1, "Y'shtola Rhul")).isNotEqualTo(returnedId);
        harness.passUntil(TurnStep.CLEANUP);
        assertThat(gd.endStepsThisTurn).isEqualTo(2);
        assertThat(gd.additionalEndStepsPending).isZero();
    }

    @Test
    @DisplayName("Returns a stolen Y'shtola to its owner and still creates an additional end step")
    void returnsStolenCreatureToOwner() {
        YshtolaRhul card = new YshtolaRhul();
        card.setOwnerId(player2.getId());
        harness.addToBattlefield(player1, card);
        UUID originalId = harness.getPermanentId(player1, "Y'shtola Rhul");
        gd.stolenCreatures.put(originalId, player2.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        harness.handlePermanentChosen(player1, originalId);
        harness.passBothPriorities();
        harness.passUntil(TurnStep.CLEANUP);

        harness.assertNotOnBattlefield(player1, "Y'shtola Rhul");
        harness.assertOnBattlefield(player2, "Y'shtola Rhul");
        assertThat(harness.getPermanentId(player2, "Y'shtola Rhul")).isNotEqualTo(originalId);
        assertThat(gd.endStepsThisTurn).isEqualTo(2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Only creatures controlled by the trigger's controller are legal targets")
    void excludesOpponentsCreatures() {
        harness.addToBattlefield(player1, new YshtolaRhul());
        harness.addToBattlefield(player2, new YshtolaRhul());
        UUID ownId = harness.getPermanentId(player1, "Y'shtola Rhul");
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);

        PendingInteraction.PermanentChoice choice = (PendingInteraction.PermanentChoice)
                gd.interaction.activeInteraction();
        assertThat(choice.validPermanentIds()).containsExactly(ownId);
    }

    @Test
    @DisplayName("A target that changes controller prevents flickering and the additional end step")
    void illegalTargetPreventsAdditionalEndStep() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new YshtolaRhul());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(TurnStep.END_STEP);
        harness.handlePermanentChosen(player1, target.getId());

        gd.playerBattlefields.get(player1.getId()).remove(target);
        gd.playerBattlefields.get(player2.getId()).add(target);
        gd.stolenCreatures.put(target.getId(), player1.getId());
        harness.passBothPriorities();
        harness.passUntil(TurnStep.CLEANUP);

        assertThat(harness.getPermanentId(player2, "Y'shtola Rhul")).isEqualTo(target.getId());
        harness.assertNotOnBattlefield(player1, "Y'shtola Rhul");
        assertThat(gd.endStepsThisTurn).isEqualTo(1);
        assertThat(gd.additionalEndStepsPending).isZero();
    }
}
