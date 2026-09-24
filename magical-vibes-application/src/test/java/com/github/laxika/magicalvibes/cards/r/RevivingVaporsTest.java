package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.a.Absorb;
import com.github.laxika.magicalvibes.cards.a.AncientSpring;
import com.github.laxika.magicalvibes.cards.o.Opt;
import com.github.laxika.magicalvibes.cards.q.QuirionElves;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RevivingVapors.class, Absorb.class, AncientSpring.class, Opt.class, QuirionElves.class})
class RevivingVaporsTest extends BaseCardTest {

    @Test
    @DisplayName("Chooses one revealed card for hand, puts the rest in the graveyard, and gains its mana value")
    void choosesCardAndGainsItsManaValue() {
        Card chosen = new QuirionElves();
        Card restOne = new Opt();
        Card restTwo = new Opt();
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
    @DisplayName("Gains life for the selected card even when it is not the first revealed card")
    void choosesNonFirstCardAndGainsItsManaValue() {
        Card restOne = new Opt();
        Card chosen = new Absorb();
        Card restTwo = new QuirionElves();
        harness.setLibrary(player1, List.of(restOne, chosen, restTwo));
        int lifeBeforeChoice = gd.playerLifeTotals.get(player1.getId());
        castRevivingVapors();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(restOne, restTwo);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBeforeChoice + 3);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("Gains no life when the chosen card has mana value zero")
    void chosenLandHasZeroManaValue() {
        Card chosen = new AncientSpring();
        Card restOne = new Opt();
        Card restTwo = new QuirionElves();
        harness.setLibrary(player1, List.of(chosen, restOne, restTwo));
        int lifeBeforeChoice = gd.playerLifeTotals.get(player1.getId());
        castRevivingVapors();

        assertThat(gd.interaction.activeInteraction()).isInstanceOf(PendingInteraction.LibraryRevealChoice.class);
        harness.handleMultipleCardsChosen(player1, List.of(chosen.getId()));

        assertThat(gd.playerHands.get(player1.getId())).contains(chosen);
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(restOne, restTwo);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBeforeChoice);
        assertThat(gd.interaction.activeInteraction()).isNull();
    }

    @Test
    @DisplayName("With one card in the library, it is put into hand and its mana value is gained")
    void oneCardLibrary() {
        Card chosen = new QuirionElves();
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
        harness.castFromHand(player1, new RevivingVapors(), "{2}{W}{U}");
        harness.passBothPriorities();
    }
}
