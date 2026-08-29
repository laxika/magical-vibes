package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfWood;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class RampartCrawlerTest extends BaseCardTest {

    @Test
    @DisplayName("Rampart Crawler can't be blocked by a Wall")
    void cannotBeBlockedByWall() {
        Permanent blocker = addReadyBlocker(new WallOfWood());
        Permanent attacker = addReadyAttacker();
        beginBlockerDeclaration();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);
        int attackerIndex = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(blockerIndex, attackerIndex))))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Rampart Crawler can be blocked by a non-Wall creature")
    void canBeBlockedByNonWall() {
        Permanent blocker = addReadyBlocker(new GrizzlyBears());
        addReadyAttacker();
        beginBlockerDeclaration();

        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(blocker);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIndex, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    private Permanent addReadyBlocker(com.github.laxika.magicalvibes.model.Card card) {
        Permanent blocker = new Permanent(card);
        blocker.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(blocker);
        return blocker;
    }

    private Permanent addReadyAttacker() {
        Permanent attacker = new Permanent(new RampartCrawler());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);
        return attacker;
    }

    private void beginBlockerDeclaration() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
