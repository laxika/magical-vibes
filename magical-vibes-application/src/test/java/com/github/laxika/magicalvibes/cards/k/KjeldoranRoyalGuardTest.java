package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.s.ScrybSprites;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.service.turn.TurnCleanupService;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import com.github.laxika.magicalvibes.testutil.CardUsed;
import com.github.laxika.magicalvibes.testutil.GameTestEngineContext;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;

import static org.assertj.core.api.Assertions.assertThat;

@CardUsed({KjeldoranRoyalGuard.class, GrizzlyBears.class, ScrybSprites.class})
class KjeldoranRoyalGuardTest extends BaseCardTest {

    @Test
    @DisplayName("Casting puts it on the stack as creature spell")
    void castingPutsOnStack() {
        harness.setHand(player1, List.of(new KjeldoranRoyalGuard()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);

        assertThat(gd.stack).hasSize(1);
        StackEntry entry = gd.stack.getFirst();
        assertThat(entry.getEntryType()).isEqualTo(StackEntryType.CREATURE_SPELL);
    }

    @Test
    @DisplayName("Resolving puts it on the battlefield")
    void resolvingPutsOnBattlefield() {
        harness.setHand(player1, List.of(new KjeldoranRoyalGuard()));
        harness.addMana(player1, ManaColor.WHITE, 5);

        harness.castCreature(player1, 0);
        harness.passBothPriorities();

        assertThat(gd.stack).isEmpty();
        assertThat(gd.playerBattlefields.get(player1.getId()))
                .anyMatch(permanent -> permanent.getCard() instanceof KjeldoranRoyalGuard);
    }

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

    @Test
    @DisplayName("Unblocked damage is redirected to Guard, player takes no damage")
    void unblockedDamageRedirectedToGuard() {
        Permanent guard = addGuardReady(player2);
        addUnblockedAttacker(player1); // Grizzly Bears 2/2
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // Activate and resolve ability
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();

        // Advance to combat damage
        resolveCombat();

        // Player takes no damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        // Guard survives (2 damage < 5 toughness)
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(guard);
    }

    @Test
    @DisplayName("Multiple unblocked attackers redirect all damage to Guard")
    void multipleUnblockedAttackersRedirectAllDamage() {
        Permanent guard = addGuardReady(player2);
        addUnblockedAttacker(player1); // 2/2
        addUnblockedAttacker(player1); // 2/2 — total 4 damage
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        resolveCombat();

        // Player takes no damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        // Guard survives (4 damage < 5 toughness)
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(guard);
    }

    @Test
    @DisplayName("Guard dies when redirected damage meets its toughness")
    void guardDiesFromRedirectedDamage() {
        Permanent guard = addGuardReady(player2);
        // Add three 2/2 attackers → 6 damage total, Guard has 5 toughness
        addUnblockedAttacker(player1);
        addUnblockedAttacker(player1);
        addUnblockedAttacker(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        resolveCombat();

        // Player still takes no damage (redirected)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        // Guard is destroyed (6 >= 5)
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(guard);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(guard.getCard());
    }

    @Test
    @DisplayName("Redirected first-strike damage persists as marked damage into the regular damage step")
    void redirectedFirstStrikeDamagePersistsAcrossSteps() {
        Permanent guard = addGuardReady(player2); // 2/5
        addUnblockedAttacker(player1, Keyword.FIRST_STRIKE); // 2/2 first strike
        addUnblockedAttacker(player1); // 2/2
        addUnblockedAttacker(player1); // 2/2
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        resolveCombat();

        // Player takes no damage; the 2 first-strike damage stays marked on the Guard
        // (CR 120.6), so the regular step's 4 more redirected damage is lethal (2 + 4 >= 5).
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(guard.getCard());
    }

    @Test
    @DisplayName("Guard dies when redirected damage plus earlier marked damage is lethal")
    void guardDiesFromRedirectedDamagePlusEarlierDamage() {
        Permanent guard = addGuardReady(player2);
        // 4 damage marked earlier this turn (e.g. a burn spell); Guard is 2/5
        guard.setMarkedDamage(4);
        addUnblockedAttacker(player1); // 2/2 — 2 redirected damage
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        resolveCombat();

        // Player takes no damage, and 4 + 2 >= 5 destroys the Guard (CR 704.5g)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(guard);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(guard.getCard());
    }

    @Test
    @DisplayName("Redirected damage from a deathtouch attacker destroys the Guard")
    void deathtouchRedirectedDamageDestroysGuard() {
        Permanent guard = addGuardReady(player2);
        addUnblockedAttacker(player1, Keyword.DEATHTOUCH); // 2/2 deathtouch
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        resolveCombat();

        // 2 redirected deathtouch damage < 5 toughness, but deathtouch makes it lethal (CR 702.2b)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(gd.playerBattlefields.get(player2.getId())).doesNotContain(guard);
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(guard.getCard());
    }

    @Test
    @DisplayName("Redirected combat damage still grants lifelink")
    void redirectedDamageStillGrantsLifelink() {
        Permanent guard = addGuardReady(player2);
        addUnblockedAttacker(player1, Keyword.LIFELINK);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        resolveCombat();

        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(22);
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        assertThat(guard.getMarkedDamage()).isEqualTo(2);
    }

    @Test
    @DisplayName("Does not redirect combat damage dealt to the Guard controller's opponent")
    void doesNotRedirectDamageDealtToOpponent() {
        Permanent guard = addGuardReady(player1);
        addUnblockedAttacker(player1);
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.activateAbility(player1, 0, null, null);
        harness.passBothPriorities();
        resolveCombat(player1);

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerLifeTotals.get(player1.getId())).isEqualTo(20);
        assertThat(guard.getMarkedDamage()).isZero();
    }

    @Test
    @DisplayName("Blocked creatures deal damage normally, not redirected")
    void blockedCreatureDamageNotRedirected() {
        Permanent guard = addGuardReady(player2);

        // Add a creature that will be blocked
        Permanent attacker = addCreatureReady(player1, new GrizzlyBears());
        attacker.setAttacking(true);

        // Add a blocker for the attacker
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0); // blocks attacker at index 0

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        // Guard is at index 0, blocker is at index 1
        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        resolveCombat();

        // Player takes no damage (creature was blocked)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        // Both blocker and attacker die in combat (2/2 vs 2/2)
        assertThat(gd.playerGraveyards.get(player1.getId())).contains(attacker.getCard());
        assertThat(gd.playerGraveyards.get(player2.getId())).contains(blocker.getCard());
    }

    @Test
    @DisplayName("Mixed blocked and unblocked: only unblocked damage is redirected")
    void mixedBlockedAndUnblocked() {
        Permanent guard = addGuardReady(player2);

        // Attacker 1: will be blocked (index 0)
        Permanent blockedAttacker = addCreatureReady(player1, new GrizzlyBears());
        blockedAttacker.setAttacking(true);

        // Attacker 2: unblocked (index 1)
        Permanent unblockedAttacker = addCreatureReady(player1, new GrizzlyBears());
        unblockedAttacker.setAttacking(true);

        // Blocker blocks attacker at index 0
        Permanent blocker = addCreatureReady(player2, new GrizzlyBears());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        resolveCombat();

        // Player takes no damage (unblocked damage redirected to Guard)
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        // Guard survives (only 2 redirected damage < 5 toughness)
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(guard);
    }

    @Test
    @DisplayName("Trample damage from a blocked creature is not redirected")
    void trampleDamageFromBlockedCreatureIsNotRedirected() {
        addGuardReady(player2);

        GrizzlyBears attackerCard = new GrizzlyBears();
        attackerCard.setKeywords(Set.of(Keyword.TRAMPLE));
        Permanent attacker = addCreatureReady(player1, attackerCard);
        attacker.setAttacking(true);

        Permanent blocker = addCreatureReady(player2, new ScrybSprites());
        blocker.setBlocking(true);
        blocker.addBlockingTarget(0);

        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();

        harness.activateAbility(player2, 0, null, null);
        harness.passBothPriorities();
        resolveCombat();
        harness.handleCombatDamageAssigned(player1, 0, Map.of(
                blocker.getId(), 1,
                player2.getId(), 1
        ));

        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(19);
    }

    @Test
    @DisplayName("Without ability active, unblocked damage goes to player normally")
    void withoutAbilityDamageGoesToPlayer() {
        Permanent guard = addGuardReady(player2);
        addUnblockedAttacker(player1); // 2/2

        // Do NOT activate ability — just pass player1's priority;
        // auto-pass handles player2 and cascades through COMBAT_DAMAGE
        resolveCombat();

        // Player takes the damage
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        assertThat(gd.playerBattlefields.get(player2.getId())).contains(guard);
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
        resolveCombat();
        harness.inMutationScope(() -> GameTestEngineContext.get().getBean(TurnCleanupService.class)
                .applyCleanupResets(gd));

        assertThat(gd.combatDamageRedirectTarget).isNull();
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

        resolveCombat();

        // Redirect target gone → damage goes to player
        assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
    }

    private Permanent addGuardReady(Player player) {
        return addCreatureReady(player, new KjeldoranRoyalGuard());
    }

    private Permanent addUnblockedAttacker(Player player, Keyword... keywords) {
        GrizzlyBears bear = new GrizzlyBears();
        if (keywords.length > 0) {
            bear.setKeywords(Set.of(keywords)); // before wrapping — cards freeze once on a Permanent
        }
        Permanent perm = addCreatureReady(player, bear);
        perm.setAttacking(true);
        return perm;
    }
}

