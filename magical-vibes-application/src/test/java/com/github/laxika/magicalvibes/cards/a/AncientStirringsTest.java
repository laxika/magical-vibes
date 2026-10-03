package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.d.Disenchant;
import com.github.laxika.magicalvibes.cards.d.Divination;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.m.Memnite;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({AncientStirrings.class, Memnite.class, LlanowarElves.class, Shock.class, Divination.class, Disenchant.class, Forest.class})
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

        harness.handleCardChosen(player1, 0);

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

        harness.handleCardChosen(player1, -1);

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

    @Test
    @DisplayName("A land can be selected and unseen cards stay above the reordered cards")
    void landToHandPreservesUnseenCards() {
        Forest first = new Forest();
        Forest second = new Forest();
        Forest third = new Forest();
        Forest fourth = new Forest();
        Forest fifth = new Forest();
        Forest sixth = new Forest();
        Forest seventh = new Forest();
        harness.setLibrary(player1, List.of(first, second, third, fourth, fifth, sixth, seventh));
        cast();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(first, second, third, fourth, fifth);
        harness.handleCardChosen(player1, 2);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(third);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(3, 1, 0, 2)));

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(sixth, seventh, fifth, second, first, fourth);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A short library still allows a choice and orders the rest")
    void shortLibrary() {
        Forest first = new Forest();
        Forest second = new Forest();
        Forest third = new Forest();
        harness.setLibrary(player1, List.of(first, second, third));
        cast();

        harness.handleCardChosen(player1, 1);
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(second);
        harness.getGameService().handleInteractionAnswer(gd, player1,
                new InteractionAnswer.CardOrder(List.of(1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(third, first);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Ancient Stirrings");
    }

    @Test
    @DisplayName("The only eligible card can still be declined")
    void declineSingleCardLibrary() {
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        cast();

        harness.handleCardChosen(player1, -1);

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Ancient Stirrings");
    }

    @Test
    @DisplayName("An empty library resolves without a choice or drawing a card")
    void emptyLibrary() {
        harness.setLibrary(player1, List.of());
        cast();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        harness.assertInGraveyard(player1, "Ancient Stirrings");
    }

    private void cast() {
        harness.setHand(player1, List.of(new AncientStirrings()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void setupTopFive(List<Card> cards) {
        harness.setLibrary(player1, cards);
    }
}
