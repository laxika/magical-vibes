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

@CardUsed({PhantomNantuko.class, GrizzlyBears.class, Shock.class})
class PhantomNantukoTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two +1/+1 counters")
    void entersWithTwoCounters() {
        harness.setHand(player1, List.of(new PhantomNantuko()));
        harness.addMana(player1, ManaColor.GREEN, 4);

        gs.playCard(gd, player1, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent nantuko = findNantuko(player1);
        assertThat(nantuko).isNotNull();
        assertThat(nantuko.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Prevents Shock damage and removes one +1/+1 counter")
    void preventsShockDamageAndRemovesOneCounter() {
        Permanent nantuko = addCreatureReady(player2, new PhantomNantuko());
        nantuko.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, nantuko.getId());
        harness.passBothPriorities();

        assertThat(nantuko.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nantuko.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Tap ability adds a +1/+1 counter")
    void tapAbilityAddsCounter() {
        Permanent nantuko = addCreatureReady(player1, new PhantomNantuko());
        nantuko.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(nantuko.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(nantuko.isTapped()).isTrue();
    }

    private Permanent findNantuko(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof PhantomNantuko)
                .findFirst()
                .orElse(null);
    }
}
