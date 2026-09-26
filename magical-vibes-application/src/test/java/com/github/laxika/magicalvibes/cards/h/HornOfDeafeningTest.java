package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.d.DurkwoodBoars;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({HornOfDeafening.class, DurkwoodBoars.class})
class HornOfDeafeningTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents combat damage dealt by the target creature")
    void preventsCombatDamageDealtByTargetCreature() {
        harness.setLife(player1, 20);
        addHorn(player1);
        Permanent attacker = addAttacker(player2, player1);

        activateHorn(attacker);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does not prevent combat damage dealt to the target creature")
    void doesNotPreventCombatDamageDealtToTargetCreature() {
        addHorn(player1);
        Permanent attacker = addAttacker(player2, player1);
        addBlocker(player1);

        activateHorn(attacker);
        resolveCombat(player2);

        harness.assertNotOnBattlefield(player2, "Durkwood Boars");
        harness.assertOnBattlefield(player1, "Durkwood Boars");
    }

    @Test
    @DisplayName("Does not prevent noncombat damage dealt by the target creature")
    void doesNotPreventNoncombatDamage() {
        addHorn(player1);
        Permanent target = addCreature(player2);

        activateHorn(target);

        assertThat(gqs.isPreventedFromDealingDamage(gd, target, true)).isTrue();
        assertThat(gqs.isPreventedFromDealingDamage(gd, target, false)).isFalse();
    }

    @Test
    @DisplayName("Prevention ends at cleanup")
    void preventionEndsAtCleanup() {
        addHorn(player1);
        Permanent target = addCreature(player2);

        activateHorn(target);

        assertThat(gqs.isPreventedFromDealingDamage(gd, target, true)).isTrue();

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(gqs.isPreventedFromDealingDamage(gd, target, true)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        addHorn(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Permanent horn = harness.addToBattlefieldAndReturn(player2, new HornOfDeafening());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, horn.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    private void activateHorn(Permanent target) {
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addHorn(Player owner) {
        return harness.addToBattlefieldAndReturn(owner, new HornOfDeafening());
    }

    private Permanent addCreature(Player owner) {
        return addCreatureReady(owner, new DurkwoodBoars());
    }

    private Permanent addAttacker(Player owner, Player defender) {
        Permanent attacker = addCreature(owner);
        attacker.setAttacking(true);
        attacker.setAttackTarget(defender.getId());
        return attacker;
    }

    private Permanent addBlocker(Player owner) {
        Permanent blocker = addCreature(owner);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        return blocker;
    }
}
