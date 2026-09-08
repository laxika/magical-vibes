package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.TestCards;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class AssembledAlphasTest extends BaseCardTest {

    @Test
    @DisplayName("Blocking deals 3 damage to the blocked creature and its controller")
    void blockingDamagesCreatureAndController() {
        Permanent alphas = addReady(player2);
        Permanent attacker = addReady(player1, new GrizzlyBears());
        TestCards.mutableCard(attacker).setToughness(4);
        attacker.setAttacking(true);
        int lifeBefore = gd.playerLifeTotals.get(player1.getId());

        declareBlock(List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(lifeBefore - 3);
        assertThat(alphas.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Becoming blocked deals 3 damage to each blocker and its controller")
    void becomingBlockedDamagesBlockerAndController() {
        Permanent alphas = addReady(player1);
        alphas.setAttacking(true);
        Permanent blocker = addReady(player2, new GrizzlyBears());
        TestCards.mutableCard(blocker).setToughness(4);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        declareBlock(List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(blocker.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 3);
        assertThat(alphas.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Becoming blocked triggers once for each blocker")
    void becomingBlockedByMultipleCreaturesTriggersForEachBlocker() {
        addReady(player1, new AssembledAlphas()).setAttacking(true);
        Permanent firstBlocker = addReady(player2, new GrizzlyBears());
        Permanent secondBlocker = addReady(player2, new GrizzlyBears());
        TestCards.mutableCard(firstBlocker).setToughness(4);
        TestCards.mutableCard(secondBlocker).setToughness(4);
        int lifeBefore = gd.playerLifeTotals.get(player2.getId());

        declareBlock(List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));
        harness.passBothPriorities();
        harness.passBothPriorities();

        assertThat(firstBlocker.getMarkedDamage()).isEqualTo(3);
        assertThat(secondBlocker.getMarkedDamage()).isEqualTo(3);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(lifeBefore - 6);
    }

    private Permanent addReady(Player player) {
        return addReady(player, new AssembledAlphas());
    }

    private Permanent addReady(Player player, com.github.laxika.magicalvibes.model.Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }

    private void declareBlock(List<BlockerAssignment> assignments) {
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, assignments);
    }
}
