package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class RevivingVaporsTest extends BaseCardTest {

    @Test
    @DisplayName("Chooses one revealed card for hand, puts the rest in the graveyard, and gains its mana value")
    void choosesCardAndGainsItsManaValue() {
        Card chosen = new GrizzlyBears();
        Card restOne = new Shock();
        Card restTwo = new Shock();
        harness.setLibrary(player1, List.of(chosen, restOne, restTwo));
        castRevivingVapors();

        GameData gameData = harness.getGameData();
        assertThat(gameData.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        int lifeBeforeChoice = gameData.playerLifeTotals.get(player1.getId());

        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gameData.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gameData.playerGraveyards.get(player1.getId())).contains(restOne, restTwo);
        assertThat(gameData.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBeforeChoice + 2);
        assertThat(gameData.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With one card in the library, it is put into hand and its mana value is gained")
    void oneCardLibrary() {
        Card chosen = new GrizzlyBears();
        harness.setLibrary(player1, List.of(chosen));
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        castRevivingVapors();

        assertThat(gd.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore + 2);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("An empty library does not cause life gain")
    void emptyLibrary() {
        harness.setLibrary(player1, List.of());
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());
        castRevivingVapors();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    private void castRevivingVapors() {
        harness.setHand(player1, List.of(new RevivingVapors()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0);
        harness.passBothPriorities();
    }
}
