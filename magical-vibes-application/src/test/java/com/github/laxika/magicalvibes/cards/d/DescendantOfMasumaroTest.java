package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.ArabaMothrider;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DescendantOfMasumaro.class, ArabaMothrider.class})
class DescendantOfMasumaroTest extends BaseCardTest {

    @Test
    void addsAndRemovesCountersBasedOnTheChosenHands() {
        Permanent descendant = harness.addToBattlefieldAndReturn(player1, new DescendantOfMasumaro());
        descendant.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        harness.setHand(player1, cards(3));
        harness.setHand(player2, cards(2));

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(descendant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(3);
    }

    @Test
    void usesHandSizesWhenTheTriggerResolves() {
        Permanent descendant = harness.addToBattlefieldAndReturn(player1, new DescendantOfMasumaro());
        harness.setHand(player1, List.of());
        harness.setHand(player2, List.of());

        advanceToUpkeep(player1);
        harness.setHand(player1, cards(3));
        harness.setHand(player2, cards(1));
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(descendant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(2);
    }

    @Test
    void upkeepTriggerCanOnlyTargetAnOpponent() {
        harness.addToBattlefield(player1, new DescendantOfMasumaro());
        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class)
                .validPlayerIds()).containsExactly(player2.getId());
    }

    @Test
    void removesNoMoreCountersThanAreOnTheCreature() {
        Permanent descendant = harness.addToBattlefieldAndReturn(player1, new DescendantOfMasumaro());
        harness.setHand(player1, cards(1));
        harness.setHand(player2, cards(4));

        advanceToUpkeep(player1);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(descendant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private List<Card> cards(int count) {
        return java.util.stream.IntStream.range(0, count)
                .mapToObj(index -> (Card) new ArabaMothrider())
                .toList();
    }
}
