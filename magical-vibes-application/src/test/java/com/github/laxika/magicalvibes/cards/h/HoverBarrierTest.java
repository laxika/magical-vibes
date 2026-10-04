package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.a.AxebaneStag;
import com.github.laxika.magicalvibes.cards.s.SunspireGriffin;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HoverBarrier.class, SunspireGriffin.class, AxebaneStag.class})
class HoverBarrierTest extends BaseCardTest {

    @Test
    void defenderPreventsAttackingEvenWhenReady() {
        addCreatureReady(player1, new HoverBarrier());

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");
    }

    @Test
    void canBlockFlyingCreatureWhileSummoningSick() {
        addCreatureReady(player1, new SunspireGriffin());
        Permanent barrier = harness.addToBattlefieldAndReturn(player2, new HoverBarrier());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(barrier.isBlocking()).isTrue();
    }

    @Test
    void canBlockCreatureWithoutFlying() {
        addCreatureReady(player1, new AxebaneStag());
        Permanent barrier = addCreatureReady(player2, new HoverBarrier());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(barrier.isBlocking()).isTrue();
    }
}
