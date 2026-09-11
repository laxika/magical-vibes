package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.g.GoblinRaider;
import com.github.laxika.magicalvibes.cards.h.HeatRay;
import com.github.laxika.magicalvibes.cards.h.Humble;
import com.github.laxika.magicalvibes.cards.t.TitaniasBoon;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({FogBank.class, GoblinRaider.class, TitaniasBoon.class, HeatRay.class, Humble.class})
class FogBankTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage dealt to Fog Bank is prevented")
    void combatDamageToFogBankIsPrevented() {
        Permanent fogBank = addCreatureReady(player1, new FogBank());
        fogBank.setBlocking(true);
        fogBank.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new GoblinRaider());
        attacker.setAttacking(true);

        resolveCombat(player2);

        // Goblin Raider's 2 damage would be lethal to a 0/2, but it is prevented.
        harness.assertOnBattlefield(player1, "Fog Bank");
        assertThat(fogBank.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Combat damage dealt by Fog Bank is prevented")
    void combatDamageByFogBankIsPrevented() {
        Permanent fogBank = addCreatureReady(player1, new FogBank());
        harness.castFromHand(player1, new TitaniasBoon(), "{3}{G}");
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, fogBank)).isEqualTo(1);

        fogBank.setBlocking(true);
        fogBank.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new GoblinRaider());
        attacker.setAttacking(true);

        resolveCombat(player2);

        // Pumped to 1/3, Fog Bank would deal 1 combat damage — all of it is prevented.
        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Does not prevent combat damage after losing all abilities")
    void preventionAbilityIsRemovedByLosingAllAbilities() {
        Permanent fogBank = addCreatureReady(player1, new FogBank());
        harness.setHand(player1, List.of(new Humble()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, fogBank.getId());

        fogBank.setBlocking(true);
        fogBank.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new GoblinRaider());
        attacker.setAttacking(true);

        resolveCombat(player2);

        assertThat(fogBank.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not prevent its combat damage after losing all abilities")
    void damagePreventionAbilityIsRemovedFromDamageDealingPath() {
        Permanent fogBank = addCreatureReady(player1, new FogBank());
        harness.setHand(player1, List.of(new Humble()));
        harness.addMana(player1, ManaColor.WHITE, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);
        harness.castAndResolveInstant(player1, 0, fogBank.getId());

        harness.castFromHand(player1, new TitaniasBoon(), "{3}{G}");
        harness.passBothPriorities();
        assertThat(gqs.getEffectivePower(gd, fogBank)).isEqualTo(1);

        fogBank.setBlocking(true);
        fogBank.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new GoblinRaider());
        attacker.setAttacking(true);

        resolveCombat(player2);

        assertThat(attacker.getMarkedDamage()).isEqualTo(1);
    }

    @Test
    @DisplayName("Noncombat damage to Fog Bank is not prevented")
    void noncombatDamageIsNotPrevented() {
        Permanent fogBank = addCreatureReady(player2, new FogBank());
        UUID fogBankId = fogBank.getId();
        harness.setHand(player1, List.of(new HeatRay()));
        harness.addMana(player1, ManaColor.RED, 1);
        harness.addMana(player1, ManaColor.COLORLESS, 1);

        harness.castInstant(player1, 0, 1, fogBankId);
        harness.passBothPriorities();

        // Only combat damage is prevented, so Heat Ray's 1 damage is marked normally.
        assertThat(fogBank.getMarkedDamage()).isEqualTo(1);
    }
}
