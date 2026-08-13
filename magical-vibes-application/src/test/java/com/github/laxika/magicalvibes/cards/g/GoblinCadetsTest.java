package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class GoblinCadetsTest extends BaseCardTest {

    @Test
    @DisplayName("When Goblin Cadets becomes blocked, an opponent gains control and it leaves combat")
    void becomesBlockedGivesControlToTargetOpponent() {
        Permanent cadets = addCreatureReady(player1, new GoblinCadets());
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        declareAttackers(List.of(0));
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player1, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player1, player2.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(cadets);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(cadets);
        assertThat(cadets.isAttacking()).isFalse();
        assertThat(cadets.isBlocking()).isFalse();
        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("When Goblin Cadets blocks, an opponent gains control")
    void blocksGivesControlToTargetOpponent() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent cadets = addCreatureReady(player2, new GoblinCadets());

        declareAttackers(List.of(0));
        harness.beginBlockerDeclarationInput();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        harness.handlePermanentChosen(player2, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(cadets);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(cadets);
        assertThat(cadets.isBlocking()).isFalse();
        assertThat(attacker.isAttacking()).isTrue();
    }
}
