package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.w.WallOfFire;
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

@CardUsed({TowerOfCoireall.class, GrizzlyBears.class, WallOfFire.class})
class TowerOfCoireallTest extends BaseCardTest {

    @Test
    @DisplayName("Target creature cannot be blocked by a Wall")
    void wallCannotBlock() {
        Permanent attacker = activateTowerAndSetAttacker();
        Permanent wall = addCreatureReady(player2, new WallOfFire());

        prepareDeclareBlockers();

        assertThatThrownBy(() -> declareBlock(wall, attacker))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("non-Wall creatures");
    }

    @Test
    @DisplayName("Target creature can be blocked by a non-Wall creature")
    void nonWallCanBlock() {
        Permanent attacker = activateTowerAndSetAttacker();
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        declareBlock(blocker, attacker);

        assertThat(blocker.isBlocking()).isTrue();
    }

    @Test
    @DisplayName("Restriction wears off at end of turn")
    void restrictionWearsOff() {
        Permanent attacker = activateTowerAndSetAttacker();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        Permanent wall = addCreatureReady(player2, new WallOfFire());
        prepareDeclareBlockers();
        declareBlock(wall, attacker);

        assertThat(wall.isBlocking()).isTrue();
    }

    private Permanent activateTowerAndSetAttacker() {
        Permanent tower = harness.addToBattlefieldAndReturn(player1, new TowerOfCoireall());
        tower.setSummoningSick(false);
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        attacker.setAttacking(true);
        return attacker;
    }

    private void declareBlock(Permanent blocker, Permanent attacker) {
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(
                gd.playerBattlefields.get(player2.getId()).indexOf(blocker),
                gd.playerBattlefields.get(player1.getId()).indexOf(attacker))));
    }
}
