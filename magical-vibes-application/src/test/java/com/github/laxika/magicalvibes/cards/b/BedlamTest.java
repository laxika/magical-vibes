package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.GameLogEntry;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class BedlamTest extends BaseCardTest {

    @Test
    @DisplayName("No creature can block while Bedlam is on the battlefield")
    void creatureCannotBlockWithBedlam() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        addBedlam(player1);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Creatures can't block");
    }

    @Test
    @DisplayName("Bedlam even stops its own controller's creatures from blocking")
    void controllerCreatureCannotBlockWithBedlam() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());
        addBedlam(player2);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Creatures can't block");
    }

    @Test
    @DisplayName("Without Bedlam a creature blocks normally")
    void creatureBlocksWithoutBedlam() {
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.gameLog.stream().map(GameLogEntry::plainText)).anyMatch(log -> log.contains("declares 1 blocker"));
    }

    private void addBedlam(Player player) {
        gd.playerBattlefields.get(player.getId()).add(new Permanent(new Bedlam()));
    }
}
