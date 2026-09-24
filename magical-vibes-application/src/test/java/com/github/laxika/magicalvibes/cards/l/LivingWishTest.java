package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.b.BattlewiseAven;
import com.github.laxika.magicalvibes.cards.b.BookBurning;
import com.github.laxika.magicalvibes.cards.i.IronshellBeetle;
import com.github.laxika.magicalvibes.cards.k.KrosanVerge;
import com.github.laxika.magicalvibes.cards.m.MentalNote;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({BattlewiseAven.class, BookBurning.class, IronshellBeetle.class, KrosanVerge.class, LivingWish.class, MentalNote.class})
class LivingWishTest extends BaseCardTest {

    @Test
    @DisplayName("Offers creature and land cards from outside the game and exiles Living Wish")
    void offersCreatureAndLandCardsFromOutsideTheGame() {
        Card creature = new IronshellBeetle();
        Card land = new KrosanVerge();
        Card nonmatching = new MentalNote();
        setSideboard(creature, land, nonmatching);

        LivingWish wish = castLivingWish();

        PendingInteraction.LibrarySearch search = pendingSearch();
        assertThat(search.params().cards()).containsExactly(creature, land);
        assertThat(search.params().sourceSideboard()).isTrue();
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().canFailToFind()).isTrue();

        choose(land);

        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(creature, nonmatching);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(wish);
    }

    @Test
    @DisplayName("May decline to take a creature or land and still exiles Living Wish")
    void mayDeclineToTakeCard() {
        Card creature = new IronshellBeetle();
        setSideboard(creature);

        LivingWish wish = castLivingWish();
        choose(null);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(creature);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(creature);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(wish);
    }

    @Test
    @DisplayName("Does not prompt when outside-the-game cards are neither creatures nor lands")
    void noMatchingCardNoPrompt() {
        Card nonmatching = new MentalNote();
        setSideboard(nonmatching);

        LivingWish wish = castLivingWish();

        assertThat(pendingSearch()).isNull();
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(nonmatching);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(wish);
    }

    @Test
    @DisplayName("Does not search an opponent's outside-the-game cards")
    void ignoresOpponentsOutsideTheGameCards() {
        Card creature = new IronshellBeetle();
        setSideboard(player2, creature);

        LivingWish wish = castLivingWish();

        assertThat(pendingSearch()).isNull();
        assertThat(gd.playerSideboards.get(player2.getId())).containsExactly(creature);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(wish);
    }

    private LivingWish castLivingWish() {
        LivingWish wish = new LivingWish();
        harness.castFromHand(player1, wish, "{1}{G}");
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

    @Test
    @DisplayName("Does not offer a matching card owned by an opponent")
    void searchesOnlyControllerOutsideTheGameCards() {
        Card ownSorcery = new BookBurning();
        Card opponentCreature = new BattlewiseAven();
        setSideboardForJudReview(ownSorcery);
        gd.playerSideboards.put(player2.getId(), new ArrayList<>(List.of(opponentCreature)));

        LivingWish wish = castLivingWish();

        assertThat(pendingSearch()).isNull();
        assertThat(gd.playerSideboards.get(player2.getId())).containsExactly(opponentCreature);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(wish);
    }

    private void setSideboardForJudReview(Card... cards) {
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(cards)));
    }
}
