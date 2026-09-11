package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BenalishInfantry;
import com.github.laxika.magicalvibes.cards.d.Disrupt;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({ParadigmShift.class, BenalishInfantry.class, Disrupt.class})
class ParadigmShiftTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the whole library, then the graveyard becomes the new library")
    void exilesLibraryThenShufflesGraveyardIn() {
        BenalishInfantry firstLibraryCard = new BenalishInfantry();
        BenalishInfantry secondLibraryCard = new BenalishInfantry();
        Disrupt thirdLibraryCard = new Disrupt();
        BenalishInfantry firstGraveyardCard = new BenalishInfantry();
        Disrupt secondGraveyardCard = new Disrupt();
        ParadigmShift paradigmShift = new ParadigmShift();
        harness.setLibrary(player1, List.of(firstLibraryCard, secondLibraryCard, thirdLibraryCard));
        harness.setGraveyard(player1, List.of(firstGraveyardCard, secondGraveyardCard));
        harness.castFromHand(player1, paradigmShift, "{1}{U}");

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player1.getId()))
                .containsExactlyInAnyOrder(firstGraveyardCard, secondGraveyardCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(paradigmShift);
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(firstLibraryCard, secondLibraryCard, thirdLibraryCard);
    }

    @Test
    @DisplayName("With an empty graveyard the library ends up empty")
    void emptyGraveyardLeavesEmptyLibrary() {
        BenalishInfantry firstLibraryCard = new BenalishInfantry();
        BenalishInfantry secondLibraryCard = new BenalishInfantry();
        ParadigmShift paradigmShift = new ParadigmShift();
        harness.setLibrary(player1, List.of(firstLibraryCard, secondLibraryCard));
        harness.setGraveyard(player1, List.of());
        harness.castFromHand(player1, paradigmShift, "{1}{U}");

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.getPlayerExiledCards(player1.getId()))
                .containsExactlyInAnyOrder(firstLibraryCard, secondLibraryCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(paradigmShift);
    }

    @Test
    @DisplayName("An empty library does not prevent the graveyard shuffle")
    void emptyLibraryStillShufflesGraveyardIn() {
        Disrupt graveyardCard = new Disrupt();
        ParadigmShift paradigmShift = new ParadigmShift();
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.castFromHand(player1, paradigmShift, "{1}{U}");

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(graveyardCard);
        assertThat(gd.getPlayerExiledCards(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(paradigmShift);
    }

    @Test
    @DisplayName("Only the controller's zones are affected")
    void opponentZonesUntouched() {
        BenalishInfantry playerLibraryCard = new BenalishInfantry();
        BenalishInfantry opponentCard = new BenalishInfantry();
        harness.setLibrary(player1, List.of(playerLibraryCard));
        harness.setGraveyard(player2, List.of(opponentCard));
        harness.castFromHand(player1, new ParadigmShift(), "{1}{U}");

        int opponentDeckBefore = harness.getGameData().playerDecks.get(player2.getId()).size();

        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(opponentDeckBefore);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentCard);
    }
}
