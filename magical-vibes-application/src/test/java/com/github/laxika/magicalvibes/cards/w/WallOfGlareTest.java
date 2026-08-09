package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class WallOfGlareTest extends BaseCardTest {

    @Test
    @DisplayName("Wall of Glare can block any number of creatures")
    void canBlockAnyNumberOfCreatures() {
        Permanent wall = addReadyWall(player2);
        addReadyAttacker(player1);
        addReadyAttacker(player1);
        addReadyAttacker(player1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1),
                new BlockerAssignment(0, 2)));

        assertThat(wall.isBlocking()).isTrue();
        assertThat(wall.getBlockingTargets()).containsExactlyInAnyOrder(0, 1, 2);
    }

    private Permanent addReadyWall(Player player) {
        Permanent permanent = new Permanent(new WallOfGlare());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void addReadyAttacker(Player player) {
        Permanent permanent = new Permanent(new GrizzlyBears());
        permanent.setSummoningSick(false);
        permanent.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(permanent);
    }
}
