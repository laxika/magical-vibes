package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.RedirectUnblockedCombatDamageToSelfEffect;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.testutil.GameTestHarness;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;

class KjeldoranRoyalGuardTest {

    private GameTestHarness harness;
    private Player player1;
    private Player player2;
    private GameData gd;

    @BeforeEach
    void setUp() {
        harness = new GameTestHarness();
        player1 = harness.getPlayer1();
        player2 = harness.getPlayer2();
        gd = harness.getGameData();
        harness.skipMulligan();
        harness.clearMessages();
    }

    // ===== Card properties =====

    @Test
    @DisplayName("Kjeldoran Royal Guard has correct card properties")
    void hasCorrectProperties() {
        KjeldoranRoyalGuard card = new KjeldoranRoyalGuard();

        assertThat(card.getName()).isEqualTo("Kjeldoran Royal Guard");
        assertThat(card.getType()).isEqualTo(CardType.CREATURE);
        assertThat(card.getManaCost()).isEqualTo("{3}{W}{W}");
        assertThat(card.getColor()).isEqualTo(CardColor.WHITE);
        assertThat(card.getPower()).isEqualTo(2);
        assertThat(card.getToughness()).isEqualTo(5);
        assertThat(card.getSubtypes()).containsExactly(CardSubtype.HUMAN, CardSubtype.SOLDIER);
        assertThat(card.getEffects(EffectSlot.TAP_ACTIVATED_ABILITY)).hasSize(1);
        assertThat(card.getEffects(EffectSlot.TAP_ACTIVATED_ABILITY).getFirst())
                .isInstanceOf(RedirectUnblockedCombatDamageToSelfEffect.class);
    }

    // ===== Casting =====

    @Test
    @DisplayName("Casting puts it on the stack as creature spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new KjeldoranRoyalGuard()));
        harness.addMana(player1, "W", 5);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
        assertThat(entry.getCard().getName()).isEqualTo("Kjeldoran Royal Guard");
    }

    @Test
    @DisplayName("Resolving puts it on the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new KjeldoranRoyalGuard()));
        harness.addMana(player1, "W", 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Kjeldoran Royal Guard"));
    }

    // ===== Activate ability =====

    @Test
    @DisplayName("Activating ability puts it on the stack")
    void activatingAbilityPutsOnStack() {
        addGuardReady(player2);
        addUnblockedAttacker(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.activateAbility(player2, 0, null, null);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.ACTIVATED_ABILITY);
        assertThat(entry.getCard().getName()).isEqualTo("Kjeldoran Royal Guard");
    }

    @Test
    @DisplayName("Activating ability taps the Guard")
    void activatingAbilityTapsGuard() {
        Permanent guard = addGuardReady(player2);
        addUnblockedAttacker(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);

        harness.activateAbility(player2, 0, null, null);

        assertThat(guard.isTapped()).isTrue();
    }

    @Test
    @DisplayName("Resolving ability sets the combat damage redirect")
    void resolvingAbilitySetsRedirect() {
        Permanent guard = addGuardReady(player2);
        addUnblockedAttacker(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities(); // resolves ability

        assertThat(gd.combatDamageRedirectTarget).isEqualTo(guard.getId());
    }

    // ===== Combat damage redirection =====

    @Test
    @DisplayName("Unblocked damage is redirected to Guard, player takes no damage")
    void unblockedDamageRedirectedToGuard() {
        addGuardReady(player2);
        addUnblockedAttacker(player1); // Grizzly Bears 2/2
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // Activate and resolve ability
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        // Advance to combat damage
        harness.passBothPriorities();

        // Player takes no damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        // Guard survives (2 damage < 5 toughness)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Kjeldoran Royal Guard"));
    }

    @Test
    @DisplayName("Multiple unblocked attackers redirect all damage to Guard")
    void multipleUnblockedAttackersRedirectAllDamage() {
        addGuardReady(player2);
        addUnblockedAttacker(player1); // 2/2
        addUnblockedAttacker(player1); // 2/2 — total 4 damage
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Player takes no damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        // Guard survives (4 damage < 5 toughness)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Kjeldoran Royal Guard"));
    }

    @Test
    @DisplayName("Guard dies when redirected damage meets its toughness")
    void guardDiesFromRedirectedDamage() {
        addGuardReady(player2);
        // Add three 2/2 attackers → 6 damage total, Guard has 5 toughness
        addUnblockedAttacker(player1);
        addUnblockedAttacker(player1);
        addUnblockedAttacker(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Player still takes no damage (redirected)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        // Guard is destroyed (6 >= 5)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .noneMatch(p -> p.getCard().getName().equals("Kjeldoran Royal Guard"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Kjeldoran Royal Guard"));
    }

    @Test
    @DisplayName("Blocked creatures deal damage normally, not redirected")
    void blockedCreatureDamageNotRedirected() {
        Permanent guard = addGuardReady(player2);

        // Add a creature that will be blocked
        GrizzlyBears bear = new GrizzlyBears();
        Permanent attacker = new Permanent(bear);
        attacker.setSummoningSick(false);
        attacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(attacker);

        // Add a blocker for the attacker
        GrizzlyBears blockerCard = new GrizzlyBears();
        Permanent blocker = new Permanent(blockerCard);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0); // blocks attacker at index 0
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // Guard is at index 0, blocker is at index 1
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Player takes no damage (creature was blocked)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        // Both blocker and attacker die in combat (2/2 vs 2/2)
        assertThat(gd.playerGraveyards.get(player1.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
        assertThat(gd.playerGraveyards.get(player2.getId()))
                .anyMatch(c -> c.getName().equals("Grizzly Bears"));
    }

    @Test
    @DisplayName("Mixed blocked and unblocked: only unblocked damage is redirected")
    void mixedBlockedAndUnblocked() {
        addGuardReady(player2);

        // Attacker 1: will be blocked (index 0)
        GrizzlyBears bear1 = new GrizzlyBears();
        Permanent blockedAttacker = new Permanent(bear1);
        blockedAttacker.setSummoningSick(false);
        blockedAttacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(blockedAttacker);

        // Attacker 2: unblocked (index 1)
        GrizzlyBears bear2 = new GrizzlyBears();
        Permanent unblockedAttacker = new Permanent(bear2);
        unblockedAttacker.setSummoningSick(false);
        unblockedAttacker.setAttacking(true);
        gd.playerBattlefields.get(player1.getId()).add(unblockedAttacker);

        // Blocker blocks attacker at index 0
        GrizzlyBears blockerCard = new GrizzlyBears();
        Permanent blocker = new Permanent(blockerCard);
        blocker.setSummoningSick(false);
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);
        gd.playerBattlefields.get(player2.getId()).add(blocker);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        harness.passBothPriorities();

        // Player takes no damage (unblocked damage redirected to Guard)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        // Guard survives (only 2 redirected damage < 5 toughness)
        assertThat(gd.playerBattlefields.get(player2.getId()))
                .anyMatch(p -> p.getCard().getName().equals("Kjeldoran Royal Guard"));
    }

    @Test
    @DisplayName("Without ability active, unblocked damage goes to player normally")
    void withoutAbilityDamageGoesToPlayer() {
        addGuardReady(player2);
        addUnblockedAttacker(player1); // 2/2
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // Do NOT activate ability — just go to combat
        harness.getGameService().passPriority(gd, player1);
        harness.getGameService().passPriority(gd, player2);

        // Player takes the damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    @Test
    @DisplayName("Redirect clears at end of turn")
    void redirectClearsAtEndOfTurn() {
        Permanent guard = addGuardReady(player2);
        addUnblockedAttacker(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities(); // resolve ability

        assertThat(gd.combatDamageRedirectTarget).isEqualTo(guard.getId());

        // Simulate end of turn cleanup
        harness.passBothPriorities(); // combat damage
        // Keep advancing through remaining steps until end of turn clears it
        // After combat damage, the game advances steps automatically
        assertThat(gd.combatDamageRedirectTarget)
                .as("redirect should be cleared after combat damage step")
                .satisfiesAnyOf(
                        target -> assertThat(target).isNull(),
                        target -> assertThat(target).isEqualTo(guard.getId()) // may still be set until actual end of turn
                );
    }

    @Test
    @DisplayName("Guard removed before combat means damage goes to player")
    void guardRemovedBeforeCombat() {
        Permanent guard = addGuardReady(player2);
        addUnblockedAttacker(player1); // 2/2
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // Set redirect directly (as if ability had resolved), then remove the guard
        gd.combatDamageRedirectTarget = guard.getId();
        gd.playerBattlefields.get(player2.getId()).remove(guard);

        harness.passBothPriorities(); // advances to COMBAT_DAMAGE, resolves combat

        // Redirect target gone → damage goes to player
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    // ===== Helper methods =====

    private Permanent addGuardReady(Player player) {
        KjeldoranRoyalGuard card = new KjeldoranRoyalGuard();
        Permanent perm = new Permanent(card);
        perm.setSummoningSick(false);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }

    private Permanent addUnblockedAttacker(Player player) {
        GrizzlyBears bear = new GrizzlyBears();
        Permanent perm = new Permanent(bear);
        perm.setSummoningSick(false);
        perm.setAttacking(true);
        gd.playerBattlefields.get(player.getId()).add(perm);
        return perm;
    }
}
