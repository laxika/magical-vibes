package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({EmbraceTheParadox.class, Forest.class})
class EmbraceTheParadoxTest extends BaseCardTest {

    @Test
    @DisplayName("A land drawn by the spell can be put onto the battlefield")
    void canPutNewlyDrawnLandTapped() {
        Forest drawnLand = new Forest();
        harness.setLibrary(player1, List.of(new EmbraceTheParadox(), drawnLand, new EmbraceTheParadox()));
        harness.castFromHand(player1, new EmbraceTheParadox(), "{3}{G}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3).contains(drawnLand);
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 1);

        assertThat(findPermanent(player1, "Forest").isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId())).hasSize(2).doesNotContain(drawnLand);
        harness.assertNotOnBattlefield(player2, "Forest");
    }

    @Test
    @DisplayName("Drawing still succeeds when there is no land in hand")
    void drawsWithoutLandInHand() {
        harness.setLibrary(player1, List.of(new EmbraceTheParadox(), new EmbraceTheParadox(), new EmbraceTheParadox()));
        harness.castFromHand(player1, new EmbraceTheParadox(), "{3}{G}{U}");
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.playerBattlefields.get(player1.getId())).isEmpty();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Draws three cards and puts a chosen land onto the battlefield tapped")
    void drawsAndPutsLandTapped() {
        harness.setHand(player1, List.of(new EmbraceTheParadox(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        // Hand is now [Forest, drawn1, drawn2, drawn3]; put the Forest (index 0) onto the battlefield.
        harness.handleCardChosen(player1, 0);

        Permanent forest = findPermanent(player1, "Forest");
        assertThat(forest.isTapped()).isTrue();
        // Started with 1 (Forest) + drew 3 - 1 put onto battlefield = 3 cards in hand.
        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
    }

    @Test
    @DisplayName("Declining the land drop leaves the land in hand")
    void decliningLeavesLandInHand() {
        harness.setHand(player1, List.of(new EmbraceTheParadox(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertNotOnBattlefield(player1, "Forest");
        harness.assertInHand(player1, "Forest");
    }
}
