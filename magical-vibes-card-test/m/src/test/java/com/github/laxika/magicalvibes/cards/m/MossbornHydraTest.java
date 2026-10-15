package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MossbornHydra.class, Forest.class})
class MossbornHydraTest extends BaseCardTest {

    @Test
    @DisplayName("Enters the battlefield with a +1/+1 counter")
    void entersWithCounter() {
        harness.setHand(player1, List.of(new MossbornHydra()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        Permanent hydra = findPermanent(player1, "Mossborn Hydra");
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    @DisplayName("Landfall doubles its +1/+1 counters")
    void landfallDoublesCounters() {
        Permanent hydra = harness.addToBattlefieldAndReturn(player1, new MossbornHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }

    @Test
    @DisplayName("An opponent's land does not trigger landfall")
    void opponentLandDoesNotTrigger() {
        Permanent hydra = harness.addToBattlefieldAndReturn(player1, new MossbornHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    @DisplayName("Landfall uses the counter count when the trigger resolves")
    void landfallUsesCurrentCounterCount() {
        Permanent hydra = harness.enterBattlefieldAndReturn(player1, new MossbornHydra());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 4);
        harness.passBothPriorities();

        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(8);
    }

    @Test
    @DisplayName("Each Hydra doubles only its own counters")
    void eachHydraDoublesItsOwnCounters() {
        Permanent first = harness.enterBattlefieldAndReturn(player1, new MossbornHydra());
        Permanent second = harness.enterBattlefieldAndReturn(player1, new MossbornHydra());
        second.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 3);
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
        assertThat(second.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(6);
    }
}
