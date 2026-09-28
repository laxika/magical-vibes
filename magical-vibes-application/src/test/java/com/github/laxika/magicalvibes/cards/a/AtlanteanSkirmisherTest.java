package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AtlanteanSkirmisher.class, GrizzlyBears.class, Mountain.class})
class AtlanteanSkirmisherTest extends BaseCardTest {

    @Test
    @DisplayName("Attacking draws, discards a nonland card, and puts a +1/+1 counter on it")
    void attackingWithNonlandDiscardAddsCounter() {
        Permanent skirmisher = addReadySkirmisher();
        harness.setHand(player1, List.of(new Mountain()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        discardByName("Grizzly Bears");

        assertThat(skirmisher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Mountain");
    }

    @Test
    @DisplayName("Attacking with a land discarded does not put a +1/+1 counter on it")
    void attackingWithLandDiscardDoesNotAddCounter() {
        Permanent skirmisher = addReadySkirmisher();
        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new Mountain()));

        declareAttackers(List.of(0));
        harness.passBothPriorities();
        discardByName("Mountain");

        assertThat(skirmisher.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Grizzly Bears");
    }

    private Permanent addReadySkirmisher() {
        return addCreatureReady(player1, new AtlanteanSkirmisher());
    }

    private void discardByName(String cardName) {
        List<Card> hand = gd.playerHands.get(player1.getId());
        int index = -1;
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).getName().equals(cardName)) {
                index = i;
                break;
            }
        }
        assertThat(index).as("card '%s' is in hand", cardName).isGreaterThanOrEqualTo(0);
        harness.handleCardChosen(player1, index);
    }
}
