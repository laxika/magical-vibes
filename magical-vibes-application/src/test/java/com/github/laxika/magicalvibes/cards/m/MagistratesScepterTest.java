package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class MagistratesScepterTest extends BaseCardTest {

    @Test
    @DisplayName("First ability puts a charge counter on Magistrate's Scepter")
    void firstAbilityPutsChargeCounter() {
        Permanent scepter = addReadyScepter();

        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.activateAbility(player1, 0, 0, null, null);
        harness.passBothPriorities();

        assertThat(scepter.getCounterCount(CounterType.CHARGE)).isEqualTo(1);
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

    private Permanent addReadyScepter() {
        harness.addToBattlefield(player1, new MagistratesScepter());
        return findPermanent(player1, "Magistrate's Scepter");
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
