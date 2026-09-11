package com.github.laxika.magicalvibes.cards.m;

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

@CardUsed({MagmaPummeler.class, Shock.class})
class MagmaPummelerTest extends BaseCardTest {

    @Test
    @DisplayName("Casting with X=3 enters with three +1/+1 counters")
    void entersWithXCounters() {
        harness.setHand(player1, List.of(new MagmaPummeler()));
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        Permanent pummeler = findPummeler(player1);
        assertThat(pummeler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Prevents damage, removes matching counters, and deals that much damage to a target")
    void preventsDamageAndDealsRemovedAmount() {
        Permanent pummeler = harness.addToBattlefieldAndReturn(player2, new MagmaPummeler());
        pummeler.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setLife(player1, 20);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, pummeler.getId());
        harness.passBothPriorities();

        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        assertThat(findPummeler(player2).getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(findPummeler(player2).getMarkedDamage()).isZero();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("The reflexive damage still resolves after all counters are removed")
    void reflexiveDamageResolvesAfterPummelerDies() {
        Permanent pummeler = harness.addToBattlefieldAndReturn(player2, new MagmaPummeler());
        pummeler.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.setLife(player1, 20);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, pummeler.getId());
        harness.passBothPriorities();

        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        harness.assertInGraveyard(player2, "Magma Pummeler");
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
    }

    private Permanent findPummeler(com.github.laxika.magicalvibes.model.Player player) {
        return gd.playerBattlefields.get(player.getId()).stream()
                .filter(p -> p.getCard() instanceof MagmaPummeler)
                .findFirst()
                .orElseThrow();
    }
}
