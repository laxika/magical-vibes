package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.t.TectonicEdge;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({MysteriesOfTheDeep.class, TectonicEdge.class})
class MysteriesOfTheDeepTest extends BaseCardTest {

    @Test
    @DisplayName("Draws two cards when no land entered under the controller's control this turn")
    void drawsTwoCardsWithoutLandfall() {
        harness.setLibrary(player1, List.of(new TectonicEdge(), new TectonicEdge(), new TectonicEdge(), new TectonicEdge()));
        harness.castFromHand(player1, new MysteriesOfTheDeep(), "{4}{U}");
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerHands.get(player1.getId())).hasSize(2);
        assertThat(harness.getGameData().playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Draws three cards after a land entered under the controller's control this turn")
    void drawsThreeCardsWithLandfall() {
        harness.setHand(player1, List.of(new TectonicEdge()));
        harness.setLibrary(player1, List.of(new TectonicEdge(), new TectonicEdge(), new TectonicEdge(), new TectonicEdge()));
        harness.playLand(player1, 0);
        harness.castFromHand(player1, new MysteriesOfTheDeep(), "{4}{U}");
        harness.passBothPriorities();

        assertThat(harness.getGameData().playerHands.get(player1.getId())).hasSize(3);
        assertThat(harness.getGameData().playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A land already on the battlefield does not enable landfall")
    void existingLandDoesNotEnableLandfall() {
        harness.addToBattlefield(player1, new TectonicEdge());
        harness.setLibrary(player1, List.of(new TectonicEdge(), new TectonicEdge(), new TectonicEdge(), new TectonicEdge()));

        harness.castFromHand(player1, new MysteriesOfTheDeep(), "{4}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("An opponent's land entry does not increase the draw")
    void opponentLandDoesNotEnableLandfall() {
        harness.enterBattlefieldAndReturn(player2, new TectonicEdge());
        harness.setLibrary(player1, List.of(new TectonicEdge(), new TectonicEdge(), new TectonicEdge(), new TectonicEdge()));

        harness.castFromHand(player1, new MysteriesOfTheDeep(), "{4}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(2);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(2);
    }

    @Test
    @DisplayName("Landfall is checked at resolution, including land entries after casting")
    void landEnteringAfterCastingEnablesLandfall() {
        harness.setLibrary(player1, List.of(new TectonicEdge(), new TectonicEdge(), new TectonicEdge(), new TectonicEdge()));
        harness.castFromHand(player1, new MysteriesOfTheDeep(), "{4}{U}");

        harness.enterBattlefieldAndReturn(player1, new TectonicEdge());
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("Multiple land entries still draw only three cards")
    void multipleLandEntriesDrawThreeCards() {
        harness.enterBattlefieldAndReturn(player1, new TectonicEdge());
        harness.enterBattlefieldAndReturn(player1, new TectonicEdge());
        harness.setLibrary(player1, List.of(new TectonicEdge(), new TectonicEdge(), new TectonicEdge(), new TectonicEdge()));

        harness.castFromHand(player1, new MysteriesOfTheDeep(), "{4}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }

    @Test
    @DisplayName("A land that entered this turn still counts after being sacrificed")
    void sacrificedLandStillEnablesLandfall() {
        var target = harness.addToBattlefieldAndReturn(player2, new TectonicEdge());
        for (int i = 0; i < 3; i++) {
            harness.addToBattlefield(player2, new TectonicEdge());
        }
        harness.setHand(player1, List.of(new TectonicEdge()));
        harness.playLand(player1, 0);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        harness.setLibrary(player1, List.of(new TectonicEdge(), new TectonicEdge(), new TectonicEdge(), new TectonicEdge()));

        harness.castFromHand(player1, new MysteriesOfTheDeep(), "{4}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerDecks.get(player1.getId())).hasSize(1);
    }
}
