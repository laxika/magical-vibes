package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.f.ForestBear;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.ArrayList;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class MindUnboundTest extends BaseCardTest {

    private void stockLibrary() {
        List<Card> library = new ArrayList<>();
        for (int i = 0; i < 20; i++) {
            library.add(new ForestBear());
        }
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of());
    }

    private void runUpkeep() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities(); // advance to upkeep, trigger goes on stack
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

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.UNTAP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
        harness.passBothPriorities();

        var enchantment = gd.playerBattlefields.get(player1.getId()).get(0);
        assertThat(enchantment.getCounterCount(CounterType.LORE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }
}
