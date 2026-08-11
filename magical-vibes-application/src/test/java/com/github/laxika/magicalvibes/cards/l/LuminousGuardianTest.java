package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

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
    @DisplayName("Colorless ability lets Luminous Guardian block two attackers")
    void blocksTwoAttackersAfterActivating() {
        Permanent guardian = addGuardian();
        int blockerIndex = gd.playerBattlefields.get(player2.getId()).indexOf(guardian);
        addAttacker();
        addAttacker();
        harness.addMana(player2, ManaColor.COLORLESS, 2);

        harness.activateAbility(player2, blockerIndex, 1, null, null);
        harness.passBothPriorities();

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.beginBlockerDeclarationInput();

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

        harness.activateAbility(player2, 0, 1, null, null);
        harness.passBothPriorities();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(guardian.getAdditionalBlocksUntilEndOfTurn()).isZero();
    }

    private Permanent addGuardian() {
        Permanent perm = new Permanent(new LuminousGuardian());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player2.getId()).add(perm);
        return perm;
    }

    private void activate(com.github.laxika.magicalvibes.model.Player player, Permanent guardian, int abilityIndex) {
        int index = gd.playerBattlefields.get(player.getId()).indexOf(guardian);
        harness.activateAbility(player, index, abilityIndex, null, null);
        harness.passBothPriorities();
    }

    private Permanent addAttacker() {
        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        gd.playerBattlefields.get(player1.getId()).add(attacker);
        return attacker;
    }
}
