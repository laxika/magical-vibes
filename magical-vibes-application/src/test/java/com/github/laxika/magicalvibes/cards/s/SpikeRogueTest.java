package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class SpikeRogueTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with two +1/+1 counters")
    void entersWithTwoPlusOnePlusOneCounters() {
        harness.setHand(player1, List.of(new SpikeRogue()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent spikeRogue = findPermanent(player1, "Spike Rogue");
        assertThat(spikeRogue.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    @DisplayName("Removes a counter to put one on target creature")
    void movesCounterToTargetCreature() {
        Permanent spikeRogue = addReadySpikeRogue(2);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, battlefieldIndex(spikeRogue), 0, null, bears.getId());
        assertThat(spikeRogue.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);

        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removes a counter from a creature you control to put one on itself")
    void movesCounterFromControlledCreatureToItself() {
        Permanent spikeRogue = addReadySpikeRogue(2);
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, battlefieldIndex(spikeRogue), 1, null, null);
        harness.handlePermanentChosen(player1, bears.getId());
        harness.passBothPriorities();

        assertThat(bears.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(spikeRogue.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Target ability cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent spikeRogue = addReadySpikeRogue(2);
        Permanent forest = harness.addToBattlefieldAndReturn(player1, new Forest());
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(spikeRogue), 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addReadySpikeRogue(int counters) {
        Permanent spikeRogue = addCreatureReady(player1, new SpikeRogue());
        spikeRogue.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counters);
        return spikeRogue;
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
