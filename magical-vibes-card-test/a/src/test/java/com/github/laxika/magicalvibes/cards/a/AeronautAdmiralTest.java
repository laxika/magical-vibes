package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.c.ConsulateDreadnought;
import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AeronautAdmiral.class, DuskLegionDreadnought.class, GrizzlyBears.class,
        ConsulateDreadnought.class, AethergeodeMiner.class})
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

    @Test
    @DisplayName("Vehicles lose granted flying when the Admiral dies")
    void vehiclesLoseFlyingWhenAdmiralDies() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new ConsulateDreadnought());
        Permanent admiral = harness.addToBattlefieldAndReturn(player1, new AeronautAdmiral());
        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.FLYING)).isTrue();

        admiral.setMarkedDamage(1);
        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Aeronaut Admiral");
        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.FLYING)).isFalse();
    }

    @Test
    @DisplayName("One remaining Admiral keeps granting flying")
    void remainingAdmiralKeepsGrantingFlying() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new ConsulateDreadnought());
        Permanent firstAdmiral = harness.addToBattlefieldAndReturn(player1, new AeronautAdmiral());
        harness.addToBattlefield(player1, new AeronautAdmiral());

        firstAdmiral.setMarkedDamage(1);
        harness.runStateBasedActions();

        harness.assertInGraveyard(player1, "Aeronaut Admiral");
        assertThat(countPermanents(player1, "Aeronaut Admiral")).isEqualTo(1);
        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.FLYING)).isTrue();
    }

    @Test
    @DisplayName("A Vehicle retains granted flying when crewed")
    void crewedVehicleHasFlying() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new ConsulateDreadnought());
        harness.addToBattlefield(player1, new AeronautAdmiral());
        harness.addToBattlefield(player1, new AethergeodeMiner());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(gqs.hasKeyword(gd, vehicle, Keyword.FLYING)).isTrue();
    }
}
