package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.i.InvasionOfKamigawa;
import com.github.laxika.magicalvibes.cards.r.RooftopSaboteurs;
import com.github.laxika.magicalvibes.cards.w.WuInfantry;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({KongmingsContraptions.class, WuInfantry.class, InvasionOfKamigawa.class,
        RooftopSaboteurs.class})
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
        harness.assertNotOnBattlefield(player1, "Wu Infantry");
        harness.assertInGraveyard(player1, "Wu Infantry");
    }

    @Test
    @DisplayName("Deals exactly 2 damage without destroying a tougher attacking creature")
    void dealsExactlyTwoDamage() {
        harness.forceActivePlayer(player1);
        Permanent attacker = addAttackerTargeting(player1, player2, new KongmingsContraptions());
        addContraptionsReady(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player2, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
        harness.assertOnBattlefield(player1, "Kongming's Contraptions");
    }

    @Test
    @DisplayName("Does not deal damage if the target stops attacking before resolution")
    void doesNotDamageTargetThatStopsAttacking() {
        harness.forceActivePlayer(player1);
        Permanent attacker = addAttackerTargeting(player1, player2);
        addContraptionsReady(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player2, 0, null, attacker.getId());
        attacker.setAttacking(false);
        harness.passBothPriorities();

        assertThat(attacker.getMarkedDamage()).isZero();
        harness.assertOnBattlefield(player1, "Wu Infantry");
    }

    @Test
    @DisplayName("Cannot activate when only your battle is attacked")
    void cannotActivateWhenOnlyBattleIsAttacked() {
        harness.forceActivePlayer(player2);
        addContraptionsReady(player2);
        Permanent battle = harness.addToBattlefieldAndReturn(player2, new InvasionOfKamigawa());
        battle.setProtectorPlayerId(player1.getId());
        battle.setCounterCount(CounterType.DEFENSE, 4);
        Permanent attacker = addAttackerTargeting(player2, player1);
        attacker.setAttackTarget(battle.getId());
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacked this step");
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
        Permanent bystander = addCreatureReady(player1, new WuInfantry());
        addContraptionsReady(player2);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        assertThatThrownBy(() -> harness.activateAbility(player2, 0, null, bystander.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("attacking creature");
    }

    private Permanent addAttackerTargeting(Player attackerController, Player defender, Card card) {
        Permanent perm = addCreatureReady(attackerController, card);
        perm.setAttacking(true);
        perm.setAttackTarget(defender.getId());
        return perm;
    }

    private Permanent addAttackerTargeting(Player attackerController, Player defender) {
        return addAttackerTargeting(attackerController, defender, new WuInfantry());
    }

    private Permanent addContraptionsReady(Player player) {
        return addCreatureReady(player, new KongmingsContraptions());
    }
}
