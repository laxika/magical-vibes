package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.c.ConcordiaPegasus;
import com.github.laxika.magicalvibes.cards.f.FeralMaaka;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({RubbleSlinger.class, ConcordiaPegasus.class, FeralMaaka.class})
class RubbleSlingerTest extends BaseCardTest {

    @Test
    void canBlockCreatureWithFlying() {
        Permanent attacker = addCreatureReady(player1, new ConcordiaPegasus());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new RubbleSlinger());

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    void reachDoesNotPreventGroundCreatureFromBlocking() {
        Permanent attacker = addCreatureReady(player1, new RubbleSlinger());
        attacker.setAttacking(true);
        Permanent blocker = addCreatureReady(player2, new FeralMaaka());

        prepareDeclareBlockers();

        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
