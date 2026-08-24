package com.github.laxika.magicalvibes.cards.u;

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

@CardUsed({UginsConjurant.class, Shock.class})
class UginsConjurantTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with X +1/+1 counters")
    void entersWithXCounters() {
        harness.setHand(player1, List.of(new UginsConjurant()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        Permanent conjurant = findPermanent(player1, "Ugin's Conjurant");
        assertThat(conjurant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
        assertThat(conjurant.getEffectivePower()).isEqualTo(3);
        assertThat(conjurant.getEffectiveToughness()).isEqualTo(3);
    }

    @Test
    @DisplayName("Damage is prevented and removes counters equal to the damage")
    void damageRemovesCountersEqualToDamage() {
        Permanent conjurant = harness.addToBattlefieldAndReturn(player2, new UginsConjurant());
        conjurant.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, conjurant.getId());
        harness.passBothPriorities();

        assertThat(conjurant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(conjurant.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Ugin's Conjurant");
    }

    @Test
    @DisplayName("Damage is dealt normally once it has no +1/+1 counters")
    void damageIsNotPreventedWithoutCounters() {
        UginsConjurant card = new UginsConjurant();
        card.setToughness(3);
        Permanent conjurant = harness.addToBattlefieldAndReturn(player2, card);

        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, conjurant.getId());
        harness.passBothPriorities();

        assertThat(conjurant.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Ugin's Conjurant");
    }
}
