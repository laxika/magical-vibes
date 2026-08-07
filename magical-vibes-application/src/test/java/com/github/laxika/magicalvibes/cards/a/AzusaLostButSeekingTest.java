package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AzusaLostButSeekingTest extends BaseCardTest {

    private long forestCount() {
        return gd.playerBattlefields.get(player1.getId()).stream()
                .filter(p -> "Forest".equals(p.getCard().getName()))
                .count();
    }

    @Test
    @DisplayName("Controller may play two additional lands each turn; opponents may not")
    void grantsControllerOnlyTwoExtraLandPlays() {
        harness.addToBattlefield(player1, new AzusaLostButSeeking());

        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(3);
        assertThat(gd.getMaxLandsThisTurn(player2.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("The controller can actually play three lands in one turn")
    void controllerPlaysThreeLandsInOneTurn() {
        harness.addToBattlefield(player1, new AzusaLostButSeeking());
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest()));

        harness.playLand(player1, 0);
        harness.playLand(player1, 0);
        harness.playLand(player1, 0);

        assertThat(forestCount()).isEqualTo(3);
    }

    @Test
    @DisplayName("A fourth land play is refused")
    void fourthLandPlayIsRefused() {
        harness.addToBattlefield(player1, new AzusaLostButSeeking());
        harness.setHand(player1, List.of(new Forest(), new Forest(), new Forest(), new Forest()));

        harness.playLand(player1, 0);
        harness.playLand(player1, 0);
        harness.playLand(player1, 0);
        harness.playLand(player1, 0);

        assertThat(forestCount()).isEqualTo(3);
        harness.assertInHand(player1, "Forest");
    }

    @Test
    @DisplayName("Without Azusa on the battlefield the limit returns to one land")
    void limitReturnsToOneWhenAzusaLeaves() {
        harness.addToBattlefield(player1, new AzusaLostButSeeking());
        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(3);

        gd.playerBattlefields.get(player1.getId()).clear();

        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(1);
    }
}
