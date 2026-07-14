package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
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
        Permanent attacker = addReadyCreature(player1);
        attacker.setAttacking(true);
        addReadyCreature(player2);
        addBedlam(player1);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Creatures can't block");
    }

    @Test
    @DisplayName("Bedlam even stops its own controller's creatures from blocking")
    void controllerCreatureCannotBlockWithBedlam() {
        Permanent attacker = addReadyCreature(player1);
        attacker.setAttacking(true);
        addReadyCreature(player2);
        addBedlam(player2);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Creatures can't block");
    }

    @Test
    @DisplayName("Without Bedlam a creature blocks normally")
    void creatureBlocksWithoutBedlam() {
        Permanent attacker = addReadyCreature(player1);
        attacker.setAttacking(true);
        addReadyCreature(player2);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

        assertThat(gd.gameLog).anyMatch(log -> log.contains("declares 1 blocker"));
    }

    private void addBedlam(Player player) {
        gd.playerBattlefields.get(player.getId()).add(new Permanent(new Bedlam()));
    }

    private Permanent addReadyCreature(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private void prepareDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
