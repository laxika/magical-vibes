package com.github.laxika.magicalvibes.cards.j;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({JhessianBalmgiver.class, GrizzlyBears.class})
class JhessianBalmgiverTest extends BaseCardTest {

    @Test
    @DisplayName("Prevention ability taps the Balmgiver and shields the target player")
    void preventionAbilitySetsShield() {
        Permanent balmgiver = addReadyBalmgiver(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player1, 0, 0, null, player2.getId());
        assertThat(balmgiver.isTapped()).isTrue();

        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields).containsEntry(player2.getId(), 1);
    }

    @Test
    @DisplayName("The prevention ability stops 1 combat damage to the target player")
    void preventionAbilityStopsCombatDamage() {
        addReadyBalmgiver(player2);
        harness.setLife(player2, 20);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.PRECOMBAT_MAIN);

        harness.activateAbility(player2, 0, 0, null, player2.getId());
        harness.passBothPriorities();

        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // 2 combat damage - 1 prevented = 1 effective → 20 - 1 = 19
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.playerDamagePreventionShields).doesNotContainKey(player2.getId());
    }

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

    @Test
    @DisplayName("Prevention protects the Balmgiver itself from combat damage")
    void preventionCanTargetSelf() {
        Permanent target = addReadyBalmgiver(player2);
        Permanent attacker = addReadyBalmgiver(player1);
        attacker.setAttacking(true);
        target.setBlocking(true);
        target.addBlockingTarget(0);
        target.addBlockingTargetId(attacker.getId());
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.activateAbility(player2, 0, 0, null, target.getId());
        harness.passBothPriorities();
        harness.resolveCombatDamage();

        assertThat(target.getDamagePreventionShield()).isZero();
        assertThat(target.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(target);
    }

    @Test
    @DisplayName("Unused prevention expires at end of turn")
    void preventionExpiresAtEndOfTurn() {
        addReadyBalmgiver(player1);
        Permanent target = addReadyBalmgiver(player2);
        harness.activateAbility(player1, 0, 0, null, target.getId());
        harness.passBothPriorities();
        assertThat(target.getDamagePreventionShield()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getDamagePreventionShield()).isZero();
    }

    @Test
    @DisplayName("Neither tap ability can be used by a summoning-sick Balmgiver")
    void summoningSicknessPreventsBothAbilities() {
        Permanent balmgiver = harness.addToBattlefieldAndReturn(player1, new JhessianBalmgiver());
        balmgiver.setSummoningSick(true);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 0, null, player1.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, balmgiver.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(balmgiver.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Prevention resolves even if the Balmgiver leaves the battlefield")
    void preventionSurvivesSourceRemoval() {
        Permanent balmgiver = addReadyBalmgiver(player1);
        harness.activateAbility(player1, 0, 0, null, player2.getId());
        gd.playerBattlefields.get(player1.getId()).remove(balmgiver);

        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields).containsEntry(player2.getId(), 1);
    }

    @Test
    @DisplayName("Prevention does not shield a creature that left before resolution")
    void preventionFizzlesIfTargetRemoved() {
        addReadyBalmgiver(player1);
        Permanent target = addReadyBalmgiver(player2);
        harness.activateAbility(player1, 0, 0, null, target.getId());
        gd.playerBattlefields.get(player2.getId()).remove(target);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(target.getDamagePreventionShield()).isZero();
    }

    @Test
    @DisplayName("Two prevention activations accumulate and expire for a player")
    void playerShieldsAccumulateAndExpire() {
        addReadyBalmgiver(player1);
        addReadyBalmgiver(player1);
        harness.activateAbility(player1, 0, 0, null, player2.getId());
        harness.passBothPriorities();
        harness.activateAbility(player1, 1, 0, null, player2.getId());
        harness.passBothPriorities();
        assertThat(gd.playerDamagePreventionShields).containsEntry(player2.getId(), 2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields).doesNotContainKey(player2.getId());
    }

    @Test
    @DisplayName("Paying one tap cost prevents activating the other ability")
    void abilitiesShareTapCost() {
        Permanent balmgiver = addReadyBalmgiver(player1);
        harness.activateAbility(player1, 0, 0, null, player1.getId());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, balmgiver.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.stack).hasSize(1);
    }

    @Test
    @DisplayName("Unblockable ability cannot target a player")
    void unblockableRejectsPlayerTarget() {
        Permanent balmgiver = addReadyBalmgiver(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, 1, null, player2.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(balmgiver.isTapped()).isFalse();
        assertThat(gd.stack).isEmpty();
    }

    private Permanent addReadyBalmgiver(Player player) {
        return addCreatureReady(player, new JhessianBalmgiver());
    }

    private Permanent addGrizzly(Player player) {
        return addCreatureReady(player, new GrizzlyBears());
    }
}
