package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class UrbanEvolutionTest extends BaseCardTest {

    @Test
    @DisplayName("Draws three cards and grants one additional land play")
    void drawsThreeAndGrantsExtraLandPlay() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new UrbanEvolution()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(3);
        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("Controller can play two lands in the same turn")
    void canPlayTwoLands() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new UrbanEvolution(), new Forest(), new Forest()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 3);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        for (int i = 0; i < 2; i++) {
            harness.ensurePriority(player1);
            gs.playCard(gd, player1, 0, 0, null, null);
        }

        assertThat(countPermanents(player1, "Forest")).isEqualTo(2);
    }
}
