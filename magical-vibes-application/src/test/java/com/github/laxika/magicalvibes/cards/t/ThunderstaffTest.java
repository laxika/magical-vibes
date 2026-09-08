package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.assertj.core.api.Assertions.assertThat;

class ThunderstaffTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents 1 of each creature's combat damage while untapped")
    void preventsCombatDamageWhileUntapped() {
        harness.addToBattlefield(player1, new Thunderstaff());
        harness.setLife(player1, 20);
        Permanent attacker = addCreatureReady(player2, new HillGiant());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());

        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Does not prevent combat damage while tapped")
    void doesNotPreventCombatDamageWhileTapped() {
        Permanent staff = harness.addToBattlefieldAndReturn(player1, new Thunderstaff());
        staff.tap();
        harness.setLife(player1, 20);
        Permanent attacker = addCreatureReady(player2, new HillGiant());
        attacker.setAttacking(true);
        attacker.setAttackTarget(player1.getId());

        resolveCombat(player2);

        assertThat(gd.getLife(player1.getId())).isEqualTo(17);
    }

    @Test
    @DisplayName("Does not prevent a creature's noncombat damage")
    void doesNotPreventNoncombatDamage() {
        harness.addToBattlefield(player1, new Thunderstaff());
        harness.setLife(player1, 20);
        Permanent sorcerer = addCreatureReady(player1, new ProdigalSorcerer());

        harness.activateAbility(player1, 1, null, player1.getId());
        harness.passBothPriorities();

        assertThat(gd.getLife(player1.getId())).isEqualTo(19);
        assertThat(sorcerer.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Activated ability boosts attacking creatures only")
    void activatedAbilityBoostsAttackingCreaturesOnly() {
        harness.addToBattlefield(player1, new Thunderstaff());
        Permanent nonAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(attacker.getPowerModifier()).isEqualTo(1);
        assertThat(nonAttacker.getPowerModifier()).isZero();
    }

}
