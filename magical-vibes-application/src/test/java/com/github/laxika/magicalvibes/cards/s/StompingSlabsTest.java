package com.github.laxika.magicalvibes.cards.s;

import static org.assertj.core.api.Assertions.assertThat;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;
import java.util.stream.IntStream;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class StompingSlabsTest extends BaseCardTest {

    private List<Card> filler(int count) {
        List<Card> cards = new ArrayList<>();
        IntStream.range(0, count).forEach(i -> cards.add(new GrizzlyBears()));
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
        harness.castSorcery(player1, 0, targetId);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("A revealed copy of Stomping Slabs deals 7 damage to a target player")
    void copyRevealedDamagesPlayer() {
        harness.setLibrary(player1, libraryWithCopy());

        castAt(player2.getId());

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
        harness.addToBattlefield(player2, new GrizzlyBears());
        harness.setLibrary(player1, libraryWithCopy());

        castAt(harness.getPermanentId(player2, "Grizzly Bears"));

        // Finish bottoming the revealed cards so the lethal-damage state-based check runs.
        List<Card> reorder = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(IntStream.range(0, reorder.size()).boxed().toList()));

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
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
}
