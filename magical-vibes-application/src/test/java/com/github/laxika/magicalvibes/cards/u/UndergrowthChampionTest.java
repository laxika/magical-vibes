package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({UndergrowthChampion.class, Forest.class, Shock.class})
class UndergrowthChampionTest extends BaseCardTest {

    @Test
    @DisplayName("Landfall puts a +1/+1 counter on Undergrowth Champion")
    void landfallPutsCounterOnSelf() {
        Permanent champion = harness.addToBattlefieldAndReturn(player1, new UndergrowthChampion());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Damage is prevented and removes only one +1/+1 counter while countered")
    void damageIsPreventedAndRemovesOneCounter() {
        Permanent champion = harness.addToBattlefieldAndReturn(player2, new UndergrowthChampion());
        champion.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, champion.getId());
        harness.passBothPriorities();

        assertThat(champion.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(champion.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player2, "Undergrowth Champion");
    }

    @Test
    @DisplayName("Damage is dealt normally without a +1/+1 counter")
    void damageIsNotPreventedWithoutCounter() {
        UndergrowthChampion card = new UndergrowthChampion();
        card.setToughness(3);
        Permanent champion = harness.addToBattlefieldAndReturn(player2, card);
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, champion.getId());
        harness.passBothPriorities();

        assertThat(champion.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player2, "Undergrowth Champion");
    }
}
