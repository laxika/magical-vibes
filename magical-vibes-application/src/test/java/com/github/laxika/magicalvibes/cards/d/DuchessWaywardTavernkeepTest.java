package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DuchessWaywardTavernkeep.class, GrizzlyBears.class})
class DuchessWaywardTavernkeepTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a quest counter on each creature that deals combat damage to a player")
    void putsQuestCounterOnCombatDamageDealer() {
        Permanent duchess = addCreatureReady(player1, new DuchessWaywardTavernkeep());
        Permanent bear = addCreatureReady(player1, new GrizzlyBears());
        bear.setAttacking(true);

        resolveCombat();
        resolveAllTriggers();

        assertThat(bear.getCounterCount(CounterType.QUEST)).isEqualTo(1);
        assertThat(duchess.getCounterCount(CounterType.QUEST)).isZero();
    }

    @Test
    @DisplayName("Removes a quest counter from a controlled permanent to create a Junk")
    void removesQuestCounterToCreateJunk() {
        Permanent duchess = harness.addToBattlefieldAndReturn(player1, new DuchessWaywardTavernkeep());
        duchess.setCounterCount(CounterType.QUEST, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(duchess.getCounterCount(CounterType.QUEST)).isZero();
        assertThat(findPermanents(player1, "Junk")).hasSize(1);
    }

    @Test
    @DisplayName("Cannot activate without a quest counter on a controlled permanent")
    void requiresControlledQuestCounter() {
        harness.addToBattlefield(player1, new DuchessWaywardTavernkeep());
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("counter");
    }
}
