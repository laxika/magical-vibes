package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RenegadeKrasisTest extends BaseCardTest {

    @Test
    @DisplayName("Evolving puts a +1/+1 counter on each other creature you control that has one")
    void evolveTriggerBoostsOtherCounterBearers() {
        Permanent krasis = harness.addToBattlefieldAndReturn(player1, new RenegadeKrasis());
        Permanent countered = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        Permanent uncountered = harness.addToBattlefieldAndReturn(player1, new HillGiant());
        Permanent opponentCountered = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        countered.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        opponentCountered.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.setHand(player1, List.of(new AirElemental()));
        harness.addMana(player1, ManaColor.BLUE, 5);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(krasis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(countered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(uncountered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentCountered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("No evolve, no trigger")
    void noEvolveNoTrigger() {
        Permanent krasis = harness.addToBattlefieldAndReturn(player1, new RenegadeKrasis());
        Permanent countered = harness.addToBattlefieldAndReturn(player1, new GrizzlyBears());
        countered.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(krasis.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(countered.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }
}
