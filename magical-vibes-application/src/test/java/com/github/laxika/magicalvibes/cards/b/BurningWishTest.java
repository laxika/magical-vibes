package com.github.laxika.magicalvibes.cards.b;

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

@CardUsed({BattleScreech.class, BattlewiseAven.class, BookBurning.class, BurningWish.class})
class BurningWishTest extends BaseCardTest {

    @Test
    @DisplayName("Offers sorcery cards from outside the game and exiles Burning Wish")
    void offersSorceryCardsFromOutsideTheGame() {
        Card sorcery = new BattleScreech();
        Card creature = new BattlewiseAven();
        setSideboard(sorcery, creature);

        BurningWish wish = castBurningWish();

        PendingInteraction.LibrarySearch search = pendingSearch();
        assertThat(search.params().cards()).containsExactly(sorcery);
        assertThat(search.params().sourceSideboard()).isTrue();
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().canFailToFind()).isTrue();

        choose(sorcery);

        assertThat(gd.playerHands.get(player1.getId())).contains(sorcery);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(creature);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(wish);
    }

    @Test
    @DisplayName("May decline to take a sorcery and still exiles Burning Wish")
    void mayDeclineToTakeSorcery() {
        Card sorcery = new BattleScreech();
        setSideboard(sorcery);

        BurningWish wish = castBurningWish();
        choose(null);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(sorcery);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(sorcery);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(wish);
    }

    @Test
    @DisplayName("Does not prompt when outside-the-game cards do not include a sorcery")
    void noMatchingCardNoPrompt() {
        Card creature = new BattlewiseAven();
        setSideboard(creature);

        BurningWish wish = castBurningWish();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(creature);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(wish);
    }

    @Test
    @DisplayName("Does not search an opponent's outside-the-game cards")
    void ignoresOpponentsOutsideTheGameCards() {
        Card sorcery = new BattleScreech();
        setSideboard(player2, sorcery);

        BurningWish wish = castBurningWish();

        assertThat(pendingSearch()).isNull();
        assertThat(gd.playerSideboards.get(player2.getId())).containsExactly(sorcery);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(wish);
    }

    private BurningWish castBurningWish() {
        BurningWish wish = new BurningWish();
        harness.setHand(player1, List.of(wish));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
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

    @Test
    @DisplayName("Does not offer an opponent's outside-the-game sorcery")
    void searchesOnlyControllerOutsideTheGameCards() {
        Card ownCreature = new BattlewiseAven();
        Card opponentSorcery = new BookBurning();
        setSideboardForJudReview(ownCreature);
        gd.playerSideboards.put(player2.getId(), new ArrayList<>(List.of(opponentSorcery)));

        BurningWish wish = castBurningWishForJudReview();

        assertThat(pendingSearch()).isNull();
        assertThat(gd.playerSideboards.get(player2.getId())).containsExactly(opponentSorcery);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(wish);
    }

    private BurningWish castBurningWishForJudReview() {
        BurningWish wish = new BurningWish();
        harness.castFromHand(player1, wish, "{1}{R}");
        harness.passBothPriorities();
        return wish;
    }

    private void setSideboardForJudReview(Card... cards) {
        gd.playerSideboards.put(player1.getId(), new ArrayList<>(List.of(cards)));
    }
}
