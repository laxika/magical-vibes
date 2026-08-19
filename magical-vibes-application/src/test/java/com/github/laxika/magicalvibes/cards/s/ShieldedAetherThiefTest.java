package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class ShieldedAetherThiefTest extends BaseCardTest {

    @Test
    void getsEnergyWhenItBlocks() {
        addCreatureReady(player1, new ShieldedAetherThief());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(player2, List.of(0));
        prepareDeclareBlockers(player2);
        gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();
        resolveAllTriggers();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isEqualTo(1);
    }

    @Test
    void paysEnergyAndTapsToDrawACard() {
        Permanent thief = addCreatureReady(player1, new ShieldedAetherThief());
        Forest forest = new Forest();
        harness.setLibrary(player1, List.of(forest));
        gd.playerEnergyCounters.put(player1.getId(), 3);
        int handSizeBefore = gd.playerHands.get(player1.getId()).size();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(gd.playerEnergyCounters.get(player1.getId())).isZero();
        assertThat(thief.isTapped()).isTrue();
        assertThat(gd.playerHands.get(player1.getId()))
                .hasSize(handSizeBefore + 1)
                .contains(forest);
    }

    @Test
    void cannotActivateWithoutThreeEnergyCounters() {
        addCreatureReady(player1, new ShieldedAetherThief());
        gd.playerEnergyCounters.put(player1.getId(), 2);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("three energy counters");
    }
}
