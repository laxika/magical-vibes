package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.cards.n.Naturalize;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MindUnbound.class, RuneclawBear.class, Naturalize.class})
class MindUnboundTest extends BaseCardTest {

    private void stockLibrary() {
        List<Card> library = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            library.add(new RuneclawBear());
        }
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of());
    }

    private void runUpkeep() {
        advanceToUpkeep(player1);
        harness.passBothPriorities(); // resolve the trigger
    }

    @Test
    @DisplayName("First upkeep puts a lore counter and draws one card")
    void firstUpkeepDrawsOne() {
        stockLibrary();
        harness.addToBattlefield(player1, new MindUnbound());

        runUpkeep();

        var enchantment = gd.playerBattlefields.get(player1.getId()).get(0);
        assertThat(enchantment.getCounterCount(CounterType.LORE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Draws increase with each accumulated lore counter")
    void drawsScaleWithLoreCounters() {
        stockLibrary();
        harness.addToBattlefield(player1, new MindUnbound());

        runUpkeep();
        runUpkeep();
        runUpkeep();

        var enchantment = gd.playerBattlefields.get(player1.getId()).get(0);
        assertThat(enchantment.getCounterCount(CounterType.LORE)).isEqualTo(3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(6); // 1 + 2 + 3
    }

    @Test
    @DisplayName("Does not trigger on the opponent's upkeep")
    void doesNotTriggerOnOpponentUpkeep() {
        stockLibrary();
        harness.addToBattlefield(player1, new MindUnbound());

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        var enchantment = gd.playerBattlefields.get(player1.getId()).get(0);
        assertThat(enchantment.getCounterCount(CounterType.LORE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Draws using the last known lore count when destroyed in response")
    void drawsAfterSourceIsDestroyed() {
        stockLibrary();
        var enchantment = harness.addToBattlefieldAndReturn(player1, new MindUnbound());
        enchantment.setCounterCount(CounterType.LORE, 3);
        harness.setHand(player2, List.of(new Naturalize()));
        harness.addMana(player2, ManaColor.GREEN, 2);

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(1);
        harness.castAndResolveInstant(player2, 0, enchantment.getId());
        harness.assertNotOnBattlefield(player1, "Mind Unbound");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Each Mind Unbound counts only its own lore counters")
    void multipleCopiesCountTheirOwnCounters() {
        stockLibrary();
        var first = harness.addToBattlefieldAndReturn(player1, new MindUnbound());
        var second = harness.addToBattlefieldAndReturn(player1, new MindUnbound());
        first.setCounterCount(CounterType.LORE, 2);
        second.setCounterCount(CounterType.LORE, 4);

        advanceToUpkeep(player1);
        assertThat(gd.stack).hasSize(2);
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(first.getCounterCount(CounterType.LORE)).isEqualTo(3);
        assertThat(second.getCounterCount(CounterType.LORE)).isEqualTo(5);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(8);
    }
}

