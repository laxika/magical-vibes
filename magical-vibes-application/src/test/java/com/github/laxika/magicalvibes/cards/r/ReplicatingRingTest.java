package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(ReplicatingRing.class)
class ReplicatingRingTest extends BaseCardTest {

    @Test
    @DisplayName("Taps for one mana of any color")
    void tapsForAnyColor() {
        Permanent ring = addRing();

        harness.activateAbility(player1, battlefieldIndex(ring), 0, null, null);
        harness.handleListChoice(player1, "RED");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.RED)).isEqualTo(1);
    }

    @Test
    @DisplayName("Creates eight snow Replicated Rings when the eighth night counter is added")
    void createsReplicatedRingsAtEightNightCounters() {
        Permanent ring = addRing();
        ring.setCounterCount(CounterType.NIGHT, 7);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(ring.getCounterCount(CounterType.NIGHT)).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Replicated Ring"))
                .hasSize(8);

        Permanent token = findPermanent(player1, "Replicated Ring");
        harness.activateAbility(player1, battlefieldIndex(token), 0, null, null);
        harness.handleListChoice(player1, "BLUE");

        assertThat(gd.playerManaPools.get(player1.getId()).get(ManaColor.BLUE)).isEqualTo(1);
        assertThat(gd.playerManaPools.get(player1.getId()).getSnowManaTotal()).isEqualTo(1);
    }

    @Test
    @DisplayName("Accumulates night counters below eight without creating tokens")
    void accumulatesNightCountersBelowThreshold() {
        Permanent ring = addRing();

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(ring.getCounterCount(CounterType.NIGHT)).isEqualTo(1);
        assertThat(countPermanents(player1, "Replicated Ring")).isZero();

        ring.setCounterCount(CounterType.NIGHT, 6);
        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(ring.getCounterCount(CounterType.NIGHT)).isEqualTo(7);
        assertThat(countPermanents(player1, "Replicated Ring")).isZero();
    }

    @Test
    @DisplayName("Removes every night counter above eight but preserves other counters")
    void replicatesAboveThresholdAndPreservesOtherCounters() {
        Permanent ring = addRing();
        ring.setCounterCount(CounterType.NIGHT, 10);
        ring.setCounterCount(CounterType.CHARGE, 2);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(ring.getCounterCount(CounterType.NIGHT)).isZero();
        assertThat(ring.getCounterCount(CounterType.CHARGE)).isEqualTo(2);
        assertThat(countPermanents(player1, "Replicated Ring")).isEqualTo(8);

        advanceToUpkeep(player1);
        resolveAllTriggers();

        assertThat(ring.getCounterCount(CounterType.NIGHT)).isEqualTo(1);
        assertThat(countPermanents(player1, "Replicated Ring")).isEqualTo(8);
        assertThat(findPermanents(player1, "Replicated Ring"))
                .allSatisfy(token -> assertThat(token.getCounterCount(CounterType.NIGHT)).isZero());
    }

    @Test
    @DisplayName("Does not add night counters during the opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        Permanent ring = addRing();
        ring.setCounterCount(CounterType.NIGHT, 7);

        advanceToUpkeep(player2);
        resolveAllTriggers();

        assertThat(ring.getCounterCount(CounterType.NIGHT)).isEqualTo(7);
        assertThat(countPermanents(player1, "Replicated Ring")).isZero();
    }

    private Permanent addRing() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        return harness.addToBattlefieldAndReturn(player1, new ReplicatingRing());
    }

    private int battlefieldIndex(Permanent permanent) {
        return gd.playerBattlefields.get(player1.getId()).indexOf(permanent);
    }
}
