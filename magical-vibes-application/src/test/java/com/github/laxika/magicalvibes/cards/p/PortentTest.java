package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.action.DrawCardsAtNextUpkeep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(Portent.class)
class PortentTest extends BaseCardTest {
    @Test
    @DisplayName("Resolving enters a reorder of the top 3 cards of the target's library")
    void resolvingEntersReorderOfTargetsLibrary() {
        harness.setHand(player1, List.of(new Portent()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        List<Card> targetDeck = gd.playerDecks.get(player2.getId());
        Card top0 = targetDeck.get(0);
        Card top1 = targetDeck.get(1);
        Card top2 = targetDeck.get(2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder.cards()).containsExactly(top0, top1, top2);
        assertThat(reorder.deckOwnerId()).isEqualTo(player2.getId());
        assertThat(reorder.playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Reordering places the chosen card on top of the target's library")
    void reorderingChangesTargetsTopCard() {
        harness.setHand(player1, List.of(new Portent()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        List<Card> targetDeck = gd.playerDecks.get(player2.getId());
        Card originallyThird = targetDeck.get(2);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        // Controller decides: put the original third card on top.
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 0, 1)));
        // Decline the shuffle so the reorder stands.
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player2.getId()).get(0)).isSameAs(originallyThird);
    }
    @Test
    @DisplayName("After reorder the controller is asked whether to shuffle")
    void afterReorderControllerAskedToShuffle() {
        harness.setHand(player1, List.of(new Portent()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1, 2)));

        PendingInteraction.MayAbilityChoice may =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(may).isNotNull();
        assertThat(may.playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Accepting the shuffle keeps the target's library size but schedules the draw")
    void acceptingShuffleKeepsTargetLibrarySize() {
        harness.setHand(player1, List.of(new Portent()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        int targetDeckSize = gd.playerDecks.get(player2.getId()).size();

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1, 2)));
        harness.handleMayAbilityChosen(player1, true);

        // The target's library is not drawn from; the shuffle just randomizes it.
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(targetDeckSize);
        assertThat(gameLogContains(player2.getUsername() + " shuffles their library")).isTrue();
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).hasSize(1);
    }
    @Test
    @DisplayName("Resolving schedules a draw for the caster at the next upkeep")
    void schedulesDrawForCaster() {
        harness.setHand(player1, List.of(new Portent()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1, 2)));
        harness.handleMayAbilityChosen(player1, false);

        List<DrawCardsAtNextUpkeep> scheduled = gd.getDelayedActions(DrawCardsAtNextUpkeep.class);
        assertThat(scheduled).hasSize(1);
        assertThat(scheduled.getFirst().controllerId()).isEqualTo(player1.getId());
        assertThat(scheduled.getFirst().count()).isEqualTo(1);
    }

    @Test
    @DisplayName("The scheduled draw resolves at the next upkeep for the caster")
    void drawResolvesAtNextUpkeep() {
        harness.setHand(player1, List.of(new Portent()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1, 2)));
        harness.handleMayAbilityChosen(player1, false);

        int handBefore = gd.playerHands.get(player1.getId()).size();
        int deckBefore = gd.playerDecks.get(player1.getId()).size();

        advanceToUpkeep(player2);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(handBefore + 1);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(deckBefore - 1);
        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).isEmpty();
    }

    @Test
    @DisplayName("A target library with fewer than three cards reorders all available cards")
    void shortTargetLibraryReordersAllAvailableCards() {
        Card first = new Portent();
        Card second = new Portent();
        harness.setLibrary(player2, List.of(first, second));
        harness.setHand(player1, List.of(new Portent()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder.cards()).containsExactly(first, second);
        assertThat(reorder.deckOwnerId()).isEqualTo(player2.getId());

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(second, first);
    }

    @Test
    @DisplayName("An empty target library still offers the shuffle choice and delayed draw")
    void emptyTargetLibraryStillOffersShuffleAndDelayedDraw() {
        harness.setLibrary(player2, List.of());
        harness.setHand(player1, List.of(new Portent()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.MayAbilityChoice may =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(may).isNotNull();
        assertThat(may.playerId()).isEqualTo(player1.getId());

        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.getDelayedActions(DrawCardsAtNextUpkeep.class)).hasSize(1);
    }
    @Test
    @DisplayName("Portent can target the caster's own library")
    void canTargetOwnLibrary() {
        harness.setHand(player1, List.of(new Portent()));
        harness.addMana(player1, ManaColor.BLUE, 1);

        harness.castSorcery(player1, 0, player1.getId());
        harness.passBothPriorities();

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.deckOwnerId()).isEqualTo(player1.getId());
    }
}
