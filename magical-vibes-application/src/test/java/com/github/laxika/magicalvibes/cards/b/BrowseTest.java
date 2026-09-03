package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Browse.class, GrizzlyBears.class})
class BrowseTest extends BaseCardTest {

    @Test
    @DisplayName("Activating looks at the top five cards and enters a library reveal choice")
    void activatingEntersRevealChoice() {
        addReadyBrowse(player1);
        payMana(player1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class).allCards()).hasSize(5);
    }

    @Test
    @DisplayName("Choosing a card puts it into hand and exiles the other four")
    void choosingPutsOneInHandRestExiled() {
        addReadyBrowse(player1);
        payMana(player1);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        Card top0 = new GrizzlyBears();
        Card top1 = new GrizzlyBears();
        Card top2 = new GrizzlyBears();
        Card top3 = new GrizzlyBears();
        Card top4 = new GrizzlyBears();
        deck.addAll(List.of(top0, top1, top2, top3, top4));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        harness.handleMultipleCardsChosen(player1, List.of(top2.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(top2);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactlyInAnyOrder(top0, top1, top3, top4);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Choosing no card is rejected because exactly one card must be put into hand")
    void choosingNoCardIsRejected() {
        addReadyBrowse(player1);
        payMana(player1);

        Card top0 = new GrizzlyBears();
        Card top1 = new GrizzlyBears();
        harness.setLibrary(player1, List.of(top0, top1));

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessage("Invalid number of cards selected");
    }

    @Test
    @DisplayName("With one card left, it goes to hand and nothing is exiled")
    void oneCardAutoToHand() {
        addReadyBrowse(player1);
        payMana(player1);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        Card only = new GrizzlyBears();
        deck.add(only);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).contains(only);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("With an empty library, nothing happens")
    void emptyLibrary() {
        addReadyBrowse(player1);
        payMana(player1);
        gd.playerDecks.get(player1.getId()).clear();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    private void payMana(Player player) {
        harness.addMana(player, ManaColor.BLUE, 2);
        harness.addMana(player, ManaColor.COLORLESS, 2);
    }

    private void addReadyBrowse(Player player) {
        addCreatureReady(player, new Browse());
    }
}
