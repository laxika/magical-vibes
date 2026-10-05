package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.ActOfAggression;
import com.github.laxika.magicalvibes.cards.g.GatheringThrong;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.m.Murder;
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

@CardUsed({RaffinesInformant.class, GatheringThrong.class, Mountain.class, ActOfAggression.class, Murder.class})
class RaffinesInformantTest extends BaseCardTest {

    @Test
    void enteringConnivesAndAddsCounterForNonlandDiscard() {
        harness.setHand(player1, List.of(new RaffinesInformant(), new Mountain()));
        harness.setLibrary(player1, List.of(new GatheringThrong()));
        addInformantMana();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent informant = findPermanent(player1, "Raffine's Informant");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        discardByName("Gathering Throng");

        assertThat(informant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Mountain");
    }

    @Test
    void enteringDoesNotAddCounterForLandDiscard() {
        harness.setHand(player1, List.of(new RaffinesInformant(), new GatheringThrong()));
        harness.setLibrary(player1, List.of(new Mountain()));
        addInformantMana();

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        Permanent informant = findPermanent(player1, "Raffine's Informant");
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DiscardChoice.class);
        discardByName("Mountain");

        assertThat(informant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isZero();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName).containsExactly("Gathering Throng");
    }

    @Test
    void conniveUsesCurrentCreatureControllerAfterControlChanges() {
        harness.setHand(player1, List.of(new RaffinesInformant(), new Mountain()));
        harness.setLibrary(player1, List.of(new Mountain()));
        harness.setHand(player2, List.of(new ActOfAggression(), new Mountain()));
        harness.setLibrary(player2, List.of(new GatheringThrong(), new Mountain()));
        addInformantMana();
        harness.addMana(player2, ManaColor.RED, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent informant = findPermanent(player1, "Raffine's Informant");
        harness.castAndResolveInstant(player2, 0, informant.getId());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Mountain");
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Mountain", "Gathering Throng");
        harness.handleCardChosen(player2, 1);

        assertThat(informant.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gd.playerHands.get(player2.getId())).extracting(Card::getName)
                .containsExactly("Mountain");
    }

    @Test
    void conniveDiscardsTheDrawnCardWhenHandWasEmpty() {
        harness.setHand(player1, List.of(new RaffinesInformant()));
        harness.setLibrary(player1, List.of(new GatheringThrong(), new Mountain()));
        addInformantMana();

        harness.castCreature(player1, 0);
        resolveAllTriggers();
        harness.handleCardChosen(player1, 0);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Gathering Throng");
        assertThat(findPermanent(player1, "Raffine's Informant")
                .getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void conniveStillDrawsAndDiscardsAfterInformantIsDestroyed() {
        harness.setHand(player1, List.of(new RaffinesInformant(), new Mountain()));
        harness.setLibrary(player1, List.of(new GatheringThrong(), new Mountain()));
        harness.setHand(player2, List.of(new Murder()));
        addInformantMana();
        harness.addMana(player2, ManaColor.BLACK, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent informant = findPermanent(player1, "Raffine's Informant");
        harness.castAndResolveInstant(player2, 0, informant.getId());
        resolveAllTriggers();
        discardByName("Gathering Throng");

        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Mountain");
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Raffine's Informant", "Gathering Throng");
    }

    private void addInformantMana() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
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
