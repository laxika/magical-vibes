package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AeronautAdmiralTest extends BaseCardTest {

    @Test
    @DisplayName("Vehicles you control have flying")
    void vehiclesYouControlHaveFlying() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new DuskLegionDreadnought());
        harness.addToBattlefield(player1, new AeronautAdmiral());

        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("Aeronaut Admiral does not grant flying to non-Vehicles")
    void doesNotGrantFlyingToNonVehicles() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.addToBattlefield(player1, new AeronautAdmiral());

        assertThat(gqs.hasKeyword(gd, creature, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Aeronaut Admiral does not grant flying to an opponent's Vehicle")
    void doesNotGrantFlyingToOpponentsVehicle() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player2, new DuskLegionDreadnought());
        harness.addToBattlefield(player1, new AeronautAdmiral());

        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("Vehicles that enter later also have flying")
    void vehiclesEnteringLaterHaveFlying() {
        harness.addToBattlefield(player1, new AeronautAdmiral());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new DuskLegionDreadnought());

        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.FLYING)).isTrue();
    }
}
