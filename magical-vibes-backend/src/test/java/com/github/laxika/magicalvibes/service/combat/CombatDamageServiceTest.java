package com.github.laxika.magicalvibes.service.combat;

import com.github.laxika.magicalvibes.cards.a.AvatarOfMight;
import com.github.laxika.magicalvibes.cards.b.BlightMamba;
import com.github.laxika.magicalvibes.cards.g.GiantSpider;
import com.github.laxika.magicalvibes.cards.g.GrizzlyBears;
import com.github.laxika.magicalvibes.cards.l.LlanowarElves;
import com.github.laxika.magicalvibes.cards.y.YouthfulKnight;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.testutil.BaseCardTest;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

class CombatDamageServiceTest extends BaseCardTest {

    // ===== Helper methods =====

    private Permanent addReadyCreature(Player player, Card card) {
        harness.addToBattlefield(player, card);
        List<Permanent> bf = gd.playerBattlefields.get(player.getId());
        Permanent p = bf.getLast();
        p.setSummoningSick(false);
        return p;
    }

    private Card withKeywords(Card card, Keyword... keywords) {
        card.setKeywords(Set.of(keywords));
        return card;
    }

    /**
     * Sets up combat state at DECLARE_BLOCKERS step with player1 as active player.
     * Callers should set attacking/blocking state on permanents before calling this,
     * then call {@code harness.passBothPriorities()} to resolve combat damage.
     */
    private void setupCombatDamageResolution() {
        harness.forceActivePlayer(player1);
        harness.forceStep(TurnStep.DECLARE_BLOCKERS);
        harness.clearPriorityPassed();
    }

    // ===== Double Strike =====

    @Nested
    @DisplayName("Double Strike")
    class DoubleStrikeTest {

        @Test
        @DisplayName("Double strike creature deals damage in both first-strike and regular phases")
        void doubleStrikeDealsDamageInBothPhases() {
            harness.setLife(player2, 20);
            // 2/2 double strike attacks unblocked → deals 2 + 2 = 4 damage
            Card dsCard = withKeywords(new GrizzlyBears(), Keyword.DOUBLE_STRIKE);
            Permanent attacker = addReadyCreature(player1, dsCard);
            attacker.setAttacking(true);
            setupCombatDamageResolution();

            harness.passBothPriorities();

            harness.assertLife(player2, 16);
        }

        @Test
        @DisplayName("Double strike creature kills blocker in first-strike phase and deals regular damage too")
        void doubleStrikeKillsBlockerThenDealsRegularDamage() {
            harness.setLife(player2, 20);
            // 2/2 double strike + trample attacks, blocked by 1/1
            // Phase 1: 2 first-strike to blocker → blocker dies (only 1 needed, but auto-assign gives all)
            // Phase 2: no living blockers → 2 tramples to player (trample needed to get overflow)
            Card dsCard = withKeywords(new GrizzlyBears(), Keyword.DOUBLE_STRIKE, Keyword.TRAMPLE);
            Permanent attacker = addReadyCreature(player1, dsCard);
            attacker.setAttacking(true);

            Permanent blocker = addReadyCreature(player2, new LlanowarElves());
            blocker.setBlocking(true);
            blocker.addBlockingTarget(0);

            setupCombatDamageResolution();
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Llanowar Elves");
            // Phase 1: 1 to blocker (lethal), 1 trample to player; Phase 2: 2 trample to player = 3 total
            assertThat(gd.playerLifeTotals.get(player2.getId())).isLessThan(20);
        }

        @Test
        @DisplayName("Double strike creature trades with equal creature in first-strike phase — no regular damage")
        void doubleStrikeTradesInFirstStrikePhase() {
            harness.setLife(player2, 20);
            // 2/1 double strike attacks, blocked by 2/2
            // Phase 1: Knight deals 2 first-strike to Bears → Bears (2/2) dies
            // Phase 2: Knight already dealt damage in both phases, no blocker left but creature was blocked
            // → No damage to player (blocked creature without trample)
            Permanent attacker = addReadyCreature(player1, new YouthfulKnight()); // 2/1 first strike
            // Override to double strike
            attacker.getCard().setKeywords(Set.of(Keyword.DOUBLE_STRIKE));
            attacker.setAttacking(true);

            Permanent blocker = addReadyCreature(player2, new GrizzlyBears()); // 2/2
            blocker.setBlocking(true);
            blocker.addBlockingTarget(0);

            setupCombatDamageResolution();
            harness.passBothPriorities();

            // Bears died from first-strike damage, Knight survives (Bears died before dealing damage back)
            harness.assertOnBattlefield(player1, "Youthful Knight");
            harness.assertNotOnBattlefield(player2, "Grizzly Bears");
            // Blocked creature with no trample: no damage to player even with double strike
            harness.assertLife(player2, 20);
        }

        @Test
        @DisplayName("Double strike vs first strike: both deal damage in first-strike phase")
        void doubleStrikeVsFirstStrike() {
            harness.setLife(player2, 20);
            // 2/1 double strike attacks, blocked by 2/1 first strike
            // Phase 1: Both deal damage simultaneously. Attacker takes 2 → dies. Blocker takes 2 → dies.
            Card dsCard = withKeywords(new YouthfulKnight(), Keyword.DOUBLE_STRIKE);
            Permanent attacker = addReadyCreature(player1, dsCard);
            attacker.setAttacking(true);

            Card fsCard = withKeywords(new YouthfulKnight(), Keyword.FIRST_STRIKE);
            Permanent blocker = addReadyCreature(player2, fsCard);
            blocker.setBlocking(true);
            blocker.addBlockingTarget(0);

            setupCombatDamageResolution();
            harness.passBothPriorities();

            // Both 2/1 creatures trade in the first-strike phase
            harness.assertNotOnBattlefield(player1, "Youthful Knight");
            harness.assertNotOnBattlefield(player2, "Youthful Knight");
        }
    }

    // ===== Lifelink =====

    @Nested
    @DisplayName("Lifelink")
    class LifelinkTest {

        @Test
        @DisplayName("Lifelink creature gains life equal to combat damage dealt to player")
        void lifelinkGainsLifeOnPlayerDamage() {
            harness.setLife(player1, 15);
            harness.setLife(player2, 20);
            Card lifelinkCard = withKeywords(new GrizzlyBears(), Keyword.LIFELINK);
            Permanent attacker = addReadyCreature(player1, lifelinkCard);
            attacker.setAttacking(true);
            setupCombatDamageResolution();

            harness.passBothPriorities();

            // Defender takes 2, attacker's controller gains 2
            harness.assertLife(player2, 18);
            harness.assertLife(player1, 17);
        }

        @Test
        @DisplayName("Lifelink creature gains life equal to combat damage dealt to blocker")
        void lifelinkGainsLifeOnCreatureDamage() {
            harness.setLife(player1, 15);
            harness.setLife(player2, 20);
            Card lifelinkCard = withKeywords(new GrizzlyBears(), Keyword.LIFELINK);
            Permanent attacker = addReadyCreature(player1, lifelinkCard);
            attacker.setAttacking(true);

            Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
            blocker.setBlocking(true);
            blocker.addBlockingTarget(0);

            setupCombatDamageResolution();
            harness.passBothPriorities();

            // Both 2/2s trade; lifelink creature dealt 2 damage to blocker → controller gains 2
            harness.assertLife(player1, 17);
        }

        @Test
        @DisplayName("Lifelink + double strike gains life from both damage phases")
        void lifelinkDoubleStrikeGainsTwice() {
            harness.setLife(player1, 10);
            harness.setLife(player2, 20);
            Card card = withKeywords(new GrizzlyBears(), Keyword.LIFELINK, Keyword.DOUBLE_STRIKE);
            Permanent attacker = addReadyCreature(player1, card);
            attacker.setAttacking(true);
            setupCombatDamageResolution();

            harness.passBothPriorities();

            // 2/2 double strike lifelink deals 2+2=4, gains 4 life
            harness.assertLife(player2, 16);
            harness.assertLife(player1, 14);
        }

        @Test
        @DisplayName("Lifelink on blocker gains life for its controller")
        void lifelinkBlockerGainsLife() {
            harness.setLife(player1, 20);
            harness.setLife(player2, 15);
            Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
            attacker.setAttacking(true);

            Card lifelinkBlocker = withKeywords(new GrizzlyBears(), Keyword.LIFELINK);
            Permanent blocker = addReadyCreature(player2, lifelinkBlocker);
            blocker.setBlocking(true);
            blocker.addBlockingTarget(0);

            setupCombatDamageResolution();
            harness.passBothPriorities();

            // Both trade, but lifelink blocker's controller gains 2 life
            harness.assertLife(player2, 17);
        }
    }

    // ===== Marked Damage =====

    @Nested
    @DisplayName("Marked Damage")
    class MarkedDamageTest {

        @Test
        @DisplayName("Surviving creature has marked damage after combat")
        void survivingCreatureHasMarkedDamage() {
            harness.setLife(player2, 20);
            // Giant Spider (2/4) blocks Grizzly Bears (2/2)
            // Spider takes 2 damage but survives (2 < 4)
            Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
            attacker.setAttacking(true);

            Permanent blocker = addReadyCreature(player2, new GiantSpider());
            blocker.setBlocking(true);
            blocker.addBlockingTarget(0);

            setupCombatDamageResolution();
            harness.passBothPriorities();

            // Spider survives with 2 marked damage
            harness.assertOnBattlefield(player2, "Giant Spider");
            Permanent spider = gd.playerBattlefields.get(player2.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Giant Spider"))
                    .findFirst().orElseThrow();
            assertThat(spider.getMarkedDamage()).isEqualTo(2);
        }

        @Test
        @DisplayName("Dead creature is removed (marked damage >= toughness)")
        void creatureDiesWhenMarkedDamageReachesToughness() {
            harness.setLife(player2, 20);
            // Grizzly Bears (2/2) blocks Grizzly Bears (2/2) → both take 2 damage, both die
            Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
            attacker.setAttacking(true);

            Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
            blocker.setBlocking(true);
            blocker.addBlockingTarget(0);

            setupCombatDamageResolution();
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player1, "Grizzly Bears");
            harness.assertNotOnBattlefield(player2, "Grizzly Bears");
            harness.assertInGraveyard(player1, "Grizzly Bears");
            harness.assertInGraveyard(player2, "Grizzly Bears");
        }
    }

    // ===== Mixed Attackers (blocked + unblocked) =====

    @Nested
    @DisplayName("Mixed Combat Scenarios")
    class MixedCombatTest {

        @Test
        @DisplayName("One blocked attacker trades, one unblocked attacker deals player damage")
        void mixedBlockedAndUnblockedAttackers() {
            harness.setLife(player2, 20);
            Permanent attacker1 = addReadyCreature(player1, new GrizzlyBears());
            attacker1.setAttacking(true);
            Permanent attacker2 = addReadyCreature(player1, new GrizzlyBears());
            attacker2.setAttacking(true);

            Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
            blocker.setBlocking(true);
            blocker.addBlockingTarget(0); // blocks first attacker only

            setupCombatDamageResolution();
            harness.passBothPriorities();

            // First attacker and blocker trade
            // Second attacker is unblocked → 2 damage to player
            harness.assertLife(player2, 18);
        }

        @Test
        @DisplayName("Three attackers: one blocked (trades), two unblocked deal full damage")
        void threeAttackersTwoUnblocked() {
            harness.setLife(player2, 20);
            Permanent atk1 = addReadyCreature(player1, new GrizzlyBears());
            atk1.setAttacking(true);
            Permanent atk2 = addReadyCreature(player1, new LlanowarElves());
            atk2.setAttacking(true);
            Permanent atk3 = addReadyCreature(player1, new GrizzlyBears());
            atk3.setAttacking(true);

            Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
            blocker.setBlocking(true);
            blocker.addBlockingTarget(0); // blocks first attacker

            setupCombatDamageResolution();
            harness.passBothPriorities();

            // Attacker 1 and blocker trade (2/2 vs 2/2)
            // Attacker 2 (1/1) unblocked → 1 damage
            // Attacker 3 (2/2) unblocked → 2 damage
            // Total: 3 damage to player
            harness.assertLife(player2, 17);
        }
    }

    // ===== First Strike killing attacker before regular damage =====

    @Nested
    @DisplayName("First Strike Phase Interactions")
    class FirstStrikePhaseTest {

        @Test
        @DisplayName("Blocker with first strike kills attacker before it can deal damage")
        void firstStrikeBlockerKillsAttackerBeforeDamage() {
            harness.setLife(player2, 20);
            // Grizzly Bears (2/2) attacks, Youthful Knight (2/1 first strike) blocks
            // Phase 1: Knight deals 2 first-strike to Bears → Bears dies
            // Phase 2: Bears is dead, doesn't deal damage back; Knight survives
            Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
            attacker.setAttacking(true);

            Permanent blocker = addReadyCreature(player2, new YouthfulKnight());
            blocker.setBlocking(true);
            blocker.addBlockingTarget(0);

            setupCombatDamageResolution();
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player1, "Grizzly Bears");
            harness.assertOnBattlefield(player2, "Youthful Knight");
        }

        @Test
        @DisplayName("First strike attacker that doesn't kill blocker still takes damage in regular phase")
        void firstStrikeAttackerTakesDamageInRegularPhase() {
            harness.setLife(player2, 20);
            // Youthful Knight (2/1 first strike) attacks, Giant Spider (2/4) blocks
            // Phase 1: Knight deals 2 first-strike to Spider → Spider at 2/4 with 2 damage, survives
            // Phase 2: Spider deals 2 to Knight → Knight (1 toughness) dies
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
    }

    // ===== Trample Validation =====

    @Nested
    @DisplayName("Trample Damage Assignment Validation")
    class TrampleValidationTest {

        @Test
        @DisplayName("Trample: must assign at least lethal damage to each blocker before trampling")
        void trampleMustAssignLethalToBlocker() {
            harness.setLife(player2, 20);
            // AvatarOfMight (8/8 trample) blocked by GrizzlyBears (2/2)
            Permanent attacker = addReadyCreature(player1, new AvatarOfMight());
            attacker.setAttacking(true);

            Permanent blocker = addReadyCreature(player2, new GrizzlyBears());
            blocker.setBlocking(true);
            blocker.addBlockingTarget(0);

            setupCombatDamageResolution();
            harness.passBothPriorities();

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.COMBAT_DAMAGE_ASSIGNMENT);

            // Assigning only 1 to a 2/2 blocker is not lethal → should be rejected
            assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0, Map.of(
                    blocker.getId(), 1,
                    player2.getId(), 7
            ))).isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Trample");
        }

        @Test
        @DisplayName("Trample: cannot assign damage to player if not enough to kill all blockers")
        void trampleCannotOverflowWhenNotEnoughForLethal() {
            harness.setLife(player2, 20);
            // 2/2 trample attacks, blocked by two 2/2 creatures
            // Total lethal needed: 2+2=4, but attacker only has 2 power → all must go to blockers
            Card trampleCard = withKeywords(new GrizzlyBears(), Keyword.TRAMPLE);
            Permanent attacker = addReadyCreature(player1, trampleCard);
            attacker.setAttacking(true);

            Permanent blocker1 = addReadyCreature(player2, new GrizzlyBears());
            blocker1.setBlocking(true);
            blocker1.addBlockingTarget(0);
            Permanent blocker2 = addReadyCreature(player2, new GrizzlyBears());
            blocker2.setBlocking(true);
            blocker2.addBlockingTarget(0);

            setupCombatDamageResolution();
            harness.passBothPriorities();

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.COMBAT_DAMAGE_ASSIGNMENT);

            // Trying to send any damage to the player should fail
            assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0, Map.of(
                    blocker1.getId(), 1,
                    player2.getId(), 1
            ))).isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Trample");
        }

        @Test
        @DisplayName("Trample with deathtouch: only 1 damage needed per blocker to be lethal")
        void trampleDeathtouchOnlyNeedsOnePerBlocker() {
            harness.setLife(player2, 20);
            // AvatarOfMight (8/8) with trample+deathtouch, blocked by two 2/2 creatures
            // Deathtouch lethal = 1 per blocker, so 1+1=2 to blockers, 6 tramples through
            Card card = withKeywords(new AvatarOfMight(), Keyword.TRAMPLE, Keyword.DEATHTOUCH);
            Permanent attacker = addReadyCreature(player1, card);
            attacker.setAttacking(true);

            Permanent blocker1 = addReadyCreature(player2, new GrizzlyBears());
            blocker1.setBlocking(true);
            blocker1.addBlockingTarget(0);
            Permanent blocker2 = addReadyCreature(player2, new GrizzlyBears());
            blocker2.setBlocking(true);
            blocker2.addBlockingTarget(0);

            setupCombatDamageResolution();
            harness.passBothPriorities();

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.COMBAT_DAMAGE_ASSIGNMENT);

            // 1 to each blocker (lethal via deathtouch), 6 to player
            harness.handleCombatDamageAssigned(player1, 0, Map.of(
                    blocker1.getId(), 1,
                    blocker2.getId(), 1,
                    player2.getId(), 6
            ));

            harness.assertNotOnBattlefield(player2, "Grizzly Bears");
            harness.assertLife(player2, 14);
        }
    }

    // ===== handleCombatDamageAssigned Validation =====

    @Nested
    @DisplayName("Damage Assignment Validation")
    class DamageAssignmentValidationTest {

        @Test
        @DisplayName("Rejects damage assignment when not in combat damage phase")
        void rejectsWhenNotInCombatDamagePhase() {
            assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0, Map.of()))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Not in combat damage assignment phase");
        }

        @Test
        @DisplayName("Rejects damage assignment from non-active player")
        void rejectsFromNonActivePlayer() {
            harness.setLife(player2, 20);
            // Set up a scenario that requires manual damage assignment
            Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
            attacker.setAttacking(true);

            Permanent blocker1 = addReadyCreature(player2, new LlanowarElves());
            blocker1.setBlocking(true);
            blocker1.addBlockingTarget(0);
            Permanent blocker2 = addReadyCreature(player2, new LlanowarElves());
            blocker2.setBlocking(true);
            blocker2.addBlockingTarget(0);

            setupCombatDamageResolution();
            harness.passBothPriorities();

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.COMBAT_DAMAGE_ASSIGNMENT);

            // player2 (non-active) tries to assign damage
            assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player2, 0, Map.of(
                    blocker1.getId(), 1,
                    blocker2.getId(), 1
            ))).isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Only the active player");
        }

        @Test
        @DisplayName("Rejects assignment with wrong total damage")
        void rejectsWrongTotalDamage() {
            harness.setLife(player2, 20);
            Permanent attacker = addReadyCreature(player1, new GrizzlyBears()); // 2/2
            attacker.setAttacking(true);

            Permanent blocker1 = addReadyCreature(player2, new LlanowarElves());
            blocker1.setBlocking(true);
            blocker1.addBlockingTarget(0);
            Permanent blocker2 = addReadyCreature(player2, new LlanowarElves());
            blocker2.setBlocking(true);
            blocker2.addBlockingTarget(0);

            setupCombatDamageResolution();
            harness.passBothPriorities();

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.COMBAT_DAMAGE_ASSIGNMENT);

            // Assign 3 total damage to a 2-power creature → should fail
            assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0, Map.of(
                    blocker1.getId(), 2,
                    blocker2.getId(), 1
            ))).isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Total assigned damage");
        }

        @Test
        @DisplayName("Rejects assignment to invalid target")
        void rejectsInvalidTarget() {
            harness.setLife(player2, 20);
            Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
            attacker.setAttacking(true);

            Permanent blocker1 = addReadyCreature(player2, new LlanowarElves());
            blocker1.setBlocking(true);
            blocker1.addBlockingTarget(0);
            Permanent blocker2 = addReadyCreature(player2, new LlanowarElves());
            blocker2.setBlocking(true);
            blocker2.addBlockingTarget(0);

            setupCombatDamageResolution();
            harness.passBothPriorities();

            // Assign damage to a random UUID that is not a valid target
            UUID bogusTarget = UUID.randomUUID();
            assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0, Map.of(
                    bogusTarget, 2
            ))).isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Invalid damage target");
        }

        @Test
        @DisplayName("Non-trample non-unblocked creature cannot assign damage to defending player")
        void nonTrampleCannotAssignToPlayer() {
            harness.setLife(player2, 20);
            Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
            attacker.setAttacking(true);

            Permanent blocker1 = addReadyCreature(player2, new LlanowarElves());
            blocker1.setBlocking(true);
            blocker1.addBlockingTarget(0);
            Permanent blocker2 = addReadyCreature(player2, new LlanowarElves());
            blocker2.setBlocking(true);
            blocker2.addBlockingTarget(0);

            setupCombatDamageResolution();
            harness.passBothPriorities();

            // Try assigning damage to the player without trample — player ID is not a valid target
            assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 0, Map.of(
                    player2.getId(), 2
            ))).isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("Invalid damage target");
        }

        @Test
        @DisplayName("Rejects assignment for attacker index that is not pending")
        void rejectsNonPendingAttackerIndex() {
            harness.setLife(player2, 20);
            Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
            attacker.setAttacking(true);

            Permanent blocker1 = addReadyCreature(player2, new LlanowarElves());
            blocker1.setBlocking(true);
            blocker1.addBlockingTarget(0);
            Permanent blocker2 = addReadyCreature(player2, new LlanowarElves());
            blocker2.setBlocking(true);
            blocker2.addBlockingTarget(0);

            setupCombatDamageResolution();
            harness.passBothPriorities();

            assertThat(gd.interaction.awaitingInputType()).isEqualTo(AwaitingInput.COMBAT_DAMAGE_ASSIGNMENT);

            // Attacker index 5 doesn't exist/isn't pending
            assertThatThrownBy(() -> harness.handleCombatDamageAssigned(player1, 5, Map.of(
                    blocker1.getId(), 1,
                    blocker2.getId(), 1
            ))).isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("not pending");
        }
    }

    // ===== Indestructible =====

    @Nested
    @DisplayName("Indestructible in Combat")
    class IndestructibleTest {

        @Test
        @DisplayName("Indestructible creature survives lethal combat damage")
        void indestructibleSurvivesLethalDamage() {
            harness.setLife(player2, 20);
            Card indestructibleCard = withKeywords(new GrizzlyBears(), Keyword.INDESTRUCTIBLE);
            Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
            attacker.setAttacking(true);

            Permanent blocker = addReadyCreature(player2, indestructibleCard);
            blocker.setBlocking(true);
            blocker.addBlockingTarget(0);

            setupCombatDamageResolution();
            harness.passBothPriorities();

            // 2/2 attacker dies (took 2 damage from blocker)
            harness.assertNotOnBattlefield(player1, "Grizzly Bears");
            // Indestructible blocker survives despite taking lethal damage
            harness.assertOnBattlefield(player2, "Grizzly Bears");
        }

        @Test
        @DisplayName("Indestructible creature still accumulates marked damage")
        void indestructibleStillAccumulatesMarkedDamage() {
            harness.setLife(player2, 20);
            // 2/2 indestructible blocks a 2/2 — takes lethal (2) damage but survives
            Card indestructibleCard = withKeywords(new GrizzlyBears(), Keyword.INDESTRUCTIBLE);
            Permanent attacker = addReadyCreature(player1, new GrizzlyBears()); // 2/2
            attacker.setAttacking(true);

            Permanent blocker = addReadyCreature(player2, indestructibleCard); // 2/2 indestructible
            blocker.setBlocking(true);
            blocker.addBlockingTarget(0);

            setupCombatDamageResolution();
            harness.passBothPriorities();

            // Indestructible creature survives but has marked damage equal to attacker's power
            harness.assertOnBattlefield(player2, "Grizzly Bears");
            Permanent survivor = gd.playerBattlefields.get(player2.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Grizzly Bears"))
                    .findFirst().orElseThrow();
            assertThat(survivor.getMarkedDamage()).isEqualTo(2);
        }
    }

    // ===== Infect + Blocking Interactions =====

    @Nested
    @DisplayName("Infect Combat Interactions")
    class InfectCombatTest {

        @Test
        @DisplayName("Infect damage to creature applies -1/-1 counters, not marked damage")
        void infectDamageAppliesCountersNotMarkedDamage() {
            harness.setLife(player2, 20);
            // BlightMamba (1/1 infect) attacks, GiantSpider (2/4) blocks
            // Infect: 1 -1/-1 counter on Spider → becomes 1/3 effectively
            // Spider: deals 2 to Mamba → Mamba dies
            Permanent attacker = addReadyCreature(player1, new BlightMamba());
            attacker.setAttacking(true);

            Permanent blocker = addReadyCreature(player2, new GiantSpider());
            blocker.setBlocking(true);
            blocker.addBlockingTarget(0);

            setupCombatDamageResolution();
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player1, "Blight Mamba");
            harness.assertOnBattlefield(player2, "Giant Spider");

            Permanent spider = gd.playerBattlefields.get(player2.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Giant Spider"))
                    .findFirst().orElseThrow();
            // Infect places -1/-1 counters, NOT marked damage
            assertThat(spider.getMinusOneMinusOneCounters()).isEqualTo(1);
            assertThat(spider.getMarkedDamage()).isEqualTo(0);
        }

        @Test
        @DisplayName("Infect creature kills blocker via -1/-1 counters reducing toughness to 0")
        void infectKillsBlockerViaToughnessReduction() {
            harness.setLife(player2, 20);
            // 2/2 infect attacks, blocked by LlanowarElves (1/1)
            // Infect: 2 -1/-1 counters → toughness goes to -1 → dies
            Card infectCard = withKeywords(new GrizzlyBears(), Keyword.INFECT);
            Permanent attacker = addReadyCreature(player1, infectCard);
            attacker.setAttacking(true);

            Permanent blocker = addReadyCreature(player2, new LlanowarElves());
            blocker.setBlocking(true);
            blocker.addBlockingTarget(0);

            setupCombatDamageResolution();
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player2, "Llanowar Elves");
        }
    }

    // ===== Prevent All Combat Damage =====

    @Nested
    @DisplayName("Prevent All Combat Damage")
    class PreventAllCombatDamageTest {

        @Test
        @DisplayName("Prevent all combat damage returns ADVANCE_AND_AUTO_PASS immediately")
        void preventAllCombatDamageAdvancesImmediately() {
            harness.setLife(player2, 20);
            Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
            attacker.setAttacking(true);

            Permanent blocker = addReadyCreature(player2, new LlanowarElves());
            blocker.setBlocking(true);
            blocker.addBlockingTarget(0);

            gd.preventAllCombatDamage = true;
            setupCombatDamageResolution();
            harness.passBothPriorities();

            // No damage dealt to player or creatures
            harness.assertLife(player2, 20);
            harness.assertOnBattlefield(player1, "Grizzly Bears");
            harness.assertOnBattlefield(player2, "Llanowar Elves");
        }
    }

    // ===== Orphaned Blocking State =====

    @Nested
    @DisplayName("Orphaned Blocking State Cleanup")
    class OrphanedBlockingStateTest {

        @Test
        @DisplayName("Surviving blocker whose blocked attacker died has blocking state cleared")
        void survivingBlockerClearedWhenAttackerDies() {
            harness.setLife(player2, 20);
            // LlanowarElves (1/1) attacks, GiantSpider (2/4) blocks
            // Elves die (1 toughness < 2 power), Spider survives
            Permanent attacker = addReadyCreature(player1, new LlanowarElves());
            attacker.setAttacking(true);

            Permanent blocker = addReadyCreature(player2, new GiantSpider());
            blocker.setBlocking(true);
            blocker.addBlockingTarget(0);
            blocker.addBlockingTargetPermanentId(attacker.getId());

            setupCombatDamageResolution();
            harness.passBothPriorities();

            harness.assertNotOnBattlefield(player1, "Llanowar Elves");
            harness.assertOnBattlefield(player2, "Giant Spider");

            // Spider's blocking state should be cleaned up since the attacker it was blocking died
            Permanent spider = gd.playerBattlefields.get(player2.getId()).stream()
                    .filter(p -> p.getCard().getName().equals("Giant Spider"))
                    .findFirst().orElseThrow();
            assertThat(spider.isBlocking()).isFalse();
            assertThat(spider.getBlockingTargets()).isEmpty();
        }
    }

    // ===== Damage to Player Tracking =====

    @Nested
    @DisplayName("Combat Damage Tracking")
    class CombatDamageTrackingTest {

        @Test
        @DisplayName("Defending player is tracked in playersDealtDamageThisTurn after combat damage")
        void defenderTrackedInDamagedPlayers() {
            harness.setLife(player2, 20);
            Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
            attacker.setAttacking(true);
            setupCombatDamageResolution();

            harness.passBothPriorities();

            assertThat(gd.playersDealtDamageThisTurn).contains(player2.getId());
        }

        @Test
        @DisplayName("Defending player not tracked when all combat damage is prevented")
        void defenderNotTrackedWhenDamagePrevented() {
            harness.setLife(player2, 20);
            Permanent attacker = addReadyCreature(player1, new GrizzlyBears());
            attacker.setAttacking(true);
            gd.preventAllCombatDamage = true;
            setupCombatDamageResolution();

            harness.passBothPriorities();

            assertThat(gd.playersDealtDamageThisTurn).doesNotContain(player2.getId());
        }

        @Test
        @DisplayName("Infect damage to player gives poison counters and tracks damage")
        void infectDamageGivesPoisonAndTracks() {
            harness.setLife(player2, 20);
            Permanent attacker = addReadyCreature(player1, new BlightMamba()); // 1/1 infect
            attacker.setAttacking(true);
            setupCombatDamageResolution();

            harness.passBothPriorities();

            // Life unchanged (infect gives poison, not life loss)
            harness.assertLife(player2, 20);
            // Poison counter applied
            assertThat(gd.playerPoisonCounters.getOrDefault(player2.getId(), 0)).isEqualTo(1);
            // Player tracked as having been dealt damage
            assertThat(gd.playersDealtDamageThisTurn).contains(player2.getId());
        }
    }
}
