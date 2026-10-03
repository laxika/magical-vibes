package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AlacrianJaguar;
import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.ScrollOfFate;
import com.github.laxika.magicalvibes.cards.v.VenomsacLagac;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CloudspireCoordinator.class, AlacrianJaguar.class, DuskLegionDreadnought.class,
        GrizzlyBears.class, ScrollOfFate.class, VenomsacLagac.class, ClamorousIronclad.class})
class CloudspireCoordinatorTest extends BaseCardTest {

    @Test
    void entersWithScryTwo() {
        harness.setHand(player1, List.of(new CloudspireCoordinator()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

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

    @Test
    void createsNoPilotsWithoutMountOrVehicleEntriesUnderYourControl() {
        Permanent coordinator = addCreatureReady(player1, new CloudspireCoordinator());
        harness.enterBattlefieldAndReturn(player1, new ScrollOfFate());
        harness.enterBattlefieldAndReturn(player2, new AlacrianJaguar());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Pilot")).isEmpty();
        assertThat(coordinator.isTapped()).isTrue();
    }

    @Test
    void countsMountsEnteringAfterActivationBeforeResolution() {
        addCreatureReady(player1, new CloudspireCoordinator());
        harness.activateAbility(player1, 0, null, null);
        harness.enterBattlefieldAndReturn(player1, new AlacrianJaguar());
        harness.enterBattlefieldAndReturn(player2, new AlacrianJaguar());

        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Pilot")).hasSize(1);
        assertThat(findPermanents(player2, "Pilot")).isEmpty();
    }

    @Test
    void countsMountsThatHaveAlreadyLeftTheBattlefield() {
        addCreatureReady(player1, new CloudspireCoordinator());
        Permanent mount = harness.enterBattlefieldAndReturn(player1, new AlacrianJaguar());
        gd.playerBattlefields.get(player1.getId()).remove(mount);
        gd.playerGraveyards.get(player1.getId()).add(mount.getCard());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Pilot")).hasSize(1);
    }

    @Test
    void summoningSickPilotCanSaddleForTwoWithoutChangingItsOrdinaryPower() {
        addCreatureReady(player1, new CloudspireCoordinator());
        Permanent mount = harness.enterBattlefieldAndReturn(player1, new VenomsacLagac());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        Permanent pilot = findPermanents(player1, "Pilot").getFirst();

        assertThat(pilot.isSummoningSick()).isTrue();
        assertThat(gqs.getEffectivePower(gd, pilot)).isEqualTo(1);
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(mount.isSaddled()).isTrue();
        assertThat(pilot.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, pilot)).isEqualTo(1);
    }

    @Test
    void manifestedMountDoesNotCountAsAMountEntry() {
        assertManifestedCardDoesNotCount(new AlacrianJaguar());
    }

    @Test
    void manifestedVehicleDoesNotCountAsAVehicleEntry() {
        assertManifestedCardDoesNotCount(new DuskLegionDreadnought());
    }

    @Test
    void onePilotCanCrewForThreeWithoutChangingItsOrdinaryPower() {
        addCreatureReady(player1, new CloudspireCoordinator());
        Permanent vehicle = harness.enterBattlefieldAndReturn(player1, new ClamorousIronclad());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        Permanent pilot = findPermanents(player1, "Pilot").getFirst();

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(pilot.isTapped()).isTrue();
        assertThat(gqs.getEffectivePower(gd, pilot)).isEqualTo(1);
    }

    private void assertManifestedCardDoesNotCount(Card card) {
        addCreatureReady(player1, new CloudspireCoordinator());
        harness.addToBattlefield(player1, new ScrollOfFate());
        harness.setHand(player1, List.of(card));
        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(Permanent::isManifested)
                .singleElement()
                .satisfies(permanent -> assertThat(permanent.isFaceDown()).isTrue());
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Pilot")).isEmpty();
    }
}
