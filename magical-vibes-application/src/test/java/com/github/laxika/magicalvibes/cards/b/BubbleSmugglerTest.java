package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(BubbleSmuggler.class)
class BubbleSmugglerTest extends BaseCardTest {

    @Test
    void disguiseCastsBubbleSmugglerFaceDown() {
        harness.setHand(player1, List.of(new BubbleSmuggler()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(findPermanent(player1, "Bubble Smuggler").isFaceDown()).isTrue();
    }

    @Test
    void putsFourCountersOnBubbleSmugglerWhenTurnedFaceUp() {
        harness.setHand(player1, List.of(new BubbleSmuggler()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreatureWithMorph(player1, 0);
        harness.passBothPriorities();
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent smuggler = findPermanent(player1, "Bubble Smuggler");
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);
        harness.turnFaceUp(player1, gd.playerBattlefields.get(player1.getId()).indexOf(smuggler));

        assertThat(smuggler.isFaceDown()).isFalse();
        assertThat(smuggler.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(4);
    }
}
