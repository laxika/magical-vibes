package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class HornOfDeafeningTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents combat damage dealt by the target creature")
    void preventsCombatDamageDealtByTargetCreature() {
        harness.setLife(player1, 20);
        addHorn(player1);
        Permanent attacker = addAttacker(player2, player1, 2, 2);

        activateHorn(attacker);
        resolveCombat(player2);

        harness.assertLife(player1, 20);
    }

    @Test
    @DisplayName("Does not prevent combat damage dealt to the target creature")
    void doesNotPreventCombatDamageDealtToTargetCreature() {
        addHorn(player1);
        Permanent attacker = addAttacker(player2, player1, 2, 2);
        addBlocker(player1, 3, 3);

        activateHorn(attacker);
        resolveCombat(player2);

        harness.assertNotOnBattlefield(player2, "Grizzly Bears");
    }

    @Test
    @DisplayName("Does not prevent noncombat damage dealt by the target creature")
    void doesNotPreventNoncombatDamage() {
        addHorn(player1);
        Permanent target = addCreature(player2, 3, 3);

        activateHorn(target);

        assertThat(gqs.isPreventedFromDealingDamage(gd, target, false)).isFalse();
    }

    @Test
    @DisplayName("Cannot target a noncreature permanent")
    void cannotTargetNoncreature() {
        addHorn(player1);
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
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

    private Permanent addCreature(Player owner, int power, int toughness) {
        Card card = new GrizzlyBears();
        card.setPower(power);
        card.setToughness(toughness);
        return addCreatureReady(owner, card);
    }

    private Permanent addAttacker(Player owner, Player defender, int power, int toughness) {
        Permanent attacker = addCreature(owner, power, toughness);
        attacker.setAttacking(true);
        attacker.setAttackTarget(defender.getId());
        return attacker;
    }

    private Permanent addBlocker(Player owner, int power, int toughness) {
        Permanent blocker = addCreature(owner, power, toughness);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        return blocker;
    }
}
