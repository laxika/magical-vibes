package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.d.Desert;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.i.IrrigatedFarmland;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SandcloudHarbinger.class, SunscorchedDesert.class, Desert.class, GrizzlyBears.class, IrrigatedFarmland.class})
@DisplayName("Sandcloud Harbinger")
class SandcloudHarbingerTest extends BaseCardTest {

    @Test
    @DisplayName("Conjures three Sunscorched Deserts into the top ten cards of each library")
    void conjuresDesertsIntoEachLibrary() {
        List<Card> playerOneLibrary = cards(10);
        List<Card> playerTwoLibrary = cards(10);
        harness.setLibrary(player1, playerOneLibrary);
        harness.setLibrary(player2, playerTwoLibrary);

        harness.enterBattlefieldAndReturn(player1, new SandcloudHarbinger());
        resolveAllTriggers();

        assertConjuredDeserts(player1);
        assertConjuredDeserts(player2);
    }

    @Test
    @DisplayName("A player who plays a Desert draws and the Harbinger's controller gains life")
    void desertPlayRewardsPlayerAndController() {
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.addToBattlefield(player1, new SandcloudHarbinger());
        resolveAllTriggers();
        harness.setHand(player2, List.of(new Desert()));
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();

        int controllerLife = gd.playerLifeTotals.get(player1.getId());
        harness.playLand(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).hasSize(1);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(controllerLife + 3);
    }

    @Test
    @DisplayName("Conjures three Deserts even into empty and short libraries")
    void conjuresIntoShortLibraries() {
        harness.setLibrary(player1, List.of());
        harness.setLibrary(player2, cards(2));

        harness.enterBattlefieldAndReturn(player1, new SandcloudHarbinger());
        resolveAllTriggers();

        assertThat(gd.playerDecks.get(player1.getId())).hasSize(3)
                .allMatch(card -> card instanceof SunscorchedDesert);
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(5);
        assertThat(gd.playerDecks.get(player2.getId()).stream()
                .filter(card -> card instanceof SunscorchedDesert)).hasSize(3);
    }

    @Test
    @DisplayName("The controller draws and gains life when playing a Desert")
    void controllerPlaysDesert() {
        harness.addToBattlefield(player1, new SandcloudHarbinger());
        GrizzlyBears drawnCard = new GrizzlyBears();
        harness.setLibrary(player1, List.of(drawnCard));
        harness.setHand(player1, List.of(new Desert()));
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int life = gd.playerLifeTotals.get(player1.getId());

        harness.playLand(player1, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnCard);
        harness.assertLife(player1, life + 3);
    }

    @Test
    @DisplayName("Putting a Desert onto the battlefield does not trigger the rewards")
    void desertEntryWithoutPlayingDoesNotTrigger() {
        harness.addToBattlefield(player1, new SandcloudHarbinger());
        harness.setHand(player2, List.of());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        int life = gd.playerLifeTotals.get(player1.getId());

        harness.enterBattlefieldAndReturn(player2, new Desert());
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        harness.assertLife(player1, life);
    }

    @Test
    @DisplayName("Playing a non-Desert land does not trigger the rewards")
    void nonDesertPlayDoesNotTrigger() {
        harness.addToBattlefield(player1, new SandcloudHarbinger());
        harness.setLibrary(player2, List.of(new GrizzlyBears()));
        harness.setHand(player2, List.of(new IrrigatedFarmland()));
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.clearPriorityPassed();
        int life = gd.playerLifeTotals.get(player1.getId());

        harness.playLand(player2, 0);
        resolveAllTriggers();

        assertThat(gd.playerHands.get(player2.getId())).isEmpty();
        assertThat(gd.playerDecks.get(player2.getId())).hasSize(1);
        harness.assertLife(player1, life);
    }

    private void assertConjuredDeserts(com.github.laxika.magicalvibes.model.Player player) {
        List<Card> library = gd.playerDecks.get(player.getId());
        assertThat(library).hasSize(13);
        assertThat(library.subList(0, 10).stream()
                .filter(card -> "Sunscorched Desert".equals(card.getName())))
                .hasSize(3);
        assertThat(library.subList(10, 13)).allMatch(card -> !"Sunscorched Desert".equals(card.getName()));
        assertThat(library.stream()
                .filter(card -> "Sunscorched Desert".equals(card.getName())))
                .allMatch(card -> player.getId().equals(card.getOwnerId()));
    }

    private List<Card> cards(int count) {
        List<Card> cards = new ArrayList<>();
        for (int i = 0; i < count; i++) {
            cards.add(new GrizzlyBears());
        }
        return cards;
    }
}
