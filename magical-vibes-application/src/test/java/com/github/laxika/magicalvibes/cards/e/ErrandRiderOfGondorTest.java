package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.y.YoshimaruEverFaithful;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ErrandRiderOfGondor.class, GrizzlyBears.class, YoshimaruEverFaithful.class})
class ErrandRiderOfGondorTest extends BaseCardTest {

    @Test
    void drawsThenPutsAHandCardOnTheBottomWithoutALegendaryCreature() {
        Card drawnCard = new GrizzlyBears();
        Card remainingLibraryCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard, remainingLibraryCard));
        harness.setHand(player1, List.of(new ErrandRiderOfGondor()));
        castErrandRider();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(
                PendingInteraction.PutCardsFromHandOnLibraryCardChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(drawnCard.getId()));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(remainingLibraryCard, drawnCard);
    }

    @Test
    void onlyDrawsWhenYouControlALegendaryCreature() {
        addCreatureReady(player1, new YoshimaruEverFaithful());
        Card drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new ErrandRiderOfGondor()));
        castErrandRider();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    private void castErrandRider() {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
    }
}
