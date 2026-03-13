package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.PreventXDamageToControllerAndRedirectToTargetPlayerEffect;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class VengefulArchonTest extends BaseCardTest {

    // ===== Card properties =====

    @Test
    @DisplayName("Vengeful Archon has correct activated ability")
    void hasCorrectAbility() {
        VengefulArchon card = new VengefulArchon();

        assertThat(card.getActivatedAbilities()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getManaCost()).isEqualTo("{X}");
        assertThat(card.getActivatedAbilities().get(0).isRequiresTap()).isFalse();
        assertThat(card.getActivatedAbilities().get(0).isNeedsTarget()).isTrue();
        assertThat(card.getActivatedAbilities().get(0).getEffects()).hasSize(1);
        assertThat(card.getActivatedAbilities().get(0).getEffects().getFirst())
                .isInstanceOf(PreventXDamageToControllerAndRedirectToTargetPlayerEffect.class);
    }

    // ===== Activation =====

    @Test
    @DisplayName("Activating ability puts it on the stack")
    void activatingPutsOnStack() {
        addReadyArchon(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 3, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Vengeful Archon");
        assertThat(entry.getXValue()).isEqualTo(3);
    }

    @Test
    @DisplayName("Activating ability does not tap the Archon")
    void activatingDoesNotTap() {
        Permanent archon = addReadyArchon(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 3, player2.getId());

        assertThat(archon.isTapped()).isFalse();
    }

    @Test
    @DisplayName("Mana is consumed when activating ability")
    void manaIsConsumedWhenActivating() {
        addReadyArchon(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        harness.activateAbility(player1, 0, 3, player2.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.playerManaPools.get(player1.getId()).getTotal()).isEqualTo(2);
    }

    // ===== Resolution =====

    @Test
    @DisplayName("Resolving ability creates a damage redirect shield")
    void resolvingCreatesRedirectShield() {
        addReadyArchon(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.damageRedirectShields).hasSize(1);
        assertThat(gd.damageRedirectShields.getFirst().protectedPlayerId()).isEqualTo(player1.getId());
        assertThat(gd.damageRedirectShields.getFirst().remainingAmount()).isEqualTo(3);
        assertThat(gd.damageRedirectShields.getFirst().redirectTargetPlayerId()).isEqualTo(player2.getId());
    }

    @Test
    @DisplayName("Resolving ability with X=0 does not create a shield")
    void resolvingWithXZeroNoShield() {
        addReadyArchon(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, 0, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.damageRedirectShields).isEmpty();
    }

    // ===== Damage prevention and redirect =====

    @Test
    @DisplayName("Redirect shield prevents damage to controller and deals it to target")
    void redirectShieldPreventsDamageAndDealsToTarget() {
        addReadyArchon(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        // Activate ability: prevent next 3, redirect to player2
        harness.activateAbility(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        // Set up combat: player2 attacks with Grizzly Bears (2/2) against player1
        harness.forceActivePlayer(player2);
        GrizzlyBears bear = new GrizzlyBears();
        Permanent attacker = new Permanent(bear);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Player1 takes 0 damage (2 prevented by redirect shield)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        // Player2 takes 2 damage (redirected from Vengeful Archon)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Redirect shield partially consumed leaves remainder")
    void redirectShieldPartiallyConsumed() {
        addReadyArchon(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        // Activate ability: prevent next 5, redirect to player2
        harness.activateAbility(player1, 0, 5, player2.getId());
        harness.passBothPriorities();

        // Set up combat: player2 attacks with Grizzly Bears (2/2) against player1
        harness.forceActivePlayer(player2);
        GrizzlyBears bear = new GrizzlyBears();
        Permanent attacker = new Permanent(bear);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Player1 takes 0 damage (2 prevented)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        // Player2 takes 2 damage (redirected)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        // Shield has 3 remaining
        assertThat(gd.damageRedirectShields).hasSize(1);
        assertThat(gd.damageRedirectShields.getFirst().remainingAmount()).isEqualTo(3);
    }

    @Test
    @DisplayName("Redirect shield fully consumed when damage exceeds shield amount")
    void redirectShieldFullyConsumedExcessDamage() {
        addReadyArchon(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        // Activate ability: prevent next 1, redirect to player2
        harness.activateAbility(player1, 0, 1, player2.getId());
        harness.passBothPriorities();

        // Set up combat: player2 attacks with Grizzly Bears (2/2) against player1
        harness.forceActivePlayer(player2);
        GrizzlyBears bear = new GrizzlyBears();
        Permanent attacker = new Permanent(bear);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.getGameData().playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        // Player1 takes 1 damage (1 prevented, 1 goes through)
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        // Player2 takes 1 damage (redirected portion)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        // Shield fully consumed
        assertThat(gd.damageRedirectShields).isEmpty();
    }

    // ===== End of turn cleanup =====

    @Test
    @DisplayName("Redirect shield is cleared at end of turn")
    void redirectShieldClearedAtEndOfTurn() {
        addReadyArchon(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 3);

        harness.activateAbility(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        assertThat(harness.getGameData().damageRedirectShields).hasSize(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(harness.getGameData().damageRedirectShields).isEmpty();
    }

    // ===== Can activate multiple times =====

    @Test
    @DisplayName("Ability can be activated multiple times")
    void canActivateMultipleTimes() {
        addReadyArchon(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 5);

        // First activation: X=2
        harness.activateAbility(player1, 0, 2, player2.getId());
        harness.passBothPriorities();

        // Second activation: X=3
        harness.addMana(player1, ManaColor.COLORLESS, 3);
        harness.activateAbility(player1, 0, 3, player2.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.damageRedirectShields).hasSize(2);
    }

    // ===== Helpers =====

    private Permanent addReadyArchon(Player player) {
        VengefulArchon card = new VengefulArchon();
        Permanent archon = new Permanent(card);
        archon.setSummoningSick(false);
        harness.getGameData().playerBattlefields.get(player.getId()).add(archon);
        return archon;
    }
}
