package com.github.laxika.magicalvibes.cards.a;

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

class AdaptiveSnapjawTest extends BaseCardTest {

    @Test
    @DisplayName("Evolve triggers on greater toughness alone — a 3/3 beats the Snapjaw's toughness 2")
    void evolvesForGreaterToughness() {
        Permanent snapjaw = harness.addToBattlefieldAndReturn(player1, new AdaptiveSnapjaw());

        harness.setHand(player1, List.of(new HillGiant()));
        harness.addMana(player1, ManaColor.RED, 4);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(snapjaw.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Evolve does not trigger for a 2/2 — lower power and equal toughness")
    void doesNotEvolveForSmallerCreature() {
        Permanent snapjaw = harness.addToBattlefieldAndReturn(player1, new AdaptiveSnapjaw());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(snapjaw.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Evolve does not trigger for a creature entering under an opponent's control")
    void doesNotEvolveForOpponentCreature() {
        Permanent snapjaw = harness.addToBattlefieldAndReturn(player1, new AdaptiveSnapjaw());

        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new HillGiant()));
        harness.addMana(player2, ManaColor.RED, 4);
        harness.castCreature(player2, 0);
        harness.passBothPriorities();

        assertThat(snapjaw.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }
}
