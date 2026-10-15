package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.j.JukaiTrainee;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MobilizerMech.class, MechtitanCore.class, JukaiTrainee.class})
class MobilizerMechTest extends BaseCardTest {

    @Test
    void crewTriggerTargetsAnotherVehicleYouControl() {
        Permanent mobilizer = addCreatureReady(player1, new MobilizerMech());
        Permanent ownVehicle = addCreatureReady(player1, new MechtitanCore());
        Permanent opponentVehicle = addCreatureReady(player2, new MechtitanCore());
        addCreatureReady(player1, new JukaiTrainee());
        addCreatureReady(player1, new JukaiTrainee());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.PermanentChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validIds()).contains(ownVehicle.getId());
        assertThat(choice.validIds()).doesNotContain(mobilizer.getId());
        assertThat(choice.validIds()).doesNotContain(opponentVehicle.getId());

        harness.handlePermanentChosen(player1, ownVehicle.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, mobilizer)).isTrue();
        assertThat(gqs.isCreature(gd, ownVehicle)).isTrue();
        assertThat(gqs.hasEffectiveSubtype(gd, ownVehicle, CardSubtype.VEHICLE)).isTrue();
        assertThat(gqs.isCreature(gd, opponentVehicle)).isFalse();
    }

    @Test
    void crewTriggerMayBeDeclined() {
        addCreatureReady(player1, new MobilizerMech());
        Permanent ownVehicle = addCreatureReady(player1, new MechtitanCore());
        addCreatureReady(player1, new JukaiTrainee());
        addCreatureReady(player1, new JukaiTrainee());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, ownVehicle)).isFalse();
    }

    @Test
    void targetAnimationEndsAtEndOfTurn() {
        addCreatureReady(player1, new MobilizerMech());
        Permanent ownVehicle = addCreatureReady(player1, new MechtitanCore());
        addCreatureReady(player1, new JukaiTrainee());
        addCreatureReady(player1, new JukaiTrainee());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, ownVehicle.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, ownVehicle)).isFalse();
    }

    @Test
    void crewWithNoOtherVehicleStillAnimatesMobilizer() {
        Permanent mobilizer = addCreatureReady(player1, new MobilizerMech());
        Permanent opponentVehicle = addCreatureReady(player2, new MechtitanCore());
        addCreatureReady(player1, new JukaiTrainee());
        addCreatureReady(player1, new JukaiTrainee());

        harness.activateAbility(player1, 0, null, null);
        resolveAllTriggers();

        assertThat(gqs.isCreature(gd, mobilizer)).isTrue();
        assertThat(gqs.isCreature(gd, opponentVehicle)).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void crewRequiresAtLeastThreeTotalPower() {
        Permanent mobilizer = addCreatureReady(player1, new MobilizerMech());
        Permanent trainee = addCreatureReady(player1, new JukaiTrainee());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(trainee.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, mobilizer)).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void crewingAgainTriggersEvenWhenMobilizerIsAlreadyACreature() {
        Permanent mobilizer = addCreatureReady(player1, new MobilizerMech());
        Permanent firstVehicle = addCreatureReady(player1, new MechtitanCore());
        Permanent secondVehicle = addCreatureReady(player1, new MechtitanCore());
        addCreatureReady(player1, new JukaiTrainee());
        addCreatureReady(player1, new JukaiTrainee());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, firstVehicle.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, mobilizer)).isTrue();
        assertThat(gqs.isCreature(gd, secondVehicle)).isFalse();

        firstVehicle.tap();
        addCreatureReady(player1, new JukaiTrainee());
        addCreatureReady(player1, new JukaiTrainee());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, secondVehicle.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, firstVehicle)).isTrue();
        assertThat(gqs.isCreature(gd, secondVehicle)).isTrue();
    }

    @Test
    void animatingAnotherMobilizerDoesNotTriggerItsCrewAbility() {
        addCreatureReady(player1, new MobilizerMech());
        Permanent otherMobilizer = addCreatureReady(player1, new MobilizerMech());
        Permanent thirdVehicle = addCreatureReady(player1, new MechtitanCore());
        addCreatureReady(player1, new JukaiTrainee());
        addCreatureReady(player1, new JukaiTrainee());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, otherMobilizer.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, otherMobilizer)).isTrue();
        assertThat(gqs.isCreature(gd, thirdVehicle)).isFalse();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.stack).isEmpty();
    }
}
