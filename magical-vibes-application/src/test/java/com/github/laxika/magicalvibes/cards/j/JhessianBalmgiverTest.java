package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class JhessianBalmgiverTest extends BaseCardTest {

    // ===== Ability 0: {T}: Prevent the next 1 damage to any target =====

    @Test
    @DisplayName("Prevention ability taps the Balmgiver and sets the global damage shield")
    void preventionAbilitySetsShield() {
        Permanent balmgiver = addReadyBalmgiver(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, null);
        assertThat(balmgiver.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.globalDamagePreventionShield).isEqualTo(1);
    }

    @Test
    @DisplayName("Global shield from the prevention ability stops 1 combat damage to a player")
    void preventionAbilityStopsCombatDamage() {
        addReadyBalmgiver(player2);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player2, 0, 0, null, null);
        harness.passBothPriorities();

        Permanent attacker = new Permanent(new GrizzlyBears());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // 2 combat damage - 1 prevented = 1 effective → 20 - 1 = 19
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.globalDamagePreventionShield).isEqualTo(0);
    }

    // ===== Ability 1: {T}: Target creature can't be blocked this turn =====

    @Test
    @DisplayName("Unblockable ability targets a creature and makes it unblockable on resolution")
    void unblockableAbilityMakesTargetUnblockable() {
        Permanent balmgiver = addReadyBalmgiver(player1);
        Permanent target = addGrizzly(player1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        assertThat(balmgiver.isTapped()).isTrue();

        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(target.getId());

        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Unblockable can target an opponent's creature")
    void unblockableCanTargetOpponentCreature() {
        addReadyBalmgiver(player1);
        Permanent target = addGrizzly(player2);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isTrue();
    }

    @Test
    @DisplayName("Unblockable wears off at end of turn")
    void unblockableWearsOffAtEndOfTurn() {
        addReadyBalmgiver(player1);
        Permanent target = addGrizzly(player1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.isCantBeBlocked()).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.isCantBeBlocked()).isFalse();
    }

    @Test
    @DisplayName("Unblockable ability fizzles if the target leaves the battlefield before resolution")
    void unblockableFizzlesIfTargetRemoved() {
        addReadyBalmgiver(player1);
        Permanent target = addGrizzly(player1);

        harness.activateAbility(player1, 0, 1, null, target.getId());
        gd.playerBattlefields.get(player1.getId()).remove(target);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(target.isCantBeBlocked()).isFalse();
    }

    // ===== Helpers =====

    private Permanent addReadyBalmgiver(Player player) {
        Permanent perm = new Permanent(new JhessianBalmgiver());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addGrizzly(Player player) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
