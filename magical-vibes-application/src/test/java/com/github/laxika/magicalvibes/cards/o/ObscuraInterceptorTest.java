package com.github.laxika.magicalvibes.cards.o;

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

@CardUsed({ObscuraInterceptor.class, GrizzlyBears.class, Mountain.class})
class ObscuraInterceptorTest extends BaseCardTest {

    @Test
    void enteringConnivesAndReturnsTargetSpell() {
        Card targetSpell = castTargetSpellAndInterceptor(new GrizzlyBears(), new Mountain());
        discardByName("Grizzly Bears");

        Permanent interceptor = findPermanent(player1, "Obscura Interceptor");
        assertThat(interceptor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, targetSpell.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
    }

    @Test
    void enteringStillReturnsTargetSpellWhenLandIsDiscarded() {
        Card targetSpell = castTargetSpellAndInterceptor(new Mountain(), new GrizzlyBears());
        discardByName("Mountain");

        Permanent interceptor = findPermanent(player1, "Obscura Interceptor");
        assertThat(interceptor.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.PermanentChoice.class);

        harness.handlePermanentChosen(player1, targetSpell.getId());
        harness.passBothPriorities();

        harness.assertInHand(player2, "Grizzly Bears");
    }

    private Card castTargetSpellAndInterceptor(Card drawnCard, Card cardToKeep) {
        GrizzlyBears targetSpell = new GrizzlyBears();
        harness.setHand(player2, List.of(targetSpell));
        harness.addMana(player2, ManaColor.GREEN, 2);

        harness.setHand(player1, List.of(new ObscuraInterceptor(), cardToKeep));
        harness.setLibrary(player1, List.of(drawnCard));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.forceActivePlayer(player2);
        harness.castCreature(player2, 0);
        harness.passPriority(player2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();

        return targetSpell;
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
