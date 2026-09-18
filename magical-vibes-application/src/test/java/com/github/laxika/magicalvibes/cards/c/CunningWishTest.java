package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.Envelop;
import com.github.laxika.magicalvibes.cards.s.SuntailHawk;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({CunningWish.class, Envelop.class, SuntailHawk.class})
class CunningWishTest extends BaseCardTest {

    @Test
    @DisplayName("Offers instant cards from outside the game and exiles Cunning Wish")
    void offersInstantCardsFromOutsideTheGame() {
        Card instant = new Envelop();
        Card creature = new SuntailHawk();
        setSideboard(instant, creature);

        CunningWish wish = castCunningWish();

        PendingInteraction.LibrarySearch search = pendingSearch();
        assertThat(search.params().cards()).containsExactly(instant);
        assertThat(search.params().sourceSideboard()).isTrue();
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().canFailToFind()).isTrue();

        choose(instant);

        assertThat(gd.playerHands.get(player1.getId())).contains(instant);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(creature);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(wish);
    }

    @Test
    @DisplayName("May decline to take an instant and still exiles Cunning Wish")
    void mayDeclineToTakeInstant() {
        Card instant = new Envelop();
        setSideboard(instant);

        CunningWish wish = castCunningWish();
        choose(null);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(instant);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(instant);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(wish);
    }

    @Test
    @DisplayName("Does not prompt when outside-the-game cards do not include an instant")
    void noMatchingCardNoPrompt() {
        Card creature = new SuntailHawk();
        setSideboard(creature);

        CunningWish wish = castCunningWish();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(creature);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(wish);
    }

    private CunningWish castCunningWish() {
        CunningWish wish = new CunningWish();
        harness.castFromHand(player1, wish, "{2}{U}");
        harness.passBothPriorities();
        return wish;
    }

    private void setSideboard(Card... cards) {
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(cards)));
    }

    private PendingInteraction.LibrarySearch pendingSearch() {
        return gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
    }

    private void choose(Card card) {
        PendingInteraction.LibrarySearch search = pendingSearch();
        int index = card == null ? -1 : search.params().cards().indexOf(card);
        harness.handleCardChosen(player1, index);
    }
}
