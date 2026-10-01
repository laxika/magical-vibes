package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({TwentyToedToad.class, GrizzlyBears.class})
class TwentyToedToadTest extends BaseCardTest {

    @Test
    @DisplayName("Adds a +1/+1 counter and draws when attacking with two creatures")
    void addsCounterAndDrawsWhenAttackingWithTwoCreatures() {
        Permanent toad = addCreatureReady(player1, new TwentyToedToad());
        addCreatureReady(player1, new GrizzlyBears());
        harness.setHand(player1, List.of());
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0, 1));
        harness.passBothPriorities();

        assertThat(toad.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Wins when attacking with twenty +1/+1 counters")
    void winsWithTwentyCounters() {
        Permanent toad = addCreatureReady(player1, new TwentyToedToad());
        toad.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 20);

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Wins when attacking with twenty cards in hand")
    void winsWithTwentyCardsInHand() {
        addCreatureReady(player1, new TwentyToedToad());
        harness.setHand(player1, cards(20));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.status).isEqualTo(GameStatus.FINISHED);
    }

    @Test
    @DisplayName("Sets the controller's maximum hand size to twenty")
    void setsMaximumHandSizeToTwenty() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.END_STEP);
        harness.addToBattlefield(player1, new TwentyToedToad());
        harness.setHand(player1, cards(21));

        gs.advanceStep(gd);

        PendingInteraction.DiscardChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.DiscardChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.remainingCount()).isEqualTo(1);
    }

    private List<Card> cards(int count) {
        return new ArrayList<>(IntStream.range(0, count)
                .mapToObj(ignored -> new GrizzlyBears())
                .toList());
    }
}
