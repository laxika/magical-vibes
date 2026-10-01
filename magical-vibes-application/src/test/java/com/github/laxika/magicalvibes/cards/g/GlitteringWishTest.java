package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.c.CloudKey;
import com.github.laxika.magicalvibes.cards.q.Quagnoth;
import com.github.laxika.magicalvibes.cards.s.SliverLegion;
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

@CardUsed({CloudKey.class, GlitteringWish.class, Quagnoth.class, SliverLegion.class})
class GlitteringWishTest extends BaseCardTest {

    @Test
    @DisplayName("Offers multicolored cards from outside the game and exiles Glittering Wish")
    void offersMulticoloredCardsFromOutsideTheGame() {
        Card multicolored = new SliverLegion();
        Card monocolored = new Quagnoth();
        setSideboard(multicolored, monocolored);

        GlitteringWish wish = castGlitteringWish();

        PendingInteraction.LibrarySearch search = pendingSearch();
        assertThat(search.params().cards()).containsExactly(multicolored);
        assertThat(search.params().sourceSideboard()).isTrue();
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().canFailToFind()).isTrue();

        choose(multicolored);

        assertThat(gd.playerHands.get(player1.getId())).contains(multicolored);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(monocolored);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(wish);
    }

    @Test
    @DisplayName("May decline to take a multicolored card and still exiles Glittering Wish")
    void mayDeclineToTakeMulticoloredCard() {
        Card multicolored = new SliverLegion();
        setSideboard(multicolored);

        GlitteringWish wish = castGlitteringWish();
        choose(null);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(multicolored);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(multicolored);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(wish);
    }

    @Test
    @DisplayName("Does not prompt when outside-the-game cards are not multicolored")
    void noMatchingCardNoPrompt() {
        Card monocolored = new Quagnoth();
        setSideboard(monocolored);

        GlitteringWish wish = castGlitteringWish();

        assertThat(pendingSearch()).isNull();
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(monocolored);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(wish);
    }

    @Test
    @DisplayName("Does not prompt for a colorless outside-the-game card")
    void colorlessCardIsNotMulticolored() {
        Card colorless = new CloudKey();
        setSideboard(colorless);

        GlitteringWish wish = castGlitteringWish();

        assertThat(pendingSearch()).isNull();
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(colorless);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(wish);
    }

    @Test
    @DisplayName("Does not search an opponent's outside-the-game cards")
    void ignoresOpponentsOutsideTheGameCards() {
        Card multicolored = new SliverLegion();
        setSideboard(player2, multicolored);

        GlitteringWish wish = castGlitteringWish();

        assertThat(pendingSearch()).isNull();
        assertThat(gd.playerSideboards.get(player2.getId())).containsExactly(multicolored);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(wish);
    }

    private GlitteringWish castGlitteringWish() {
        GlitteringWish wish = new GlitteringWish();
        harness.setHand(player1, List.of(wish));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.castAndResolveSorcery(player1, 0, 0);
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
