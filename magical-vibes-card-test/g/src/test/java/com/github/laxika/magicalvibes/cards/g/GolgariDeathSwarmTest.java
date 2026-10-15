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

@CardUsed({GolgariDeathSwarm.class, GrizzlyBears.class})
class GolgariDeathSwarmTest extends BaseCardTest {

    @Test
    @DisplayName("Flying prevents a nonflying creature from blocking Golgari Death Swarm")
    void flyingPreventsNonflyingCreatureFromBlocking() {
        addCreatureReady(player1, new GolgariDeathSwarm());
        addCreatureReady(player2, new GrizzlyBears());

        declareAttackersAndPrepareBlockers(List.of(0));

        assertThatThrownBy(() -> gs.declareBlockers(
                gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("(flying)");
    }

    @Test
    @DisplayName("Vigilance keeps Golgari Death Swarm untapped when it attacks")
    void vigilanceKeepsItUntappedWhenItAttacks() {
        Permanent swarm = addCreatureReady(player1, new GolgariDeathSwarm());

        declareAttackers(List.of(0));

        assertThat(swarm.isTapped()).isFalse();
    }

    @Test
    @DisplayName("A flying creature can block Golgari Death Swarm")
    void flyingCreatureCanBlock() {
        addCreatureReady(player1, new GolgariDeathSwarm());
        Permanent blocker = addCreatureReady(player2, new GolgariDeathSwarm());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Vigilance does not allow a tapped Golgari Death Swarm to attack")
    void tappedCreatureCannotAttackDespiteVigilance() {
        Permanent swarm = addCreatureReady(player1, new GolgariDeathSwarm());
        swarm.tap();

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");

        assertThat(swarm.isAttacking()).isFalse();
        assertThat(swarm.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Vigilance does not allow a summoning-sick Golgari Death Swarm to attack")
    void summoningSickCreatureCannotAttackDespiteVigilance() {
        Permanent swarm = addCreatureReady(player1, new GolgariDeathSwarm());
        swarm.setSummoningSick(true);

        assertThatThrownBy(() -> declareAttackers(List.of(0)))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Invalid attacker index");

        assertThat(swarm.isAttacking()).isFalse();
        assertThat(swarm.isTapped()).isFalse();
    }
}
