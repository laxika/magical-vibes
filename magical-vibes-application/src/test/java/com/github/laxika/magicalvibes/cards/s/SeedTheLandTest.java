package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class SeedTheLandTest extends BaseCardTest {

    @Test
    @DisplayName("A land entering under your control creates a Snake token for you")
    void ownLandCreatesSnake() {
        harness.addToBattlefield(player1, new SeedTheLand());
        harness.setHand(player1, List.of(new Forest()));

        harness.playLand(player1, 0);
        harness.passBothPriorities();

        Permanent snake = gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Snake"))
                .findFirst()
                .orElseThrow();
        assertThat(snake.getEffectivePower()).isEqualTo(1);
        assertThat(snake.getEffectiveToughness()).isEqualTo(1);
    }

    @Test
    @DisplayName("A land entering under an opponent's control creates a Snake token for that player")
    void opponentsLandCreatesSnakeForOpponent() {
        harness.addToBattlefield(player1, new SeedTheLand());
        harness.setHand(player2, List.of(new Forest()));

        harness.forceActivePlayer(player2);
        harness.playLand(player2, 0);
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Snake")))
                .hasSize(1);
        assertThat(gd.playerBattlefields.get(player1.getId()).stream()
                .filter(permanent -> permanent.getCard().isToken()
                        && permanent.getCard().getName().equals("Snake")))
                .isEmpty();
    }
}
