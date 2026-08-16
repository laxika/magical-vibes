package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class TrufflesnoutTest extends BaseCardTest {

    @Test
    void entersWithPlusOnePlusOneCounterWhenThatModeIsChosen() {
        harness.setHand(player1, List.of(new Trufflesnout()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        Permanent trufflesnout = gd.playerBattlefields.get(player1.getId()).getFirst();
        assertThat(trufflesnout.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void gainsFourLifeWhenThatModeIsChosen() {
        harness.setLife(player1, 10);
        harness.setHand(player1, List.of(new Trufflesnout()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.castCreature(player1, 0, 1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        harness.assertLife(player1, 14);
    }
}
