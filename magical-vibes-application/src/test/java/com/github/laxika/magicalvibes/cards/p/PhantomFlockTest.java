package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhantomFlock.class, GrizzlyBears.class, Shock.class})
class PhantomFlockTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with three +1/+1 counters")
    void entersWithThreeCounters() {
        harness.setHand(player1, List.of(new PhantomFlock()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent flock = findFlock(player1);
        assertThat(flock).isNotNull();
        assertThat(flock.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Prevents Shock damage and removes one +1/+1 counter")
    void preventsShockDamageAndRemovesOneCounter() {
        Permanent flock = addCreatureReady(player2, new PhantomFlock());
        flock.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, flock.getId());
        harness.passBothPriorities();

        assertThat(flock.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(flock.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Combat damage is prevented and removes one +1/+1 counter")
    void preventsCombatDamageAndRemovesOneCounter() {
        Permanent blocker = addCreatureReady(player2, new PhantomFlock());
        blocker.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(blocker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    private Permanent findFlock(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof PhantomFlock)
                .findFirst()
                .orElse(null);
    }
}
