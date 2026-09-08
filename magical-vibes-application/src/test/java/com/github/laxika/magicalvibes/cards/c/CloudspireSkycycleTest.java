package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.b.BoostedSloop;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CloudspireSkycycleTest extends BaseCardTest {

    @Test
    void entersAndDistributesCountersAmongCreatureAndVehicle() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new BoostedSloop());

        castCloudspireSkycycle(List.of(creature.getId(), vehicle.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.getPlusOnePlusOneCounters()).isEqualTo(1);
        assertThat(vehicle.getPlusOnePlusOneCounters()).isEqualTo(1);
    }

    @Test
    void entersAndPutsBothCountersOnOneTarget() {
        Permanent creature = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        castCloudspireSkycycle(List.of(creature.getId()));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(creature.getPlusOnePlusOneCounters()).isEqualTo(2);
    }

    @Test
    void cannotTargetOpponentsPermanent() {
        Permanent opponentCreature = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());

        assertThatThrownBy(() -> castCloudspireSkycycle(List.of(opponentCreature.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void crewAnimatesVehicle() {
        Permanent vehicle = harness.addToBattlefieldAndReturn(player1, new CloudspireSkycycle());
        vehicle.setSummoningSick(false);
        harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gqs.isCreature(gd, vehicle)).isTrue();
    }

    private void castCloudspireSkycycle(List<UUID> targetIds) {
        harness.setHand(player1, List.of(new CloudspireSkycycle()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0, targetIds);
    }
}
