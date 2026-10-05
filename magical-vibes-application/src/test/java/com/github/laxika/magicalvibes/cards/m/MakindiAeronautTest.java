package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.n.NetcasterSpider;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThatCode;

@CardUsed({MakindiAeronaut.class, GrizzlyBears.class, NetcasterSpider.class})
class MakindiAeronautTest extends BaseCardTest {

    @Test
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent aeronaut = addCreatureReady(player1, new MakindiAeronaut());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(aeronaut);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }
    @Test
    void flyingCreatureCanBlockAeronaut() {
        addCreatureReady(player1, new MakindiAeronaut());
        addCreatureReady(player2, new MakindiAeronaut());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    void reachCreatureCanBlockAeronaut() {
        addCreatureReady(player1, new MakindiAeronaut());
        addCreatureReady(player2, new NetcasterSpider());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }

    @Test
    void aeronautCanBlockCreatureWithoutFlying() {
        addCreatureReady(player1, new NetcasterSpider());
        addCreatureReady(player2, new MakindiAeronaut());

        declareAttackersAndPrepareBlockers(player1, List.of(0));

        assertThatCode(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0))))
                .doesNotThrowAnyException();
    }
}
