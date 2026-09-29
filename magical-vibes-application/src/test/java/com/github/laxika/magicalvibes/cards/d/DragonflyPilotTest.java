package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FellFlagship;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DragonflyPilot.class, DragonflySuit.class, FellFlagship.class})
class DragonflyPilotTest extends BaseCardTest {

    @Test
    void entersAndConjuresDragonflySuitIntoHand() {
        harness.enterBattlefieldAndReturn(player1, new DragonflyPilot());
        resolveAllTriggers();

        harness.assertInHand(player1, "Dragonfly Suit");
    }

    @Test
    void powerBonusLetsItCrewVehicleWithCrewThree() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new FellFlagship());
        Permanent pilot = harness.addToBattlefieldAndReturn(player1, new DragonflyPilot());
        resolveAllTriggers();
        pilot.setSummoningSick(false);

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(vehicle), null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(pilot.isTapped()).isTrue();
    }
}
