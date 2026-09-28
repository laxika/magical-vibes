package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HypnoticGrifter;
import com.github.laxika.magicalvibes.cards.m.MadameMasque;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({IronMongerSadisticTycoon.class, HypnoticGrifter.class, MadameMasque.class, GrizzlyBears.class})
class IronMongerSadisticTycoonTest extends BaseCardTest {

    @Test
    void connivingPutsCountersOnEachVillainYouControl() {
        Permanent ironMonger = addCreatureReady(player1, new IronMongerSadisticTycoon());
        Permanent otherVillain = addCreatureReady(player1, new MadameMasque());
        Permanent nonVillain = addCreatureReady(player1, new GrizzlyBears());
        Permanent opponentVillain = addCreatureReady(player2, new MadameMasque());
        addCreatureReady(player1, new HypnoticGrifter());

        harness.setHand(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 3, null, null);
        harness.passBothPriorities();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        discardByName(player1, "Grizzly Bears");
        harness.passBothPriorities();

        assertThat(ironMonger.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(otherVillain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(nonVillain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(opponentVillain.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    private void discardByName(com.github.laxika.magicalvibes.model.Player player, String cardName) {
        List<Card> hand = gd.playerHands.get(player.getId());
        int index = -1;
        for (int i = 0; i < hand.size(); i++) {
            if (hand.get(i).getName().equals(cardName)) {
                index = i;
                break;
            }
        }
        assertThat(index).as("card '%s' is in hand", cardName).isGreaterThanOrEqualTo(0);
        harness.handleCardChosen(player, index);
    }
}
