package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MwonvuliOoze.class})
class MwonvuliOozeTest extends BaseCardTest {

    @Test
    @DisplayName("With no age counters it is 1/1")
    void ptWithoutCounters() {
        Permanent ooze = harness.addToBattlefieldAndReturn(player1, new MwonvuliOoze());

        assertThat(gqs.getEffectivePower(gd, ooze)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, ooze)).isEqualTo(1);
    }

    @Test
    @DisplayName("P/T is 1 plus twice the number of age counters on it")
    void ptScalesWithAgeCounters() {
        Permanent ooze = harness.addToBattlefieldAndReturn(player1, new MwonvuliOoze());
        ooze.setCounterCount(CounterType.AGE, 3);

        assertThat(gqs.getEffectivePower(gd, ooze)).isEqualTo(7);
        assertThat(gqs.getEffectiveToughness(gd, ooze)).isEqualTo(7);
    }

    @Test
    @DisplayName("Paying the cumulative upkeep keeps it and grows it to 3/3")
    void payingUpkeepGrowsIt() {
        Permanent ooze = harness.addToBattlefieldAndReturn(player1, new MwonvuliOoze());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ooze);
        assertThat(ooze.getCounterCount(CounterType.AGE)).isEqualTo(1);
        assertThat(gqs.getEffectivePower(gd, ooze)).isEqualTo(3);
        assertThat(gqs.getEffectiveToughness(gd, ooze)).isEqualTo(3);
    }

    @Test
    @DisplayName("Cumulative upkeep costs {2} for each age counter")
    void cumulativeUpkeepCostScalesWithAgeCounters() {
        Permanent ooze = harness.addToBattlefieldAndReturn(player1, new MwonvuliOoze());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.handleMayAbilityChosen(player1, true);

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(ooze.getCounterCount(CounterType.AGE)).isEqualTo(2);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ooze);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gqs.getEffectivePower(gd, ooze)).isEqualTo(5);
        assertThat(gqs.getEffectiveToughness(gd, ooze)).isEqualTo(5);
    }

    @Test
    @DisplayName("Declining the cumulative upkeep sacrifices it")
    void decliningUpkeepSacrificesIt() {
        Permanent ooze = harness.addToBattlefieldAndReturn(player1, new MwonvuliOoze());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ooze);
    }

    @Test
    @DisplayName("An opponent's upkeep does not add age counters or require payment")
    void opponentUpkeepDoesNotTrigger() {
        Permanent ooze = harness.addToBattlefieldAndReturn(player1, new MwonvuliOoze());

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(ooze.getCounterCount(CounterType.AGE)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ooze);
    }

    @Test
    @DisplayName("Other counters do not count as age counters, and bonuses apply after the base P/T")
    void otherCountersDoNotIncreaseAgeBasedPowerToughness() {
        Permanent ooze = harness.addToBattlefieldAndReturn(player1, new MwonvuliOoze());
        ooze.setCounterCount(CounterType.AGE, 2);
        ooze.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        assertThat(gqs.getEffectivePower(gd, ooze)).isEqualTo(8);
        assertThat(gqs.getEffectiveToughness(gd, ooze)).isEqualTo(8);

        ooze.setCounterCount(CounterType.AGE, 0);

        assertThat(gqs.getEffectivePower(gd, ooze)).isEqualTo(4);
        assertThat(gqs.getEffectiveToughness(gd, ooze)).isEqualTo(4);
    }

    @Test
    @DisplayName("Upkeep includes existing age counters but ignores other counters")
    void upkeepCountsOnlyAgeCounters() {
        Permanent ooze = harness.addToBattlefieldAndReturn(player1, new MwonvuliOoze());
        ooze.setCounterCount(CounterType.AGE, 2);
        ooze.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.addMana(player1, ManaColor.COLORLESS, 6);
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerBattlefields.get(player1.getId())).contains(ooze);
        assertThat(ooze.getCounterCount(CounterType.AGE)).isEqualTo(3);
        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.COLORLESS)).isZero();
        assertThat(gqs.getEffectivePower(gd, ooze)).isEqualTo(10);
        assertThat(gqs.getEffectiveToughness(gd, ooze)).isEqualTo(10);
    }
}
