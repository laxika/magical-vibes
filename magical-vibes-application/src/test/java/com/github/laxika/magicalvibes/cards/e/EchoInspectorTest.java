package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.a.ActOfAggression;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.m.Murder;
import com.github.laxika.magicalvibes.cards.s.SewerCrocodile;
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

@CardUsed({EchoInspector.class, SewerCrocodile.class, Mountain.class, Murder.class, ActOfAggression.class})
class EchoInspectorTest extends BaseCardTest {

    @Test
    void enteringConnivesAndAddsCounterForNonlandDiscard() {
        harness.setHand(player1, List.of(new EchoInspector(), new Mountain()));
        harness.setLibrary(player1, List.of(new SewerCrocodile()));
        addInspectorMana();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent inspector = findPermanent(player1, "Echo Inspector");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        discardByName("Sewer Crocodile");

        assertThat(inspector.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Mountain");
    }

    @Test
    void enteringDoesNotAddCounterForLandDiscard() {
        harness.setHand(player1, List.of(new EchoInspector(), new SewerCrocodile()));
        harness.setLibrary(player1, List.of(new Mountain()));
        addInspectorMana();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent inspector = findPermanent(player1, "Echo Inspector");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        discardByName("Mountain");

        assertThat(inspector.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Sewer Crocodile");
    }

    @Test
    void mayDiscardAnExistingCardInsteadOfTheDrawnCard() {
        harness.setHand(player1, List.of(new EchoInspector(), new SewerCrocodile()));
        harness.setLibrary(player1, List.of(new Mountain()));
        addInspectorMana();

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        discardByName("Sewer Crocodile");

        assertThat(findPermanent(player1, "Echo Inspector").getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Mountain");
        harness.assertInGraveyard(player1, "Sewer Crocodile");
    }

    @Test
    void connivesEvenIfInspectorIsDestroyedBeforeTriggerResolves() {
        harness.setHand(player1, List.of(new EchoInspector(), new Mountain()));
        harness.setLibrary(player1, List.of(new SewerCrocodile()));
        harness.setHand(player2, List.of(new Murder()));
        addInspectorMana();
        harness.addMana(player2, ManaColor.BLACK, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 1);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent inspector = findPermanent(player1, "Echo Inspector");
        harness.castInstant(player2, 0, inspector.getId());
        resolveAllTriggers();
        discardByName("Sewer Crocodile");

        harness.assertNotOnBattlefield(player1, "Echo Inspector");
        harness.assertInGraveyard(player1, "Echo Inspector");
        harness.assertInGraveyard(player1, "Sewer Crocodile");
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Mountain");
        assertThat(inspector.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
    }

    @Test
    void currentControllerConnivesAfterControlChangesInResponse() {
        harness.setHand(player1, List.of(new EchoInspector(), new Mountain()));
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.setHand(player2, List.of(new ActOfAggression(), new Mountain()));
        harness.setLibrary(player2, List.of(new SewerCrocodile()));
        addInspectorMana();
        harness.addMana(player2, ManaColor.RED, 2);
        harness.addMana(player2, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent inspector = findPermanent(player1, "Echo Inspector");
        harness.castInstant(player2, 0, inspector.getId());
        resolveAllTriggers();

        harness.assertOnBattlefield(player2, "Echo Inspector");
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Mountain");
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Mountain", "Sewer Crocodile");
        harness.handleCardChosen(player2, 1);

        assertThat(inspector.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        harness.assertInGraveyard(player2, "Sewer Crocodile");
    }

    private void addInspectorMana() {
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
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
