package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.c.CityOfTraitors;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpikeHatcher.class, RagingGoblin.class, CityOfTraitors.class})
class SpikeHatcherTest extends BaseCardTest {

    @Test
    @DisplayName("Enters with six +1/+1 counters")
    void entersWithSixPlusOnePlusOneCounters() {
        harness.setHand(player1, List.of(new SpikeHatcher()));
        harness.addMana(player1, ManaColor.GREEN, 7);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent spikeHatcher = findPermanent(player1, "Spike Hatcher");
        assertThat(spikeHatcher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    @DisplayName("Removes a counter to put one on target creature")
    void movesCounterToTargetCreature() {
        Permanent spikeHatcher = addReadySpikeHatcher(6);
        Permanent goblin = addCreatureReady(player1, new RagingGoblin());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, battlefieldIndex(spikeHatcher), 0, null, goblin.getId());
        assertThat(spikeHatcher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);

        harness.passBothPriorities();

        assertThat(goblin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Counter ability can target an opposing creature")
    void counterAbilityCanTargetOpposingCreature() {
        Permanent spikeHatcher = addReadySpikeHatcher(6);
        Permanent goblin = addCreatureReady(player2, new RagingGoblin());
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.activateAbility(player1, battlefieldIndex(spikeHatcher), 0, null, goblin.getId());
        harness.passBothPriorities();

        assertThat(spikeHatcher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(goblin.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Removes a counter to regenerate itself")
    void regeneratesItself() {
        Permanent spikeHatcher = addReadySpikeHatcher(6);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, battlefieldIndex(spikeHatcher), 1, null, null);
        harness.passBothPriorities();

        assertThat(spikeHatcher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
        assertThat(spikeHatcher.getRegenerationShield()).isEqualTo(1);
    }

    @Test
    @DisplayName("Regeneration shield prevents lethal damage")
    void regenerationShieldPreventsLethalDamage() {
        Permanent spikeHatcher = addReadySpikeHatcher(6);
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, battlefieldIndex(spikeHatcher), 1, null, null);
        harness.passBothPriorities();

        spikeHatcher.setMarkedDamage(5);
        harness.runStateBasedActions();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(spikeHatcher);
        assertThat(spikeHatcher.getMarkedDamage()).isZero();
        assertThat(spikeHatcher.getRegenerationShield()).isZero();
        assertThat(spikeHatcher.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Counter ability cannot target a noncreature permanent")
    void cannotTargetNoncreaturePermanent() {
        Permanent spikeHatcher = addReadySpikeHatcher(6);
        Permanent cityOfTraitors = harness.addToBattlefieldAndReturn(player1, new CityOfTraitors());
        harness.addMana(player1, ManaColor.GREEN, 2);

        assertThatThrownBy(() -> harness.activateAbility(
                player1, battlefieldIndex(spikeHatcher), 0, null, cityOfTraitors.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    private Permanent addReadySpikeHatcher(int counters) {
        Permanent spikeHatcher = addCreatureReady(player1, new SpikeHatcher());
        spikeHatcher.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, counters);
        return spikeHatcher;
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
