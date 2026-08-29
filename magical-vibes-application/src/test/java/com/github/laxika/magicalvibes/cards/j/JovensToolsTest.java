package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.w.WallOfKelp;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JovensTools.class, Joven.class, WallOfKelp.class})
class JovensToolsTest extends BaseCardTest {

    @Test
    @DisplayName("Affected creature can't be blocked by a non-Wall creature")
    void nonWallCreatureCannotBlock() {
        restrictAttacker();

        addCreatureReady(player2, new Joven());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1))))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Walls");
    }

    @Test
    @DisplayName("Affected creature can still be blocked by a Wall")
    void wallCanBlock() {
        restrictAttacker();

        Permanent wall = addCreatureReady(player2, new WallOfKelp());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(wall.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Restriction wears off at end of turn")
    void restrictionWearsOff() {
        restrictAttacker();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent nonWall = addCreatureReady(player2, new Joven());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 1)));

        assertThat(nonWall.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Can only target a creature")
    void cannotTargetNonCreaturePermanent() {
        harness.addToBattlefield(player1, new JovensTools());
        Permanent target = harness.addToBattlefieldAndReturn(player2, new JovensTools());
        harness.addMana(player1, ManaColor.WHITE, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    @Test
    @DisplayName("Tapping Joven's Tools is part of the activation cost")
    void tapsAsCost() {
        Permanent tools = harness.addToBattlefieldAndReturn(player1, new JovensTools());
        Permanent target = addCreatureReady(player2, new Joven());
        harness.addMana(player1, ManaColor.WHITE, 4);

        harness.activateAbility(player1, 0, null, target.getId());

        assertThat(tools.isTapped()).isTrue();
        harness.passBothPriorities();
    }

    @Test
    @DisplayName("Requires four generic mana to activate")
    void cannotActivateWithoutEnoughMana() {
        Permanent tools = harness.addToBattlefieldAndReturn(player1, new JovensTools());
        Permanent target = addCreatureReady(player2, new Joven());
        harness.addMana(player1, ManaColor.WHITE, 3);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(tools.isTapped()).isFalse();
    }

    /**
     * Activates Joven's Tools targeting a fresh attacker and resolves the "can't be blocked except
     * by Walls" restriction.
     */
    private void restrictAttacker() {
        harness.addToBattlefield(player1, new JovensTools());
        harness.addMana(player1, ManaColor.WHITE, 4);

        Permanent attacker = addCreatureReady(player1, new Joven());

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        attacker.setAttacking(true);
    }
}
