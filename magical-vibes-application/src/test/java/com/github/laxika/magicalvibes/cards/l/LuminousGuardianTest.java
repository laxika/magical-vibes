package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.w.WoodlandDruid;
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

@CardUsed({LuminousGuardian.class, WoodlandDruid.class})
class LuminousGuardianTest extends BaseCardTest {

    @Test
    @DisplayName("White ability gives Luminous Guardian +0/+1 until end of turn")
    void boostsToughness() {
        Permanent guardian = addGuardian();
        harness.addMana(player2, ManaColor.WHITE, 1);

        activate(player2, guardian, 0);

        assertThat(gqs.getEffectivePower(gd, guardian)).isEqualTo(1);
        assertThat(gqs.getEffectiveToughness(gd, guardian)).isEqualTo(5);
    }

    @Test
    @DisplayName("Luminous Guardian cannot block two attackers without activating")
    void cannotBlockTwoAttackersWithoutActivating() {
        Permanent guardian = addGuardian();
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(guardian);
        addAttacker();
        addAttacker();

        prepareDeclareBlockers();

        assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIndex, 0),
                new BlockerAssignment(blockerIndex, 1)
        ))).isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Colorless ability lets Luminous Guardian block two attackers")
    void blocksTwoAttackersAfterActivating() {
        Permanent guardian = addGuardian();
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(guardian);
        addAttacker();
        addAttacker();
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        activate(player2, guardian, 1);

        prepareDeclareBlockers();

        gs.declareBlockers(gd, player2, List.of(
                new BlockerAssignment(blockerIndex, 0),
                new BlockerAssignment(blockerIndex, 1)
        ));

        assertThat(guardian.getBlockingTargets()).containsExactlyInAnyOrder(0, 1);
    }

    @Test
    @DisplayName("Additional blocking ability wears off at end of turn")
    void additionalBlockExpiresAtEndOfTurn() {
        Permanent guardian = addGuardian();
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        activate(player2, guardian, 1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(guardian.getAdditionalBlocksUntilEndOfTurn()).isZero();
    }

    @Test
    @DisplayName("White ability wears off at end of turn")
    void toughnessBoostExpiresAtEndOfTurn() {
        Permanent guardian = addGuardian();
        harness.addMana(player2, ManaColor.WHITE, 1);

        activate(player2, guardian, 0);

        assertThat(gqs.getEffectiveToughness(gd, guardian)).isEqualTo(5);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.getEffectiveToughness(gd, guardian)).isEqualTo(4);
    }

    private Permanent addGuardian() {
        return addCreatureReady(player2, new LuminousGuardian());
    }

    private void activate(com.github.laxika.magicalvibes.model.Player player, Permanent guardian, int abilityIndex) {
        int index = gd.playerBattlefields.get(player.getId()).indexOf(guardian);
        harness.activateAbility(player, index, abilityIndex, null, null);
        harness.passBothPriorities();
    }

    private Permanent addAttacker() {
        Permanent attacker = addCreatureReady(player1, new WoodlandDruid());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        return attacker;
    }
}
