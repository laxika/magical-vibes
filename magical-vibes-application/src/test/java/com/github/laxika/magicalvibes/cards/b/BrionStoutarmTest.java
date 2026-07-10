package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BrionStoutarmTest extends BaseCardTest {

    @Test
    @DisplayName("Deals damage equal to sacrificed creature's power to target player")
    void dealsSacrificedPowerToPlayer() {
        addReadyBrion(player1);
        addCreatureReady(player1, new GrizzlyBears()); // 2/2
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        harness.assertInGraveyard(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Uses the sacrificed creature's boosted (effective) power")
    void usesBoostedPower() {
        addReadyBrion(player1);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1); // power becomes 3
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Taps Brion and consumes the red mana as part of the cost")
    void tapsAndConsumesMana() {
        Permanent brion = addReadyBrion(player1);
        addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        harness.activateAbility(player1, 0, null, player2.getId());

        assertThat(brion.isTapped()).isTrue();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isZero();
    }

    @Test
    @DisplayName("Cannot activate when Brion is the only creature (excludeSelf)")
    void cannotSacrificeItself() {
        addReadyBrion(player1);
        harness.addMana(player1, ManaColor.RED, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects a creature as the damage target")
    void rejectsCreatureTarget() {
        addReadyBrion(player1);
        addCreatureReady(player1, new GrizzlyBears());
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.addMana(player1, ManaColor.RED, 1);

        UUID creatureTarget = harness.getPermanentId(player2, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, creatureTarget))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyBrion(Player player) {
        return addCreatureReady(player, new BrionStoutarm());
    }
}
