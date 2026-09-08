package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EffortlessMaster.class, LightningBolt.class})
class EffortlessMasterTest extends BaseCardTest {

    @Test
    void countsItselfAsTheSecondSpellThisTurn() {
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        castEffortlessMaster();

        assertThat(findEffortlessMaster().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void doesNotEnterWithCountersAsTheFirstSpellThisTurn() {
        castEffortlessMaster();

        assertThat(findEffortlessMaster().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void castEffortlessMaster() {
        harness.setHand(player1, List.of(new EffortlessMaster()));
        harness.addMana(player1, ManaColor.RED, 3);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }

    private Permanent findEffortlessMaster() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof EffortlessMaster)
                .findFirst()
                .orElseThrow();
    }
}
