package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.w.WickerboughElder;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HelixPinnacle.class, WickerboughElder.class})
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
        assertThat(gameLogContains("wins the game")).isTrue();
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

    @Test
    @DisplayName("X can be zero without adding counters")
    void canActivateWithZeroMana() {
        Permanent pinnacle = harness.addToBattlefieldAndReturn(player1, new HelixPinnacle());
        pinnacle.setCounterCount(CounterType.TOWER, 7);

        harness.activateAbility(player1, 0, 0, null);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(pinnacle.getCounterCount(CounterType.TOWER)).isEqualTo(7);
    }

    @Test
    @DisplayName("The X cost accepts mana of different colors and colorless mana")
    void paysGenericXCostWithMixedMana() {
        Permanent pinnacle = harness.addToBattlefieldAndReturn(player1, new HelixPinnacle());
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 6, null);
        assertThat(pinnacle.getCounterCount(CounterType.TOWER)).isZero();
        harness.passBothPriorities();

        assertThat(pinnacle.getCounterCount(CounterType.TOWER)).isEqualTo(6);
    }

    @Test
    @DisplayName("Reaching 100 counters during upkeep does not create a win trigger")
    void reachingThresholdAfterUpkeepBeginsDoesNotWin() {
        Permanent pinnacle = harness.addToBattlefieldAndReturn(player1, new HelixPinnacle());
        pinnacle.setCounterCount(CounterType.TOWER, 99);
        advanceToUpkeep(player1);
        assertThat(gd.stack).isEmpty();
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.activateAbility(player1, 0, 1, null);
        harness.passBothPriorities();

        assertThat(pinnacle.getCounterCount(CounterType.TOWER)).isEqualTo(100);
        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @Test
    @DisplayName("More than 100 tower counters also wins at upkeep")
    void winsAboveThreshold() {
        Permanent pinnacle = harness.addToBattlefieldAndReturn(player1, new HelixPinnacle());
        pinnacle.setCounterCount(CounterType.TOWER, 101);

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("The win trigger rechecks the tower counter threshold at resolution")
    void doesNotWinWhenCountersDropBeforeResolution() {
        Permanent pinnacle = harness.addToBattlefieldAndReturn(player1, new HelixPinnacle());
        pinnacle.setCounterCount(CounterType.TOWER, 100);
        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        pinnacle.setCounterCount(CounterType.TOWER, 99);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.status).isEqualTo(GameStatus.RUNNING);
    }

    @ParameterizedTest
    @ValueSource(ints = {99, 100})
    @DisplayName("After leaving the battlefield, the trigger uses the last known tower counter count")
    void usesLastKnownCountersWhenSourceLeaves(int countersAtDeparture) {
        Permanent pinnacle = harness.addToBattlefieldAndReturn(player1, new HelixPinnacle());
        pinnacle.setCounterCount(CounterType.TOWER, 100);
        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);

        pinnacle.setCounterCount(CounterType.TOWER, countersAtDeparture);
        harness.inMutationScope(() ->
                harness.getPermanentRemovalService().removePermanentToGraveyard(gd, pinnacle));
        harness.assertInGraveyard(player1, "Helix Pinnacle");
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(countersAtDeparture >= 100
                ? GameStatus.FINISHED : GameStatus.RUNNING);
    }

    @ParameterizedTest
    @ValueSource(booleans = {false, true})
    @DisplayName("Shroud prevents both players from targeting Helix Pinnacle")
    void shroudPreventsTargetingByEitherPlayer(boolean opponentActivates) {
        Permanent pinnacle = harness.addToBattlefieldAndReturn(player1, new HelixPinnacle());
        Player activatingPlayer = opponentActivates ? player2 : player1;
        Permanent elder = harness.addToBattlefieldAndReturn(activatingPlayer, new WickerboughElder());
        elder.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, 1);
        harness.addMana(activatingPlayer, ManaColor.GREEN, 1);
        harness.ensurePriority(activatingPlayer);

        int elderIndex = gd.playerBattlefields.get(activatingPlayer.getId()).indexOf(elder);
        assertThatThrownBy(() -> harness.activateAbility(activatingPlayer, elderIndex, null, pinnacle.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("shroud");
    }
}
