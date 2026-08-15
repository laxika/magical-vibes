package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class StonewiseFortifierTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents all damage from the targeted creature to itself")
    void preventsDamageFromTargetCreatureToItself() {
        Permanent fortifier = addCreatureReady(player1, new StonewiseFortifier());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());
        fortifier.setBlocking(true);
        fortifier.addBlockingTarget(0);

        activateFortifier(fortifier, attacker);
        resolveCombat(player2);

        assertThat(fortifier.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fortifier);
    }

    @Test
    @DisplayName("Prevents noncombat damage from the targeted creature to itself")
    void preventsNoncombatDamageFromTargetCreatureToItself() {
        Permanent fortifier = addCreatureReady(player1, new StonewiseFortifier());
        Permanent sorcerer = addCreatureReady(player2, new ProdigalSorcerer());

        activateFortifier(fortifier, sorcerer);
        harness.activateAbility(player2, 0, null, fortifier.getId());
        harness.passBothPriorities();

        assertThat(fortifier.getMarkedDamage()).isZero();
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fortifier);
    }

    @Test
    @DisplayName("Does not prevent the targeted creature from damaging other recipients")
    void doesNotPreventDamageToOtherRecipients() {
        harness.setLife(player1, 20);
        Permanent fortifier = addCreatureReady(player1, new StonewiseFortifier());
        Permanent attacker = addCreatureReady(player2, new GrizzlyBears());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());

        activateFortifier(fortifier, attacker);
        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not prevent damage from another creature")
    void doesNotPreventDamageFromAnotherCreature() {
        Permanent fortifier = addCreatureReady(player1, new StonewiseFortifier());
        Permanent targetedCreature = addCreatureReady(player2, new GrizzlyBears());
        Permanent otherAttacker = addCreatureReady(player2, new GrizzlyBears());
        otherAttacker.setAttacking(true);
        otherAttacker.setAttackTarget(player1.getId());
        fortifier.setBlocking(true);
        fortifier.addBlockingTarget(1);

        activateFortifier(fortifier, targetedCreature);
        resolveCombat(player2);

        assertThat(gd.playerBattlefields.get(player1.getId())).doesNotContain(fortifier);
    }

    @Test
    @DisplayName("Cannot target a non-creature permanent")
    void cannotTargetNonCreature() {
        Permanent fortifier = addCreatureReady(player1, new StonewiseFortifier());
        harness.addToBattlefield(player2, new Forest());
        Permanent forest = findPermanent(player2, "Forest");
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);

        assertThatThrownBy(() -> harness.activateAbility(player1, 0, null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
        assertThat(gd.playerBattlefields.get(player1.getId())).contains(fortifier);
    }

    private void activateFortifier(Permanent fortifier, Permanent target) {
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 4);
        int fortifierIndex = gd.playerBattlefields.get(player1.getId()).indexOf(fortifier);
        harness.activateAbility(player1, fortifierIndex, null, target.getId());
        harness.passBothPriorities();
    }
}
