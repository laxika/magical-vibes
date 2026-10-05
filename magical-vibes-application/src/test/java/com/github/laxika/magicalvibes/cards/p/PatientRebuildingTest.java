package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.b.BruvacTheGrandiloquent;
import com.github.laxika.magicalvibes.cards.d.DwarvenPriest;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({PatientRebuilding.class, Mountain.class, DwarvenPriest.class, BruvacTheGrandiloquent.class})
class PatientRebuildingTest extends BaseCardTest {

    @Test
    @DisplayName("Upkeep trigger only offers opponents as targets")
    void upkeepTriggerOnlyTargetsOpponents() {
        harness.addToBattlefield(player1, new PatientRebuilding());

        advanceToUpkeep(player1);

        assertThat(gd.interaction.activeInteraction(PendingInteraction.PermanentChoice.class).validIds())
                .containsExactly(player2.getId());
    }

    @Test
    @DisplayName("Opponent mills three and the controller draws one card per land milled")
    void millsThreeAndDrawsPerLand() {
        harness.addToBattlefield(player1, new PatientRebuilding());
        harness.setLibrary(player2, List.of(new Mountain(), new DwarvenPriest(), new Mountain(), new DwarvenPriest()));

        advanceToUpkeep(player1);
        int handSize = gd.playerHands.get(player1.getId()).size();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 2);
    }

    @Test
    @DisplayName("No draws when no land cards are milled")
    void noDrawsWithoutLands() {
        harness.addToBattlefield(player1, new PatientRebuilding());
        harness.setLibrary(player2, List.of(new DwarvenPriest(), new DwarvenPriest(), new DwarvenPriest(), new DwarvenPriest()));

        advanceToUpkeep(player1);
        int handSize = gd.playerHands.get(player1.getId()).size();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
    }

    @Test
    @DisplayName("Mills only the remaining cards when the library is smaller than three")
    void millsOnlyRemainingWhenLibrarySmall() {
        harness.addToBattlefield(player1, new PatientRebuilding());
        harness.setLibrary(player2, List.of(new Mountain(), new DwarvenPriest()));

        advanceToUpkeep(player1);
        int handSize = gd.playerHands.get(player1.getId()).size();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(2);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 1);
    }

    @Test
    @DisplayName("Does not trigger during the opponent's upkeep")
    void doesNotTriggerDuringOpponentsUpkeep() {
        harness.addToBattlefield(player1, new PatientRebuilding());
        harness.setLibrary(player2, List.of(new Mountain(), new Mountain(), new Mountain()));
        int handSize = gd.playerHands.get(player1.getId()).size();

        advanceToUpkeep(player2);

        assertThat(gd.stack).isEmpty();
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(3);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
    }

    @Test
    @DisplayName("An empty library causes no draws, even with lands already in the graveyard")
    void emptyLibraryDoesNotCountExistingGraveyardLands() {
        harness.addToBattlefield(player1, new PatientRebuilding());
        harness.setLibrary(player2, List.of());
        harness.setGraveyard(player2, List.of(new Mountain()));

        advanceToUpkeep(player1);
        int handSize = gd.playerHands.get(player1.getId()).size();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDecks.get(player2.getId())).isEmpty();
        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize);
    }

    @Test
    @DisplayName("Draws for all lands milled when Bruvac doubles the mill")
    void drawsForLandsInEntireDoubledMill() {
        harness.addToBattlefield(player1, new PatientRebuilding());
        harness.addToBattlefield(player1, new BruvacTheGrandiloquent());
        harness.setLibrary(player2, List.of(
                new DwarvenPriest(), new DwarvenPriest(), new DwarvenPriest(),
                new Mountain(), new Mountain(), new Mountain(), new DwarvenPriest()));

        advanceToUpkeep(player1);
        int handSize = gd.playerHands.get(player1.getId()).size();
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerGraveyards.get(player2.getId())).hasSize(6);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(handSize + 3);
    }
}
