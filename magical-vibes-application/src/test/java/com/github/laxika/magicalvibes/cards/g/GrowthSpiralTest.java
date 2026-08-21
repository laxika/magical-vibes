package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GrowthSpiralTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card and puts a chosen land from hand onto the battlefield untapped")
    void drawsAndPutsLandOntoBattlefield() {
        harness.setHand(player1, List.of(new GrowthSpiral(), new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addManaForGrowthSpiral();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, true);
        harness.passBothPriorities();
        harness.handleCardChosen(player1, 0);

        Permanent forest = findPermanent(player1, "Forest");
        assertThat(forest.isTapped()).isFalse();
        harness.assertInHand(player1, "Grizzly Bears");
    }

    @Test
    @DisplayName("Declining the land drop still draws a card")
    void decliningLandDropStillDraws() {
        harness.setHand(player1, List.of(new GrowthSpiral(), new Forest()));
        harness.setLibrary(player1, List.of(new GrizzlyBears()));
        addManaForGrowthSpiral();

        harness.castInstant(player1, 0);
        harness.passBothPriorities();
        harness.handleMayAbilityChosen(player1, false);

        harness.assertInHand(player1, "Forest");
        harness.assertInHand(player1, "Grizzly Bears");
    }

    private void addManaForGrowthSpiral() {
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.BLUE, 1);
    }
}
