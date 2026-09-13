package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({CrazedSkirge.class, CoralMerfolk.class})
class CrazedSkirgeTest extends BaseCardTest {

    @Test
    @DisplayName("Haste lets Crazed Skirge attack immediately")
    void hasteLetsItAttackImmediately() {
        Permanent skirge = harness.addToBattlefieldAndReturn(player1, new CrazedSkirge());
        addCreatureReady(player2, new CrazedSkirge());

        declareAttackers(player1, List.of(0));

        assertThat(skirge.isAttacking()).isTrue();
    }

    @Test
    @DisplayName("Flying prevents a ground creature from blocking Crazed Skirge")
    void flyingPreventsGroundCreatureFromBlocking() {
        Permanent attacker = addCreatureReady(player1, new CrazedSkirge());
        addCreatureReady(player2, new CoralMerfolk());

        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("flying");
    }

    @Test
    @DisplayName("A creature with flying can block Crazed Skirge")
    void flyingCreatureCanBlock() {
        addCreatureReady(player1, new CrazedSkirge());
        Permanent blocker = addCreatureReady(player2, new CrazedSkirge());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }
}
