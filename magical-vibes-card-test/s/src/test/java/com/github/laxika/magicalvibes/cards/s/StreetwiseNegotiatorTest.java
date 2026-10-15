package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.g.GoblinPiker;
import com.github.laxika.magicalvibes.cards.k.KarsusDepthguard;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({StreetwiseNegotiator.class, GoblinPiker.class, KarsusDepthguard.class, StasisField.class})
class StreetwiseNegotiatorTest extends BaseCardTest {

    @Test
    @DisplayName("Streetwise Negotiator assigns combat damage equal to its toughness")
    void assignsCombatDamageUsingItsToughness() {
        Permanent negotiator = harness.addToBattlefieldAndReturn(player1, new StreetwiseNegotiator());
        Permanent piker = harness.addToBattlefieldAndReturn(player1, new GoblinPiker());

        assertThat(gqs.getEffectiveCombatDamage(gd, negotiator)).isEqualTo(2);
        assertThat(gqs.getEffectiveCombatDamage(gd, piker)).isEqualTo(2);
    }

    @Test
    @DisplayName("Backup grants another creature toughness-based combat damage until end of turn")
    void backupGrantsToughnessBasedCombatDamage() {
        Permanent piker = harness.addToBattlefieldAndReturn(player1, new GoblinPiker());
        castStreetwiseNegotiatorTargeting(piker);

        assertThat(piker.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectiveCombatDamage(gd, piker)).isEqualTo(2);

        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);

        assertThat(gqs.getEffectiveCombatDamage(gd, piker)).isEqualTo(3);
    }

    private void castStreetwiseNegotiatorTargeting(Permanent target) {
        harness.setHand(player1, List.of(new StreetwiseNegotiator()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        harness.handlePermanentChosen(player1, target.getId());
        harness.passBothPriorities();
    }

    @Test
    void backupCanTargetItselfAndItsPrintedAbilityPersists() {
        harness.setHand(player1, List.of(new StreetwiseNegotiator()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castCreature(player1, 0);
        harness.passBothPriorities();
        Permanent negotiator = gd.playerBattlefields.get(player1.getId()).getFirst();
        harness.handlePermanentChosen(player1, negotiator.getId());
        harness.passBothPriorities();

        assertThat(negotiator.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectiveCombatDamage(gd, negotiator)).isEqualTo(3);
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        assertThat(gqs.getEffectiveCombatDamage(gd, negotiator)).isEqualTo(3);
        assertThat(gqs.getPowerBasedDamage(gd, negotiator)).isEqualTo(1);
    }

    @Test
    void backupCanGrantAbilityToOpponentsCreature() {
        Permanent depthguard = harness.addToBattlefieldAndReturn(player2, new KarsusDepthguard());
        castStreetwiseNegotiatorTargeting(depthguard);

        assertThat(depthguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectiveCombatDamage(gd, depthguard)).isEqualTo(4);
        assertThat(gqs.getPowerBasedDamage(gd, depthguard)).isEqualTo(5);
        harness.passUntilWithNoAttackers(player2, TurnStep.UPKEEP);
        assertThat(gqs.getEffectiveCombatDamage(gd, depthguard)).isEqualTo(5);
        assertThat(depthguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
    }

    @Test
    void unblockedCombatUsesModifiedToughnessWithoutSubtractingMarkedDamage() {
        Permanent negotiator = harness.addToBattlefieldAndReturn(player1, new StreetwiseNegotiator());
        negotiator.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, 1);
        negotiator.setMarkedDamage(1);
        negotiator.setSummoningSick(false);
        negotiator.setAttacking(true);
        harness.setLife(player2, 20);
        harness.forceStep(TurnStep.COMBAT_DAMAGE);
        harness.resolveCombatDamage();

        harness.assertLife(player2, 17);
    }

    @Test
    void losingPrintedAbilityMakesNegotiatorAssignDamageUsingPower() {
        Permanent negotiator = harness.addToBattlefieldAndReturn(player1, new StreetwiseNegotiator());
        castStasisFieldTargeting(negotiator);

        assertThat(gqs.getEffectiveCombatDamage(gd, negotiator)).isZero();
    }

    @Test
    void losingGrantedAbilityMakesBackupRecipientAssignDamageUsingPower() {
        Permanent depthguard = harness.addToBattlefieldAndReturn(player1, new KarsusDepthguard());
        castStreetwiseNegotiatorTargeting(depthguard);
        castStasisFieldTargeting(depthguard);

        assertThat(depthguard.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE)).isEqualTo(1);
        assertThat(gqs.getEffectiveCombatDamage(gd, depthguard)).isEqualTo(1);
    }

    private void castStasisFieldTargeting(Permanent target) {
        harness.setHand(player1, List.of(new StasisField()));
        harness.addMana(player1, ManaColor.BLUE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castEnchantment(player1, 0, target.getId());
        harness.passBothPriorities();
    }
}
