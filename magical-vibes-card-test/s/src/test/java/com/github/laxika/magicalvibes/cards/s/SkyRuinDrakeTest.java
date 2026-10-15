package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GiantScorpion;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SkyRuinDrake.class, GiantScorpion.class, SpidersilkNet.class})
class SkyRuinDrakeTest extends BaseCardTest {

    @Test
    @DisplayName("Sky Ruin Drake's flying prevents a ground creature from blocking it")
    void flyingPreventsGroundBlock() {
        Permanent drake = harness.addToBattlefieldAndReturn(player1, new SkyRuinDrake());
        drake.setSummoningSick(false);
        drake.setAttacking(true);

        harness.addToBattlefield(player2, new GiantScorpion());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Sky Ruin Drake can be blocked by a flying creature")
    void flyingCreatureCanBlock() {
        Permanent drake = harness.addToBattlefieldAndReturn(player1, new SkyRuinDrake());
        drake.setSummoningSick(false);
        drake.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new SkyRuinDrake());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Sky Ruin Drake can be blocked by a ground creature equipped with Spidersilk Net")
    void reachCreatureCanBlock() {
        Permanent drake = harness.addToBattlefieldAndReturn(player1, new SkyRuinDrake());
        drake.setSummoningSick(false);
        drake.setAttacking(true);
        Permanent blocker = harness.addToBattlefieldAndReturn(player2, new GiantScorpion());
        Permanent net = harness.addToBattlefieldAndReturn(player2, new SpidersilkNet());
        net.setAttachedTo(blocker.getId());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Sky Ruin Drake can block a ground creature")
    void canBlockGroundCreature() {
        Permanent attacker = harness.addToBattlefieldAndReturn(player1, new GiantScorpion());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        Permanent drake = harness.addToBattlefieldAndReturn(player2, new SkyRuinDrake());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(drake.isBlocking()).isTrue();
    }
}
