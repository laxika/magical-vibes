package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed(MagistratesScepter.class)
class MagistratesScepterTest extends BaseCardTest {

    @Test
    @DisplayName("First ability puts a charge counter on Magistrate's Scepter")
    void firstAbilityPutsChargeCounter() {
        Permanent scepter = addReadyScepter();

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(scepter.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(scepter.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Second ability removes three charge counters and queues an extra turn")
    void secondAbilityQueuesExtraTurn() {
        Permanent scepter = addReadyScepter();
        scepter.setCounterCount(CounterType.CHARGE, 3);
        enableAutoStopAtPrecombatMain();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(scepter.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(gd.extraTurns).containsExactly(player1.getId());
    }

    @Test
    @DisplayName("Second ability cannot be activated with fewer than three charge counters")
    void secondAbilityNeedsThreeChargeCounters() {
        Permanent scepter = addReadyScepter();
        scepter.setCounterCount(CounterType.CHARGE, 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, null))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("First ability cannot be activated without four mana")
    void firstAbilityNeedsFourMana() {
        Permanent scepter = addReadyScepter();
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, null))
                .isInstanceOf(IllegalStateException.class);

        assertThat(scepter.getCounterCount(CounterType.CHARGE)).isZero();
        assertThat(scepter.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Second ability removes exactly three charge counters")
    void secondAbilityRemovesExactlyThreeChargeCounters() {
        Permanent scepter = addReadyScepter();
        scepter.setCounterCount(CounterType.CHARGE, 4);
        enableAutoStopAtPrecombatMain();

        harness.activateAbility(player1, 0, 1, null, null);
        harness.passBothPriorities();

        assertThat(scepter.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
        assertThat(scepter.isTapped()).isTrue();
        assertThat(gd.extraTurns).containsExactly(player1.getId());
    }

    private Permanent addReadyScepter() {
        return harness.addToBattlefieldAndReturn(player1, new MagistratesScepter());
    }

    private void enableAutoStopAtPrecombatMain() {
        Set<TurnStep> stops = ConcurrentHashMap.newKeySet();
        stops.add(TurnStep.PRECOMBAT_MAIN);
        gd.playerAutoStopSteps.put(player1.getId(), stops);
        Set<TurnStep> opponentStops = ConcurrentHashMap.newKeySet();
        opponentStops.add(TurnStep.PRECOMBAT_MAIN);
        gd.playerAutoStopSteps.put(player2.getId(), opponentStops);
    }
}
