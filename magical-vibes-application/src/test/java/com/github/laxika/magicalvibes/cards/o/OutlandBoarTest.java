package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class OutlandBoarTest extends BaseCardTest {

    @Test
    @DisplayName("Can't be blocked by creatures with power 2 or less")
    void cannotBeBlockedByPowerTwoOrLess() {
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        Permanent boar = addCreatureReady(player1, new OutlandBoar());
        boar.setAttacking(true);

        prepareDeclareBlockers();
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(boar);

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Can be blocked by a creature with power 3 or greater")
    void canBeBlockedByPowerThreeOrGreater() {
        Permanent blocker = addCreatureReady(player2, new HillGiant());
        Permanent boar = addCreatureReady(player1, new OutlandBoar());
        boar.setAttacking(true);

        prepareDeclareBlockers();
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(boar);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
