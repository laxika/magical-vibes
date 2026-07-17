package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfFire;
import com.github.laxika.magicalvibes.model.ManaColor;
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

class JovensToolsTest extends BaseCardTest {

    @Test
    @DisplayName("Affected creature can't be blocked by a non-Wall creature")
    void nonWallCreatureCannotBlock() {
        Permanent attacker = restrictAttacker();

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Walls");
    }

    @Test
    @DisplayName("Affected creature can still be blocked by a Wall")
    void wallCanBlock() {
        Permanent attacker = restrictAttacker();

        Permanent wall = new Permanent(new WallOfFire());
        wall.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(wall);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(wall.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Restriction wears off at end of turn")
    void restrictionWearsOff() {
        Permanent attacker = restrictAttacker();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent bears = new Permanent(new GrizzlyBears());
        bears.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(bears);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(bears.isBlocking()).isTrue();
    }

    /**
     * Activates Joven's Tools targeting a fresh attacker, resolves it, and returns the attacker with
     * the "can't be blocked except by Walls" restriction applied.
     */
    private Permanent restrictAttacker() {
        Permanent tools = new Permanent(new JovensTools());
        tools.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(tools);
        harness.addMana(player1, ManaColor.WHITE, 4);

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        attacker.setAttacking(true);
        return attacker;
    }

    private void prepareDeclareBlockers() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();
    }
}
