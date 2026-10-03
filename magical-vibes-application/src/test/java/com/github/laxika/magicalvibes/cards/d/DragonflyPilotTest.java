package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.f.FellFlagship;
import com.github.laxika.magicalvibes.cards.t.TrainedArynx;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DragonflyPilot.class, DragonflySuit.class, FellFlagship.class, TrainedArynx.class})
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

    @Test
    void eachEntryConjuresExactlyOneSuitForItsController() {
        harness.enterBattlefieldAndReturn(player2, new DragonflyPilot());
        resolveAllTriggers();
        harness.enterBattlefieldAndReturn(player2, new DragonflyPilot());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId()))
                .filteredOn(card -> "Dragonfly Suit".equals(card.getName()))
                .hasSize(2);
        harness.assertNotInHand(player1, "Dragonfly Suit");
    }

    @Test
    void summoningSickPilotCanCrewVehicle() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new DragonflySuit());
        Permanent pilot = harness.enterBattlefieldAndReturn(player1, new DragonflyPilot());
        resolveAllTriggers();

        harness.activateAbility(player1, gd.playerBattlefields.get(player1.getId()).indexOf(vehicle), null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
        assertThat(pilot.isTapped()).isTrue();
    }

    @Test
    void crewBonusDoesNotHelpPaySaddleCosts() {
        Permanent mount = harness.addToBattlefieldAndReturn(player1, new TrainedArynx());
        Permanent pilot = harness.addToBattlefieldAndReturn(player1, new DragonflyPilot());

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(mount), null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Not enough creature power");
        assertThat(pilot.isTapped()).isFalse();
        assertThat(mount.isSaddled()).isFalse();
    }
}
