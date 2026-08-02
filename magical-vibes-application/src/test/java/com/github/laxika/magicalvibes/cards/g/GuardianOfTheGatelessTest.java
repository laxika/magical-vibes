package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class GuardianOfTheGatelessTest extends BaseCardTest {

    @Test
    @DisplayName("Guardian of the Gateless gets +1/+1 when it blocks one creature")
    void getsOnePlusOneWhenBlockingOneCreature() {
        Permanent guardian = addReadyGuardian(player2);
        addReadyAttacker(player1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(guardian.getPowerModifier()).isEqualTo(1);
        assertThat(guardian.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Guardian of the Gateless can block three creatures and triggers once")
    void blocksThreeCreaturesAndTriggersOnce() {
        Permanent guardian = addReadyGuardian(player2);
        addReadyAttacker(player1);
        addReadyAttacker(player1);
        addReadyAttacker(player1);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(0, 1),
                new BlockerAssignment(0, 2)));

        assertThat(guardian.getBlockingTargets()).containsExactlyInAnyOrder(0, 1, 2);
        assertThat(gd.stack).hasSize(1);

        harness.passBothPriorities();

        assertThat(guardian.getPowerModifier()).isEqualTo(3);
        assertThat(guardian.getToughnessModifier()).isEqualTo(3);
    }

    private Permanent addReadyGuardian(Player player) {
        Permanent permanent = new Permanent(new GuardianOfTheGateless());
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
