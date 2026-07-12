package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.t.TumbleMagnet;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class WoeleecherTest extends BaseCardTest {

    private void addReadyWoeleecher(com.github.laxika.magicalvibes.model.Player player) {
        Permanent woeleecher = harness.addToBattlefieldAndReturn(player, new Woeleecher());
        woeleecher.setSummoningSick(false);
    }

    @Test
    @DisplayName("Removes a -1/-1 counter from target creature and gains 2 life")
    void removesCounterAndGainsLife() {
        addReadyWoeleecher(player1);
        harness.addToBattlefield(player1, new HillGiant());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setLife(player1, 20);

        // Hill Giant (3/3) survives with two -1/-1 counters (1/1); one is removed.
        Permanent giant = findPermanent(player1, "Hill Giant");
        giant.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 2);

        harness.activateAbility(player1, 0, 0, null, giant.getId());
        harness.passBothPriorities();

        assertThat(giant.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isEqualTo(1);
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("No life gained when the target has no -1/-1 counter")
    void noLifeWhenNoCounter() {
        addReadyWoeleecher(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.setLife(player1, 20);

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        harness.activateAbility(player1, 0, 0, null, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)).isZero();
        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        addReadyWoeleecher(player1);
        harness.addToBattlefield(player2, new TumbleMagnet());
        harness.addMana(player1, ManaColor.WHITE, 1);

        Permanent magnet = findPermanent(player2, "Tumble Magnet");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, magnet.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Cannot activate without white mana")
    void cannotActivateWithoutMana() {
        addReadyWoeleecher(player1);
        harness.addToBattlefield(player1, new GrizzlyBears());

        Permanent bears = findPermanent(player1, "Grizzly Bears");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, bears.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
