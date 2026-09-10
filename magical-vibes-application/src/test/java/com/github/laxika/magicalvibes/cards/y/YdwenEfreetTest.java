package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YdwenEfreet.class, GrizzlyBears.class})
class YdwenEfreetTest extends BaseCardTest {

    @Test
    @DisplayName("A lost flip removes Ydwen Efreet and unblocks its sole attacker")
    void lostFlipRemovesSourceAndUnblocksSoleAttacker() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent efreet = addCreatureReady(player2, new YdwenEfreet());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        boolean wonFlip = gameLogContains("wins the coin flip for Ydwen Efreet");
        boolean lostFlip = gameLogContains("loses the coin flip for Ydwen Efreet");
        assertThat(wonFlip).isNotEqualTo(lostFlip);
        if (lostFlip) {
            assertThat(efreet.isBlocking()).isFalse();
            assertThat(efreet.isCantBlockThisTurn()).isTrue();
            assertThat(attacker.isBlockedWithoutBlockers()).isFalse();
        } else {
            assertThat(efreet.isBlocking()).isTrue();
            assertThat(attacker.isBlockedWithoutBlockers()).isFalse();
        }
    }

    @Test
    @DisplayName("A lost flip does not unblock an attacker with another blocker")
    void lostFlipKeepsAttackerBlockedByAnotherBlocker() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent efreet = addCreatureReady(player2, new YdwenEfreet());
        Permanent otherBlocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(1, 0),
                new BlockerAssignment(0, 0)));
        resolveAllTriggers();

        boolean wonFlip = gameLogContains("wins the coin flip for Ydwen Efreet");
        boolean lostFlip = gameLogContains("loses the coin flip for Ydwen Efreet");
        assertThat(wonFlip).isNotEqualTo(lostFlip);
        assertThat(efreet.isBlocking()).isEqualTo(wonFlip);
        assertThat(otherBlocker.isBlocking()).isTrue();
        assertThat(attacker.isBlockedWithoutBlockers()).isFalse();
        assertThat(efreet.isCantBlockThisTurn()).isEqualTo(lostFlip);
    }
}
