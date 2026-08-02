package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class AssembleTheLegionTest extends BaseCardTest {

    @Test
    @DisplayName("First upkeep adds a muster counter and creates one Soldier")
    void firstUpkeepCreatesOneSoldier() {
        harness.addToBattlefield(player1, new AssembleTheLegion());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        Permanent legion = findPermanent(player1, "Assemble the Legion");
        assertThat(legion.getCounterCount(CounterType.MUSTER)).isEqualTo(1);
        assertThat(countPermanents(player1, "Soldier")).isEqualTo(1);
    }

    @Test
    @DisplayName("Second upkeep makes two more Soldiers (one per muster counter)")
    void tokenCountScalesWithMusterCounters() {
        harness.addToBattlefield(player1, new AssembleTheLegion());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        Permanent legion = findPermanent(player1, "Assemble the Legion");
        assertThat(legion.getCounterCount(CounterType.MUSTER)).isEqualTo(2);
        assertThat(countPermanents(player1, "Soldier")).isEqualTo(3);
    }

    @Test
    @DisplayName("Soldier tokens have haste")
    void tokensHaveHaste() {
        harness.addToBattlefield(player1, new AssembleTheLegion());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        Permanent soldier = findPermanent(player1, "Soldier");
        assertThat(gqs.hasKeyword(gd, soldier, Keyword.HASTE)).isTrue();
    }

    @Test
    @DisplayName("Trigger does not fire during the opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        harness.addToBattlefield(player1, new AssembleTheLegion());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        Permanent legion = findPermanent(player1, "Assemble the Legion");
        assertThat(legion.getCounterCount(CounterType.MUSTER)).isZero();
        assertThat(countPermanents(player1, "Soldier")).isZero();
    }
}
