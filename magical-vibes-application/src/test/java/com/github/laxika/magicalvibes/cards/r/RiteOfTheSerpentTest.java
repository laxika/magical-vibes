package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

class RiteOfTheSerpentTest extends BaseCardTest {

    @Test
    void destroysCreatureWithCounterAndCreatesOneSnakeForItsController() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 2);
        castRite(target);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        harness.assertInGraveyard(player2, "Grizzly Bears");
        assertThat(harness.getGameData().playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Snake"))
                .hasSize(1);
    }

    @Test
    void destroysCreatureWithoutCounterAndCreatesNoSnake() {
        Permanent target = addCreatureReady(player2, new GrizzlyBears());
        castRite(target);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        assertThat(harness.getGameData().playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Snake"));
    }

    @Test
    void createsSnakeWhenCounteredIndestructibleCreatureIsNotDestroyed() {
        Card indestructibleBears = new GrizzlyBears();
        indestructibleBears.setKeywords(Set.of(Keyword.INDESTRUCTIBLE));
        Permanent target = addCreatureReady(player2, indestructibleBears);
        target.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        castRite(target);

        harness.assertOnBattlefield(player2, "Grizzly Bears");
        assertThat(harness.getGameData().playerBattlefields.get(player2.getId()))
                .filteredOn(p -> p.getCard().getName().equals("Snake"))
                .hasSize(1);
    }

    private void castRite(Permanent target) {
        harness.setHand(player1, List.of(new RiteOfTheSerpent()));
        harness.addMana(player1, ManaColor.BLACK, 6);
        harness.castSorcery(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
