package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.DuskLegionDreadnought;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpikeshellHarrierTest extends BaseCardTest {

    @Test
    void returnsTargetCreatureAndReducesItsLeadingControllersSpeed() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.playerSpeeds.put(player1.getId(), 2);
        gd.playerSpeeds.put(player2.getId(), 4);

        castHarrier(target.getId());

        harness.assertInHand(player2, "Grizzly Bears");
        assertThat(gd.playerSpeeds.get(player2.getId())).isEqualTo(3);
        harness.assertOnBattlefield(player1, "Spikeshell Harrier");
    }

    @Test
    void returnsTargetVehicleWithoutReducingSpeedWhenItIsNotLeading() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new DuskLegionDreadnought());
        gd.playerSpeeds.put(player1.getId(), 4);
        gd.playerSpeeds.put(player2.getId(), 4);

        castHarrier(target.getId());

        harness.assertInHand(player2, "Dusk Legion Dreadnought");
        assertThat(gd.playerSpeeds.get(player2.getId())).isEqualTo(4);
    }

    @Test
    void doesNotReduceSpeedBelowOne() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        gd.playerSpeeds.put(player1.getId(), 0);
        gd.playerSpeeds.put(player2.getId(), 1);

        castHarrier(target.getId());

        assertThat(gd.playerSpeeds.get(player2.getId())).isEqualTo(1);
        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    void cannotTargetOwnCreature() {
        Permanent target = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        harness.setHand(player1, List.of(new SpikeshellHarrier()));
        harness.addMana(player1, ManaColor.BLUE, 5);

        assertThatThrownBy(() -> harness.castCreature(player1, 0, 0, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void castHarrier(UUID targetId) {
        harness.setHand(player1, List.of(new SpikeshellHarrier()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castCreature(player1, 0, 0, targetId);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
