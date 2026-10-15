package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GrayscaledGharial;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SunhomeEnforcer.class, GrayscaledGharial.class})
class SunhomeEnforcerTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage to a player gains that much life")
    void combatDamageToPlayerGainsLife() {
        addAttacker(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Combat damage to a creature gains that much life")
    void combatDamageToCreatureGainsLife() {
        addAttacker(player1);
        Permanent blocker = addCreatureReady(player2, new GrayscaledGharial());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setLife(player1, 20);

        resolveCombat();
        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("Activated ability gives +1/+0 until end of turn")
    void activatedAbilityBoostsSelfUntilEndOfTurn() {
        Permanent enforcer = addReadyEnforcer(player1);
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(enforcer.getPowerModifier()).isEqualTo(1);
        assertThat(enforcer.getToughnessModifier()).isEqualTo(0);
        assertThat(enforcer.isTapped()).isFalse();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(enforcer.getPowerModifier()).isEqualTo(0);
    }

    @Test
    @DisplayName("Repeated activations increase damage and life gain, which waits for resolution")
    void boostedCombatDamageGainsLifeOnlyWhenTriggerResolves() {
        Permanent enforcer = addReadyEnforcer(player1);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        enforcer.setAttacking(true);

        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(16);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(gd.stack).hasSize(1);

        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
    }

    @Test
    @DisplayName("A blocking Enforcer still gains life when it dies from simultaneous combat damage")
    void dyingBlockerStillTriggersLifeGainForItsController() {
        Permanent attacker = addReadyEnforcer(player1);
        harness.addMana(player1, ManaColor.RED, 2);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        attacker.setAttacking(true);
        Permanent blocker = addReadyEnforcer(player2);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        resolveCombat();

        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(blocker);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);

        resolveAllTriggers();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(24);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(22);
    }

    @Test
    @DisplayName("The pump ability can be activated while tapped and summoning sick")
    void tappedSummoningSickEnforcerCanActivate() {
        Permanent enforcer = harness.addToBattlefieldAndReturn(player1, new SunhomeEnforcer());
        enforcer.setSummoningSick(true);
        enforcer.tap();
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(enforcer.getPowerModifier()).isEqualTo(1);
        assertThat(enforcer.getToughnessModifier()).isZero();
        assertThat(enforcer.isTapped()).isTrue();
    }

    private Permanent addAttacker(Player player) {
        Permanent enforcer = addReadyEnforcer(player);
        enforcer.setAttacking(true);
        return enforcer;
    }

    private Permanent addReadyEnforcer(Player player) {
        return addCreatureReady(player, new SunhomeEnforcer());
    }
}
