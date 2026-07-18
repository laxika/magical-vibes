package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class ElderLandWurmTest extends BaseCardTest {

    @Test
    @DisplayName("Elder Land Wurm keeps defender until it blocks")
    void keepsDefenderUntilItBlocks() {
        Permanent wurm = addReadyWurm(player2);

        assertThat(gqs.hasKeyword(gd, wurm, Keyword.DEFENDER)).isTrue();
    }

    @Test
    @DisplayName("When Elder Land Wurm blocks, it loses defender")
    void blockingRemovesDefender() {
        Permanent attacker = addReadySpider(player1);
        attacker.setAttacking(true);
        Permanent wurm = addReadyWurm(player2);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        // Resolve the block trigger.
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wurm, Keyword.DEFENDER)).isFalse();
    }

    @Test
    @DisplayName("The defender loss lasts past end of turn (it can attack on a later turn)")
    void defenderLossPersistsPastEndOfTurn() {
        Permanent attacker = addReadySpider(player1);
        attacker.setAttacking(true);
        Permanent wurm = addReadyWurm(player2);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wurm, Keyword.DEFENDER)).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Unlike an "until end of turn" removal, the loss is indefinite.
        assertThat(gqs.hasKeyword(gd, wurm, Keyword.DEFENDER)).isFalse();
    }

    @Test
    @DisplayName("Elder Land Wurm that never blocks keeps defender")
    void notBlockingKeepsDefender() {
        Permanent attacker = addReadySpider(player1);
        attacker.setAttacking(true);
        Permanent wurm = addReadyWurm(player2);

        setupDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of()); // stays back
        harness.passBothPriorities();

        assertThat(gqs.hasKeyword(gd, wurm, Keyword.DEFENDER)).isTrue();
    }

    // ===== Helpers =====

    private Permanent addReadyWurm(Player player) {
        Permanent perm = new Permanent(new ElderLandWurm());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addReadySpider(Player player) {
        Permanent perm = new Permanent(new GiantSpider());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void setupDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
