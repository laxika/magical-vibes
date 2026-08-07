package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

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
    @DisplayName("Declining the cumulative upkeep sacrifices it")
    void decliningUpkeepSacrificesIt() {
        Permanent ooze = harness.addToBattlefieldAndReturn(player1, new MwonvuliOoze());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, false);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(ooze);
    }
}
