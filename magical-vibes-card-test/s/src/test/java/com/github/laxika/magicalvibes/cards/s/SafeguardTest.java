package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.FightingDrake;
import com.github.laxika.magicalvibes.cards.f.Fireslinger;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({Safeguard.class, FightingDrake.class, Fireslinger.class, Forest.class})
class SafeguardTest extends BaseCardTest {

    @Test
    @DisplayName("Prevented attacker deals no combat damage to the player")
    void preventsCombatDamageToPlayer() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new Safeguard());
        Permanent attacker = addAttacker(player2, new FightingDrake());

        activateSafeguard(attacker);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Only combat damage is prevented, not all damage")
    void combatDamageOnly() {
        harness.addToBattlefield(player1, new Safeguard());
        Permanent attacker = addAttacker(player2, new FightingDrake());

        activateSafeguard(attacker);

        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());
        assertThat(gd.permanentsPreventedFromDealingDamage).doesNotContain(attacker.getId());
    }

    @Test
    @DisplayName("Prevention is cleared at end of turn")
    void preventionClearedAtEndOfTurn() {
        harness.addToBattlefield(player1, new Safeguard());
        Permanent attacker = addAttacker(player2, new FightingDrake());

        activateSafeguard(attacker);
        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.POSTCOMBAT_MAIN);
        harness.passUntil(player1, TurnStep.END_STEP);

        assertThat(gd.creaturesPreventedFromDealingCombatDamage).contains(attacker.getId());

        harness.passUntil(player2, TurnStep.UPKEEP);

        assertThat(gd.creaturesPreventedFromDealingCombatDamage).isEmpty();

        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        harness.assertLife(player1, 18);
    }

    @Test
    @DisplayName("Targeted creature can still deal noncombat damage")
    void noncombatDamageIsNotPrevented() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);
        harness.addToBattlefield(player1, new Safeguard());
        Permanent fireslinger = addCreatureReady(player2, new Fireslinger());

        activateSafeguard(fireslinger);

        harness.activateAbility(player2, 0, null, player1.getId());
        harness.passBothPriorities();

        harness.assertLife(player1, 19);
        harness.assertLife(player2, 19);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        harness.addToBattlefield(player1, new Safeguard());
        harness.addToBattlefield(player1, new Forest());
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        UUID forestId = harness.getPermanentId(player1, "Forest");

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forestId))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Prevented attacker still receives combat damage from its blocker")
    void preventsDamageByAttackerButNotDamageToIt() {
        harness.addToBattlefield(player1, new Safeguard());
        Permanent attacker = addAttacker(player2, new FightingDrake());
        Permanent blocker = addCreatureReady(player1, new FightingDrake());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        activateSafeguard(attacker);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        assertThat(attacker.getMarkedDamage()).isEqualTo(2);
        assertThat(blocker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Can prevent combat damage by a blocking creature you control")
    void preventsDamageByOwnBlocker() {
        harness.addToBattlefield(player1, new Safeguard());
        Permanent attacker = addAttacker(player2, new FightingDrake());
        Permanent blocker = addCreatureReady(player1, new FightingDrake());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        activateSafeguard(blocker);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        assertThat(attacker.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Multiple activations prevent only the targeted creatures' damage")
    void canPreventMultipleCreaturesWithoutPreventingOthers() {
        harness.setLife(player1, 20);
        harness.addToBattlefield(player1, new Safeguard());
        Permanent first = addAttacker(player2, new FightingDrake());
        Permanent second = addAttacker(player2, new FightingDrake());
        addAttacker(player2, new FightingDrake());

        activateSafeguard(first);
        activateSafeguard(second);
        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        harness.assertLife(player1, 18);
    }

    private void activateSafeguard(Permanent target) {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();
    }

    private Permanent addAttacker(Player owner, com.github.laxika.magicalvibes.model.Card card) {
        Permanent attacker = addCreatureReady(owner, card);
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        return attacker;
    }
}
