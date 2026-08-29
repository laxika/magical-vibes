package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.f.Forest;
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

@CardUsed({LivingWish.class, GrizzlyBears.class, Forest.class, Divination.class})
class LivingWishTest extends BaseCardTest {

    @Test
    @DisplayName("Offers creature and land cards from outside the game and exiles Living Wish")
    void offersCreatureAndLandCardsFromOutsideTheGame() {
        Card creature = new GrizzlyBears();
        Card land = new Forest();
        Card sorcery = new Divination();
        setSideboard(creature, land, sorcery);

        LivingWish wish = castLivingWish();

        PendingInteraction.LibrarySearch search = pendingSearch();
        assertThat(search.params().cards()).containsExactly(creature, land);
        assertThat(search.params().sourceSideboard()).isTrue();
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().canFailToFind()).isTrue();

        choose(land);

        assertThat(gd.playerHands.get(player1.getId())).contains(land);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(creature, sorcery);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(wish);
    }

    @Test
    @DisplayName("May decline to take a creature or land and still exiles Living Wish")
    void mayDeclineToTakeCard() {
        Card creature = new GrizzlyBears();
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
        Card sorcery = new Divination();
        setSideboard(sorcery);

        LivingWish wish = castLivingWish();

        assertThat(pendingSearch()).isNull();
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(sorcery);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(wish);
    }

    private LivingWish castLivingWish() {
        LivingWish wish = new LivingWish();
        harness.setHand(player1, List.of(wish));
        harness.addMana(player1, ManaColor.GREEN, 1);
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
