package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SummerBloomTest extends BaseCardTest {

    @Test
    @DisplayName("Grants three additional land plays this turn")
    void grantsThreeAdditionalLandPlays() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SummerBloom()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(4);
    }

    @Test
    @DisplayName("Controller can play four lands in a single turn")
    void canPlayFourLands() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SummerBloom(),
                new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        for (int i = 0; i < 4; i++) {
            harness.ensurePriority(player1);
            gs.playCard(gd, player1, 0, 0, null, null);
        }

        long forests = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> p.getCard().getName().equals("Forest"))
                .count();
        assertThat(forests).isEqualTo(4);
    }

    @Test
    @DisplayName("A fifth land is no longer playable once the extra plays are used")
    void fifthLandNotPlayable() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new SummerBloom(),
                new Forest(), new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 2);

        harness.castSorcery(player1, 0, 0);
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        for (int i = 0; i < 4; i++) {
            harness.ensurePriority(player1);
            gs.playCard(gd, player1, 0, 0, null, null);
        }

        // One Forest remains in hand (index 0) but the four-land limit is now reached.
        assertThat(harness.getGameBroadcastService().getPlayableCardIndices(gd, player1.getId()))
                .doesNotContain(0);
    }
}
