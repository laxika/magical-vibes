package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AssembleTheLegion.class, Naturalize.class})
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

    @Test
    @DisplayName("Created Soldiers are red and white 1/1 creature tokens")
    void createsOracleSoldierTokens() {
        harness.addToBattlefield(player1, new AssembleTheLegion());

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        Permanent soldier = findPermanent(player1, "Soldier");
        assertThat(soldier.getCard().isToken()).isTrue();
        assertThat(gqs.isCreature(gd, soldier)).isTrue();
        assertThat(gqs.getEffectivePower(gd, soldier)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, soldier)).isEqualTo(1);
        assertThat(gqs.getEffectiveColors(gd, soldier))
                .containsExactlyInAnyOrder(CardColor.RED, CardColor.WHITE);
        assertThat(soldier.getCard().getSubtypes()).containsExactly(CardSubtype.SOLDIER);
        assertThat(countPermanents(player2, "Soldier")).isZero();
    }

    @Test
    @DisplayName("Each copy uses its own muster counters")
    void copiesCountTheirOwnCounters() {
        harness.addToBattlefield(player1, new AssembleTheLegion());
        Permanent first = findPermanent(player1, "Assemble the Legion");
        first.getCounters().put(CounterType.MUSTER, 3);
        harness.addToBattlefield(player1, new AssembleTheLegion());

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.MUSTER)).isEqualTo(4);
        assertThat(findPermanents(player1, "Assemble the Legion").get(1)
                .getCounterCount(CounterType.MUSTER)).isEqualTo(1);
        assertThat(countPermanents(player1, "Soldier")).isEqualTo(5);
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 3})
    @DisplayName("Removal in response uses the last known muster count without adding a counter")
    void removedSourceUsesLastKnownCounters(int counters) {
        harness.addToBattlefield(player1, new AssembleTheLegion());
        Permanent legion = findPermanent(player1, "Assemble the Legion");
        legion.getCounters().put(CounterType.MUSTER, counters);
        harness.setHand(player1, List.of(new Naturalize()));

        advanceToUpkeep(player1);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castInstant(player1, 0, legion.getId());
        harness.passBothPriorities();
        harness.assertInGraveyard(player1, "Assemble the Legion");
        harness.passBothPriorities();

        assertThat(countPermanents(player1, "Assemble the Legion")).isZero();
        assertThat(countPermanents(player1, "Soldier")).isEqualTo(counters);
    }
}
