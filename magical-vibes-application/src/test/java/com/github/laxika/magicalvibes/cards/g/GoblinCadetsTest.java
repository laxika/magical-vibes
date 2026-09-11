package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({GoblinCadets.class, GoblinPatrol.class})
class GoblinCadetsTest extends BaseCardTest {

    @Test
    @DisplayName("When Goblin Cadets becomes blocked, an opponent gains control and it leaves combat")
    void becomesBlockedGivesControlToTargetOpponent() {
        Permanent cadets = addCreatureReady(player1, new GoblinCadets());
        Permanent blocker = addCreatureReady(player2, new GoblinPatrol());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
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
    @DisplayName("When Goblin Cadets becomes blocked by multiple creatures, one opponent gains control")
    void becomesBlockedByMultipleCreaturesTriggersOnce() {
        Permanent cadets = addCreatureReady(player1, new GoblinCadets());
        addCreatureReady(player2, new GoblinPatrol());
        addCreatureReady(player2, new GoblinPatrol());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(0, 0),
                new BlockerAssignment(1, 0)));

        harness.handlePermanentChosen(player1, player2.getId());
        assertThat(gd.interaction.isAwaitingInput()).isFalse();
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(cadets);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(cadets);
        assertThat(cadets.isAttacking()).isFalse();
        assertThat(cadets.isBlocking()).isFalse();
    }

    @Test
    @DisplayName("When Goblin Cadets blocks, an opponent gains control")
    void blocksGivesControlToTargetOpponent() {
        addCreatureReady(player1, new GoblinPatrol());
        Permanent cadets = addCreatureReady(player2, new GoblinCadets());

        declareAttackers(List.of(0));
        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThatThrownBy(() -> harness.handlePermanentChosen(player2, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        harness.handlePermanentChosen(player2, player1.getId());
        resolveAllTriggers();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(cadets);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(cadets);
        assertThat(cadets.isAttacking()).isFalse();
        assertThat(cadets.isBlocking()).isFalse();
    }
}
