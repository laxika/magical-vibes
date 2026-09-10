package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.InteractionOptions;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.service.interaction.InteractionAnswer;
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
    @DisplayName("Resolving combines library and graveyard into a mandatory choice")
    void promptsChoiceOverLibraryAndGraveyard() {
        Card shock = new Shock();
        Card bears = new GrizzlyBears();
        Card elves = new LlanowarElves();
        harness.setLibrary(player1, List.of(shock, bears));
        harness.setGraveyard(player1, List.of(elves));

        cast();

        PendingInteraction.DoomsdayChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.DoomsdayChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.validCardIds()).containsExactly(shock.getId(), bears.getId(), elves.getId());
        assertThat(choice.legalOptions())
                .isEqualTo(new InteractionOptions.MultiCardPick(choice.validCardIds(), 3, 3));
    }

    @Test
    @DisplayName("Keeping the only available card puts it on top without exiling it")
    void keepSingleCard() {
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(shock));
        harness.setGraveyard(player1, List.of());

        cast();

        harness.handleMultipleCardsChosen(player1, List.of(shock.getId()));

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).containsExactly(shock);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .doesNotContain(shock);
    }

    @Test
    @DisplayName("Keeping multiple cards prompts a reorder, then places them on top in that order")
    void keepMultipleCardsReorder() {
        Card shock = new Shock();
        Card bears = new GrizzlyBears();
        Card elves = new LlanowarElves();
        Card secondShock = new Shock();
        Card secondBears = new GrizzlyBears();
        Card secondElves = new LlanowarElves();
        harness.setLibrary(player1, List.of(shock, bears, elves));
        harness.setGraveyard(player1, List.of(secondShock, secondBears, secondElves));

        cast();

        // Keep five cards (pool order: shock, bears, elves, second shock, second bears, second elves).
        harness.handleMultipleCardsChosen(player1,
                List.of(shock.getId(), elves.getId(), secondShock.getId(), secondBears.getId(), secondElves.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactly(bears);

        // Chosen order = [shock, elves, second shock, second bears, second elves]; reverse it.
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(4, 3, 2, 1, 0)));

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).containsExactly(secondElves, secondBears, secondShock, elves, shock);
    }

    @Test
    @DisplayName("Choosing fewer than five cards when five are available is rejected")
    void cannotChooseFewerThanFiveAvailableCards() {
        List<Card> pool = List.of(
                new Shock(), new GrizzlyBears(), new LlanowarElves(), new Shock(), new GrizzlyBears());
        harness.setLibrary(player1, pool.subList(0, 3));
        harness.setGraveyard(player1, pool.subList(3, 5));

        cast();

        assertThatThrownBy(() -> harness.handleMultipleCardsChosen(player1,
                pool.subList(0, 4).stream().map(Card::getId).toList()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DoomsdayChoice.class);
    }

    @Test
    @DisplayName("With five or more available cards, exactly five must be chosen")
    void requiresExactlyFiveCardsWhenPoolIsLarge() {
        List<Card> pool = List.of(
                new Shock(), new GrizzlyBears(), new LlanowarElves(), new Shock(), new GrizzlyBears());
        harness.setLibrary(player1, pool.subList(0, 3));
        harness.setGraveyard(player1, pool.subList(3, 5));

        cast();

        PendingInteraction.DoomsdayChoice choice =
                gd.interaction.activeInteraction(PendingInteraction.DoomsdayChoice.class);
        assertThat(choice).isNotNull();
        assertThat(choice.legalOptions())
                .isEqualTo(new InteractionOptions.MultiCardPick(choice.validCardIds(), 5, 5));
    }

    @Test
    @DisplayName("Half your life loss is rounded up and happens after the card choice")
    void halfLifeRoundedUp() {
        Card shock = new Shock();
        harness.setLibrary(player1, List.of(shock));
        harness.setGraveyard(player1, List.of());
        harness.setLife(player1, 7);

        cast();

        harness.assertLife(player1, 7);
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
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(oppGraveyard);
    }

    @Test
    @DisplayName("An empty library and graveyard still cause the life loss without a choice")
    void emptyZonesStillLoseHalfLifeWithoutChoice() {
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of());
        harness.setLife(player1, 20);

        cast();

        harness.assertLife(player1, 10);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }
}
