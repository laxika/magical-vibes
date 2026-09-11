package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Doomsday.class, Shock.class, GrizzlyBears.class, LlanowarElves.class})
class DoomsdayTest extends BaseCardTest {

    private void cast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.setHand(player1, List.of(new Doomsday()));
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Choice and reordering finish before the controller loses half life")
    void choiceAndReorderingFinishBeforeLifeLoss() {
        Card shock = new Shock();
        Card bears = new GrizzlyBears();
        Card elves = new LlanowarElves();
        harness.setLibrary(player1, List.of(shock, bears));
        harness.setGraveyard(player1, List.of(elves));
        harness.setLife(player1, 20);

        cast();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DoomsdayChoice.class);
        harness.assertLife(player1, 20);

        harness.handleMultipleCardsChosen(player1, List.of(shock.getId(), bears.getId(), elves.getId()));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1, 2)));

        harness.assertLife(player1, 10);
    }

    @Test
    @DisplayName("Keeping five cards puts them on top and exiles the rest")
    void keepsFiveCardsAndExilesTheRest() {
        Card first = new Shock();
        Card second = new GrizzlyBears();
        Card third = new LlanowarElves();
        Card fourth = new Shock();
        Card fifth = new GrizzlyBears();
        Card sixth = new LlanowarElves();
        harness.setLibrary(player1, List.of(first, second, third));
        harness.setGraveyard(player1, List.of(fourth, fifth, sixth));

        cast();

        harness.handleMultipleCardsChosen(player1,
                List.of(first.getId(), second.getId(), third.getId(), fourth.getId(), fifth.getId()));
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(4, 3, 2, 1, 0)));

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).containsExactly(fifth, fourth, third, second, first);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(sixth);
    }

    @Test
    @DisplayName("Keeping all available cards when fewer than five are available allows reordering")
    void keepsAllAvailableCardsWhenFewerThanFiveAndReorders() {
        Card shock = new Shock();
        Card bears = new GrizzlyBears();
        Card elves = new LlanowarElves();
        harness.setLibrary(player1, List.of(shock, bears));
        harness.setGraveyard(player1, List.of(elves));

        cast();

        harness.handleMultipleCardsChosen(player1, List.of(shock.getId(), bears.getId(), elves.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);

        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(2, 0, 1)));

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).containsExactly(elves, shock, bears);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Keeping all available cards when fewer than five are available exiles nothing")
    void keepsAllCardsWhenFewerThanFiveAreAvailable() {
        Card shock = new Shock();
        Card bears = new GrizzlyBears();
        harness.setLibrary(player1, List.of(shock));
        harness.setGraveyard(player1, List.of(bears));

        cast();

        harness.handleMultipleCardsChosen(player1, List.of(shock.getId(), bears.getId()));
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(0, 1)));

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(shock, bears);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
    }

    @Test
    @DisplayName("Rejects selecting fewer than five cards when at least five are available")
    void requiresFiveCardsWhenEnoughAreAvailable() {
        Card first = new Shock();
        Card second = new GrizzlyBears();
        Card third = new LlanowarElves();
        Card fourth = new Shock();
        Card fifth = new GrizzlyBears();
        Card sixth = new LlanowarElves();
        harness.setLibrary(player1, List.of(first, second, third, fourth, fifth, sixth));

        cast();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                List.of(first.getId(), second.getId(), third.getId(), fourth.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rejects selecting fewer than all available cards when fewer than five are available")
    void requiresAllCardsWhenFewerThanFiveAreAvailable() {
        Card first = new Shock();
        Card second = new GrizzlyBears();
        harness.setLibrary(player1, List.of(first, second));

        cast();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1, List.of(first.getId())))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Half your life loss is rounded up")
    void halfLifeRoundedUp() {
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(shock));
        harness.setGraveyard(player1, List.of());
        harness.setLife(player1, 7);

        cast();

        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));
        harness.assertLife(player1, 3);
    }

    @Test
    @DisplayName("Only the controller's library and graveyard are affected")
    void onlyAffectsController() {
        Card ownLibrary = new Shock();
        harness.setLibrary(player1, List.of(ownLibrary));
        harness.setGraveyard(player1, List.of());

        Card oppLibrary = new GrizzlyBears();
        Card oppGraveyard = new LlanowarElves();
        harness.setLibrary(player2, List.of(oppLibrary));
        harness.setGraveyard(player2, List.of(oppGraveyard));

        cast();
        harness.handleMultipleCardsChosen(player1, List.of(ownLibrary.getId()));

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(oppLibrary);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(oppGraveyard);
    }
}
