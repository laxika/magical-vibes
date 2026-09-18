package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.h.HillGiant;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({Thunderstaff.class, GrizzlyBears.class, HillGiant.class, ProdigalSorcerer.class})
class ThunderstaffTest extends BaseCardTest {

    @Test
    @DisplayName("Prevents 1 of each creature's combat damage while untapped")
    void preventsCombatDamageWhileUntapped() {
        harness.addToBattlefield(player1, new Thunderstaff());
        harness.setLife(player1, 20);
        addCreatureReady(player2, new HillGiant());

        declareAttackers(player2, List.of(0));

        assertThat(gd.getLife(player1.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Prevents 1 combat damage from each attacking creature")
    void preventsCombatDamagePerCreatureSource() {
        harness.addToBattlefield(player1, new Thunderstaff());
        harness.setLife(player1, 20);
        addCreatureReady(player2, new HillGiant());
        addCreatureReady(player2, new HillGiant());

        declareAttackers(player2, List.of(0, 1));

        assertThat(gd.getLife(player1.getId())).isEqualTo(16);
    }

    @Test
    @DisplayName("Does not prevent combat damage while tapped")
    void doesNotPreventCombatDamageWhileTapped() {
        Permanent staff = harness.addToBattlefieldAndReturn(player1, new Thunderstaff());
        staff.tap();
        harness.setLife(player1, 20);
        addCreatureReady(player2, new HillGiant());

        declareAttackers(player2, List.of(0));

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

    @Test
    @DisplayName("Activated ability boosts attacking creatures on either battlefield")
    void activatedAbilityBoostsOpposingAttackingCreatures() {
        harness.addToBattlefield(player1, new Thunderstaff());
        Permanent ownAttacker = addCreatureReady(player1, new GrizzlyBears());
        Permanent opposingAttacker = addCreatureReady(player2, new HillGiant());
        ownAttacker.setAttacking(true);
        opposingAttacker.setAttacking(true);

        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(ownAttacker.getPowerModifier()).isEqualTo(1);
        assertThat(opposingAttacker.getPowerModifier()).isEqualTo(1);
    }

    @Test
    @DisplayName("Activated ability pays its tap cost")
    void activatedAbilityTapsThunderstaff() {
        Permanent staff = harness.addToBattlefieldAndReturn(player1, new Thunderstaff());
        harness.addMana(player1, ManaColor.COLORLESS, 2);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();

        assertThat(staff.isTapped()).isTrue();
    }

}
