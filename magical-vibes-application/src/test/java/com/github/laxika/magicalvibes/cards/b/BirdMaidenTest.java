package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({BirdMaiden.class, GrizzlyBears.class, GiantSpider.class})
class BirdMaidenTest extends BaseCardTest {

    @Test
    void flyingPreventsNonFlyingCreatureFromBlocking() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player1, new BirdMaiden());

        declareAttackersAndPrepareBlockers(List.of(0));

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    void flyingCreatureCanBlockNonFlyingCreature() {
        Permanent blocker = addCreatureReady(player1, new BirdMaiden());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(player2, List.of(0));

        int blockerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(attacker);

        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void flyingCreatureCanBlockBirdMaiden() {
        Permanent blocker = addCreatureReady(player2, new BirdMaiden());
        Permanent attacker = addCreatureReady(player1, new BirdMaiden());

        declareAttackersAndPrepareBlockers(List.of(0));

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void reachCreatureCanBlockBirdMaiden() {
        Permanent blocker = addCreatureReady(player2, new GiantSpider());
        Permanent attacker = addCreatureReady(player1, new BirdMaiden());

        declareAttackersAndPrepareBlockers(List.of(0));

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, attackerIndex)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
