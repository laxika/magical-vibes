package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AncientStirringsTest extends BaseCardTest {

    @Test
    @DisplayName("Only colorless cards among the top five are offered")
    void offersOnlyColorlessCards() {
        setupTopFive(List.of(new Memnite(), new LlanowarElves(), new Shock(), new Divination(), new Disenchant()));
        cast();

        PendingInteraction.LibrarySearch search = gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().playerId()).isEqualTo(player1.getId());
        assertThat(search.params().canFailToFind()).isTrue();
        assertThat(search.params().cards()).extracting(Card::getName).containsExactly("Memnite");
    }

    @Test
    @DisplayName("Choosing a colorless card puts it into hand and orders the rest onto the bottom")
    void chosenCardToHandRestOnBottom() {
        Memnite memnite = new Memnite();
        LlanowarElves elves = new LlanowarElves();
        Shock shock = new Shock();
        Divination divination = new Divination();
        Disenchant disenchant = new Disenchant();
        setupTopFive(List.of(memnite, elves, shock, divination, disenchant));
        cast();

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(0));

        harness.assertInHand(player1, "Memnite");
        List<Card> remaining = gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards();
        assertThat(remaining).containsExactly(elves, shock, divination, disenchant);

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(3, 2, 1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(disenchant, divination, shock, elves);
        harness.assertInGraveyard(player1, "Ancient Stirrings");
    }

    @Test
    @DisplayName("Declining leaves all five cards to be ordered onto the bottom")
    void mayDecline() {
        setupTopFive(List.of(new Memnite(), new LlanowarElves(), new Shock(), new Divination(), new Disenchant()));
        cast();

        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(5);
    }

    @Test
    @DisplayName("With no colorless card among the top five, all are put on the bottom")
    void noColorlessCardsGoesStraightToReorder() {
        setupTopFive(List.of(new LlanowarElves(), new Shock(), new Divination(), new Disenchant(), new LlanowarElves()));
        cast();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards()).hasSize(5);
    }

    private void cast() {
        harness.setHand(player1, List.of(new AncientStirrings()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void setupTopFive(List<Card> cards) {
        List<Card> deck = gd.playerDecks.get(player1.getId());
        deck.clear();
        deck.addAll(cards);
    }
}
