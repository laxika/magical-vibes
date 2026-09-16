package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CephalidVandal.class, CephalidAristocrat.class})
class CephalidVandalTest extends BaseCardTest {

    @Test
    @DisplayName("The first upkeep adds a shred counter and mills one card")
    void firstUpkeepAddsCounterAndMillsOne() {
        Permanent vandal = harness.addToBattlefieldAndReturn(player1, new CephalidVandal());
        harness.setLibrary(player1, List.of(new CephalidAristocrat()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(vandal.getCounterCount(CounterType.SHRED)).isEqualTo(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("The mill count includes the shred counter added that upkeep")
    void millsForAllShredCounters() {
        Permanent vandal = harness.addToBattlefieldAndReturn(player1, new CephalidVandal());
        vandal.setCounterCount(CounterType.SHRED, 2);
        harness.setLibrary(player1, List.of(
                new CephalidAristocrat(), new CephalidAristocrat(), new CephalidAristocrat()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(vandal.getCounterCount(CounterType.SHRED)).isEqualTo(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("The ability triggers only during its controller's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        Permanent vandal = harness.addToBattlefieldAndReturn(player1, new CephalidVandal());
        harness.setLibrary(player1, List.of(new CephalidAristocrat()));

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(vandal.getCounterCount(CounterType.SHRED)).isZero();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Each controller upkeep adds one counter and mills the updated count")
    void repeatedUpkeepsAccumulateCountersAndMillCounts() {
        Permanent vandal = harness.addToBattlefieldAndReturn(player1, new CephalidVandal());
        harness.setLibrary(player1, List.of(
                new CephalidAristocrat(), new CephalidAristocrat(), new CephalidAristocrat()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        advanceToUpkeep(player2);
        harness.passBothPriorities();
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(vandal.getCounterCount(CounterType.SHRED)).isEqualTo(2);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("The ability mills only the cards available when the library is short")
    void millsOnlyAvailableCardsFromShortLibrary() {
        Permanent vandal = harness.addToBattlefieldAndReturn(player1, new CephalidVandal());
        vandal.setCounterCount(CounterType.SHRED, 2);
        harness.setLibrary(player1, List.of(new CephalidAristocrat()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();

        assertThat(vandal.getCounterCount(CounterType.SHRED)).isEqualTo(3);
        assertThat(gd.playerGraveyards.get(player1.getId())).hasSize(1);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }
}
