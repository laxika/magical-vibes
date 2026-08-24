package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
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

@CardUsed({BondOfFlourishing.class, Forest.class, GrizzlyBears.class, Shock.class})
class BondOfFlourishingTest extends BaseCardTest {

    @Test
    @DisplayName("Offers only permanent cards from the top three")
    void offersPermanentCards() {
        GrizzlyBears bears = new GrizzlyBears();
        Shock shock = new Shock();
        Forest forest = new Forest();
        setupTopCards(bears, shock, forest);
        cast();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().canFailToFind()).isTrue();
        assertThat(search.params().cards()).containsExactly(bears, forest);
    }

    @Test
    @DisplayName("Puts the chosen permanent in hand, orders the rest on the bottom, and gains 3 life")
    void choosesPermanentOrdersRestAndGainsLife() {
        GrizzlyBears bears = new GrizzlyBears();
        Shock shock = new Shock();
        Forest forest = new Forest();
        setupTopCards(bears, shock, forest);
        int lifeBefore = gd.getLife(player1.getId());
        cast();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards())
                .containsExactly(shock, forest);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).contains(bears);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest, shock);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    @DisplayName("May decline the permanent and still order all three cards on the bottom")
    void mayDeclinePermanent() {
        GrizzlyBears bears = new GrizzlyBears();
        Shock shock = new Shock();
        Forest forest = new Forest();
        setupTopCards(bears, shock, forest);
        int lifeBefore = gd.getLife(player1.getId());
        cast();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards())
                .containsExactly(bears, shock, forest);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest, shock, bears);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    @Test
    @DisplayName("With no permanent among the top three, orders them on the bottom and gains life")
    void noPermanentCards() {
        Shock shock1 = new Shock();
        Shock shock2 = new Shock();
        Shock shock3 = new Shock();
        setupTopCards(shock1, shock2, shock3);
        int lifeBefore = gd.getLife(player1.getId());
        cast();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards())
                .containsExactly(shock1, shock2, shock3);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 0, 1)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(shock3, shock1, shock2);
        assertThat(gd.getLife(player1.getId())).isEqualTo(lifeBefore + 3);
    }

    private void cast() {
        harness.setHand(player1, List.of(new BondOfFlourishing()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void setupTopCards(Card... cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(List.of(cards));
    }
}
