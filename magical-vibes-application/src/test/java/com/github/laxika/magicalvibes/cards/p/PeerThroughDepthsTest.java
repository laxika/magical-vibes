package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.h.HanaKami;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LavaSpike;
import com.github.laxika.magicalvibes.cards.r.ReachThroughMists;
import com.github.laxika.magicalvibes.cards.s.Swamp;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PeerThroughDepths.class, ReachThroughMists.class, LavaSpike.class, HanaKami.class,
        Island.class, Swamp.class})
class PeerThroughDepthsTest extends BaseCardTest {

    @Test
    @DisplayName("Only instant and sorcery cards among the top five are offered")
    void offersOnlyInstantsAndSorceries() {
        setupTopFive(List.of(new ReachThroughMists(), new HanaKami(), new LavaSpike(), new Island(), new Swamp()));
        cast();

        GameData gd = harness.getGameData();
        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().playerId()).isEqualTo(player1.getId());
        assertThat(search.params().canFailToFind()).isTrue();
        assertThat(search.params().cards().stream().map(Card::getName))
                .containsExactlyInAnyOrder("Reach Through Mists", "Lava Spike");
    }

    @Test
    @DisplayName("Chosen instant or sorcery goes to hand and the rest are ordered onto the bottom")
    void chosenCardToHandRestOnBottom() {
        setupTopFive(List.of(new ReachThroughMists(), new HanaKami(), new LavaSpike(), new Island(), new Swamp()));
        cast();

        GameData gd = harness.getGameData();
        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        harness.handleCardChosen(player1, indexOf(offered, "Lava Spike"));

        harness.assertInHand(player1, "Lava Spike");

        List<Card> remaining = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        assertThat(remaining).hasSize(4);
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(
                indexOf(remaining, "Island"),
                indexOf(remaining, "Reach Through Mists"),
                indexOf(remaining, "Swamp"),
                indexOf(remaining, "Hana Kami"))));

        assertThat(gd.playerDecks.get(player1.getId()).stream().map(Card::getName))
                .containsExactly("Island", "Reach Through Mists", "Swamp", "Hana Kami");
        harness.assertInGraveyard(player1, "Peer Through Depths");
    }

    @Test
    @DisplayName("The reveal is optional — declining keeps all five and bottoms them")
    void mayDecline() {
        setupTopFive(List.of(new ReachThroughMists(), new HanaKami(), new LavaSpike(), new Island(), new Swamp()));
        cast();

        GameData gd = harness.getGameData();
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();
        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSizeBefore);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(5);
    }

    @Test
    @DisplayName("With no instant or sorcery among the top five, all are put on the bottom")
    void noEligibleCardsGoesStraightToReorder() {
        setupTopFive(List.of(new HanaKami(), new Island(), new Swamp(), new Island(), new HanaKami()));
        cast();

        GameData gd = harness.getGameData();
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(5);
    }

    @Test
    @DisplayName("With an empty library nothing happens")
    void emptyLibrary() {
        GameData gd = harness.getGameData();
        harness.setLibrary(player1, List.of());
        cast();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("With fewer than five cards, all available cards are considered")
    void considersShortLibrary() {
        setupTopFive(List.of(new ReachThroughMists(), new HanaKami(), new Island()));
        cast();

        GameData gd = harness.getGameData();
        List<Card> offered = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards();
        assertThat(offered.stream().map(Card::getName)).containsExactly("Reach Through Mists");

        harness.handleCardChosen(player1, 0);
        harness.assertInHand(player1, "Reach Through Mists");

        List<Card> remaining = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(indexOf(remaining, "Island"), indexOf(remaining, "Hana Kami"))));
        assertThat(gd.playerDecks.get(player1.getId()).stream().map(Card::getName))
                .containsExactly("Island", "Hana Kami");
    }

    private void cast() {
        harness.castFromHand(player1, new PeerThroughDepths(), "{1}{U}");
        harness.passBothPriorities();
    }

    private void setupTopFive(List<Card> cards) {
        harness.setLibrary(player1, cards);
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
