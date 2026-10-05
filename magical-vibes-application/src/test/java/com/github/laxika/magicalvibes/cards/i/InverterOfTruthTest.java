package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.k.KozileksTranslator;
import com.github.laxika.magicalvibes.cards.w.Wastes;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ExiledCardEntry;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({InverterOfTruth.class, Wastes.class, KozileksTranslator.class})
class InverterOfTruthTest extends BaseCardTest {

    @Test
    @DisplayName("Exiles the library face down and shuffles the graveyard into it")
    void exilesLibraryFaceDownAndShufflesGraveyardIntoLibrary() {
        Card libraryCard = new Wastes();
        Card secondLibraryCard = new KozileksTranslator();
        Card graveyardCard = new KozileksTranslator();
        harness.setLibrary(player1, List.of(libraryCard, secondLibraryCard));
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setHand(player1, List.of(new InverterOfTruth()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(graveyardCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards)
                .extracting(ExiledCardEntry::card)
                .containsExactly(libraryCard, secondLibraryCard);
        assertThat(gd.exiledCards).allMatch(ExiledCardEntry::faceDown);
    }

    @Test
    @DisplayName("An empty library still receives every graveyard card")
    void emptyLibraryStillReceivesGraveyard() {
        Card land = new Wastes();
        Card creature = new KozileksTranslator();
        harness.setLibrary(player1, List.of());
        harness.setGraveyard(player1, List.of(land, creature));
        harness.setHand(player1, List.of(new InverterOfTruth()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactlyInAnyOrder(land, creature);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).isEmpty();
    }

    @Test
    @DisplayName("An empty graveyard leaves the library empty after the trigger")
    void emptyGraveyardLeavesLibraryEmpty() {
        Card libraryCard = new Wastes();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setGraveyard(player1, List.of());
        harness.setHand(player1, List.of(new InverterOfTruth()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(ExiledCardEntry::card).containsExactly(libraryCard);
        assertThat(gd.exiledCards).allMatch(ExiledCardEntry::faceDown);
        harness.assertOnBattlefield(player1, "Inverter of Truth");
    }

    @Test
    @DisplayName("The trigger waits for resolution and leaves the opponent's zones unchanged")
    void triggerWaitsForResolutionAndOnlyAffectsController() {
        Card libraryCard = new Wastes();
        Card graveyardCard = new KozileksTranslator();
        Card opponentLibraryCard = new KozileksTranslator();
        Card opponentGraveyardCard = new Wastes();
        harness.setLibrary(player1, List.of(libraryCard));
        harness.setGraveyard(player1, List.of(graveyardCard));
        harness.setLibrary(player2, List.of(opponentLibraryCard));
        harness.setGraveyard(player2, List.of(opponentGraveyardCard));
        harness.setHand(player1, List.of(new InverterOfTruth()));
        harness.addMana(player1, ManaColor.BLACK, 4);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        harness.assertOnBattlefield(player1, "Inverter of Truth");
        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(libraryCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).containsExactly(graveyardCard);
        assertThat(gd.exiledCards).isEmpty();

        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).containsExactly(graveyardCard);
        assertThat(gd.playerGraveyards.get(player1.getId())).isEmpty();
        assertThat(gd.exiledCards).extracting(ExiledCardEntry::card).containsExactly(libraryCard);
        assertThat(gd.playerDecks.get(player2.getId())).containsExactly(opponentLibraryCard);
        assertThat(gd.playerGraveyards.get(player2.getId())).containsExactly(opponentGraveyardCard);
    }
}
