package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.d.DutifulThrull;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class CloudfinRaptorTest extends BaseCardTest {

    @Test
    @DisplayName("Evolve puts a +1/+1 counter on Cloudfin Raptor when power is greater")
    void evolvesWhenEnteringCreatureHasGreaterPower() {
        Permanent raptor = harness.addToBattlefieldAndReturn(player1, new CloudfinRaptor());

        harness.setHand(player1, List.of(new DutifulThrull()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(raptor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Evolve does not trigger when the entering creature has equal power and toughness")
    void doesNotEvolveForEqualStats() {
        Permanent raptor = harness.addToBattlefieldAndReturn(player1, new CloudfinRaptor());

        harness.setHand(player1, List.of(new CloudfinRaptor()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(raptor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
