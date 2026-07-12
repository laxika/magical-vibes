package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class BloodshotCyclopsTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to sacrificed creature's power to target player")
    void dealsSacrificedPowerToPlayer() {
        addReadyCyclops(player1);
        addCreatureReady(player1, new GrizzlyBears()); // 2/2
        UUID bears = harness.getPermanentId(player1, "Grizzly Bears");
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, bears);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Uses the sacrificed creature's boosted (effective) power")
    void usesBoostedPower() {
        addReadyCyclops(player1);
        Permanent bearsPerm = addCreatureReady(player1, new GrizzlyBears());
        bearsPerm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1); // power becomes 3
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, bearsPerm.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Deals damage equal to sacrificed power to a target creature, killing it")
    void dealsSacrificedPowerToCreature() {
        addReadyCyclops(player1);
        addCreatureReady(player1, new GrizzlyBears()); // sacrificed, power 2
        UUID bears = harness.getPermanentId(player1, "Grizzly Bears");
        harness.addToBattlefield(player2, new GrizzlyBears()); // 2/2 victim
        UUID victim = harness.getPermanentId(player2, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, victim);
        harness.handlePermanentChosen(player1, bears);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Taps the Cyclops and sacrifices the creature as part of the cost")
    void tapsAndSacrificesAsCost() {
        Permanent cyclops = addReadyCyclops(player1);
        addCreatureReady(player1, new GrizzlyBears());
        UUID bears = harness.getPermanentId(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, bears);

        assertThat(cyclops.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Can sacrifice itself, dealing its own power to the target")
    void canSacrificeItself() {
        addReadyCyclops(player1); // 4/4, the only creature available
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        harness.assertInGraveyard(player1, "Bloodshot Cyclops");
    }

    private Permanent addReadyCyclops(Player player) {
        return addCreatureReady(player, new BloodshotCyclops());
    }
}
