package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GoblinBerserker;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BloodshotCyclops.class, GoblinBerserker.class})
class BloodshotCyclopsTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to sacrificed creature's power to target player")
    void dealsSacrificedPowerToPlayer() {
        addReadyCyclops(player1);
        addCreatureReady(player1, new GoblinBerserker()); // 2/2
        UUID berserker = harness.getPermanentId(player1, "Goblin Berserker");
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, berserker);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Goblin Berserker");
    }

    @Test
    @DisplayName("Uses the sacrificed creature's boosted (effective) power")
    void usesBoostedPower() {
        addReadyCyclops(player1);
        Permanent berserker = addCreatureReady(player1, new GoblinBerserker());
        berserker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1); // power becomes 3
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, berserker.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Deals damage equal to sacrificed power to a target creature, killing it")
    void dealsSacrificedPowerToCreature() {
        addReadyCyclops(player1);
        addCreatureReady(player1, new GoblinBerserker()); // sacrificed, power 2
        UUID berserker = harness.getPermanentId(player1, "Goblin Berserker");
        harness.addToBattlefield(player2, new GoblinBerserker()); // 2/2 victim
        UUID victim = harness.getPermanentId(player2, "Goblin Berserker");

        harness.activateAbility(player1, 0, null, victim);
        harness.handlePermanentChosen(player1, berserker);
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Goblin Berserker");
    }

    @Test
    @DisplayName("Taps the Cyclops and sacrifices the creature as part of the cost")
    void tapsAndSacrificesAsCost() {
        Permanent cyclops = addReadyCyclops(player1);
        addCreatureReady(player1, new GoblinBerserker());
        UUID berserker = harness.getPermanentId(player1, "Goblin Berserker");

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.handlePermanentChosen(player1, berserker);

        assertThat(cyclops.isTapped()).isTrue();
        harness.assertInGraveyard(player1, "Goblin Berserker");
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
