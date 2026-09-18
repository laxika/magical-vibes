package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(NaturalSelection.class)
class NaturalSelectionTest extends BaseCardTest {

    @Test
    @DisplayName("Resolving reorders the top three cards of the target's library")
    void resolvingEntersReorderOfTargetsLibrary() {
        harness.setHand(player1, List.of(new NaturalSelection()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        List<Card> targetDeck = gd.playerDecks.get(player2.getId());
        Card top0 = targetDeck.get(0);
        Card top1 = targetDeck.get(1);
        Card top2 = targetDeck.get(2);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder.cards()).containsExactly(top0, top1, top2);
        assertThat(reorder.deckOwnerId()).isEqualTo(player2.getId());
        assertThat(reorder.playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("After reordering, the controller is asked whether to shuffle the target's library")
    void afterReorderControllerAskedToShuffle() {
        harness.setHand(player1, List.of(new NaturalSelection()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1, 2)));

        PendingInteraction.MayAbilityChoice may =
                gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class);
        assertThat(may).isNotNull();
        assertThat(may.playerId()).isEqualTo(player1.getId());
    }

    @Test
    @DisplayName("Reordering can put the original third card on top")
    void reorderingChangesTargetsTopCard() {
        harness.setHand(player1, List.of(new NaturalSelection()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        Card originallyThird = gd.playerDecks.get(player2.getId()).get(2);
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 0, 1)));
        harness.handleMayAbilityChosen(player1, false);

        assertThat(gd.playerDecks.get(player2.getId()).get(0)).isSameAs(originallyThird);
    }

    @Test
    @DisplayName("Accepting the choice shuffles the target's library")
    void acceptingShuffleKeepsTargetLibraryCards() {
        harness.setHand(player1, List.of(new NaturalSelection()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        List<Card> before = List.copyOf(gd.playerDecks.get(player2.getId()));
        harness.castInstant(player1, 0, player2.getId());
        harness.passBothPriorities();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1, 2)));
        harness.handleMayAbilityChosen(player1, true);

        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyInAnyOrderElementsOf(before);
        assertThat(gameLogContains(player2.getUsername() + " shuffles their library")).isTrue();
    }

    @Test
    @DisplayName("A short target library reorders all available cards")
    void shortTargetLibraryReordersAllAvailableCards() {
        Card first = new NaturalSelection();
        Card second = new NaturalSelection();
        harness.setLibrary(player2, List.of(first, second));
        harness.setHand(player1, List.of(new NaturalSelection()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, player2.getId());
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
    @DisplayName("Natural Selection can target the caster's own library")
    void canTargetOwnLibrary() {
        harness.setHand(player1, List.of(new NaturalSelection()));
        harness.addMana(player1, ManaColor.GREEN, 1);

        harness.castInstant(player1, 0, player1.getId());
        harness.passBothPriorities();

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.deckOwnerId()).isEqualTo(player1.getId());
    }
}
