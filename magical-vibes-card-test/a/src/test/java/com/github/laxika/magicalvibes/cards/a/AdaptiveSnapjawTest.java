package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.r.RuinationWurm;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AdaptiveSnapjaw.class, GrizzlyBears.class, HillGiant.class, RuinationWurm.class})
class AdaptiveSnapjawTest extends BaseCardTest {

    @Test
    @DisplayName("Evolve triggers on greater toughness alone — a 3/3 beats the Snapjaw's toughness 2")
    void evolvesForGreaterToughness() {
        Permanent snapjaw = harness.addToBattlefieldAndReturn(player1, new AdaptiveSnapjaw());

        harness.castFromHand(player1, new HillGiant(), "{3}{R}");
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(snapjaw.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Evolve does not trigger for a 2/2 — lower power and equal toughness")
    void doesNotEvolveForSmallerCreature() {
        Permanent snapjaw = harness.addToBattlefieldAndReturn(player1, new AdaptiveSnapjaw());

        harness.castFromHand(player1, new GrizzlyBears(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(snapjaw.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Evolve does not trigger for a creature entering under an opponent's control")
    void doesNotEvolveForOpponentCreature() {
        Permanent snapjaw = harness.addToBattlefieldAndReturn(player1, new AdaptiveSnapjaw());

        harness.forceActivePlayer(player2);
        harness.castFromHand(player2, new HillGiant(), "{3}{R}");
        harness.passBothPriorities();

        assertThat(snapjaw.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    @DisplayName("Evolve triggers for greater power with equal toughness")
    void evolvesForGreaterPowerOnly() {
        Permanent snapjaw = harness.addToBattlefieldAndReturn(player1, new AdaptiveSnapjaw());
        snapjaw.setToughnessModifier(4);

        harness.castFromHand(player1, new RuinationWurm(), "{4}{R}{G}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(snapjaw.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Evolve checks the source's current size again on resolution")
    void doesNotEvolveWhenSourceGrowsBeforeResolution() {
        Permanent snapjaw = harness.addToBattlefieldAndReturn(player1, new AdaptiveSnapjaw());

        harness.castFromHand(player1, new HillGiant(), "{3}{R}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        snapjaw.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.passBothPriorities();

        assertThat(snapjaw.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Evolve uses the entering creature's power just before it leaves")
    void usesLastKnownPowerWhenEnteringCreatureLeaves() {
        Permanent snapjaw = harness.addToBattlefieldAndReturn(player1, new AdaptiveSnapjaw());
        snapjaw.setToughnessModifier(4);

        harness.castFromHand(player1, new RuinationWurm(), "{4}{R}{G}");
        harness.passBothPriorities();
        assertThat(gd.stack).hasSize(1);
        Permanent wurm = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard() instanceof RuinationWurm)
                .findFirst().orElseThrow();
        wurm.setPowerModifier(-2);
        harness.inMutationScope(() -> harness.getPermanentRemovalService().removePermanentToHand(gd, wurm));
        harness.passBothPriorities();

        assertThat(snapjaw.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.stack).isEmpty();
    }
}
