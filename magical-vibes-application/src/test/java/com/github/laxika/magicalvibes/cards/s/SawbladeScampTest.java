package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LightningBolt;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SawbladeScampTest extends BaseCardTest {

    @Test
    void castingANoncreatureSpellPutsAnOilCounterOnSawbladeScamp() {
        Permanent scamp = addReadyScamp();
        harness.setHand(player1, List.of(new LightningBolt()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(scamp.getCounterCount(CounterType.OIL)).isEqualTo(1);
    }

    @Test
    void castingACreatureSpellDoesNotPutAnOilCounterOnSawbladeScamp() {
        Permanent scamp = addReadyScamp();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castCreature(player1, 0);

        assertThat(scamp.getCounterCount(CounterType.OIL)).isZero();
    }

    @Test
    void removingAnOilCounterDealsDamageToEachOpponent() {
        Permanent scamp = addReadyScamp();
        scamp.setCounterCount(CounterType.OIL, 1);
        harness.setLife(player2, 20);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(scamp.getCounterCount(CounterType.OIL)).isZero();
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    void cannotActivateWithoutAnOilCounter() {
        Permanent scamp = addReadyScamp();

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    private Permanent addReadyScamp() {
        Permanent scamp = addCreatureReady(player1, new SawbladeScamp());
        scamp.setCounterCount(CounterType.OIL, 0);
        return scamp;
    }
}
