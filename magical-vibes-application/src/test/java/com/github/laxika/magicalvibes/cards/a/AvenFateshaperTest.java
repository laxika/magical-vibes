package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.ValueSource;

import java.util.List;
import java.util.stream.IntStream;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed(AvenFateshaper.class)
class AvenFateshaperTest extends BaseCardTest {

    @Test
    @DisplayName("Entering lets its controller reorder the top four cards")
    void enteringReordersTopFourCards() {
        harness.setHand(player1, List.of(new AvenFateshaper()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card top0 = deck.get(0);
        Card top1 = deck.get(1);
        Card top2 = deck.get(2);
        Card top3 = deck.get(3);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.cards()).containsExactly(top0, top1, top2, top3);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(3, 2, 1, 0)));

        assertThat(deck).containsSubsequence(top3, top2, top1, top0);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Activating the ability lets its controller reorder the top four cards")
    void activatedAbilityReordersTopFourCards() {
        addCreatureReady(player1, new AvenFateshaper());
        harness.addMana(player1, ManaColor.BLUE, 5);

        List<Card> deck = gd.playerDecks.get(player1.getId());
        Card top0 = deck.get(0);
        Card top1 = deck.get(1);
        Card top2 = deck.get(2);
        Card top3 = deck.get(3);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        PendingInteraction.LibraryReorder reorder =
                gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
        assertThat(reorder).isNotNull();
        assertThat(reorder.cards()).containsExactly(top0, top1, top2, top3);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 3, 0, 2)));

        assertThat(deck).containsSubsequence(top1, top3, top0, top2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @ParameterizedTest
    @ValueSource(ints = {0, 1, 2, 3})
    void enteringWithShortLibraryReordersOnlyAvailableCards(int size) {
        List<Card> cards = IntStream.range(0, size)
                .mapToObj(i -> (Card) new AvenFateshaper()).toList();
        harness.setLibrary(player1, cards);
        harness.setHand(player1, List.of(new AvenFateshaper()));
        harness.addMana(player1, ManaColor.BLUE, 7);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        if (size > 0) {
            PendingInteraction.LibraryReorder reorder =
                    gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
            assertThat(reorder).isNotNull();
            assertThat(reorder.cards()).containsExactlyElementsOf(cards);
            gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(
                    IntStream.range(0, size).map(i -> size - 1 - i).boxed().toList()));
        }

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(
                IntStream.range(0, size).mapToObj(i -> cards.get(size - 1 - i)).toList());
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    void tappedSummoningSickCreatureCanActivateRepeatedly() {
        var permanent = harness.addToBattlefieldAndReturn(player1, new AvenFateshaper());
        permanent.setSummoningSick(true);
        permanent.tap();
        List<Card> cards = List.of(new AvenFateshaper(), new AvenFateshaper(),
                new AvenFateshaper(), new AvenFateshaper());
        harness.setLibrary(player1, cards);
        List<Card> opponentLibrary = List.copyOf(gd.playerDecks.get(player2.getId()));
        harness.addMana(player1, ManaColor.BLUE, 10);

        for (int activation = 0; activation < 2; activation++) {
            harness.activateAbility(player1, 0, null, null);
            harness.passBothPriorities();
            PendingInteraction.LibraryReorder reorder =
                    gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class);
            assertThat(reorder).isNotNull();
            assertThat(reorder.playerId()).isEqualTo(player1.getId());
            gs.handleInteractionAnswer(gd, player1,
                    new InteractionAnswer.CardOrder(List.of(3, 2, 1, 0)));
        }

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyElementsOf(cards);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactlyElementsOf(opponentLibrary);
        assertThat(permanent.isTapped()).isTrue();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
