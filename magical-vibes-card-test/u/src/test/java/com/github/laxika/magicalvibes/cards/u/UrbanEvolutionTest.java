package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({UrbanEvolution.class, Forest.class})
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

        harness.castAndResolveSorcery(player1, 0, 0);

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

        harness.castAndResolveSorcery(player1, 0, 0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        for (int i = 0; i < 2; i++) {
            harness.playLand(player1, 0);
        }

        assertThat(countPermanents(player1, "Forest")).isEqualTo(2);
    }

    @Test
    @DisplayName("Drawn lands can be played after the normal land play, but only one extra")
    void canPlayDrawnLandAfterNormalLandPlay() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new Forest()));
        harness.playLand(player1, 0);
        Forest first = new Forest();
        Forest second = new Forest();
        Forest third = new Forest();
        harness.setLibrary(player1, List.of(first, second, third, new Forest()));

        harness.castFromHand(player1, new UrbanEvolution(), "{3}{G}{U}");
        harness.passBothPriorities();

        assertThat(gd.playerHands.get(player1.getId())).containsExactly(first, second, third);
        harness.playLand(player1, 0);
        assertThat(countPermanents(player1, "Forest")).isEqualTo(2);
        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.getMaxLandsThisTurn(player2.getId())).isEqualTo(1);
        harness.assertInGraveyard(player1, "Urban Evolution");
    }

    @Test
    @DisplayName("Two resolutions draw three cards each and allow three land plays")
    void multipleResolutionsAccumulate() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setHand(player1, List.of(new UrbanEvolution(), new UrbanEvolution()));
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(),
                new Forest(), new Forest(), new Forest(), new Forest()));
        harness.addMana(player1, ManaColor.GREEN, 8);
        harness.addMana(player1, ManaColor.BLUE, 2);

        harness.castAndResolveSorcery(player1, 0, 0);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(4);
        harness.castAndResolveSorcery(player1, 0, 0);
        assertThat(gd.playerHands.get(player1.getId())).hasSize(6);
        harness.playLand(player1, 0);
        harness.playLand(player1, 0);
        harness.playLand(player1, 0);

        assertThat(countPermanents(player1, "Forest")).isEqualTo(3);
        assertThatThrownBy(() -> harness.playLand(player1, 0))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Unused additional land permission expires before the next turn")
    void additionalLandPermissionExpires() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);
        harness.setLibrary(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));
        harness.setLibrary(player2, List.of(new Forest(), new Forest()));
        harness.castFromHand(player1, new UrbanEvolution(), "{3}{G}{U}");
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
