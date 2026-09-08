package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ExplorationTest extends BaseCardTest {

    @Test
    @DisplayName("Exploration grants its controller one additional land play")
    void grantsControllerOneAdditionalLandPlay() {
        harness.addToBattlefield(player1, new Exploration());

        assertThat(gd.getMaxLandsThisTurn(player1.getId())).isEqualTo(2);
        assertThat(gd.getMaxLandsThisTurn(player2.getId())).isEqualTo(1);
    }

    @Test
    @DisplayName("The controller can play two lands in one turn")
    void controllerCanPlayTwoLandsInOneTurn() {
        harness.addToBattlefield(player1, new Exploration());
        harness.setHand(player1, List.of(new Forest(), new Forest()));

        harness.playLand(player1, 0);
        harness.playLand(player1, 0);

        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> "Forest".equals(permanent.getCard().getName())))
                .hasSize(2);
    }
}
