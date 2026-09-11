package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.t.TrainedArmodon;
import com.github.laxika.magicalvibes.cards.w.WindDrake;
import com.github.laxika.magicalvibes.model.Card;
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

@CardUsed({MountedArchers.class, TrainedArmodon.class, WindDrake.class})
class MountedArchersTest extends BaseCardTest {

    @Test
    @DisplayName("Activating the ability grants one additional block this turn")
    void grantsAdditionalBlock() {
        Permanent archers = addArchers();

        activate(archers);

        assertThat(archers.getAdditionalBlocksUntilEndOfTurn()).isEqualTo(1);
    }

    @Test
    @DisplayName("Ability can be activated twice for two additional blocks")
    void grantsStack() {
        Permanent archers = addArchers();

        activate(archers);
        activate(archers);

        assertThat(archers.getAdditionalBlocksUntilEndOfTurn()).isEqualTo(2);
    }

    @Test
    @DisplayName("Mounted Archers blocks two attackers after activating once")
    void blocksTwoAttackers() {
        Permanent archers = addArchers();
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(archers);
        addAttacker();
        addAttacker();

        activate(archers);

        prepareDeclareBlockers(player1);

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIdx, 0),
                new BlockerAssignment(blockerIdx, 1)
        ));

        assertThat(archers.getBlockingTargets()).containsExactlyInAnyOrder(0, 1);
    }

    @Test
    @DisplayName("Reach lets Mounted Archers block a creature with flying")
    void blocksFlyingCreature() {
        Permanent archers = addArchers();
        Permanent attacker = addFlyingAttacker();
        int blockerIdx = gd.playerBattlefields.get(player2.getId()).indexOf(archers);
        int attackerIdx = gd.playerBattlefields.get(player1.getId()).indexOf(attacker);

        prepareDeclareBlockers(player1);

        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(blockerIdx, attackerIdx)));

        assertThat(archers.getBlockingTargets()).containsExactly(attackerIdx);
    }

    @Test
    @DisplayName("The grant wears off at end of turn")
    void grantExpiresAtEndOfTurn() {
        Permanent archers = addArchers();
        activate(archers);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(archers.getAdditionalBlocksUntilEndOfTurn()).isZero();
    }

    private Permanent addArchers() {
        return addCreatureReady(player2, new MountedArchers());
    }

    private void activate(Permanent archers) {
        int idx = gd.playerBattlefields.get(player2.getId()).indexOf(archers);
        harness.addMana(player2, ManaColor.WHITE, 1);
        harness.activateAbility(player2, idx, null, null);
        harness.passBothPriorities();
    }

    private Permanent addAttacker() {
        return addAttacker(new TrainedArmodon());
    }

    private Permanent addFlyingAttacker() {
        return addAttacker(new WindDrake());
    }

    private Permanent addAttacker(Card card) {
        Permanent atk = addCreatureReady(player1, card);
        atk.setAttacking(true);
        atk.setAttackTarget(player2.getId());
        return atk;
    }
}
