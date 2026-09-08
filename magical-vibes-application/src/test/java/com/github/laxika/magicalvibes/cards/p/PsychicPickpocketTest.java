package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
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
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({PsychicPickpocket.class, GrizzlyBears.class, Mountain.class})
class PsychicPickpocketTest extends BaseCardTest {

    @Test
    void enteringConnivesThenReturnsTargetNonlandPermanent() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castWithDrawnCard(new GrizzlyBears(), new Mountain());

        Permanent pickpocket = findPermanent(player1, "Psychic Pickpocket");
        discardByName("Grizzly Bears");

        assertThat(pickpocket.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    void enteringReturnsTargetEvenWhenLandIsDiscarded() {
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castWithDrawnCard(new Mountain(), new GrizzlyBears());

        Permanent pickpocket = findPermanent(player1, "Psychic Pickpocket");
        discardByName("Mountain");

        assertThat(pickpocket.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    void enteringCannotReturnAland() {
        Permanent land = harness.addToBattlefieldAndReturn(player2, new Mountain());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new GrizzlyBears());
        castWithDrawnCard(new GrizzlyBears(), new Mountain());

        discardByName("Grizzly Bears");

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, land.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
    }

    private void castWithDrawnCard(Card drawnCard, Card cardToKeep) {
        harness.setHand(player1, List.of(new PsychicPickpocket(), cardToKeep));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
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
