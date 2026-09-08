package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PhantomWurm.class, GrizzlyBears.class, Shock.class})
class PhantomWurmTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with four +1/+1 counters")
    void entersWithFourCounters() {
        harness.setHand(player1, List.of(new PhantomWurm()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 4);
        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent wurm = findWurm(player1);
        assertThat(wurm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(wurm.getEffectivePower()).isEqualTo(6);
        assertThat(wurm.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("Prevents damage and removes one +1/+1 counter")
    void preventsDamageAndRemovesOneCounter() {
        harness.addToBattlefield(player2, new PhantomWurm());
        Permanent wurm = findWurm(player2);
        wurm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, wurm.getId());
        harness.passBothPriorities();

        assertThat(findWurm(player2)).isNotNull();
        assertThat(wurm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(wurm.getMarkedDamage()).isEqualTo(0);
    }

    @Test
    @DisplayName("Each separate damage event removes one counter")
    void eachDamageEventRemovesOneCounter() {
        harness.addToBattlefield(player2, new PhantomWurm());
        Permanent wurm = findWurm(player2);
        wurm.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.castInstant(player1, 0, wurm.getId());
        harness.passBothPriorities();
        harness.setHand(player1, List.of(new Shock()));
        harness.castInstant(player1, 0, wurm.getId());
        harness.passBothPriorities();

        assertThat(wurm.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(wurm.getMarkedDamage()).isEqualTo(0);
    }

    private Permanent findWurm(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard().getName().equals("Phantom Wurm"))
                .findFirst()
                .orElseThrow();
    }
}
