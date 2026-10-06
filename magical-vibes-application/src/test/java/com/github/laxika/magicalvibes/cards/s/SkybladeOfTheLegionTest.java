package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AtzocanArcher;
import com.github.laxika.magicalvibes.cards.q.QueensBaySoldier;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SkybladeOfTheLegion.class, QueensBaySoldier.class, AtzocanArcher.class})
class SkybladeOfTheLegionTest extends BaseCardTest {

    @Test
    void creatureWithoutFlyingOrReachCannotBlock() {
        addCreatureReady(player1, new SkybladeOfTheLegion());
        addCreatureReady(player2, new QueensBaySoldier());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    void flyingCreatureCanBlock() {
        addCreatureReady(player1, new SkybladeOfTheLegion());
        Permanent blocker = addCreatureReady(player2, new SkybladeOfTheLegion());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void reachCreatureCanBlock() {
        addCreatureReady(player1, new SkybladeOfTheLegion());
        Permanent blocker = addCreatureReady(player2, new AtzocanArcher());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void canBlockCreatureWithoutFlying() {
        addCreatureReady(player1, new QueensBaySoldier());
        Permanent blocker = addCreatureReady(player2, new SkybladeOfTheLegion());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
