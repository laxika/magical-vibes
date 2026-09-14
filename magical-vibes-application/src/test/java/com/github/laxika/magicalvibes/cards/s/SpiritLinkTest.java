package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.m.Mountain;
import com.github.laxika.magicalvibes.cards.p.ProdigalSorcerer;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@CardUsed({SpiritLink.class, GrizzlyBears.class, Mountain.class, ProdigalSorcerer.class})
class SpiritLinkTest extends BaseCardTest {

    // ===== Unblocked attacker deals damage to player =====

    @Test
    @DisplayName("Controller gains life when enchanted creature deals combat damage to player")
    void controllerGainsLifeOnCombatDamageToPlayer() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // Grizzly Bears (2/2) with Spirit Link attacks unblocked
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);
        attachSpiritLink(player1, bears);

        resolveCombat();

        harness.passBothPriorities();

        // Player2 takes 2 combat damage: 20 - 2 = 18
        harness.assertLife(player2, 18);
        // Player1 gains 2 life from Spirit Link: 20 + 2 = 22
        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("Life gain equals damage dealt by enchanted creature")
    void lifeGainEqualsDamageDealt() {
        harness.setLife(player1, 10);
        harness.setLife(player2, 20);

        // 4/4 creature with Spirit Link attacks unblocked
        GrizzlyBears bigCreature = new GrizzlyBears();
        bigCreature.setPower(4);
        bigCreature.setToughness(4);
        Permanent attacker = addCreatureReady(player1, bigCreature);
        attacker.setAttacking(true);
        attachSpiritLink(player1, attacker);

        resolveCombat();

        harness.passBothPriorities();

        // Player2 takes 4 damage: 20 - 4 = 16
        harness.assertLife(player2, 16);
        // Player1 gains 4 life: 10 + 4 = 14
        harness.assertLife(player1, 14);
    }

    // ===== Blocked attacker deals damage to blocker =====

    @Test
    @DisplayName("Controller gains life when enchanted creature deals combat damage to blocker")
    void controllerGainsLifeOnCombatDamageToBlocker() {
        harness.setLife(player1, 20);

        // Grizzly Bears (2/2) with Spirit Link attacks, blocked by 2/2
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);
        attachSpiritLink(player1, attacker);

        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.passBothPriorities();
        // Bears dealt 2 damage to blocker → controller gains 2 life: 20 + 2 = 22
        harness.assertLife(player1, 22);
    }

    // ===== Blocker with Spirit Link =====

    @Test
    @DisplayName("Controller gains life when enchanted blocker deals combat damage")
    void controllerGainsLifeFromEnchantedBlocker() {
        harness.setLife(player2, 20);

        // Player1 attacks with 2/2
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        // Player2 blocks with 2/2 that has Spirit Link
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        attachSpiritLink(player2, blocker);

        resolveCombat();

        harness.passBothPriorities();
        // Blocker dealt 2 damage to attacker → player2 gains 2 life: 20 + 2 = 22
        harness.assertLife(player2, 22);
    }

    // ===== Spirit Link on opponent's creature =====

    @Test
    @DisplayName("Aura controller gains life, not creature controller")
    void auraControllerGainsLifeNotCreatureController() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        // Player1 attacks with Grizzly Bears (2/2)
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        // Player2 enchants player1's creature with Spirit Link
        attachSpiritLink(player2, attacker);

        // Player2 blocks with a 2/2 — attacker deals damage to blocker, not to player
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        resolveCombat();

        harness.passBothPriorities();
        // Attacker dealt 2 to blocker → player2 (aura controller) gains 2 life: 20 + 2 = 22
        harness.assertLife(player2, 22);
        // Player1 does NOT gain life (not the aura controller)
        harness.assertLife(player1, 20);
    }

    // ===== No damage, no life gain =====

    @Test
    @DisplayName("No life gained when enchanted creature does not deal damage")
    void noLifeGainWhenNoDamageDealt() {
        harness.setLife(player1, 20);

        // Creature with Spirit Link does not attack
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        attachSpiritLink(player1, bears);

        // Another creature attacks unblocked
        GrizzlyBears otherBear = new GrizzlyBears();
        Permanent otherAttacker = addCreatureReady(player1, otherBear);
        otherAttacker.setAttacking(true);

        resolveCombat();

        // Player1 gains no life — enchanted creature didn't deal damage
        harness.assertLife(player1, 20);
    }

    @Test
    void noncombatDamageWaitsForTriggeredLifeGain() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent pinger = addCreatureReady(player1, new ProdigalSorcerer());
        attachSpiritLink(player1, pinger);

        harness.activateAbility(player1, 0, null, player2.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 19);
        harness.assertLife(player1, 20);

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    @DisplayName("Controller gains life when enchanted creature deals noncombat damage to a creature")
    void controllerGainsLifeOnNoncombatDamageToCreature() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent pinger = addCreatureReady(player1, new ProdigalSorcerer());
        attachSpiritLink(player1, pinger);
        Permanent target = addCreatureReady(player2, new GrizzlyBears());

        harness.activateAbility(player1, 0, null, target.getId());
        harness.passBothPriorities();

        harness.assertLife(player2, 20);
        harness.assertLife(player1, 20);

        harness.passBothPriorities();

        harness.assertLife(player1, 21);
    }

    @Test
    void combatDamageWaitsForTriggeredLifeGain() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);
        attachSpiritLink(player1, bears);

        harness.resolveCombatDamage();

        harness.assertLife(player2, 18);
        harness.assertLife(player1, 20);

        harness.passBothPriorities();

        harness.assertLife(player1, 22);
    }

    @Test
    @DisplayName("A queued life-gain trigger resolves after Spirit Link leaves the battlefield")
    void triggeredLifeGainResolvesAfterSpiritLinkLeavesBattlefield() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);
        Permanent aura = attachSpiritLink(player1, bears);

        harness.resolveCombatDamage();
        gd.playerBattlefields.get(player1.getId()).remove(aura);

        harness.assertLife(player1, 20);

        harness.passBothPriorities();

        harness.assertLife(player1, 22);
    }

    // ===== Logs =====

    @Test
    @DisplayName("Spirit Link life gain is logged")
    void spiritLinkLifeGainIsLogged() {
        harness.setLife(player1, 20);
        harness.setLife(player2, 20);

        Permanent bears = addCreatureReady(player1, new GrizzlyBears());
        bears.setAttacking(true);
        attachSpiritLink(player1, bears);

        resolveCombat();

        harness.passBothPriorities();
        assertThat(gameLogContains("Alice gains 2 life.")).isTrue();
    }

    // ===== Targeting restriction =====

    @Test
    @DisplayName("Resolving Spirit Link attaches it to target creature")
    void resolvingAttachesToTargetCreature() {
        Permanent bears = addCreatureReady(player1, new GrizzlyBears());

        harness.setHand(player1, List.of(new SpiritLink()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        harness.castEnchantment(player1, 0, bears.getId());
        harness.passBothPriorities();

        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard() instanceof SpiritLink
                        && p.isAttached()
                        && p.getAttachedTo().equals(bears.getId()));
    }

    @Test
    @DisplayName("Cannot enchant a land")
    void cannotEnchantALand() {
        // A creature must exist so the spell is playable; targeting the land is then rejected.
        harness.addToBattlefield(player2, new GrizzlyBears());
        Permanent mountain = harness.addToBattlefieldAndReturn(player1, new Mountain());
        harness.setHand(player1, List.of(new SpiritLink()));
        harness.addMana(player1, ManaColor.WHITE, 2);

        assertThatThrownBy(() -> harness.castEnchantment(player1, 0, mountain.getId()))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("Target must be a creature");
    }

    // ===== Helpers =====

    private Permanent attachSpiritLink(Player controller, Permanent target) {
        Permanent aura = harness.addToBattlefieldAndReturn(controller, new SpiritLink());
        aura.setAttachedTo(target.getId());
        return aura;
    }
}
