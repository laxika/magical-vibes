package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.r.RuneclawBear;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({InfantryVeteran.class, RuneclawBear.class})
class InfantryVeteranTest extends BaseCardTest {

    // ===== Activation on attacking creature =====

    @Test
    @DisplayName("Activating ability on attacking creature puts it on the stack")
    void activatingOnAttackingCreaturePutsOnStack() {
        addReadyVeteran(player1);
        Permanent attacker = addAttackingCreature(player1);

        harness.activateAbility(player1, 0, null, attacker.getId());

        GameData gd = harness.getGameData();
        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getTargetId()).isEqualTo(attacker.getId());
    }

    @Test
    @DisplayName("Resolving ability gives attacking creature +1/+1")
    void resolvingBoostsAttackingCreature() {
        addReadyVeteran(player1);
        Permanent attacker = addAttackingCreature(player1);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        GameData gd = harness.getGameData();
        assertThat(gd.stack).isEmpty();
        assertThat(attacker.getPowerModifier()).isEqualTo(1);
        assertThat(attacker.getToughnessModifier()).isEqualTo(1);
        assertThat(attacker.getEffectivePower()).isEqualTo(3);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(3);
    }

    // ===== Tap cost =====

    @Test
    @DisplayName("Activating ability taps Infantry Veteran")
    void activatingTapsVeteran() {
        Permanent veteran = addReadyVeteran(player1);
        Permanent attacker = addAttackingCreature(player1);

        harness.activateAbility(player1, 0, null, attacker.getId());

        assertThat(veteran.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Cannot activate ability with summoning sickness")
    void cannotActivateWithSummoningSickness() {
        harness.addToBattlefield(player1, new InfantryVeteran());
        Permanent attacker = addAttackingCreature(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("summoning sickness");
    }

    @Test
    @DisplayName("Cannot activate ability when already tapped")
    void cannotActivateWhenAlreadyTapped() {
        Permanent veteran = addReadyVeteran(player1);
        veteran.tap();
        Permanent attacker = addAttackingCreature(player1);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, attacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("already tapped");
    }

    // ===== Target restriction: must be attacking =====

    @Test
    @DisplayName("Cannot target a non-attacking creature")
    void cannotTargetNonAttackingCreature() {
        addReadyVeteran(player1);
        Permanent nonAttacker = addCreatureReady(player1, new RuneclawBear());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, nonAttacker.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be an attacking creature");
    }

    // ===== Can target opponent's attacking creature =====

    @Test
    @DisplayName("Can target opponent's attacking creature")
    void canTargetOpponentAttackingCreature() {
        addReadyVeteran(player1);
        Permanent opponentAttacker = addAttackingCreature(player2);

        harness.activateAbility(player1, 0, null, opponentAttacker.getId());
        harness.passBothPriorities();

        assertThat(opponentAttacker.getPowerModifier()).isEqualTo(1);
        assertThat(opponentAttacker.getToughnessModifier()).isEqualTo(1);
    }

    // ===== End of turn cleanup =====

    @Test
    @DisplayName("Boost resets at end of turn")
    void boostResetsAtEndOfTurn() {
        addReadyVeteran(player1);
        Permanent attacker = addAttackingCreature(player1);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isEqualTo(1);
        assertThat(attacker.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isEqualTo(0);
        assertThat(attacker.getToughnessModifier()).isEqualTo(0);
        assertThat(attacker.getEffectivePower()).isEqualTo(2);
        assertThat(attacker.getEffectiveToughness()).isEqualTo(2);
    }

    // ===== Fizzle =====

    @Test
    @DisplayName("Ability fizzles if target creature is removed before resolution")
    void abilityFizzlesIfTargetRemoved() {
        addReadyVeteran(player1);
        Permanent attacker = addAttackingCreature(player1);

        harness.activateAbility(player1, 0, null, attacker.getId());

        gd.playerBattlefields.get(player1.getId()).remove(attacker);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
    }

    @Test
    @DisplayName("Ability does not resolve if the target stops attacking")
    void abilityFizzlesIfTargetStopsAttacking() {
        addReadyVeteran(player1);
        Permanent attacker = addAttackingCreature(player1);

        harness.activateAbility(player1, 0, null, attacker.getId());
        attacker.setAttacking(false);

        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(attacker.getPowerModifier()).isEqualTo(0);
        assertThat(attacker.getToughnessModifier()).isEqualTo(0);
    }

    // ===== Helpers =====

    @Test
    @DisplayName("An attacking Infantry Veteran can target itself and remains attacking after paying the tap cost")
    void attackingVeteranCanBoostItself() {
        Permanent veteran = addReadyVeteran(player1);
        veteran.setAttacking(true);

        harness.activateAbility(player1, 0, null, veteran.getId());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(veteran.isTapped()).isTrue();
        assertThat(veteran.isAttacking()).isTrue();
        assertThat(veteran.getPowerModifier()).isEqualTo(1);
        assertThat(veteran.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("The ability resolves even if Infantry Veteran leaves the battlefield")
    void abilityResolvesAfterSourceLeavesBattlefield() {
        Permanent veteran = addReadyVeteran(player1);
        Permanent attacker = addAttackingCreature(player1);
        attacker.tap();

        harness.activateAbility(player1, 0, null, attacker.getId());
        gd.playerBattlefields.get(player1.getId()).remove(veteran);
        gd.playerGraveyards.get(player1.getId()).add(veteran.getCard());
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(attacker.getPowerModifier()).isEqualTo(1);
        assertThat(attacker.getToughnessModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("A resolved boost remains after the creature stops attacking")
    void resolvedBoostRemainsAfterCombat() {
        addReadyVeteran(player1);
        Permanent attacker = addAttackingCreature(player1);

        harness.activateAbility(player1, 0, null, attacker.getId());
        harness.passBothPriorities();
        attacker.setAttacking(false);

        assertThat(attacker.getPowerModifier()).isEqualTo(1);
        assertThat(attacker.getToughnessModifier()).isEqualTo(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isZero();
        assertThat(attacker.getToughnessModifier()).isZero();
    }

    private Permanent addReadyVeteran(Player player) {
        return addCreatureReady(player, new InfantryVeteran());
    }

    private Permanent addAttackingCreature(Player player) {
        Permanent perm = addCreatureReady(player, new RuneclawBear());
        perm.setAttacking(true);
        return perm;
    }
}
