package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RitesOfSpringTest extends BaseCardTest {

    @Test
    @DisplayName("Discards two cards and puts up to two revealed basic lands into hand")
    void discardsAndSearchesForThatManyBasicLands() {
        castWithHand(new GrizzlyBears(), new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new GrizzlyBears()));

        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 2);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Island");
        assertThat(search.params().reveals()).isTrue();

        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Island");
        assertThat(gd.playerGraveyards.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Rites of Spring", "Grizzly Bears", "Grizzly Bears");
    }

    @Test
    @DisplayName("Choosing zero discards leaves the hand unchanged and searches for zero lands")
    void zeroDiscardSearchesForNoLands() {
        castWithHand(new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new Island()));

        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Grizzly Bears");
        assertThat(gd.playerDecks.get(player1.getId())).extracting(Card::getName)
                .containsExactlyInAnyOrder("Forest", "Island");
    }

    @Test
    @DisplayName("The basic land search may stop before reaching the discard count")
    void maySearchForFewerLands() {
        castWithHand(new GrizzlyBears(), new GrizzlyBears());
        harness.setLibrary(player1, List.of(new Forest(), new Island(), new Forest()));

        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 2);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.getGameService().handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).extracting(Card::getName)
                .containsExactly("Forest");
    }

    private void castWithHand(Card... cardsToDiscard) {
        List<Card> hand = new ArrayList<>();
        hand.add(new RitesOfSpring());
        hand.addAll(List.of(cardsToDiscard));
        harness.setHand(player1, hand);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castSorcery(player1, 0, 0);
    }
}
