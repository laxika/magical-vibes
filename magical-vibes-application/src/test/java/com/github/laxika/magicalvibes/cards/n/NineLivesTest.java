package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class NineLivesTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents noncombat damage and gets one incarnation counter")
    void preventsNoncombatDamageAndGetsCounter() {
        Permanent nineLives = harness.addToBattlefieldAndReturn(player1, new NineLives());
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(nineLives.getCounterCount(CounterType.INCARNATION)).isEqualTo(1);
    }

    @Test
    @DisplayName("Prevents combat damage and gets one incarnation counter per attacker")
    void preventsCombatDamageAndGetsCounter() {
        Permanent nineLives = harness.addToBattlefieldAndReturn(player1, new NineLives());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));

        assertThat(gd.getLife(player1.getId())).isEqualTo(20);
        assertThat(nineLives.getCounterCount(CounterType.INCARNATION)).isEqualTo(1);
    }

    @Test
    @DisplayName("Exiles at nine incarnation counters and its leaves trigger causes a loss")
    void exilesAtNineCountersAndLosesWhenItLeaves() {
        Permanent nineLives = harness.addToBattlefieldAndReturn(player1, new NineLives());
        nineLives.setCounterCount(CounterType.INCARNATION, 8);
        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);

        harness.castInstant(player2, 0, player1.getId());
        harness.passBothPriorities();
        assertThat(nineLives.getCounterCount(CounterType.INCARNATION)).isEqualTo(9);

        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }
}
