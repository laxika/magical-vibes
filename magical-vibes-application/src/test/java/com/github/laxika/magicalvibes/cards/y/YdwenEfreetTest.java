package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.b.BenalishHero;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({YdwenEfreet.class, GrizzlyBears.class, BenalishHero.class})
class YdwenEfreetTest extends BaseCardTest {

    @Test
    @DisplayName("A lost flip removes Ydwen Efreet and unblocks its sole attacker")
    void lostFlipRemovesSourceAndUnblocksSoleAttacker() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent efreet = addCreatureReady(player2, new YdwenEfreet());

        declareAttackersAndPrepareBlockers(List.of(0));
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

        declareAttackersAndPrepareBlockers(List.of(0));
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

    @Test
    @CardUsed({YdwenEfreet.class, BenalishHero.class})
    @DisplayName("Blocking an attacking band triggers only one coin flip")
    void blockingAnAttackingBandTriggersOnlyOnce() {
        Permanent firstAttacker = addCreatureReady(player1, new BenalishHero());
        Permanent secondAttacker = addCreatureReady(player1, new BenalishHero());
        Permanent efreet = addCreatureReady(player2, new YdwenEfreet());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        harness.beginAttackerDeclarationInput();
        harness.withAutoStop(TurnStep.DECLARE_ATTACKERS,
                () -> harness.inMutationScope(() -> harness.getCombatAttackService()
                        .declareAttackers(gd, player1, List.of(0, 1), null, List.of(List.of(0, 1)))));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(efreet.getBlockingTargetIds()).containsExactlyInAnyOrder(
                firstAttacker.getId(), secondAttacker.getId());
        assertThat(gd.stack).hasSize(1);
        resolveAllTriggers();

        assertThat(gd.gameLog.stream()
                .filter(entry -> entry.plainText().contains("coin flip for Ydwen Efreet")))
                .hasSize(1);
        boolean lostFlip = gameLogContains("loses the coin flip for Ydwen Efreet");
        assertThat(efreet.isBlocking()).isEqualTo(!lostFlip);
        assertThat(efreet.isCantBlockThisTurn()).isEqualTo(lostFlip);
        assertThat(firstAttacker.isBlockedWithoutBlockers()).isFalse();
        assertThat(secondAttacker.isBlockedWithoutBlockers()).isFalse();
    }

    @Test
    @CardUsed(YdwenEfreet.class)
    @DisplayName("Only a lost flip lets the sole blocked attacker damage the defender")
    void combatDamageFollowsCoinFlipOutcome() {
        addCreatureReady(player1, new YdwenEfreet());
        addCreatureReady(player2, new YdwenEfreet());
        harness.setLife(player2, 20);

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveAllTriggers();
        boolean lostFlip = gameLogContains("loses the coin flip for Ydwen Efreet");
        resolveCombat();

        harness.assertLife(player2, lostFlip ? 17 : 20);
    }
}
