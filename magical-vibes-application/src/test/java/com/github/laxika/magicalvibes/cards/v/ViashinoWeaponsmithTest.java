package com.github.laxika.magicalvibes.cards.v;

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

class ViashinoWeaponsmithTest extends BaseCardTest {

    @Test
    @DisplayName("When Viashino Weaponsmith becomes blocked by a creature, it gets +2/+2 until end of turn")
    void becomesBlockedGetsBoost() {
        Permanent weaponsmith = addReadyWeaponsmith(player1);
        weaponsmith.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        assertThat(weaponsmith.getPowerModifier()).isEqualTo(2);
        assertThat(weaponsmith.getToughnessModifier()).isEqualTo(2);
    }

    @Test
    @DisplayName("When Viashino Weaponsmith is unblocked, it gets no boost")
    void unblockedNoBoost() {
        Permanent weaponsmith = addReadyWeaponsmith(player1);
        weaponsmith.setAttacking(true);

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of());

        assertThat(gd.stack).isEmpty();
        assertThat(weaponsmith.getPowerModifier()).isZero();
        assertThat(weaponsmith.getToughnessModifier()).isZero();
    }

    @Test
    @DisplayName("The boost wears off at end of turn")
    void boostWearsOff() {
        Permanent weaponsmith = addReadyWeaponsmith(player1);
        weaponsmith.setAttacking(true);
        addCreatureReady(player2, new GrizzlyBears());

        prepareDeclareBlockers();
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(weaponsmith.getPowerModifier()).isZero();
        assertThat(weaponsmith.getToughnessModifier()).isZero();
    }

    private Permanent addReadyWeaponsmith(Player player) {
        Permanent permanent = new Permanent(new ViashinoWeaponsmith());
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(permanent);
        return permanent;
    }
}
