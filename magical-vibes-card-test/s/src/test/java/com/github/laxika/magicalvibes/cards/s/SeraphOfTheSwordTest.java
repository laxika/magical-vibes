package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({SeraphOfTheSword.class, SerraAngel.class, Shock.class})
class SeraphOfTheSwordTest extends BaseCardTest {

    @Test
    @DisplayName("Combat damage dealt to Seraph of the Sword is prevented")
    void combatDamageToSeraphIsPrevented() {
        Permanent seraph = addCreatureReady(player1, new SeraphOfTheSword());
        seraph.setBlocking(true);
        seraph.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new SerraAngel());
        attacker.setAttacking(true);

        resolveCombat(player2);

        // Serra Angel's 4 damage would be lethal to a 3/3, but it is prevented.
        harness.assertOnBattlefield(player1, "Seraph of the Sword");
        assertThat(seraph.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Seraph of the Sword still deals its own combat damage")
    void seraphStillDealsCombatDamage() {
        Permanent seraph = addCreatureReady(player1, new SeraphOfTheSword());
        seraph.setBlocking(true);
        seraph.addBlockingTarget(0);

        Permanent attacker = addCreatureReady(player2, new SerraAngel());
        attacker.setAttacking(true);

        resolveCombat(player2);

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

    @Test
    @DisplayName("Combat damage from a blocker is prevented while Seraph still damages the blocker")
    void combatDamageToAttackingSeraphIsPrevented() {
        Permanent seraph = addCreatureReady(player1, new SeraphOfTheSword());
        Permanent blocker = addCreatureReady(player2, new SerraAngel());

        declareAttackersAndPrepareBlockers(List.of(0));
        gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
        resolveCombat();

        harness.assertOnBattlefield(player1, "Seraph of the Sword");
        assertThat(seraph.getMarkedDamage()).isZero();
        assertThat(blocker.getMarkedDamage()).isEqualTo(3);
        harness.assertLife(player2, 20);
    }

    @Test
    @DisplayName("Lethal noncombat damage destroys Seraph of the Sword")
    void lethalNoncombatDamageIsNotPrevented() {
        Permanent seraph = addCreatureReady(player2, new SeraphOfTheSword());
        harness.setHand(player1, List.of(new Shock(), new Shock()));
        harness.addMana(player1, ManaColor.RED, 2);

        harness.castInstant(player1, 0, seraph.getId());
        harness.passBothPriorities();
        harness.castInstant(player1, 0, seraph.getId());
        harness.passBothPriorities();

        harness.assertNotOnBattlefield(player2, "Seraph of the Sword");
        harness.assertInGraveyard(player2, "Seraph of the Sword");
    }
}
