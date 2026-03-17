package com.github.laxika.magicalvibes.service.combat;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CombatDamagePhase1State;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.networking.message.AttackTarget;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.contains;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class CombatServiceTest {

    @Mock
    private CombatAttackService combatAttackService;

    @Mock
    private CombatBlockService combatBlockService;

    @Mock
    private CombatDamageService combatDamageService;

    @Mock
    private GameBroadcastService gameBroadcastService;

    @Mock
    private PermanentRemovalService permanentRemovalService;

    @InjectMocks
    private CombatService combatService;

    private GameData gd;
    private UUID player1Id;
    private UUID player2Id;

    @BeforeEach
    void setUp() {
        player1Id = UUID.randomUUID();
        player2Id = UUID.randomUUID();
        gd = new GameData(UUID.randomUUID(), "test", player1Id, "Player1");
        gd.orderedPlayerIds.add(player1Id);
        gd.orderedPlayerIds.add(player2Id);
        gd.playerIds.add(player1Id);
        gd.playerIds.add(player2Id);
        gd.playerIdToName.put(player1Id, "Player1");
        gd.playerIdToName.put(player2Id, "Player2");
        gd.playerBattlefields.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerBattlefields.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerGraveyards.put(player1Id, Collections.synchronizedList(new ArrayList<>()));
        gd.playerGraveyards.put(player2Id, Collections.synchronizedList(new ArrayList<>()));
    }

    // ===== Helper methods =====

    private static Card createCreature(String name) {
        Card card = new Card();
        card.setName(name);
        card.setType(CardType.CREATURE);
        card.setManaCost("{2}");
        card.setPower(2);
        card.setToughness(2);
        return card;
    }

    private Permanent addPermanent(UUID playerId, Card card) {
        Permanent permanent = new Permanent(card);
        permanent.setSummoningSick(false);
        gd.playerBattlefields.get(playerId).add(permanent);
        return permanent;
    }

    // ===== Delegation Tests =====

    @Nested
    @DisplayName("Delegation")
    class DelegationTest {

        @Test
        @DisplayName("declareAttackers delegates to CombatAttackService")
        void declareAttackersDelegates() {
            Player player = new Player(player1Id, "Player1");
            List<Integer> indices = List.of(0, 1);
            Map<Integer, UUID> targets = Map.of(0, player2Id);
            when(combatAttackService.declareAttackers(gd, player, indices, targets))
                    .thenReturn(CombatResult.AUTO_PASS_ONLY);

            CombatResult result = combatService.declareAttackers(gd, player, indices, targets);

            assertThat(result).isEqualTo(CombatResult.AUTO_PASS_ONLY);
            verify(combatAttackService).declareAttackers(gd, player, indices, targets);
        }

        @Test
        @DisplayName("declareBlockers delegates to CombatBlockService")
        void declareBlockersDelegates() {
            Player player = new Player(player2Id, "Player2");
            List<BlockerAssignment> assignments = List.of(new BlockerAssignment(0, 0));
            when(combatBlockService.declareBlockers(gd, player, assignments))
                    .thenReturn(CombatResult.AUTO_PASS_ONLY);

            CombatResult result = combatService.declareBlockers(gd, player, assignments);

            assertThat(result).isEqualTo(CombatResult.AUTO_PASS_ONLY);
            verify(combatBlockService).declareBlockers(gd, player, assignments);
        }

        @Test
        @DisplayName("resolveCombatDamage delegates to CombatDamageService")
        void resolveCombatDamageDelegates() {
            when(combatDamageService.resolveCombatDamage(gd))
                    .thenReturn(CombatResult.ADVANCE_ONLY);

            CombatResult result = combatService.resolveCombatDamage(gd);

            assertThat(result).isEqualTo(CombatResult.ADVANCE_ONLY);
            verify(combatDamageService).resolveCombatDamage(gd);
        }

        @Test
        @DisplayName("getAttackableCreatureIndices delegates to CombatAttackService")
        void getAttackableIndicesDelegates() {
            when(combatAttackService.getAttackableCreatureIndices(gd, player1Id))
                    .thenReturn(List.of(0, 2));

            List<Integer> result = combatService.getAttackableCreatureIndices(gd, player1Id);

            assertThat(result).containsExactly(0, 2);
            verify(combatAttackService).getAttackableCreatureIndices(gd, player1Id);
        }

        @Test
        @DisplayName("getMustAttackIndices delegates to CombatAttackService")
        void getMustAttackIndicesDelegates() {
            List<Integer> attackable = List.of(0, 1);
            when(combatAttackService.getMustAttackIndices(gd, player1Id, attackable))
                    .thenReturn(List.of(0));

            List<Integer> result = combatService.getMustAttackIndices(gd, player1Id, attackable);

            assertThat(result).containsExactly(0);
            verify(combatAttackService).getMustAttackIndices(gd, player1Id, attackable);
        }

        @Test
        @DisplayName("buildAvailableTargets delegates to CombatAttackService")
        void buildAvailableTargetsDelegates() {
            List<AttackTarget> expected = List.of(new AttackTarget(player2Id.toString(), "Player2", true));
            when(combatAttackService.buildAvailableTargets(gd, player1Id)).thenReturn(expected);

            List<AttackTarget> result = combatService.buildAvailableTargets(gd, player1Id);

            assertThat(result).isEqualTo(expected);
            verify(combatAttackService).buildAvailableTargets(gd, player1Id);
        }

        @Test
        @DisplayName("handleDeclareAttackersStep delegates to CombatAttackService")
        void handleDeclareAttackersStepDelegates() {
            combatService.handleDeclareAttackersStep(gd);

            verify(combatAttackService).handleDeclareAttackersStep(gd);
        }

        @Test
        @DisplayName("handleDeclareBlockersStep delegates to CombatBlockService")
        void handleDeclareBlockersStepDelegates() {
            when(combatBlockService.handleDeclareBlockersStep(gd))
                    .thenReturn(CombatResult.DONE);

            CombatResult result = combatService.handleDeclareBlockersStep(gd);

            assertThat(result).isEqualTo(CombatResult.DONE);
            verify(combatBlockService).handleDeclareBlockersStep(gd);
        }

        @Test
        @DisplayName("getAttackingCreatureIndices delegates to CombatAttackService")
        void getAttackingIndicesDelegates() {
            when(combatAttackService.getAttackingCreatureIndices(gd, player1Id))
                    .thenReturn(List.of(0));

            List<Integer> result = combatService.getAttackingCreatureIndices(gd, player1Id);

            assertThat(result).containsExactly(0);
            verify(combatAttackService).getAttackingCreatureIndices(gd, player1Id);
        }

        @Test
        @DisplayName("getBlockableCreatureIndices delegates to CombatBlockService")
        void getBlockableIndicesDelegates() {
            when(combatBlockService.getBlockableCreatureIndices(gd, player2Id))
                    .thenReturn(List.of(0, 1));

            List<Integer> result = combatService.getBlockableCreatureIndices(gd, player2Id);

            assertThat(result).containsExactly(0, 1);
            verify(combatBlockService).getBlockableCreatureIndices(gd, player2Id);
        }

        @Test
        @DisplayName("computeLegalBlockPairs delegates to CombatBlockService")
        void computeLegalBlockPairsDelegates() {
            List<Integer> blockers = List.of(0);
            List<Integer> attackers = List.of(0);
            Map<Integer, List<Integer>> expected = Map.of(0, List.of(0));
            when(combatBlockService.computeLegalBlockPairs(gd, blockers, attackers, player2Id, player1Id))
                    .thenReturn(expected);

            Map<Integer, List<Integer>> result = combatService.computeLegalBlockPairs(gd, blockers, attackers, player2Id, player1Id);

            assertThat(result).isEqualTo(expected);
            verify(combatBlockService).computeLegalBlockPairs(gd, blockers, attackers, player2Id, player1Id);
        }

        @Test
        @DisplayName("handleCombatDamageAssigned delegates to CombatDamageService")
        void handleCombatDamageAssignedDelegates() {
            Player player = new Player(player1Id, "Player1");
            Map<UUID, Integer> assignments = Map.of(player2Id, 3);

            combatService.handleCombatDamageAssigned(gd, player, 0, assignments);

            verify(combatDamageService).handleCombatDamageAssigned(gd, player, 0, assignments);
        }
    }

    // ===== clearCombatState Tests =====

    @Nested
    @DisplayName("clearCombatState")
    class ClearCombatStateTest {

        @Test
        @DisplayName("Clears attacking flag on all permanents")
        void clearsAttackingFlag() {
            Permanent attacker = addPermanent(player1Id, createCreature("Grizzly Bears"));
            attacker.setAttacking(true);
            assertThat(attacker.isAttacking()).isTrue();

            combatService.clearCombatState(gd);

            assertThat(attacker.isAttacking()).isFalse();
        }

        @Test
        @DisplayName("Clears blocking flag and blocking targets on all permanents")
        void clearsBlockingFlagAndTargets() {
            Permanent blocker = addPermanent(player2Id, createCreature("Grizzly Bears"));
            blocker.setBlocking(true);
            blocker.addBlockingTarget(0);
            assertThat(blocker.isBlocking()).isTrue();
            assertThat(blocker.getBlockingTargets()).hasSize(1);

            combatService.clearCombatState(gd);

            assertThat(blocker.isBlocking()).isFalse();
            assertThat(blocker.getBlockingTargets()).isEmpty();
        }

        @Test
        @DisplayName("Clears combat state across both players' battlefields")
        void clearsCombatStateAcrossBothPlayers() {
            Permanent attacker = addPermanent(player1Id, createCreature("Grizzly Bears"));
            attacker.setAttacking(true);
            Permanent blocker = addPermanent(player2Id, createCreature("Serra Angel"));
            blocker.setBlocking(true);
            blocker.addBlockingTarget(0);

            combatService.clearCombatState(gd);

            assertThat(attacker.isAttacking()).isFalse();
            assertThat(blocker.isBlocking()).isFalse();
            assertThat(blocker.getBlockingTargets()).isEmpty();
        }

        @Test
        @DisplayName("Clears combat damage player assignments")
        void clearsCombatDamagePlayerAssignments() {
            gd.combatDamagePlayerAssignments.put(0, new HashMap<>(Map.of(player2Id, 3)));

            combatService.clearCombatState(gd);

            assertThat(gd.combatDamagePlayerAssignments).isEmpty();
        }

        @Test
        @DisplayName("Clears combat damage pending indices")
        void clearsCombatDamagePendingIndices() {
            gd.combatDamagePendingIndices.add(0);
            gd.combatDamagePendingIndices.add(1);

            combatService.clearCombatState(gd);

            assertThat(gd.combatDamagePendingIndices).isEmpty();
        }

        @Test
        @DisplayName("Resets combat damage phase 1 complete flag")
        void resetsCombatDamagePhase1CompleteFlag() {
            gd.combatDamagePhase1Complete = true;

            combatService.clearCombatState(gd);

            assertThat(gd.combatDamagePhase1Complete).isFalse();
        }

        @Test
        @DisplayName("Clears combat damage phase 1 state")
        void clearsCombatDamagePhase1State() {
            gd.combatDamagePhase1State = new CombatDamagePhase1State(
                    Set.of(), Set.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of(), Map.of(),
                    0, 0, Map.of(), Map.of(), false, Set.of(), Set.of());

            combatService.clearCombatState(gd);

            assertThat(gd.combatDamagePhase1State).isNull();
        }

        @Test
        @DisplayName("Handles empty battlefields without error")
        void handlesEmptyBattlefields() {
            combatService.clearCombatState(gd);

            assertThat(gd.combatDamagePlayerAssignments).isEmpty();
            assertThat(gd.combatDamagePendingIndices).isEmpty();
            assertThat(gd.combatDamagePhase1Complete).isFalse();
            assertThat(gd.combatDamagePhase1State).isNull();
        }
    }

    // ===== processEndOfCombatSacrifices Tests =====

    @Nested
    @DisplayName("processEndOfCombatSacrifices")
    class ProcessEndOfCombatSacrificesTest {

        @Test
        @DisplayName("Sacrifices permanent marked for end-of-combat sacrifice")
        void sacrificesMarkedPermanent() {
            Permanent creature = addPermanent(player1Id, createCreature("Grizzly Bears"));
            gd.permanentsToSacrificeAtEndOfCombat.add(creature.getId());

            combatService.processEndOfCombatSacrifices(gd);

            verify(permanentRemovalService).removePermanentToGraveyard(gd, creature);
        }

        @Test
        @DisplayName("Logs sacrifice message for each sacrificed permanent")
        void logsSacrificeMessage() {
            Permanent creature = addPermanent(player1Id, createCreature("Grizzly Bears"));
            gd.permanentsToSacrificeAtEndOfCombat.add(creature.getId());

            combatService.processEndOfCombatSacrifices(gd);

            verify(gameBroadcastService).logAndBroadcast(eq(gd), contains("Grizzly Bears"));
            verify(gameBroadcastService).logAndBroadcast(eq(gd), contains("sacrificed"));
        }

        @Test
        @DisplayName("Clears sacrifice set after processing")
        void clearsSacrificeSetAfterProcessing() {
            Permanent creature = addPermanent(player1Id, createCreature("Grizzly Bears"));
            gd.permanentsToSacrificeAtEndOfCombat.add(creature.getId());

            combatService.processEndOfCombatSacrifices(gd);

            assertThat(gd.permanentsToSacrificeAtEndOfCombat).isEmpty();
        }

        @Test
        @DisplayName("Removes orphaned auras after sacrificing")
        void removesOrphanedAurasAfterSacrificing() {
            Permanent creature = addPermanent(player1Id, createCreature("Grizzly Bears"));
            gd.permanentsToSacrificeAtEndOfCombat.add(creature.getId());

            combatService.processEndOfCombatSacrifices(gd);

            verify(permanentRemovalService).removeOrphanedAuras(gd);
        }

        @Test
        @DisplayName("No-op when no permanents are marked for sacrifice")
        void noOpWhenNoMarkedPermanents() {
            addPermanent(player1Id, createCreature("Grizzly Bears"));

            combatService.processEndOfCombatSacrifices(gd);

            verify(permanentRemovalService, never()).removePermanentToGraveyard(any(), any());
        }

        @Test
        @DisplayName("Still removes orphaned auras even when no sacrifices occur")
        void removesOrphanedAurasEvenWithNoSacrifices() {
            combatService.processEndOfCombatSacrifices(gd);

            verify(permanentRemovalService).removeOrphanedAuras(gd);
        }

        @Test
        @DisplayName("Sacrifices multiple permanents from different players")
        void sacrificesMultiplePermanentsFromDifferentPlayers() {
            Permanent creature1 = addPermanent(player1Id, createCreature("Grizzly Bears"));
            Permanent creature2 = addPermanent(player2Id, createCreature("Serra Angel"));
            gd.permanentsToSacrificeAtEndOfCombat.add(creature1.getId());
            gd.permanentsToSacrificeAtEndOfCombat.add(creature2.getId());

            combatService.processEndOfCombatSacrifices(gd);

            verify(permanentRemovalService).removePermanentToGraveyard(gd, creature1);
            verify(permanentRemovalService).removePermanentToGraveyard(gd, creature2);
            assertThat(gd.permanentsToSacrificeAtEndOfCombat).isEmpty();
        }

        @Test
        @DisplayName("Does not sacrifice permanent that is not marked")
        void doesNotSacrificeUnmarkedPermanent() {
            Permanent marked = addPermanent(player1Id, createCreature("Grizzly Bears"));
            Permanent unmarked = addPermanent(player1Id, createCreature("Serra Angel"));
            gd.permanentsToSacrificeAtEndOfCombat.add(marked.getId());

            combatService.processEndOfCombatSacrifices(gd);

            verify(permanentRemovalService).removePermanentToGraveyard(gd, marked);
            verify(permanentRemovalService, never()).removePermanentToGraveyard(gd, unmarked);
        }
    }
}
