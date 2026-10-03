package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BrightfieldGlider;
import com.github.laxika.magicalvibes.cards.l.LumberingWorldwagon;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CloudspireCaptain.class, BrightfieldGlider.class, LumberingWorldwagon.class})
class CloudspireCaptainTest extends BaseCardTest {

    @Test
    @DisplayName("Mounts and Vehicles you control get +1/+1")
    void boostsOwnMountsAndVehicles() {
        Permanent ownMount = addMountReady(player1);
        Permanent ownVehicle = addVehicleReady(player1);
        Permanent opposingMount = addMountReady(player2);

        int ownMountPower = gqs.getEffectivePower(gd, ownMount);
        int ownMountToughness = gqs.getEffectiveToughness(gd, ownMount);
        int ownVehiclePower = gqs.getEffectivePower(gd, ownVehicle);
        int ownVehicleToughness = gqs.getEffectiveToughness(gd, ownVehicle);
        int opposingMountPower = gqs.getEffectivePower(gd, opposingMount);
        int opposingMountToughness = gqs.getEffectiveToughness(gd, opposingMount);

        addCreatureReady(player1, new CloudspireCaptain());

        assertThat(gqs.getEffectivePower(gd, ownMount)).isEqualTo(ownMountPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, ownMount)).isEqualTo(ownMountToughness + 1);
        assertThat(gqs.getEffectivePower(gd, ownVehicle)).isEqualTo(ownVehiclePower + 1);
        assertThat(gqs.getEffectiveToughness(gd, ownVehicle)).isEqualTo(ownVehicleToughness + 1);
        assertThat(gqs.getEffectivePower(gd, opposingMount)).isEqualTo(opposingMountPower);
        assertThat(gqs.getEffectiveToughness(gd, opposingMount)).isEqualTo(opposingMountToughness);
    }

    @Test
    @DisplayName("This creature crews a Vehicle as though its power were 2 greater")
    void crewsWithPowerBonus() {
        addCreatureReady(player1, new CloudspireCaptain());
        Permanent vehicle = addVehicleReady(player1);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(vehicle.isTapped()).isFalse();
        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()).get(0).isTapped()).isTrue();
    }

    @Test
    void saddlesWithPowerBonusEvenWithSummoningSickness() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new CloudspireCaptain());
        captain.setSummoningSick(true);
        Permanent mount = addMountReady(player1);
        int captainPower = gqs.getEffectivePower(gd, captain);
        int captainToughness = gqs.getEffectiveToughness(gd, captain);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(captain.isTapped()).isTrue();
        assertThat(mount.isSaddled()).isTrue();
        assertThat(mount.isTapped()).isFalse();
        assertThat(gqs.getEffectivePower(gd, captain)).isEqualTo(captainPower);
        assertThat(gqs.getEffectiveToughness(gd, captain)).isEqualTo(captainToughness);
    }

    @Test
    void crewBonusDoesNotIncreaseOrdinaryPowerAndWorksWithSummoningSickness() {
        Permanent captain = harness.addToBattlefieldAndReturn(player1, new CloudspireCaptain());
        captain.setSummoningSick(true);
        Permanent vehicle = addVehicleReady(player1);
        int captainPower = gqs.getEffectivePower(gd, captain);
        int captainToughness = gqs.getEffectiveToughness(gd, captain);

        harness.activateAbility(player1, 1, null, null);
        harness.passBothPriorities();

        assertThat(captain.isTapped()).isTrue();
        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(gqs.getEffectivePower(gd, captain)).isEqualTo(captainPower);
        assertThat(gqs.getEffectiveToughness(gd, captain)).isEqualTo(captainToughness);
    }

    @Test
    void anthemStacksAndEndsWhenCaptainLeaves() {
        Permanent mount = addMountReady(player1);
        Permanent vehicle = addVehicleReady(player1);
        Permanent opposingVehicle = addVehicleReady(player2);
        int mountPower = gqs.getEffectivePower(gd, mount);
        int mountToughness = gqs.getEffectiveToughness(gd, mount);
        int vehiclePower = gqs.getEffectivePower(gd, vehicle);
        int vehicleToughness = gqs.getEffectiveToughness(gd, vehicle);
        int opposingPower = gqs.getEffectivePower(gd, opposingVehicle);
        int opposingToughness = gqs.getEffectiveToughness(gd, opposingVehicle);
        Permanent firstCaptain = addCreatureReady(player1, new CloudspireCaptain());
        Permanent secondCaptain = addCreatureReady(player1, new CloudspireCaptain());
        int captainPower = gqs.getEffectivePower(gd, firstCaptain);
        int captainToughness = gqs.getEffectiveToughness(gd, firstCaptain);

        assertThat(gqs.getEffectivePower(gd, mount)).isEqualTo(mountPower + 2);
        assertThat(gqs.getEffectiveToughness(gd, mount)).isEqualTo(mountToughness + 2);
        assertThat(gqs.getEffectivePower(gd, vehicle)).isEqualTo(vehiclePower + 2);
        assertThat(gqs.getEffectiveToughness(gd, vehicle)).isEqualTo(vehicleToughness + 2);
        assertThat(gqs.getEffectivePower(gd, opposingVehicle)).isEqualTo(opposingPower);
        assertThat(gqs.getEffectiveToughness(gd, opposingVehicle)).isEqualTo(opposingToughness);

        gd.playerBattlefields.get(player1.getId()).remove(secondCaptain);
        assertThat(gqs.getEffectivePower(gd, mount)).isEqualTo(mountPower + 1);
        assertThat(gqs.getEffectiveToughness(gd, vehicle)).isEqualTo(vehicleToughness + 1);
        assertThat(gqs.getEffectivePower(gd, firstCaptain)).isEqualTo(captainPower);
        assertThat(gqs.getEffectiveToughness(gd, firstCaptain)).isEqualTo(captainToughness);

        gd.playerBattlefields.get(player1.getId()).remove(firstCaptain);
        assertThat(gqs.getEffectivePower(gd, mount)).isEqualTo(mountPower);
        assertThat(gqs.getEffectiveToughness(gd, mount)).isEqualTo(mountToughness);
        assertThat(gqs.getEffectivePower(gd, vehicle)).isEqualTo(vehiclePower);
        assertThat(gqs.getEffectiveToughness(gd, vehicle)).isEqualTo(vehicleToughness);
    }

    private Permanent addMountReady(Player player) {
        return addCreatureReady(player, new BrightfieldGlider());
    }

    private Permanent addVehicleReady(Player player) {
        Permanent permanent = harness.addToBattlefieldAndReturn(player, new LumberingWorldwagon());
        permanent.setSummoningSick(false);
        return permanent;
    }
}
