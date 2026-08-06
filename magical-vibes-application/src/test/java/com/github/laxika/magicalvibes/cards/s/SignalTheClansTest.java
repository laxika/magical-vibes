package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SignalTheClansTest extends BaseCardTest {

    private void castSignalTheClans(List<Card> library) {
        harness.setLibrary(player1, library);
        harness.setHand(player1, List.of(new SignalTheClans()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }

    private List<String> offeredNames() {
        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        return search == null ? null : search.params().cards().stream().map(Card::getName).toList();
    }

    private void pickFromLibrary(String name) {
        List<String> offered = offeredNames();
        int index = offered.indexOf(name);
        assertThat(index).isGreaterThanOrEqualTo(0);
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(index));
    }

    private void declinePick() {
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));
    }

    @Test
    @DisplayName("Three creature cards with different names put one of them into hand at random")
    void threeDifferentNamesPutOneIntoHand() {
        Card bears = new GrizzlyBears();
        Card spider = new GiantSpider();
        Card giant = new HillGiant();
        castSignalTheClans(List.of(bears, spider, giant, new Island()));

        pickFromLibrary("Grizzly Bears");
        pickFromLibrary("Giant Spider");
        pickFromLibrary("Hill Giant");

        List<Card> hand = gd.playerHands.get(player1.getId());
        assertThat(hand).hasSize(1);
        assertThat(hand.getFirst()).isIn(bears, spider, giant);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        assertThat(deck).hasSize(3).doesNotContain(hand.getFirst());
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Signal the Clans");
    }

    @Test
    @DisplayName("Only creature cards can be revealed")
    void onlyCreatureCardsAreOffered() {
        castSignalTheClans(List.of(new GrizzlyBears(), new Island(), new Shock()));

        assertThat(offeredNames()).containsExactly("Grizzly Bears");
    }

    @Test
    @DisplayName("Revealing two cards with the same name puts nothing into hand")
    void duplicateNamesPutNothingIntoHand() {
        castSignalTheClans(List.of(new GrizzlyBears(), new GrizzlyBears(), new HillGiant()));

        pickFromLibrary("Grizzly Bears");
        pickFromLibrary("Grizzly Bears");
        pickFromLibrary("Hill Giant");

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        harness.assertInGraveyard(player1, "Signal the Clans");
    }

    @Test
    @DisplayName("Stopping short of three cards puts nothing into hand")
    void stoppingEarlyPutsNothingIntoHand() {
        castSignalTheClans(List.of(new GrizzlyBears(), new GiantSpider(), new HillGiant()));

        pickFromLibrary("Grizzly Bears");
        pickFromLibrary("Giant Spider");
        declinePick();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Fewer than three creature cards in the library finds nothing")
    void fewerThanThreeCreaturesFindsNothing() {
        castSignalTheClans(List.of(new GrizzlyBears(), new GiantSpider(), new Island()));

        pickFromLibrary("Grizzly Bears");
        pickFromLibrary("Giant Spider");

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A library with no creature cards prompts nothing")
    void noCreaturesInLibraryPromptsNothing() {
        castSignalTheClans(List.of(new Island(), new Shock()));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
        harness.assertInGraveyard(player1, "Signal the Clans");
    }
}
