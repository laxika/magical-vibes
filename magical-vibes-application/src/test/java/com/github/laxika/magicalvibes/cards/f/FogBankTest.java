package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.cards.g.GiantGrowth;
import com.github.laxika.magicalvibes.cards.s.Shock;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class FogBankTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage dealt to Fog Bank is prevented")
    void combatDamageToFogBankIsPrevented() {
        Permanent fogBank = addCreatureReady(player1, new FogBank());
        fogBank.setBlocking(true);
        fogBank.addBlockingTarget(0);

        Permanent attacker = new Permanent(new AirElemental());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Air Elemental's 4 damage would be lethal to a 0/2, but it is prevented.
        harness.assertOnBattlefield(player1, "Fog Bank");
        assertThat(fogBank.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Combat damage dealt by Fog Bank is prevented")
    void combatDamageByFogBankIsPrevented() {
        Permanent fogBank = addCreatureReady(player1, new FogBank());
        UUID fogBankId = harness.getPermanentId(player1, "Fog Bank");
        harness.setHand(player1, List.of(new GiantGrowth()));
        harness.addMana(player1, ManaColor.GREEN, 1);
        harness.castInstant(player1, 0, fogBankId);
        harness.passBothPriorities();

        fogBank.setBlocking(true);
        fogBank.addBlockingTarget(0);

        Permanent attacker = new Permanent(new AirElemental());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Pumped to 3/5, Fog Bank would deal 3 combat damage — all of it is prevented.
        assertThat(attacker.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Noncombat damage to Fog Bank is not prevented")
    void noncombatDamageIsNotPrevented() {
        Permanent fogBank = addCreatureReady(player2, new FogBank());
        UUID fogBankId = harness.getPermanentId(player2, "Fog Bank");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, fogBankId);
        harness.passBothPriorities();

        // Only combat damage is prevented, so Shock's 2 damage is marked normally.
        assertThat(fogBank.getMarkedDamage()).isEqualTo(2);
    }
}
