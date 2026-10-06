package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.i.Island;
import com.github.laxika.magicalvibes.cards.d.DruidOfTheCowl;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.Plains;
import com.github.laxika.magicalvibes.cards.t.TitanicGrowth;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkallaWolf.class, DruidOfTheCowl.class, Shock.class, Island.class, Plains.class, Mountain.class})
class SkallaWolfTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers one green card from the top five")
    void offersGreenCardFromTopFive() {
        Card green = new DruidOfTheCowl();
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
        Card green = new DruidOfTheCowl();
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

    @Test
    @DisplayName("Only the top five are eligible and the rest go below untouched cards")
    void leavesUntouchedCardsAboveRemainder() {
        Card firstGreen = new DruidOfTheCowl();
        Card secondGreen = new SkallaWolf();
        Card sixth = new DruidOfTheCowl();
        Card seventh = new Island();
        List<Card> topFive = List.of(firstGreen, new Shock(), secondGreen, new Plains(), new Mountain());
        setLibrary(List.of(topFive.get(0), topFive.get(1), topFive.get(2), topFive.get(3), topFive.get(4), sixth, seventh));

        castAndResolve();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(firstGreen.getId(), secondGreen.getId());
        harness.handleMultipleCardsChosen(player1, List.of(secondGreen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(secondGreen);
        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(6).startsWith(sixth, seventh);
        assertThat(library.subList(2, 6)).containsExactlyInAnyOrder(firstGreen, topFive.get(1), topFive.get(3), topFive.get(4));
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining with a short library still leaves the green card in the library")
    void mayDeclineWithOneCardLibrary() {
        Card green = new DruidOfTheCowl();
        setLibrary(List.of(green));

        castAndResolve();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(green);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A short library allows taking its green card")
    void selectsFromShortLibrary() {
        Card green = new DruidOfTheCowl();
        Card other = new Shock();
        setLibrary(List.of(other, green));

        castAndResolve();
        harness.handleMultipleCardsChosen(player1, List.of(green.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(green);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(other);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library resolves without a choice or drawing a card")
    void emptyLibrary() {
        setLibrary(List.of());

        castAndResolve();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @CardUsed({TitanicGrowth.class, SatyrEnchanter.class})
    @DisplayName("Green instants and multicolored green cards are eligible")
    void acceptsAnyGreenCard() {
        Card instant = new TitanicGrowth();
        Card multicolored = new SatyrEnchanter();
        setLibrary(List.of(instant, multicolored, new Shock()));

        castAndResolve();

        PendingInteraction.LibraryRevealChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.LibraryRevealChoice.class);
        assertThat(choice.validCardIds()).containsExactlyInAnyOrder(instant.getId(), multicolored.getId());
        harness.handleMultipleCardsChosen(player1, List.of(instant.getId()));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(instant);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2).contains(multicolored);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With no green card among the top five, those cards go below the sixth card")
    void noMatchKeepsUnseenGreenCardOnTop() {
        List<Card> topFive = List.of(new Shock(), new Island(), new Plains(), new Mountain(), new Shock());
        Card sixth = new DruidOfTheCowl();
        setLibrary(List.of(topFive.get(0), topFive.get(1), topFive.get(2), topFive.get(3), topFive.get(4), sixth));

        castAndResolve();

        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(6).startsWith(sixth);
        assertThat(library.subList(1, 6)).containsExactlyInAnyOrderElementsOf(topFive);
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new SkallaWolf()));
        harness.addMana(player1, ManaColor.GREEN, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.castCreature(player1, 0);
        resolveAllTriggers();
    }

    private void setLibrary(List<Card> cards) {
        harness.setLibrary(player1, cards);
    }
}
