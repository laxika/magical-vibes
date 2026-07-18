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
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class DoomsdayTest extends BaseCardTest {

    private void cast() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.addMana(player1, ManaColor.BLACK, 3);
        harness.setHand(player1, List.of(new Doomsday()));
        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();
    }

    private void setLibrary(List<Card> cards) {
        gd.playerDecks.get(player1.getId()).clear();
        gd.playerDecks.get(player1.getId()).addAll(cards);
    }

    private void setGraveyard(List<Card> cards) {
        gd.playerGraveyards.get(player1.getId()).clear();
        gd.playerGraveyards.get(player1.getId()).addAll(cards);
    }

    @Test
    @DisplayName("Resolving prompts a choice over library+graveyard and controller loses half life")
    void promptsChoiceAndLosesHalfLife() {
        setLibrary(List.of(new Shock(), new GrizzlyBears()));
        setGraveyard(List.of(new LlanowarElves()));
        harness.setLife(player1, 20);

        cast();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.DoomsdayChoice.class);
        harness.assertLife(player1, 10);
    }

    @Test
    @DisplayName("Keeping a single card puts it on top; the rest are exiled")
    void keepSingleCard() {
        Card shock = new Shock();
        Card bears = new GrizzlyBears();
        setLibrary(List.of(shock));
        setGraveyard(List.of(bears));

        cast();

        harness.handleMultipleCardsChosen(player1, List.of(bears.getId()));

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(1);
        assertThat(library.getFirst().getId()).isEqualTo(bears.getId());
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(shock.getId()));
    }

    @Test
    @DisplayName("Keeping multiple cards prompts a reorder, then places them on top in that order")
    void keepMultipleCardsReorder() {
        Card shock = new Shock();
        Card bears = new GrizzlyBears();
        Card elves = new LlanowarElves();
        setLibrary(List.of(shock, bears));
        setGraveyard(List.of(elves));

        cast();

        // Keep Shock and Llanowar Elves (pool order: shock, bears, elves).
        harness.handleMultipleCardsChosen(player1, List.of(shock.getId(), elves.getId()));

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryReorder.class);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(bears.getId()));

        // Chosen order = [shock, elves]; reorder [1, 0] puts Elves on top, Shock second.
        gs.handleInteractionAnswer(gd, player1, new InteractionAnswer.CardOrder(List.of(1, 0)));

        List<Card> library = gd.playerDecks.get(player1.getId());
        assertThat(library).hasSize(2);
        assertThat(library.get(0).getId()).isEqualTo(elves.getId());
        assertThat(library.get(1).getId()).isEqualTo(shock.getId());
    }

    @Test
    @DisplayName("Keeping zero cards exiles everything and leaves the library empty")
    void keepZeroCards() {
        Card shock = new Shock();
        Card bears = new GrizzlyBears();
        setLibrary(List.of(shock));
        setGraveyard(List.of(bears));

        cast();

        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .anyMatch(c -> c.getId().equals(shock.getId()))
                .anyMatch(c -> c.getId().equals(bears.getId()));
    }

    @Test
    @DisplayName("Half your life loss is rounded up")
    void halfLifeRoundedUp() {
        setLibrary(List.of(new Shock()));
        setGraveyard(List.of());
        harness.setLife(player1, 7);

        cast();

        harness.assertLife(player1, 3);
    }

    @Test
    @DisplayName("Only the controller's library and graveyard are affected")
    void onlyAffectsController() {
        setLibrary(List.of(new Shock()));
        setGraveyard(List.of());

        Card oppLibrary = new GrizzlyBears();
        Card oppGraveyard = new LlanowarElves();
        gd.playerDecks.get(player2.getId()).clear();
        gd.playerDecks.get(player2.getId()).add(oppLibrary);
        gd.playerGraveyards.get(player2.getId()).clear();
        gd.playerGraveyards.get(player2.getId()).add(oppGraveyard);

        cast();
        harness.handleMultipleCardsChosen(player1, List.of());

        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(oppLibrary);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(oppGraveyard);
    }
}
