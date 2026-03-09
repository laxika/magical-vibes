package com.github.laxika.magicalvibes.service.combat;

import com.github.laxika.magicalvibes.cards.a.AngelicWall;
import com.github.laxika.magicalvibes.cards.b.BlightMamba;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GoblinSkyRaider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.r.RagingGoblin;
import com.github.laxika.magicalvibes.cards.s.SerraAngel;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CombatServiceTest extends BaseCardTest {

    // ===== Helper methods =====

    private Permanent addReadyCreature(Player player, Card card) {
        harness.addToBattlefield(player, card);
        List<Permanent> bf = gd.playerBattlefields.get(player.getId());
        Permanent p = bf.getLast();
        p.setSummoningSick(false);
        return p;
    }

    private void setupAttackerDeclaration() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_ATTACKERS);
        harness.clearPriorityPassed();
        gd.interaction.beginAttackerDeclaration(player1.getId());
    }

    private void setupBlockerDeclaration() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
        gd.interaction.beginBlockerDeclaration(player2.getId());
    }

    /**
     * Sets up combat state for combat damage resolution.
     * Callers should manually set attacking/blocking state on permanents before calling this,
     * then call {@code harness.passBothPriorities()} to advance from DECLARE_BLOCKERS through
     * COMBAT_DAMAGE and resolve damage.
     */
    private void setupCombatDamageResolution() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }

    // ===== Attacker Declaration Tests =====

    @Nested
    @DisplayName("Declare Attackers")
    class DeclareAttackersTest {

        @Test
        @DisplayName("Ready creature can be declared as attacker")
        void readyCreatureCanAttack() {
            addReadyCreature(player1, new GrizzlyBears());
            addReadyCreature(player2, new GrizzlyBears());
            setupAttackerDeclaration();

            gs.declareAttackers(gd, player1, List.of(0));

            Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
            assertThat(attacker.isAttacking()).isTrue();
        }

        @Test
        @DisplayName("Declaring attackers taps them")
        void attackersTapWhenDeclared() {
            addReadyCreature(player1, new GrizzlyBears());
            addReadyCreature(player2, new GrizzlyBears());
            setupAttackerDeclaration();

            gs.declareAttackers(gd, player1, List.of(0));

            Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
            assertThat(attacker.isTapped()).isTrue();
        }

        @Test
        @DisplayName("Vigilance creature does not tap when attacking")
        void vigilanceCreatureDoesNotTap() {
            addReadyCreature(player1, new SerraAngel());
            addReadyCreature(player2, new GrizzlyBears());
            setupAttackerDeclaration();

            gs.declareAttackers(gd, player1, List.of(0));

            Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
            assertThat(attacker.isAttacking()).isTrue();
            assertThat(attacker.isTapped()).isFalse();
        }

        @Test
        @DisplayName("Summoning sick creature cannot be declared as attacker")
        void summoningSickCreatureCannotAttack() {
            harness.addToBattlefield(player1, new GrizzlyBears());
            addReadyCreature(player2, new GrizzlyBears());
            setupAttackerDeclaration();

            assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Invalid attacker index");
        }

        @Test
        @DisplayName("Haste creature can attack while summoning sick")
        void hasteCreatureCanAttackWhileSummoningSick() {
            harness.addToBattlefield(player1, new RagingGoblin());
            addReadyCreature(player2, new GrizzlyBears());
            setupAttackerDeclaration();

            gs.declareAttackers(gd, player1, List.of(0));

            Permanent attacker = gd.playerBattlefields.get(player1.getId()).getFirst();
            assertThat(attacker.isAttacking()).isTrue();
        }

        @Test
        @DisplayName("Tapped creature cannot be declared as attacker")
        void tappedCreatureCannotAttack() {
            Permanent creature = addReadyCreature(player1, new GrizzlyBears());
            creature.tap();
            addReadyCreature(player2, new GrizzlyBears());
            setupAttackerDeclaration();

            assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Invalid attacker index");
        }

        @Test
        @DisplayName("Defender creature cannot be declared as attacker")
        void defenderCreatureCannotAttack() {
            addReadyCreature(player1, new AngelicWall());
            addReadyCreature(player2, new GrizzlyBears());
            setupAttackerDeclaration();

            assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Invalid attacker index");
        }

        @Test
        @DisplayName("Declaring no attackers is valid")
        void declaringNoAttackersIsValid() {
            addReadyCreature(player1, new GrizzlyBears());
            setupAttackerDeclaration();

            gs.declareAttackers(gd, player1, List.of());

            Permanent creature = gd.playerBattlefields.get(player1.getId()).getFirst();
            assertThat(creature.isAttacking()).isFalse();
            assertThat(creature.isTapped()).isFalse();
        }

        @Test
        @DisplayName("Duplicate attacker indices are rejected")
        void duplicateAttackerIndicesRejected() {
            addReadyCreature(player1, new GrizzlyBears());
            addReadyCreature(player2, new GrizzlyBears());
            setupAttackerDeclaration();

            assertThatThrownBy(() -> gs.declareAttackers(gd, player1, List.of(0, 0)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Duplicate attacker indices");
        }

        @Test
        @DisplayName("Only active player can declare attackers")
        void onlyActivePlayerCanDeclareAttackers() {
            addReadyCreature(player2, new GrizzlyBears());
            setupAttackerDeclaration();

            assertThatThrownBy(() -> gs.declareAttackers(gd, player2, List.of(0)))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Only the active player");
        }

        @Test
        @DisplayName("Multiple creatures can attack simultaneously")
        void multipleCreaturesCanAttack() {
            addReadyCreature(player1, new GrizzlyBears());
            addReadyCreature(player1, new LlanowarElves());
            addReadyCreature(player2, new GrizzlyBears());
            setupAttackerDeclaration();

            gs.declareAttackers(gd, player1, List.of(0, 1));

            List<Permanent> bf = gd.playerBattlefields.get(player1.getId());
            assertThat(bf.get(0).isAttacking()).isTrue();
            assertThat(bf.get(1).isAttacking()).isTrue();
        }
    }

    // ===== Blocker Declaration Tests =====

    @Nested
    @DisplayName("Declare Blockers")
    class DeclareBlockersTest {

        @Test
        @DisplayName("Valid blocking assignment results in combat trade")
        void validBlockingResultsInCombat() {
            harness.setLife(player2, 20);
            Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
            attacker.setAttacking(true);
            addReadyCreature(player2, new GrizzlyBears());
            setupBlockerDeclaration();

            gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
            harness.passBothPriorities();

            // Both 2/2 creatures traded in combat
            harness.assertInGraveyard(player1, "Grizzly Bears");
            harness.assertInGraveyard(player2, "Grizzly Bears");
            // Blocked creature dealt no damage to player
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        }

        @Test
        @DisplayName("Flying creature cannot be blocked by ground creature")
        void flyingCannotBeBlockedByGround() {
            Permanent attacker = addReadyCreature(player1, new GoblinSkyRaider());
            attacker.setAttacking(true);
            addReadyCreature(player2, new GrizzlyBears());
            setupBlockerDeclaration();

            assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("flying");
        }

        @Test
        @DisplayName("Reach creature can block flying creature")
        void reachCreatureCanBlockFlying() {
            harness.setLife(player2, 20);
            // Goblin Sky Raider (1/2 Flying) vs Giant Spider (2/4 Reach)
            Permanent attacker = addReadyCreature(player1, new GoblinSkyRaider());
            attacker.setAttacking(true);
            addReadyCreature(player2, new GiantSpider());
            setupBlockerDeclaration();

            // Should not throw — reach can block flying
            gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));
            harness.passBothPriorities();

            // Goblin Sky Raider dies (1/2 takes 2 damage), Giant Spider survives (2/4 takes 1 damage)
            harness.assertNotOnBattlefield(player1, "Goblin Sky Raider");
            harness.assertOnBattlefield(player2, "Giant Spider");
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        }

        @Test
        @DisplayName("Flying creature can block another flying creature")
        void flyingCanBlockFlying() {
            harness.setLife(player2, 20);
            // Both Goblin Sky Raider (1/2 Flying) — neither kills the other (1 power < 2 toughness)
            Permanent attacker = addReadyCreature(player1, new GoblinSkyRaider());
            attacker.setAttacking(true);
            addReadyCreature(player2, new GoblinSkyRaider());
            setupBlockerDeclaration();

            // Should not throw — flying can block flying
            gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0)));

            // Both survive (1 power < 2 toughness), no damage to player
            harness.assertOnBattlefield(player1, "Goblin Sky Raider");
            harness.assertOnBattlefield(player2, "Goblin Sky Raider");
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        }

        @Test
        @DisplayName("Tapped creature cannot block")
        void tappedCreatureCannotBlock() {
            Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
            attacker.setAttacking(true);
            Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
            blocker.tap();
            setupBlockerDeclaration();

            assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(new BlockerAssignment(0, 0))))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Invalid blocker index");
        }

        @Test
        @DisplayName("Only defending player can declare blockers")
        void onlyDefendingPlayerCanDeclareBlockers() {
            Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
            attacker.setAttacking(true);
            addReadyCreature(player2, new GrizzlyBears());
            setupBlockerDeclaration();

            assertThatThrownBy(() -> gs.declareBlockers(gd, player1, List.of(new BlockerAssignment(0, 0))))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Only the defending player");
        }

        @Test
        @DisplayName("Empty blocker assignments is valid")
        void emptyBlockerAssignmentsIsValid() {
            Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
            attacker.setAttacking(true);
            addReadyCreature(player2, new GrizzlyBears());
            setupBlockerDeclaration();

            gs.declareBlockers(gd, player2, List.of());

            Permanent defender = gd.playerBattlefields.get(player2.getId()).getFirst();
            assertThat(defender.isBlocking()).isFalse();
        }

        @Test
        @DisplayName("Same creature cannot block more than one attacker by default")
        void blockerAssignedTooManyTimesRejected() {
            Permanent attacker1 = addReadyCreature(player1, new GrizzlyBears());
            attacker1.setAttacking(true);
            Permanent attacker2 = addReadyCreature(player1, new LlanowarElves());
            attacker2.setAttacking(true);
            addReadyCreature(player2, new GrizzlyBears());
            setupBlockerDeclaration();

            assertThatThrownBy(() -> gs.declareBlockers(gd, player2, List.of(
                    new BlockerAssignment(0, 0),
                    new BlockerAssignment(0, 1)
            )))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("assigned too many times");
        }
    }

    // ===== Combat Damage Resolution Tests =====

    @Nested
    @DisplayName("Combat Damage")
    class CombatDamageTest {

        @Test
        @DisplayName("Unblocked creature deals damage to defending player")
        void unblockedCreatureDealsDamageToPlayer() {
            harness.setLife(player2, 20);
            Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
            attacker.setAttacking(true);
            setupCombatDamageResolution();

            harness.passBothPriorities();

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(18);
        }

        @Test
        @DisplayName("Blocked creatures trade when both have lethal power")
        void blockedCreaturesTradeWhenBothDie() {
            harness.setLife(player2, 20);
            Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
            attacker.setAttacking(true);
            Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
            blocker.setBlocking(true);
            blocker.addBlockingTarget(0);
            setupCombatDamageResolution();

            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player1, "Grizzly Bears");
            harness.assertNotOnBattlefield(player2, "Grizzly Bears");
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        }

        @Test
        @DisplayName("Larger creature survives combat with smaller creature")
        void largerCreatureSurvivesCombat() {
            harness.setLife(player2, 20);
            Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
            attacker.setAttacking(true);
            Permanent blocker = addReadyCreature(player2, new LlanowarElves());
            blocker.setBlocking(true);
            blocker.addBlockingTarget(0);
            setupCombatDamageResolution();

            harness.passBothPriorities();

            harness.assertOnBattlefield(player1, "Grizzly Bears");
            harness.assertNotOnBattlefield(player2, "Llanowar Elves");
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        }

        @Test
        @DisplayName("Blocked creature does not deal damage to player even if blocker dies")
        void blockedCreatureDoesNotDamagePlayer() {
            harness.setLife(player2, 20);
            // Avatar of Might (8/8 trample) blocked by Llanowar Elves (1/1)
            // Because Avatar has trample, excess goes to player — let's use a non-trample creature
            Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
            attacker.setAttacking(true);
            Permanent blocker = addReadyCreature(player2, new LlanowarElves());
            blocker.setBlocking(true);
            blocker.addBlockingTarget(0);
            setupCombatDamageResolution();

            harness.passBothPriorities();

            // Blocker dies but attacker's excess damage does NOT go to player (no trample)
            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        }

        @Test
        @DisplayName("Multiple unblocked attackers each deal damage to defending player")
        void multipleUnblockedAttackersDealDamage() {
            harness.setLife(player2, 20);
            Permanent attacker1 = addReadyCreature(player1, new GrizzlyBears());
            attacker1.setAttacking(true);
            Permanent attacker2 = addReadyCreature(player1, new LlanowarElves());
            attacker2.setAttacking(true);
            setupCombatDamageResolution();

            harness.passBothPriorities();

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(17);
        }

        @Test
        @DisplayName("First strike creature kills blocker before it can deal damage back")
        void firstStrikeKillsBlockerBeforeItDealsDamage() {
            harness.setLife(player2, 20);
            // Youthful Knight (2/1 First Strike) vs Grizzly Bears (2/2)
            // Phase 1: Knight deals 2 first-strike damage to Bears → Bears die
            // Phase 2: Bears already dead → Knight survives
            Permanent attacker = addReadyCreature(player1, new YouthfulKnight());
            attacker.setAttacking(true);
            Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
            blocker.setBlocking(true);
            blocker.addBlockingTarget(0);
            setupCombatDamageResolution();

            harness.passBothPriorities();

            harness.assertOnBattlefield(player1, "Youthful Knight");
            harness.assertNotOnBattlefield(player2, "Grizzly Bears");
        }

        @Test
        @DisplayName("First strike creature dies to blocker with higher toughness")
        void firstStrikeCreatureDiesToLargerBlocker() {
            harness.setLife(player2, 20);
            // Youthful Knight (2/1 First Strike) vs Giant Spider (2/4 Reach)
            // Phase 1: Knight deals 2 first-strike damage to Spider → Spider at 2 dmg/4 toughness, survives
            // Phase 2: Spider deals 2 damage to Knight → Knight (1 toughness) dies
            Permanent attacker = addReadyCreature(player1, new YouthfulKnight());
            attacker.setAttacking(true);
            Permanent blocker = addReadyCreature(player2, new GiantSpider());
            blocker.setBlocking(true);
            blocker.addBlockingTarget(0);
            setupCombatDamageResolution();

            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player1, "Youthful Knight");
            harness.assertOnBattlefield(player2, "Giant Spider");
        }

        @Test
        @DisplayName("Prevent all combat damage flag prevents all damage")
        void preventAllCombatDamagePreventsAllDamage() {
            harness.setLife(player2, 20);
            Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
            attacker.setAttacking(true);
            gd.preventAllCombatDamage = true;
            setupCombatDamageResolution();

            harness.passBothPriorities();

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
        }

        @Test
        @DisplayName("Prevention shield saves creature from lethal combat damage")
        void preventionShieldSavesCreature() {
            harness.setLife(player2, 20);
            Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
            attacker.setAttacking(true);
            Permanent blocker = addReadyCreature(player2, new LlanowarElves());
            blocker.setBlocking(true);
            blocker.addBlockingTarget(0);
            blocker.setDamagePreventionShield(2);
            setupCombatDamageResolution();

            harness.passBothPriorities();

            // Blocker survives: 2 damage - 2 prevention = 0 effective
            harness.assertOnBattlefield(player2, "Llanowar Elves");
        }

        @Test
        @DisplayName("Infect creature applies -1/-1 counters to blocker instead of damage")
        void infectAppliesCountersToCreature() {
            harness.setLife(player2, 20);
            // Blight Mamba (1/1 Infect) vs Grizzly Bears (2/2)
            // Infect deals 1 -1/-1 counter to Bears → Bears becomes 1/1, survives
            // Bears deals 2 damage to Mamba → Mamba (1 toughness) dies
            Permanent attacker = addReadyCreature(player1, new BlightMamba());
            attacker.setAttacking(true);
            Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
            blocker.setBlocking(true);
            blocker.addBlockingTarget(0);
            setupCombatDamageResolution();

            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player1, "Blight Mamba");
            Permanent survivingBlocker = gd.playerBattlefields.get(player2.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                    .findFirst().orElse(null);
            assertThat(survivingBlocker).isNotNull();
            assertThat(survivingBlocker.getMinusOneMinusOneCounters()).isEqualTo(1);
        }

        @Test
        @DisplayName("Infect creature deals poison counters to player instead of life loss")
        void infectDealsPoisonCountersToPlayer() {
            harness.setLife(player2, 20);
            Permanent attacker = addReadyCreature(player1, new BlightMamba());
            attacker.setAttacking(true);
            setupCombatDamageResolution();

            harness.passBothPriorities();

            assertThat(gd.playerLifeTotals.get(player2.getId())).isEqualTo(20);
            assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
        }

        @Test
        @DisplayName("Prevent all combat damage also prevents creature-to-creature damage")
        void preventAllCombatDamageAlsoPreventsCreatureDamage() {
            harness.setLife(player2, 20);
            Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
            attacker.setAttacking(true);
            Permanent blocker = addReadyCreature(player2, new LlanowarElves());
            blocker.setBlocking(true);
            blocker.addBlockingTarget(0);
            gd.preventAllCombatDamage = true;
            setupCombatDamageResolution();

            harness.passBothPriorities();

            // Both creatures survive — all combat damage prevented
            harness.assertOnBattlefield(player1, "Grizzly Bears");
            harness.assertOnBattlefield(player2, "Llanowar Elves");
        }
    }

    // ===== Combat State Management Tests =====

    @Nested
    @DisplayName("Combat State")
    class CombatStateTest {

        @Test
        @DisplayName("Permanent.clearCombatState resets attacking and blocking flags")
        void clearCombatStateResetsFlags() {
            Permanent creature1 = addReadyCreature(player1, new GrizzlyBears());
            creature1.setAttacking(true);
            Permanent creature2 = addReadyCreature(player2, new GrizzlyBears());
            creature2.setBlocking(true);
            creature2.addBlockingTarget(0);

            assertThat(creature1.isAttacking()).isTrue();
            assertThat(creature2.isBlocking()).isTrue();
            assertThat(creature2.getBlockingTargets()).hasSize(1);

            creature1.clearCombatState();
            creature2.clearCombatState();

            assertThat(creature1.isAttacking()).isFalse();
            assertThat(creature2.isBlocking()).isFalse();
            assertThat(creature2.getBlockingTargets()).isEmpty();
        }

        @Test
        @DisplayName("Combat state is cleared after full combat resolution")
        void combatStateClearedAfterResolution() {
            harness.setLife(player2, 20);
            // Use a high-toughness creature as both attacker and blocker so they survive
            Permanent attacker = addReadyCreature(player1, new GiantSpider());
            attacker.setAttacking(true);
            Permanent blocker = addReadyCreature(player2, new GiantSpider());
            blocker.setBlocking(true);
            blocker.addBlockingTarget(0);
            setupCombatDamageResolution();

            harness.passBothPriorities();

            // After combat resolves and advances past END_OF_COMBAT, flags should be cleared
            Permanent survivingAttacker = gd.playerBattlefields.get(player1.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Giant Spider"))
                    .findFirst().orElse(null);
            assertThat(survivingAttacker).isNotNull();
            assertThat(survivingAttacker.isAttacking()).isFalse();

            Permanent survivingBlocker = gd.playerBattlefields.get(player2.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Giant Spider"))
                    .findFirst().orElse(null);
            assertThat(survivingBlocker).isNotNull();
            assertThat(survivingBlocker.isBlocking()).isFalse();
            assertThat(survivingBlocker.getBlockingTargets()).isEmpty();
        }
    }
}
