package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Explore.class, Forest.class})
class ExploreTest extends BaseCardTest {

    @Test
    @DisplayName("Draws a card and grants one additional land play this turn")
    void drawsCardAndGrantsAdditionalLandPlay() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, List.of(new Forest()));
        harness.castFromHand(player1, new Explore(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).hasSize(1);
        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("The drawn land can be played after the normal land play is used")
    void canPlayDrawnLandAfterNormalLandPlay() {
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        Forest drawnLand = new Forest();
        harness.setLibrary(player1, List.of(drawnLand, new Forest()));

        harness.castFromHand(player1, new Explore(), "{1}{G}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(drawnLand);
        harness.playLand(player1, 0);
        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(2);
        harness.setHand(player1, List.of(new Forest()));
        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getMaxLandsThisTurn(player2.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("Two Explores allow three land plays and draw one card each")
    void multipleExploresAccumulate() {
        Forest firstDraw = new Forest();
        Forest secondDraw = new Forest();
        harness.setLibrary(player1, List.of(firstDraw, secondDraw, new Forest()));

        harness.castFromHand(player1, new Explore(), "{1}{G}");
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(firstDraw);
        harness.castFromHand(player1, new Explore(), "{1}{G}");
        harness.passBothPriorities();
        assertThat(gd.playerHands.get(player1.getId())).containsExactly(secondDraw);

        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.playLand(player1, 0);
        harness.playLand(player1, 0);
        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId())).hasSize(3);
        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Unused additional land permission expires before the next turn")
    void additionalLandPermissionExpires() {
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.castFromHand(player1, new Explore(), "{1}{G}");
        harness.passBothPriorities();

        harness.passUntil(player2, TurnStep.PRECOMBAT_MAIN);
        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(1);
        assertThat(gd.getMaxLandsThisTurn(player2.getId())).isEqualTo(1);
        harness.passUntil(player1, TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Forest(), new Forest()));
        harness.playLand(player1, 0);
        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }
}
