package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.c.ChromeCat;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PsionicSnoop.class, ChromeCat.class, Mountain.class, Murder.class})
class PsionicSnoopTest extends BaseCardTest {

    @Test
    void enteringConnivesAndAddsCounterForNonlandDiscard() {
        harness.setHand(player1, List.of(new PsionicSnoop(), new Mountain()));
        harness.setLibrary(player1, List.of(new ChromeCat()));
        addSnoopMana();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent snoop = findPermanent(player1, "Psionic Snoop");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        discardByName("Chrome Cat");

        assertThat(snoop.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Mountain");
    }

    @Test
    void enteringDoesNotAddCounterForLandDiscard() {
        harness.setHand(player1, List.of(new PsionicSnoop(), new ChromeCat()));
        harness.setLibrary(player1, List.of(new Mountain()));
        addSnoopMana();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent snoop = findPermanent(player1, "Psionic Snoop");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        discardByName("Mountain");

        assertThat(snoop.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Chrome Cat");
    }

    @Test
    void canEnterAndConniveDuringOpponentsEndStep() {
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.setHand(player1, List.of(new PsionicSnoop()));
        harness.setLibrary(player1, List.of(new ChromeCat()));
        addSnoopMana();

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        discardByName("Chrome Cat");

        harness.assertOnBattlefield(player1, "Psionic Snoop");
        assertThat(findPermanent(player1, "Psionic Snoop")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        harness.assertInGraveyard(player1, "Chrome Cat");
    }

    @Test
    void stillDrawsAndDiscardsWhenDestroyedBeforeConniveResolves() {
        harness.setHand(player1, List.of(new PsionicSnoop(), new Mountain()));
        harness.setLibrary(player1, List.of(new ChromeCat()));
        harness.setHand(player2, List.of(new Murder()));
        addSnoopMana();
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent snoop = findPermanent(player1, "Psionic Snoop");
        harness.castAndResolveInstant(player2, 0, snoop.getId());
        harness.assertInGraveyard(player1, "Psionic Snoop");
        resolveAllTriggers();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        discardByName("Chrome Cat");

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Mountain");
        harness.assertInGraveyard(player1, "Chrome Cat");
        assertThat(gd.stack).isEmpty();
    }

    private void addSnoopMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
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
