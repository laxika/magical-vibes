package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.s.Spellbook;
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

@CardUsed({GoldenWish.class, Spellbook.class, GloriousAnthem.class, GrizzlyBears.class})
class GoldenWishTest extends BaseCardTest {

    @Test
    @DisplayName("Offers artifact and enchantment cards from outside the game")
    void offersArtifactAndEnchantmentCards() {
        Card artifact = new Spellbook();
        Card enchantment = new GloriousAnthem();
        Card creature = new GrizzlyBears();
        setSideboard(artifact, enchantment, creature);

        GoldenWish wish = castGoldenWish();

        PendingInteraction.LibrarySearch search = pendingSearch();
        assertThat(search.params().cards()).containsExactly(artifact, enchantment);
        assertThat(search.params().sourceSideboard()).isTrue();
        assertThat(search.params().reveals()).isTrue();
        assertThat(search.params().canFailToFind()).isTrue();

        choose(artifact);

        assertThat(gd.playerHands.get(player1.getId())).contains(artifact);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(enchantment, creature);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(wish);
    }

    @Test
    @DisplayName("Can fail to find and still exiles Golden Wish")
    void canFailToFind() {
        Card artifact = new Spellbook();
        setSideboard(artifact);

        GoldenWish wish = castGoldenWish();
        choose(null);

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(artifact);
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(artifact);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(wish);
    }

    @Test
    @DisplayName("Does not prompt when outside-the-game cards do not match")
    void noMatchingCardNoPrompt() {
        Card creature = new GrizzlyBears();
        setSideboard(creature);

        GoldenWish wish = castGoldenWish();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class)).isNull();
        assertThat(gd.playerSideboards.get(player1.getId())).containsExactly(creature);
        assertThat(gd.getPlayerExiledCards(player1.getId())).contains(wish);
    }

    private GoldenWish castGoldenWish() {
        GoldenWish wish = new GoldenWish();
        harness.setHand(player1, List.of(wish));
        harness.addMana(player1, ManaColor.WHITE, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
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
