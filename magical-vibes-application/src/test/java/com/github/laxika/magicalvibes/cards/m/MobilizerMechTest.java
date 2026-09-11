package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MobilizerMech.class, DuskLegionDreadnought.class, GrizzlyBears.class})
class MobilizerMechTest extends BaseCardTest {

    @Test
    void crewTriggerTargetsAnotherVehicleYouControl() {
        Permanent mobilizer = addReadyMobilizer(player1);
        Permanent ownVehicle = addReadyVehicle(player1);
        Permanent opponentVehicle = addReadyVehicle(player2);
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

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
        addReadyMobilizer(player1);
        Permanent ownVehicle = addReadyVehicle(player1);
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, player1.getId());
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, ownVehicle)).isFalse();
    }

    @Test
    void targetAnimationEndsAtEndOfTurn() {
        addReadyMobilizer(player1);
        Permanent ownVehicle = addReadyVehicle(player1);
        addCreatureReady(player1, new GrizzlyBears());
        addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, ownVehicle.getId());
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, ownVehicle)).isFalse();
    }

    private Permanent addReadyMobilizer(Player player) {
        Permanent mobilizer = harness.addToBattlefieldAndReturn(player, new MobilizerMech());
        mobilizer.setSummoningSick(false);
        return mobilizer;
    }

    private Permanent addReadyVehicle(Player player) {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player, new DuskLegionDreadnought());
        vehicle.setSummoningSick(false);
        return vehicle;
    }
}
