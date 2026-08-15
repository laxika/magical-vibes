package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HeroesBaneTest extends BaseCardTest {

    @Test
    @DisplayName("Heroes' Bane enters with four +1/+1 counters")
    void entersWithFourPlusOneCounters() {
        castHeroesBane();

        Permanent bane = getBane();
        assertThat(bane.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
        assertThat(bane.getEffectivePower()).isEqualTo(4);
        assertThat(bane.getEffectiveToughness()).isEqualTo(4);
    }

    @Test
    @DisplayName("The activated ability adds counters equal to current power")
    void addsCountersEqualToCurrentPower() {
        castHeroesBane();
        harness.addMana(player1, ManaColor.GREEN, 8);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(getBane().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(8);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        assertThat(getBane().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(16);
    }

    private void castHeroesBane() {
        harness.setHand(player1, List.of(new HeroesBane()));
        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent getBane() {
        return harness.getGameData().playerBattlefields.get(player1.getId()).getFirst();
    }
}
