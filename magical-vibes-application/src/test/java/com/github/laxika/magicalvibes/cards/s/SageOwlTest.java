package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import java.util.List;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GrizzlyBears.class, SageOwl.class})
class SageOwlTest extends BaseCardTest {

    @Test
    @DisplayName("Casting Sage Owl puts it on the stack")
    void castingPutsOnStack() {
        harness.castFromHand(player1, new SageOwl(), "{1}{U}");

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard()).isInstanceOf(SageOwl.class);
    }

    @Test
    @DisplayName("Resolving Sage Owl enters battlefield and triggers ETB reorder")
    void resolvingEntersBattlefieldAndTriggersEtb() {
        SageOwl sageOwl = new SageOwl();
        harness.castFromHand(player1, sageOwl, "{1}{U}");

        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() == sageOwl);

        assertThat(gd.stack).hasSize(1);
        StackEntry trigger = gd.stack.getFirst();
        assertThat(trigger.getEntryType()).isEqualTo(StackEntryType.TRIGGERED_ABILITY);
        assertThat(trigger.getCard()).isSameAs(sageOwl);
    }

    @Test
    @DisplayName("Resolving ETB enters library reorder state")
    void resolvingEtbEntersLibraryReorderState() {
        harness.castFromHand(player1, new SageOwl(), "{1}{U}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).playerId()).isEqualTo(player1.getId());
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(4);
    }

    @Test
    @DisplayName("Library reorder changes top cards of library")
    void libraryReorderChangesTopCards() {
        Card originalTop0 = new GrizzlyBears();
        Card originalTop1 = new GrizzlyBears();
        Card originalTop2 = new GrizzlyBears();
        Card originalTop3 = new GrizzlyBears();
        Card untouched = new GrizzlyBears();
        harness.setLibrary(player1, List.of(originalTop0, originalTop1, originalTop2, originalTop3, untouched));
        harness.castFromHand(player1, new SageOwl(), "{1}{U}");

        List<Card> deck = gd.playerDecks.get(player1.getId());

        resolveAllTriggers();

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(3, 2, 1, 0)));

        assertThat(deck).containsExactly(originalTop3, originalTop2, originalTop1, originalTop0, untouched);
    }

    @Test
    @DisplayName("Library reorder clears awaiting state")
    void libraryReorderClearsAwaitingState() {
        harness.castFromHand(player1, new SageOwl(), "{1}{U}");

        resolveAllTriggers();

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1, 2, 3)));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
    }

    @Test
    @DisplayName("Library with fewer than 4 cards reorders available cards")
    void libraryWithFewerThanFourCards() {
        Card cardA = new GrizzlyBears();
        Card cardB = new GrizzlyBears();
        harness.setLibrary(player1, List.of(cardA, cardB));
        List<Card> deck = gd.playerDecks.get(player1.getId());

        harness.castFromHand(player1, new SageOwl(), "{1}{U}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(2);

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(deck.get(0)).isSameAs(cardB);
        assertThat(deck.get(1)).isSameAs(cardA);
    }

    @Test
    @DisplayName("Library with exactly 1 card skips reorder prompt")
    void libraryWithOneCardSkipsReorder() {
        Card onlyCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(onlyCard));

        harness.castFromHand(player1, new SageOwl(), "{1}{U}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(onlyCard);
        assertThat(gameLogContains("looks at the top card")).isTrue();
    }

    @Test
    @DisplayName("Empty library skips reorder entirely")
    void emptyLibrarySkipsReorder() {
        harness.setLibrary(player1, List.of());

        harness.castFromHand(player1, new SageOwl(), "{1}{U}");
        resolveAllTriggers();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gameLogContains("library is empty")).isTrue();
    }

    @Test
    @DisplayName("Flying prevents a nonflying creature from blocking Sage Owl")
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        addCreatureReady(player1, new SageOwl());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(flying)");
    }
}
