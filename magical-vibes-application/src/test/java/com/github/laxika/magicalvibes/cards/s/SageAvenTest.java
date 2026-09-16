package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.cards.g.GlorySeeker;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SageAven.class, GlorySeeker.class})
class SageAvenTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving Sage Aven enters battlefield and triggers ETB reorder")
    void resolvingEntersBattlefieldAndTriggersEtb() {
        SageAven sageAven = new SageAven();
        harness.castFromHand(player1, sageAven, "{3}{U}");

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == sageAven);

        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard()).isSameAs(sageAven);
    }

    @Test
    @DisplayName("Resolving ETB enters library reorder state over top four cards")
    void resolvingEtbEntersLibraryReorderState() {
        harness.castFromHand(player1, new SageAven(), "{3}{U}");

        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(4);
    }

    @Test
    @DisplayName("Library reorder changes top cards of library")
    void libraryReorderChangesTopCards() {
        harness.castFromHand(player1, new SageAven(), "{3}{U}");

        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card originalTop0 = deck.get(0);
        Card originalTop1 = deck.get(1);
        Card originalTop2 = deck.get(2);
        Card originalTop3 = deck.get(3);

        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(3, 2, 1, 0)));

        assertThat(deck.get(0)).isSameAs(originalTop3);
        assertThat(deck.get(1)).isSameAs(originalTop2);
        assertThat(deck.get(2)).isSameAs(originalTop1);
        assertThat(deck.get(3)).isSameAs(originalTop0);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Library with fewer than four cards reorders available cards")
    void libraryWithFewerThanFourCards() {
        Card cardA = new GlorySeeker();
        Card cardB = new GlorySeeker();
        harness.setLibrary(player1, List.of(cardA, cardB));
        List<Card> deck = gd.playerDecks.get(player1.getId());

        harness.castFromHand(player1, new SageAven(), "{3}{U}");
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(2);

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(deck.get(0)).isSameAs(cardB);
        assertThat(deck.get(1)).isSameAs(cardA);
    }

    @Test
    @DisplayName("Empty library skips reorder entirely")
    void emptyLibrarySkipsReorder() {
        harness.setLibrary(player1, List.of());

        harness.castFromHand(player1, new SageAven(), "{3}{U}");
        harness.passBothPriorities(); // resolve creature
        harness.passBothPriorities(); // resolve ETB

        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Flying prevents a nonflying creature from blocking Sage Aven")
    void flyingPreventsNonflyingCreatureFromBlocking() {
        addCreatureReady(player1, new SageAven());
        addCreatureReady(player2, new GlorySeeker());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(flying)");
    }
}
