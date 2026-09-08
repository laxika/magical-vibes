package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AlacrianJaguar;
import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CloudspireCoordinatorTest extends BaseCardTest {

    @Test
    void entersWithScryTwo() {
        harness.setHand(player1, List.of(new CloudspireCoordinator()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.Scry.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.Scry.class).cards()).hasSize(2);
    }

    @Test
    void createsOnePilotForEachMountOrVehicleEnteredUnderYourControlThisTurn() {
        Card vehicle = new DuskLegionDreadnought();
        Card mount = new AlacrianJaguar();
        Card creature = new GrizzlyBears();
        gd.permanentsEnteredBattlefieldThisTurn.put(player1.getId(), new ArrayList<>(List.of(vehicle, mount, creature)));
        gd.permanentsEnteredBattlefieldThisTurn.put(player2.getId(), new ArrayList<>(List.of(new DuskLegionDreadnought())));

        Permanent coordinator = addCreatureReady(player1, new CloudspireCoordinator());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Pilot")).hasSize(2);
        assertThat(coordinator.isTapped()).isTrue();
    }

    @Test
    void pilotContributesTwoAdditionalPowerToCrew() {
        Card vehicle = new DuskLegionDreadnought();
        gd.permanentsEnteredBattlefieldThisTurn.put(player1.getId(), new ArrayList<>(List.of(vehicle)));
        Permanent vehiclePermanent = addCreatureReady(player1, vehicle);
        Permanent coordinator = addCreatureReady(player1, new CloudspireCoordinator());
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        Permanent pilot = findPermanents(player1, "Pilot").getFirst();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehiclePermanent)).isTrue();
        assertThat(pilot.isTapped()).isTrue();
        assertThat(coordinator.isTapped()).isTrue();
    }
}
