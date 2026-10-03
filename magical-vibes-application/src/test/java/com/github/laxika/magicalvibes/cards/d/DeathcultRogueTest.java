package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.a.ArmoredTransport;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({DeathcultRogue.class, ArmoredTransport.class})
class DeathcultRogueTest extends BaseCardTest {

    @Test
    @DisplayName("Deathcult Rogue cannot be blocked by a non-Rogue creature")
    void cannotBeBlockedByNonRogue() {
        addAttackingRogue();

        addCreatureReady(player2, new ArmoredTransport());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by Rogues");
    }

    @Test
    @DisplayName("Deathcult Rogue can be blocked by a Rogue")
    void canBeBlockedByRogue() {
        addAttackingRogue();

        addCreatureReady(player2, new DeathcultRogue());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gameLogContains("declares 1 blocker")).isTrue();
    }

    @Test
    @DisplayName("A Rogue blocker does not allow a non-Rogue to join the block")
    void rejectsMixedRogueAndNonRogueBlockers() {
        addAttackingRogue();
        addCreatureReady(player2, new DeathcultRogue());
        addCreatureReady(player2, new ArmoredTransport());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2,
                List.of(new BlockerAssignment(0, 0), new BlockerAssignment(1, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("can only be blocked by Rogues");
    }

    @Test
    @DisplayName("Deathcult Rogue can block a non-Rogue attacker")
    void canBlockNonRogueAttacker() {
        Permanent attacker = addCreatureReady(player1, new ArmoredTransport());
        attacker.setAttacking(true);
        addCreatureReady(player2, new DeathcultRogue());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gameLogContains("declares 1 blocker")).isTrue();
    }

    private void addAttackingRogue() {
        Permanent rogue = addCreatureReady(player1, new DeathcultRogue());
        rogue.setAttacking(true);
    }
}
