package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ProfaneTutor.class, GrizzlyBears.class})
class ProfaneTutorTest extends BaseCardTest {

    @Test
    @DisplayName("Suspend exiles Profane Tutor with two time counters")
    void suspendExilesWithTwoTimeCounters() {
        ProfaneTutor card = suspendCard();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(card);
        assertThat(gd.exiledCardTimeCounters).containsEntry(card.getId(), 2);
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("The last suspend counter offers a free cast that searches any card into hand")
    void lastCounterOffersFreeCastAndSearchesAnyCardIntoHand() {
        ProfaneTutor card = suspendCard();
        GrizzlyBears chosenCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(chosenCard, new GrizzlyBears()));

        advanceToUpkeep(player1);
        harness.passBothPriorities();
        advanceToUpkeep(player1);
        harness.passBothPriorities();

        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactlyElementsOf(gd.playerDecks.get(player1.getId()));
        assertThat(search.params().canFailToFind()).isFalse();

        harness.handleCardChosen(player1, search.params().cards().indexOf(chosenCard));

        assertThat(gd.playerHands.get(player1.getId())).contains(chosenCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(card);
    }

    private ProfaneTutor suspendCard() {
        ProfaneTutor card = new ProfaneTutor();
        harness.setHand(player1, List.of(card));
        harness.addMana(player1, ManaColor.BLACK, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateHandAbility(player1, 0, null);
        return card;
    }
}
