package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DeathWish.class, GrizzlyBears.class})
class DeathWishTest extends BaseCardTest {

    @Test
    @DisplayName("Puts a chosen outside-the-game card into hand, loses half life rounded up, and exiles Death Wish")
    void choosesOutsideTheGameCard() {
        Card chosen = new GrizzlyBears();
        setSideboard(chosen);
        harness.setLife(player1, 11);

        DeathWish wish = castDeathWish();

        PendingInteraction.LibrarySearch search = pendingSearch();
        assertThat(search.params().cards()).containsExactly(chosen);
        assertThat(search.params().reveals()).isFalse();
        choose(chosen);

        assertThat(gd.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gd.playerSideboards.get(player1.getId())).isEmpty();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(5);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(wish);
    }

    @Test
    @DisplayName("May decline the outside-the-game card and still loses half life and exiles Death Wish")
    void mayDeclineOutsideTheGameCard() {
        Card available = new GrizzlyBears();
        setSideboard(available);
        harness.setLife(player1, 20);

        DeathWish wish = castDeathWish();
        choose(null);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(available);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(available);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(wish);
    }

    @Test
    @DisplayName("Still loses half life and exiles Death Wish when no outside-the-game card is available")
    void noOutsideTheGameCardAvailable() {
        harness.setLife(player1, 20);

        DeathWish wish = castDeathWish();

        assertThat(pendingSearch()).isNull();
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(10);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(wish);
    }

    private DeathWish castDeathWish() {
        DeathWish wish = new DeathWish();
        harness.setHand(player1, List.of(wish));
        harness.addMana(player1, ManaColor.BLACK, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castSorcery(player1, 0, 0);
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
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(index));
    }
}
