package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.e.EarthOriginYak;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FeiyiSnake.class, WindDrake.class, EarthOriginYak.class})
class FeiyiSnakeTest extends BaseCardTest {

    @Test
    void reachAllowsBlockingFlyingCreature() {
        addCreatureReady(player1, new WindDrake());
        Permanent snake = addCreatureReady(player2, new FeiyiSnake());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(snake.isBlocking()).isTrue();
    }

    @Test
    @CardUsed({FeiyiSnake.class})
    void reachCreatureCanBlockAnAttackingReachCreature() {
        addCreatureReady(player1, new FeiyiSnake());
        Permanent snake = addCreatureReady(player2, new FeiyiSnake());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(snake.isBlocking()).isTrue();
    }

    @Test
    @CardUsed({FeiyiSnake.class, EarthOriginYak.class})
    void reachDoesNotPreventBlockingGroundCreatures() {
        addCreatureReady(player1, new EarthOriginYak());
        Permanent snake = addCreatureReady(player2, new FeiyiSnake());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(snake.isBlocking()).isTrue();
    }

    @Test
    @CardUsed({FeiyiSnake.class, EarthOriginYak.class})
    void reachDoesNotGiveFlyingWhenAttacking() {
        addCreatureReady(player1, new FeiyiSnake());
        Permanent yak = addCreatureReady(player2, new EarthOriginYak());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(yak.isBlocking()).isTrue();
    }
}
