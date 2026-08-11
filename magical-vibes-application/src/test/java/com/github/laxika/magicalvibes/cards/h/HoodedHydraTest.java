package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class HoodedHydraTest extends BaseCardTest {

    @Test
    void entersWithXPlusOnePlusOneCounters() {
        harness.setHand(player1, List.of(new HoodedHydra()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.WHITE, 3);

        gs.playCard(gd, player1, 0, 3, null, null);
        harness.passBothPriorities();

        Permanent hydra = findPermanent(player1, "Hooded Hydra");
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void putsFiveCountersOnItWhenTurnedFaceUp() {
        harness.setHand(player1, List.of(new HoodedHydra()));
        harness.addMana(player1, ManaColor.GREEN, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent hydra = findPermanent(player1, "Hooded Hydra");
        assertThat(hydra.isFaceDown()).isTrue();

        harness.addMana(player1, ManaColor.GREEN, 5);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(hydra));

        assertThat(hydra.isFaceDown()).isFalse();
        assertThat(hydra.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(5);
    }

    @Test
    void createsOneGreenSnakeForEachPlusOneCounterWhenItDies() {
        Permanent hydra = harness.addToBattlefieldAndReturn(player1, new HoodedHydra());
        hydra.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);

        harness.setHand(player2, List.of(new Shock()));
        harness.addMana(player2, ManaColor.RED, 1);
        harness.castInstant(player2, 0, hydra.getId());
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(findPermanents(player1, "Snake")).hasSize(2);
        for (Permanent snake : findPermanents(player1, "Snake")) {
            assertThat(snake.getCard().getColor()).isEqualTo(CardColor.GREEN);
            assertThat(snake.getCard().getSubtypes()).containsExactly(CardSubtype.SNAKE);
        }
    }
}
