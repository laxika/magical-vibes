package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.e.EliteVanguard;
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

@CardUsed({CourageousOutrider.class, EliteVanguard.class, GrizzlyBears.class, Shock.class, Forest.class})
class CourageousOutriderTest extends BaseCardTest {

    @Test
    @DisplayName("ETB offers the Human card among the top four")
    void etbOffersHumanCard() {
        Card human = humanCard();
        setupTopFour(List.of(human, new GrizzlyBears(), new Shock(), new Forest()));
        castAndResolve();

        PendingInteraction.LibrarySearch search =
                gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class);
        assertThat(search.params().cards()).containsExactly(human);
    }

    @Test
    @DisplayName("Choosing a Human puts it into hand and reorders the rest to the bottom")
    void choosingHumanPutsItIntoHand() {
        Card human = humanCard();
        Shock shock = new Shock();
        GrizzlyBears bears = new GrizzlyBears();
        Forest forest = new Forest();
        setupTopFour(List.of(human, shock, bears, forest));
        castAndResolve();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));
        harness.assertInHand(player1, "Elite Vanguard");
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards())
                .containsExactly(shock, bears, forest);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest, bears, shock);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining the optional Human reveal puts all four cards on the bottom")
    void decliningRevealPutsAllCardsOnBottom() {
        Card human = humanCard();
        Shock shock = new Shock();
        GrizzlyBears bears = new GrizzlyBears();
        Forest forest = new Forest();
        setupTopFour(List.of(human, shock, bears, forest));
        castAndResolve();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));
        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards())
                .containsExactly(human, shock, bears, forest);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(3, 2, 1, 0)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest, bears, shock, human);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Declining or finding no Human puts all four cards on the bottom")
    void noHumanIsPutOnBottom() {
        Shock shock = new Shock();
        GrizzlyBears bears = new GrizzlyBears();
        Forest forest = new Forest();
        Shock secondShock = new Shock();
        setupTopFour(List.of(shock, bears, forest, secondShock));
        castAndResolve();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(3, 2, 1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(secondShock, forest, bears, shock);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Only one Human is revealed and the rest go below the untouched library")
    void selectsOneOfMultipleHumansAndBottomsOnlyTopFour() {
        CourageousOutrider firstHuman = new CourageousOutrider();
        CourageousOutrider secondHuman = new CourageousOutrider();
        Forest firstForest = new Forest();
        Forest secondForest = new Forest();
        Forest untouched = new Forest();
        harness.setLibrary(player1, List.of(firstHuman, firstForest, secondHuman, secondForest, untouched));
        castAndResolve();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibrarySearch.class).params().cards())
                .containsExactly(firstHuman, secondHuman);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(1));
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(secondHuman);
        assertThat(gd.gameLog).anyMatch(log -> log.plainText()
                .contains("reveals Courageous Outrider and puts it into their hand"));
        assertThat(gd.interaction.activeInteraction(PendingInteraction.LibraryReorder.class).cards())
                .containsExactly(firstHuman, firstForest, secondForest);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 0, 1)));

        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactly(untouched, secondForest, firstHuman, firstForest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A Human below the top four cannot be chosen")
    void humanBelowTopFourIsNotOffered() {
        Forest first = new Forest();
        Forest second = new Forest();
        Forest third = new Forest();
        Forest fourth = new Forest();
        CourageousOutrider fifth = new CourageousOutrider();
        harness.setLibrary(player1, List.of(first, second, third, fourth, fifth));
        castAndResolve();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(3, 2, 1, 0)));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(fifth, fourth, third, second, first);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("A short library still allows choosing a Human and bottoms the remaining card")
    void choosesHumanFromShortLibrary() {
        Forest forest = new Forest();
        CourageousOutrider human = new CourageousOutrider();
        harness.setLibrary(player1, List.of(forest, human));
        castAndResolve();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(0));

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(human);
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(forest);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("The only card in the library can be declined even when it is Human")
    void declinesOnlyHumanInLibrary() {
        CourageousOutrider human = new CourageousOutrider();
        harness.setLibrary(player1, List.of(human));
        castAndResolve();

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.LibraryCardChosen(-1));

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(human);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library finishes without offering a choice")
    void emptyLibraryDoesNothing() {
        harness.setLibrary(player1, List.of());
        castAndResolve();

        assertThat(gd.playerHands.get(player1.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.interaction.activeInteraction()).isNull();
        assertThat(gd.stack).isEmpty();
        harness.assertOnBattlefield(player1, "Courageous Outrider");
    }

    private void setupTopFour(List<Card> cards) {
        harness.setLibrary(player1, cards);
    }

    private Card humanCard() {
        return new EliteVanguard();
    }

    private void castAndResolve() {
        harness.setHand(player1, List.of(new CourageousOutrider()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.passBothPriorities();
    }
}
