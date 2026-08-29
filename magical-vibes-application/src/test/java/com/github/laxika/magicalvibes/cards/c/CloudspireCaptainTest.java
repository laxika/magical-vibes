package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BrightfieldGlider;
import com.github.laxika.magicalvibes.cards.c.ConquerorsGalleon;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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

    private Permanent addMountReady(Player player) {
        return addCreatureReady(player, new BrightfieldGlider());
    }

    private Permanent addVehicleReady(Player player) {
        Permanent permanent = new Permanent(new ConquerorsGalleon());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
