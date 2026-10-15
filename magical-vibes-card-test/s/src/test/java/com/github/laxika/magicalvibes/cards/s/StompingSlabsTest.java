package com.github.laxika.magicalvibes.cards.s;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.cards.e.ElvishWarrior;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

@CardUsed({StompingSlabs.class, ElvishWarrior.class})
class StompingSlabsTest extends BaseCardTest {

    private List<Card> filler(int count) {
        List<Card> cards = new ArrayList<>();
        IntStream.range(0, count).forEach(i -> cards.add(new ElvishWarrior()));
        return cards;
    }

    private List<Card> libraryWithCopy() {
        List<Card> library = filler(6);
        library.add(new StompingSlabs());
        return library;
    }

    private void castAt(UUID targetId) {
        harness.setHand(player1, List.of(new StompingSlabs()));
        harness.addMana(player1, ManaColor.RED, 3); // {2}{R}
        harness.castAndResolveSorcery(player1, 0, targetId);
    }

    @Test
    @DisplayName("A revealed copy of Stomping Slabs deals 7 damage to a target player")
    void copyRevealedDamagesPlayer() {
        harness.setLibrary(player1, libraryWithCopy());

        castAt(player2.getId());

        List<Card> reorder = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(IntStream.range(0, reorder.size()).boxed().toList()));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
    }

    @Test
    @DisplayName("A revealed copy deals damage only after the revealed cards are bottomed")
    void copyRevealedDamageWaitsForBottoming() {
        harness.setLibrary(player1, libraryWithCopy());

        castAt(player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);

        List<Card> reorder = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(IntStream.range(0, reorder.size()).boxed().toList()));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
    }

    @Test
    @DisplayName("No damage is dealt when no copy of Stomping Slabs is revealed")
    void noCopyRevealedDealsNoDamage() {
        harness.setLibrary(player1, filler(7));

        castAt(player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
    }

    @Test
    @DisplayName("A revealed copy deals 7 damage to a target creature, destroying it")
    void copyRevealedDamagesCreature() {
        harness.addToBattlefield(player2, new ElvishWarrior());
        harness.setLibrary(player1, libraryWithCopy());

        castAt(harness.getPermanentId(player2, "Elvish Warrior"));

        // Finish bottoming the revealed cards so the lethal-damage state-based check runs.
        List<Card> reorder = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(IntStream.range(0, reorder.size()).boxed().toList()));

        harness.assertNotOnBattlefield(player2, "Elvish Warrior");
        harness.assertInGraveyard(player2, "Elvish Warrior");
    }

    @Test
    @DisplayName("A short library reveals all available cards")
    void shortLibraryRevealsAllAvailableCards() {
        List<Card> library = List.of(new ElvishWarrior(), new StompingSlabs());
        harness.setLibrary(player1, library);

        castAt(player2.getId());

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder.cards()).containsExactlyElementsOf(library);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(13);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(library.get(1), library.get(0));
    }

    @Test
    @DisplayName("An empty library reveals no cards and deals no damage")
    void emptyLibraryDealsNoDamage() {
        harness.setLibrary(player1, List.of());

        castAt(player2.getId());

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("All revealed cards are put on the bottom of the library in any order")
    void revealedCardsGoToBottom() {
        harness.setLibrary(player1, libraryWithCopy());

        castAt(player2.getId());

        // Seven cards were revealed, so their controller orders them onto the bottom.
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        List<Card> reorder = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        assertThat(reorder).hasSize(7);

        List<Integer> order = IntStream.range(0, reorder.size()).boxed().toList();
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(order));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(7);
    }

    @Test
    @DisplayName("Revealed cards can be put on the bottom in a chosen order")
    void revealedCardsCanBeReordered() {
        harness.setLibrary(player1, libraryWithCopy());

        castAt(player2.getId());

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        List<Card> revealedCards = List.copyOf(reorder.cards());
        List<Integer> order = IntStream.range(0, reorder.cards().size())
                .map(i -> reorder.cards().size() - 1 - i)
                .boxed()
                .toList();
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(order));

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyElementsOf(order.stream().map(revealedCards::get).toList());
    }

    @Test
    @DisplayName("A copy below the top seven is not revealed and the unrevealed cards stay on top")
    void copyBelowTopSevenDealsNoDamage() {
        List<Card> library = filler(7);
        Card hiddenCopy = new StompingSlabs();
        library.add(hiddenCopy);
        harness.setLibrary(player1, library);

        castAt(player2.getId());

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder.cards()).containsExactlyElementsOf(library.subList(0, 7));
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(6, 5, 4, 3, 2, 1, 0)));

        harness.assertLife(player2, 20);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(
                hiddenCopy, library.get(6), library.get(5), library.get(4), library.get(3),
                library.get(2), library.get(1), library.get(0));
    }

    @Test
    @DisplayName("Revealing multiple copies still deals only seven damage")
    void multipleCopiesDealSevenDamage() {
        List<Card> library = filler(5);
        library.add(new StompingSlabs());
        library.add(new StompingSlabs());
        harness.setLibrary(player1, library);

        castAt(player2.getId());

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(0, 1, 2, 3, 4, 5, 6)));

        harness.assertLife(player2, 13);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(library);
    }
}
