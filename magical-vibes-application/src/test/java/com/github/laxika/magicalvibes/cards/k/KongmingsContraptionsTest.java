package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class KongmingsContraptionsTest extends BaseCardTest {

    @Test
    @DisplayName("Deals 2 damage to attacking creature during declare attackers while attacked")
    void dealsDamageToAttacker() {
        harness.forceActivePlayer(player1);
        Permanent attacker = addAttackerTargeting(player1, player2);
        Permanent contraptions = addContraptionsReady(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player2, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(contraptions.isTapped()).isTrue();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .noneMatch(p -> p.getId().equals(attacker.getId()));
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Cannot activate outside the declare attackers step")
    void cannotActivateOutsideDeclareAttackers() {
        harness.forceActivePlayer(player1);
        Permanent attacker = addAttackerTargeting(player1, player2);
        addContraptionsReady(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("declare attackers step");
    }

    @Test
    @DisplayName("Cannot activate if not being attacked")
    void cannotActivateWhenNotAttacked() {
        harness.forceActivePlayer(player1);
        // Attacker aims at the active player, not the Contraptions controller.
        Permanent attacker = addAttackerTargeting(player1, player1);
        addContraptionsReady(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacked this step");
    }

    @Test
    @DisplayName("Cannot target a non-attacking creature")
    void cannotTargetNonAttackingCreature() {
        harness.forceActivePlayer(player1);
        addAttackerTargeting(player1, player2);
        Permanent bystander = new Permanent(new GrizzlyBears());
        bystander.setSummoningSick(false);
        gd.playerBattlefields.get(player1.getId()).add(bystander);
        addContraptionsReady(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, bystander.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking creature");
    }

    private Permanent addAttackerTargeting(Player attackerController, Player defender) {
        Permanent perm = new Permanent(new GrizzlyBears());
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        perm.setAttackTarget(defender.getId());
        gd.playerBattlefields.get(attackerController.getId()).add(perm);
        return perm;
    }

    private Permanent addContraptionsReady(Player player) {
        Permanent perm = new Permanent(new KongmingsContraptions());
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
