package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.a.AirElemental;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

class SeraphOfTheSwordTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage dealt to Seraph of the Sword is prevented")
    void combatDamageToSeraphIsPrevented() {
        Permanent seraph = addCreatureReady(player1, new SeraphOfTheSword());
        seraph.setBlocking(true);
        seraph.addBlockingTarget(0);

        Permanent attacker = new Permanent(new AirElemental());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Air Elemental's 4 damage would be lethal to a 3/3, but it is prevented.
        harness.assertOnBattlefield(player1, "Seraph of the Sword");
        assertThat(seraph.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Seraph of the Sword still deals its own combat damage")
    void seraphStillDealsCombatDamage() {
        Permanent seraph = addCreatureReady(player1, new SeraphOfTheSword());
        seraph.setBlocking(true);
        seraph.addBlockingTarget(0);

        Permanent attacker = new Permanent(new AirElemental());
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player2.getId()).add(attacker);

        harness.forceActivePlayer(player2);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        harness.passBothPriorities();

        // Unlike Fog Bank, only the damage dealt *to* the Seraph is prevented.
        assertThat(attacker.getMarkedDamage()).isEqualTo(3);
    }

    @Test
    @DisplayName("Noncombat damage to Seraph of the Sword is not prevented")
    void noncombatDamageIsNotPrevented() {
        Permanent seraph = addCreatureReady(player2, new SeraphOfTheSword());
        UUID seraphId = harness.getPermanentId(player2, "Seraph of the Sword");
        harness.setHand(player1, List.of(new Shock()));
        harness.addMana(player1, ManaColor.RED, 1);

        harness.castInstant(player1, 0, seraphId);
        harness.passBothPriorities();

        // Only combat damage is prevented, so Shock's 2 damage is marked normally.
        assertThat(seraph.getMarkedDamage()).isEqualTo(2);
    }
}
