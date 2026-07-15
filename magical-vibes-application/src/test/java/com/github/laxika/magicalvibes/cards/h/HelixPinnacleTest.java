package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class HelixPinnacleTest extends BaseCardTest {

    @Test
    @DisplayName("{X} ability puts X tower counters on Helix Pinnacle")
    void abilityPutsXTowerCounters() {
        Permanent pinnacle = harness.addToBattlefieldAndReturn(player1, new HelixPinnacle());
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.activateAbility(player1, 0, 3, null);
        harness.passBothPriorities();

        assertThat(pinnacle.getCounterCount(CounterType.TOWER)).isEqualTo(3);
    }

    @Test
    @DisplayName("Tower counters accumulate across activations")
    void towerCountersAccumulate() {
        Permanent pinnacle = harness.addToBattlefieldAndReturn(player1, new HelixPinnacle());

        harness.addMana(player1, ManaColor.GREEN, 4);
        harness.activateAbility(player1, 0, 4, null);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.activateAbility(player1, 0, 5, null);
        harness.passBothPriorities();

        assertThat(pinnacle.getCounterCount(CounterType.TOWER)).isEqualTo(9);
    }

    @Test
    @DisplayName("Wins the game at upkeep with exactly 100 tower counters")
    void winsWithExactlyOneHundredCounters() {
        Permanent pinnacle = harness.addToBattlefieldAndReturn(player1, new HelixPinnacle());
        pinnacle.setCounterCount(CounterType.TOWER, 100);

        advanceToUpkeep(player1);

        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities(); // resolve trigger

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(l -> l.contains("wins the game"));
    }

    @Test
    @DisplayName("Does not trigger at upkeep with fewer than 100 tower counters")
    void doesNotTriggerBelowOneHundred() {
        Permanent pinnacle = harness.addToBattlefieldAndReturn(player1, new HelixPinnacle());
        pinnacle.setCounterCount(CounterType.TOWER, 99);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Does not trigger during opponent's upkeep")
    void doesNotTriggerOnOpponentsUpkeep() {
        Permanent pinnacle = harness.addToBattlefieldAndReturn(player1, new HelixPinnacle());
        pinnacle.setCounterCount(CounterType.TOWER, 100);

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    private void advanceToUpkeep(Player activePlayer) {
        harness.forceActivePlayer(activePlayer);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advances to UPKEEP
    }
}
