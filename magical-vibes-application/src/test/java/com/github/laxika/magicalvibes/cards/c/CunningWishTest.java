package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.a.AvenFogbringer;
import com.github.laxika.magicalvibes.cards.e.Envelop;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AvenFogbringer.class, CunningWish.class, Envelop.class})
class CunningWishTest extends BaseCardTest {

    @Test
    @DisplayName("Offers instant cards from outside the game and exiles Cunning Wish")
    void offersInstantCardsFromOutsideTheGame() {
        Card instant = new Envelop();
        Card creature = new AvenFogbringer();
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
        Card creature = new AvenFogbringer();
        setSideboard(creature);

        CunningWish wish = castCunningWish();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(creature);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(wish);
    }

    @Test
    @DisplayName("Does not search an opponent's outside-the-game cards")
    void ignoresOpponentsOutsideTheGameCards() {
        Card instant = new Envelop();
        setSideboard(player2, instant);

        CunningWish wish = castCunningWish();

        assertThat(pendingSearch()).isNull();
        assertThat(gd.playerSideboards.get(player2.getId())).containsExactly(instant);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(wish);
    }

    private CunningWish castCunningWish() {
        CunningWish wish = new CunningWish();
        harness.setHand(player1, List.of(wish));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        return wish;
    }

    private void setSideboard(Card... cards) {
        setSideboard(player1, cards);
    }

    private void setSideboard(Player player, Card... cards) {
        gd.playerSideboards.put(player.getId(), new ArrayList<>(List.of(cards)));
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
