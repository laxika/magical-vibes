package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.a.AvenFlock;
import com.github.laxika.magicalvibes.cards.f.Firebolt;
import com.github.laxika.magicalvibes.cards.f.Forest;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({MasterApothecary.class, AvenFlock.class, Forest.class, Firebolt.class})
class MasterApothecaryTest extends BaseCardTest {

    @Test
    @DisplayName("Tapping an untapped Cleric prevents 2 damage to a target creature")
    void preventsDamageToCreature() {
        Permanent apothecary = addCreatureReady(player1, new MasterApothecary());
        Permanent cleric = addCreatureReady(player1, new MasterApothecary());
        Permanent target = addCreatureReady(player2, new AvenFlock());

        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(apothecary);
        harness.activateAbility(player1, sourceIndex, null, target.getId());
        harness.handlePermanentChosen(player1, cleric.getId());
        harness.passBothPriorities();

        assertThat(cleric.isTapped()).isTrue();
        assertThat(apothecary.isTapped()).isFalse();
        assertThat(target.getDamagePreventionShield()).isEqualTo(2);
    }

    @Test
    @DisplayName("Tapping an untapped Cleric prevents 2 damage to a target player")
    void preventsDamageToPlayer() {
        Permanent apothecary = addCreatureReady(player1, new MasterApothecary());
        Permanent cleric = addCreatureReady(player1, new MasterApothecary());

        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(apothecary);
        harness.activateAbility(player1, sourceIndex, null, player2.getId());
        harness.handlePermanentChosen(player1, cleric.getId());
        harness.passBothPriorities();

        assertThat(gd.playerDamagePreventionShields.get(player2.getId())).isEqualTo(2);
    }

    @Test
    @DisplayName("The player shield prevents the next 2 combat damage to the target player")
    void preventsNextTwoDamageToPlayer() {
        Permanent apothecary = addCreatureReady(player1, new MasterApothecary());
        Permanent cleric = addCreatureReady(player1, new MasterApothecary());
        Permanent attacker = addCreatureReady(player1, new AvenFlock());

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(apothecary), null, player2.getId());
        harness.handlePermanentChosen(player1, cleric.getId());
        harness.passBothPriorities();

        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        resolveCombat(player1);

        harness.assertLife(player2, 20);
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("The player shield prevents the next 2 noncombat damage to the target player")
    void preventsNextTwoNoncombatDamageToPlayer() {
        Permanent apothecary = addCreatureReady(player1, new MasterApothecary());
        Permanent cleric = addCreatureReady(player1, new MasterApothecary());

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(apothecary), null, player2.getId());
        harness.handlePermanentChosen(player1, cleric.getId());
        harness.passBothPriorities();

        harness.setHand(player1, List.of(new Firebolt()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.castAndResolveSorcery(player1, 0, player2.getId());

        harness.assertLife(player2, 20);
        assertThat(gd.playerDamagePreventionShields.getOrDefault(player2.getId(), 0)).isZero();
    }

    @Test
    @DisplayName("The prevention shield prevents the next 2 damage dealt to a target creature")
    void preventsNextTwoDamageToCreature() {
        Permanent apothecary = addCreatureReady(player1, new MasterApothecary());
        Permanent cleric = addCreatureReady(player1, new MasterApothecary());
        Permanent attacker = addCreatureReady(player1, new AvenFlock());
        Permanent target = addCreatureReady(player2, new AvenFlock());

        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(apothecary);
        harness.activateAbility(player1, sourceIndex, null, target.getId());
        harness.handlePermanentChosen(player1, cleric.getId());
        harness.passBothPriorities();

        attacker.setAttacking(true);
        attacker.setAttackTarget(player2.getId());
        target.setBlocking(true);
        target.addBlockingTarget(gd.playerBattlefields.get(player1.getId()).indexOf(attacker));
        resolveCombat(player1);

        assertThat(target.getMarkedDamage()).isZero();
        assertThat(target.getDamagePreventionShield()).isZero();
    }

    @Test
    @DisplayName("The source itself may be tapped as the Cleric cost")
    void mayTapSourceAsClericCost() {
        Permanent apothecary = addCreatureReady(player1, new MasterApothecary());
        Permanent target = addCreatureReady(player2, new AvenFlock());

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(apothecary), null, target.getId());
        harness.handlePermanentChosen(player1, apothecary.getId());
        harness.passBothPriorities();

        assertThat(apothecary.isTapped()).isTrue();
        assertThat(target.getDamagePreventionShield()).isEqualTo(2);
    }

    @Test
    @DisplayName("The prevention shield expires at end of turn")
    void preventionShieldExpiresAtEndOfTurn() {
        Permanent apothecary = addCreatureReady(player1, new MasterApothecary());
        Permanent cleric = addCreatureReady(player1, new MasterApothecary());
        Permanent target = addCreatureReady(player2, new AvenFlock());

        harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(apothecary), null, target.getId());
        harness.handlePermanentChosen(player1, cleric.getId());
        harness.passBothPriorities();

        assertThat(target.getDamagePreventionShield()).isEqualTo(2);

        harness.forceStep(TurnStep.END_STEP);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        assertThat(target.getDamagePreventionShield()).isZero();
    }

    @Test
    @DisplayName("Cannot target a land")
    void cannotTargetLand() {
        Permanent apothecary = addCreatureReady(player1, new MasterApothecary());
        Permanent forest = harness.addToBattlefieldAndReturn(player2, new Forest());

        assertThatThrownBy(() -> harness.activateAbility(player1,
                gd.playerBattlefields.get(player1.getId()).indexOf(apothecary), null, forest.getId()))
                .isInstanceOf(IllegalStateException.class);
    }

    @Test
    @DisplayName("Cannot activate without an untapped Cleric to tap")
    void requiresUntappedCleric() {
        Permanent apothecary = addCreatureReady(player1, new MasterApothecary());
        apothecary.tap();
        Permanent tappedCleric = addCreatureReady(player1, new MasterApothecary());
        tappedCleric.tap();
        addCreatureReady(player1, new AvenFlock());
        addCreatureReady(player2, new MasterApothecary());
        Permanent target = addCreatureReady(player2, new AvenFlock());

        int sourceIndex = gd.playerBattlefields.get(player1.getId()).indexOf(apothecary);
        assertThatThrownBy(() -> harness.activateAbility(player1, sourceIndex, null, target.getId()))
                .isInstanceOf(IllegalStateException.class);
    }
}
