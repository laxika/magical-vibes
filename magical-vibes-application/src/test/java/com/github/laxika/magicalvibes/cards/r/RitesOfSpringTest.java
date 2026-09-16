package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.CephalidColiseum;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RitesOfSpring.class, CephalidColiseum.class, Forest.class, Island.class})
class RitesOfSpringTest extends BaseCardTest {

    @Test
    @DisplayName("Discards two cards and puts up to two revealed basic lands into hand")
    void discardsAndSearchesForThatManyBasicLands() {
        CephalidColiseum discarded1 = new CephalidColiseum();
        CephalidColiseum discarded2 = new CephalidColiseum();
        Forest forest = new Forest();
        Island island = new Island();
        CephalidColiseum nonBasicLand = new CephalidColiseum();
        RitesOfSpring ritesOfSpring = castWithHand(discarded1, discarded2);
        harness.setLibrary(player1, List.of(forest, island, nonBasicLand));

        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 2);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search).isNotNull();
        assertThat(search.params().cards()).containsExactlyInAnyOrder(forest, island);
        assertThat(search.params().reveals()).isTrue();

        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactlyInAnyOrder(forest, island);
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .containsExactlyInAnyOrder(ritesOfSpring, discarded1, discarded2);
        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("Library is shuffled"));
    }

    @Test
    @DisplayName("Choosing zero discards leaves the hand unchanged and searches for zero lands")
    void zeroDiscardSearchesForNoLands() {
        CephalidColiseum cardToKeep = new CephalidColiseum();
        Forest forest = new Forest();
        Island island = new Island();
        castWithHand(cardToKeep);
        harness.setLibrary(player1, List.of(forest, island));

        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 0);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(cardToKeep);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, island);
        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("Library is shuffled"));
    }

    @Test
    @DisplayName("The basic land search may stop before reaching the discard count")
    void maySearchForFewerLands() {
        Forest forest = new Forest();
        Island island = new Island();
        CephalidColiseum discarded1 = new CephalidColiseum();
        CephalidColiseum discarded2 = new CephalidColiseum();
        CephalidColiseum nonBasicLand = new CephalidColiseum();
        castWithHand(discarded1, discarded2);
        harness.setLibrary(player1, List.of(forest, island, nonBasicLand));

        harness.passBothPriorities();
        harness.handleXValueChosen(player1, 2);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, 0);
        harness.handleCardChosen(player1, -1);

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(forest);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(island, nonBasicLand);
    }

    @Test
    @DisplayName("With no other cards in hand, the spell discards zero without prompting")
    void noCardsToDiscard() {
        Forest forest = new Forest();
        Island island = new Island();
        RitesOfSpring ritesOfSpring = castWithHand();
        harness.setLibrary(player1, List.of(forest, island));

        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(forest, island);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(ritesOfSpring);
        assertThat(gd.gameLog).anyMatch(entry -> entry.plainText().contains("Library is shuffled"));
    }

    private RitesOfSpring castWithHand(Card... cardsToDiscard) {
        RitesOfSpring ritesOfSpring = new RitesOfSpring();
        List<Card> hand = new ArrayList<>();
        hand.add(ritesOfSpring);
        hand.addAll(List.of(cardsToDiscard));
        harness.setHand(player1, hand);
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castSorcery(player1, 0, 0);
        return ritesOfSpring;
    }
}
