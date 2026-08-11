package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ChanceEncounterTest extends BaseCardTest {

    @Test
    @DisplayName("Winning a coin flip puts a luck counter on Chance Encounter")
    void winningCoinFlipPutsLuckCounterOnChanceEncounter() {
        Permanent chanceEncounter = harness.addToBattlefieldAndReturn(player1, new ChanceEncounter());
        harness.addToBattlefield(player1, new com.github.laxika.magicalvibes.cards.s.SorcerersStrongbox());
        harness.addMana(player1, com.github.laxika.magicalvibes.model.ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 1, null, null);
        resolveAllTriggers();

        boolean wonFlip = gameLogContains("wins the coin flip for Sorcerer's Strongbox");
        assertThat(chanceEncounter.getCounterCount(CounterType.LUCK)).isEqualTo(wonFlip ? 1 : 0);
    }

    @Test
    @DisplayName("Wins the game at upkeep with ten luck counters")
    void winsWithTenLuckCounters() {
        Permanent chanceEncounter = harness.addToBattlefieldAndReturn(player1, new ChanceEncounter());
        chanceEncounter.setCounterCount(CounterType.LUCK, 10);

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Does not trigger at upkeep below ten luck counters")
    void doesNotTriggerBelowTenLuckCounters() {
        Permanent chanceEncounter = harness.addToBattlefieldAndReturn(player1, new ChanceEncounter());
        chanceEncounter.setCounterCount(CounterType.LUCK, 9);

        advanceToUpkeep(player1);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Does not trigger during an opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        Permanent chanceEncounter = harness.addToBattlefieldAndReturn(player1, new ChanceEncounter());
        chanceEncounter.setCounterCount(CounterType.LUCK, 10);

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isNotEqualTo(GameStatus.FINISHED);
    }
}
