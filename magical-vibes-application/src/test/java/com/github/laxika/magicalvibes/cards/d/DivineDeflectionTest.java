package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.g.GiselaBladeOfGoldnight;
import com.github.laxika.magicalvibes.cards.m.MoorlandInquisitor;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({DivineDeflection.class, MoorlandInquisitor.class, GiselaBladeOfGoldnight.class})
class DivineDeflectionTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents combat damage to the controller and deals it to the target player")
    void preventsDamageToControllerAndDealsToTarget() {
        castDeflection(3, player2.getId());

        Permanent attacker = addAttacker(player2);

        runCombatDamage();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(gd.damageRedirectShields.getFirst().remainingAmount()).isEqualTo(1);
    }

    @Test
    @DisplayName("Prevents combat damage to a permanent the controller controls and deals it to the target")
    void preventsDamageToControlledPermanent() {
        castDeflection(3, player2.getId());

        Permanent attacker = addAttacker(player2);
        Permanent blocker = harness.addToBattlefieldAndReturn(player1, new MoorlandInquisitor());
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        runCombatDamage();

        // The 2 damage the attacker would deal to the blocker is prevented...
        assertThat(blocker.getMarkedDamage()).isZero();
        // ...and Divine Deflection deals that much to its target instead.
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.damageRedirectShields.getFirst().remainingAmount()).isEqualTo(1);
        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Prevented damage can be dealt to a target creature, killing it")
    void dealsPreventedDamageToTargetCreature() {
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new MoorlandInquisitor());

        castDeflection(2, victim.getId());
        addAttacker(player2);

        runCombatDamage();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        harness.assertInGraveyard(player2, "Moorland Inquisitor");
    }

    @Test
    @DisplayName("Excess damage beyond X is dealt normally and the shield is consumed")
    void excessDamageIsNotPrevented() {
        castDeflection(1, player2.getId());

        addAttacker(player2);

        runCombatDamage();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(19);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
        assertThat(gd.damageRedirectShields).isEmpty();
    }

    @Test
    @DisplayName("The shield wears off at end of turn")
    void shieldClearedAtEndOfTurn() {
        castDeflection(3, player2.getId());
        assertThat(gd.damageRedirectShields).hasSize(1);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gd.damageRedirectShields).isEmpty();
    }

    @Test
    @DisplayName("X=0 creates no shield")
    void xZeroCreatesNoShield() {
        castDeflection(0, player2.getId());

        assertThat(gd.damageRedirectShields).isEmpty();
    }

    @Test
    @DisplayName("Gisela doubles the damage Divine Deflection deals to the opponent")
    void reflectedPlayerDamageIsDoubledByGisela() {
        harness.addToBattlefield(player1, new GiselaBladeOfGoldnight());
        castDeflection(1, player2.getId());
        addAttacker(player2);

        runCombatDamage();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 18);
    }

    @Test
    @DisplayName("The resolved shield still prevents damage when its target leaves the battlefield")
    void resolvedShieldSurvivesTargetLeavingBattlefield() {
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new MoorlandInquisitor());
        castDeflection(2, victim.getId());
        gd.playerBattlefields.get(player2.getId()).remove(victim);
        gd.playerGraveyards.get(player2.getId()).add(victim.getCard());
        addAttacker(player2);

        runCombatDamage();

        harness.assertLife(player1, 20);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("An illegal target on resolution prevents the shield from being created")
    void illegalTargetOnResolutionCreatesNoShield() {
        Permanent victim = harness.addToBattlefieldAndReturn(player2, new MoorlandInquisitor());
        harness.setHand(player1, List.of(new DivineDeflection()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.castInstant(player1, 0, 2, victim.getId());
        gd.playerBattlefields.get(player2.getId()).remove(victim);
        gd.playerGraveyards.get(player2.getId()).add(victim.getCard());
        harness.passBothPriorities();
        addAttacker(player2);

        runCombatDamage();

        harness.assertLife(player1, 18);
        harness.assertLife(player2, 20);
    }

    /** Casts Divine Deflection from player1's hand for the given X at the given target and resolves it. */
    private void castDeflection(int xValue, java.util.UUID targetId) {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.setHand(player1, List.of(new DivineDeflection()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, xValue);

        harness.castInstant(player1, 0, xValue, targetId);
        harness.passBothPriorities();
    }

    /** Adds an attacking Moorland Inquisitor (2/2) for the given player and makes them the active player. */
    private Permanent addAttacker(com.github.laxika.magicalvibes.model.Player player) {
        Permanent attacker = harness.addToBattlefieldAndReturn(player, new MoorlandInquisitor());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        harness.forceActivePlayer(player);
        return attacker;
    }

    private void runCombatDamage() {
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();
    }
}
