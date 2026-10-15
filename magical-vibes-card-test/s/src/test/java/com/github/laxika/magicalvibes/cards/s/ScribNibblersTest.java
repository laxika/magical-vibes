package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.t.TectonicEdge;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ScribNibblers.class, TectonicEdge.class})
class ScribNibblersTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping Scrib Nibblers exiles a land and gains 1 life")
    void exilesLandAndGainsLife() {
        addReadyScribNibblers();
        Card land = new TectonicEdge();
        Card remaining = new ScribNibblers();
        harness.setLibrary(player2, List.of(land, remaining));
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(21);
        assertThat(harness.getGameData().getPlayerExiledCards(player2.getId())).containsExactly(land);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(remaining);
    }

    @Test
    @DisplayName("Tapping Scrib Nibblers exiles a nonland without gaining life")
    void exilesNonlandWithoutGainingLife() {
        addReadyScribNibblers();
        Card nonland = new ScribNibblers();
        harness.setLibrary(player2, List.of(nonland));
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(harness.getGameData().getPlayerExiledCards(player2.getId())).containsExactly(nonland);
        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
    }

    @Test
    @DisplayName("Landfall may untap Scrib Nibblers")
    void landfallMayUntap() {
        Permanent scribNibblers = addReadyScribNibblers();
        scribNibblers.tap();
        harness.setHand(player1, List.of(new TectonicEdge()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class).playerId())
                .isEqualTo(player1.getId());
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(scribNibblers.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Declining landfall leaves Scrib Nibblers tapped")
    void decliningLandfallLeavesItTapped() {
        Permanent scribNibblers = addReadyScribNibblers();
        scribNibblers.tap();
        harness.setHand(player1, List.of(new TectonicEdge()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        assertThat(scribNibblers.isTapped()).isTrue();
    }

    @Test
    void canTargetOwnLibraryWithoutMana() {
        Permanent nibblers = addReadyScribNibblers();
        Card land = new TectonicEdge();
        harness.setLibrary(player1, List.of(land));
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, player1.getId());
        assertThat(nibblers.isTapped()).isTrue();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(land);
        harness.passBothPriorities();

        harness.assertLife(player1, 21);
        assertThat(gd.getPlayerExiledCards(player1.getId())).containsExactly(land);
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
    }

    @Test
    void emptyLibraryDoesNotGainLife() {
        Permanent nibblers = addReadyScribNibblers();
        harness.setLibrary(player2, List.of());
        harness.setLife(player1, 20);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 20);
        assertThat(nibblers.isTapped()).isTrue();
        assertThat(gd.getPlayerExiledCards(player2.getId())).isEmpty();
    }

    @Test
    void opposingLandDoesNotTriggerUntap() {
        Permanent nibblers = addReadyScribNibblers();
        nibblers.tap();
        harness.forceActivePlayer(player2);
        harness.setHand(player2, List.of(new TectonicEdge()));

        harness.playLand(player2, 0);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.activeInteraction(PendingInteraction.MayAbilityChoice.class)).isNull();
        assertThat(nibblers.isTapped()).isTrue();
    }

    private Permanent addReadyScribNibblers() {
        Permanent scribNibblers = harness.addToBattlefieldAndReturn(player1, new ScribNibblers());
        scribNibblers.setSummoningSick(false);
        return scribNibblers;
    }
}
