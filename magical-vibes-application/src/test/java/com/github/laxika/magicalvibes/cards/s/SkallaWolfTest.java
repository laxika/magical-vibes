package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SkallaWolfTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers one green card from the top five")
    void offersGreenCardFromTopFive() {
        Card green = new LlanowarElves();
        List<Card> topFive = List.of(new Shock(), green, new Island(), new Plains(), new Mountain());
        setLibrary(topFive);

        castAndResolve();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactly(green.getId());
        assertThat(choice.maxCount()).isEqualTo(1);
        assertThat(choice.randomRemainingToBottom()).isTrue();

        harness.handleMultipleCardsChosen(player1, List.of(green.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(green);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(4).doesNotContain(green)
                .containsExactlyInAnyOrder(topFive.get(0), topFive.get(2), topFive.get(3), topFive.get(4));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the optional reveal leaves all five cards in the library")
    void mayDeclineGreenCard() {
        Card green = new LlanowarElves();
        List<Card> topFive = List.of(green, new Shock(), new Island(), new Plains(), new Mountain());
        setLibrary(topFive);

        castAndResolve();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).doesNotContain(green);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5).containsExactlyInAnyOrderElementsOf(topFive);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("ETB has no choice when the top five contain no green card")
    void noGreenCardMeansNoChoice() {
        List<Card> topFive = List.of(new Shock(), new Island(), new Plains(), new Mountain(), new Shock());
        setLibrary(topFive);

        castAndResolve();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(5).containsExactlyInAnyOrderElementsOf(topFive);
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new SkallaWolf()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }

    private void setLibrary(List<Card> cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(cards);
    }
}
