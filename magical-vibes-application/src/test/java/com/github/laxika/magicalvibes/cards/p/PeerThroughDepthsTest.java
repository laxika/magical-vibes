package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class PeerThroughDepthsTest extends BaseCardTest {

    @Test
    @DisplayName("Only instant and sorcery cards among the top five are offered")
    void offersOnlyInstantsAndSorceries() {
        setupTopFive(List.of(new Shock(), new GrizzlyBears(), new Divination(), new Island(), new Swamp()));
        cast();

        GameData gd = harness.getGameData();
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().playerId()).isEqualTo(player1.getId());
        assertThat(search.params().canFailToFind()).isTrue();
        assertThat(search.params().cards().stream().map(Card::getName))
                .containsExactlyInAnyOrder("Shock", "Divination");
    }

    @Test
    @DisplayName("Chosen instant goes to hand and the rest are ordered onto the bottom")
    void chosenCardToHandRestOnBottom() {
        setupTopFive(List.of(new Shock(), new GrizzlyBears(), new Divination(), new Island(), new Swamp()));
        cast();

        GameData gd = harness.getGameData();
        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(indexOf(offered, "Shock")));

        harness.assertInHand(player1, "Shock");

        List<Card> remaining = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        assertThat(remaining).hasSize(4);
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(
                indexOf(remaining, "Island"),
                indexOf(remaining, "Divination"),
                indexOf(remaining, "Swamp"),
                indexOf(remaining, "Grizzly Bears"))));

        assertThat(gd.playerDecks.get(player1.getId()).stream().map(Card::getName))
                .containsExactly("Island", "Divination", "Swamp", "Grizzly Bears");
        harness.assertInGraveyard(player1, "Peer Through Depths");
    }

    @Test
    @DisplayName("The reveal is optional — declining keeps all five and bottoms them")
    void mayDecline() {
        setupTopFive(List.of(new Shock(), new GrizzlyBears(), new Divination(), new Island(), new Swamp()));
        cast();

        GameData gd = harness.getGameData();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(5);
    }

    @Test
    @DisplayName("With no instant or sorcery among the top five, all are put on the bottom")
    void noEligibleCardsGoesStraightToReorder() {
        setupTopFive(List.of(new GrizzlyBears(), new Island(), new Swamp(), new Island(), new GrizzlyBears()));
        cast();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(5);
    }

    @Test
    @DisplayName("With an empty library nothing happens")
    void emptyLibrary() {
        GameData gd = harness.getGameData();
        gd.playerDecks.get(player1.getId()).clear();
        cast();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    private void cast() {
        harness.setHand(player1, List.of(new PeerThroughDepths()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private void setupTopFive(List<Card> cards) {
        List<Card> deck = harness.getGameData().playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(cards);
    }

    private int indexOf(List<Card> cards, String name) {
        for (int i = 0; i < cards.size(); i++) {
            if (cards.get(i).getName().equals(name)) {
                return i;
            }
        }
        throw new IllegalStateException("Card not found in list: " + name);
    }
}
