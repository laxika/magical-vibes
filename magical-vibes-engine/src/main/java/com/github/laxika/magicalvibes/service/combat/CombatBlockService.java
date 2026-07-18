package com.github.laxika.magicalvibes.service.combat;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.BlockCostEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfWhenBlockingKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedByAtMostNCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedByFewerThanNCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockAloneEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessCountAlsoDoesEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockUnlessGreaterPowerAlsoDoesEffect;
import com.github.laxika.magicalvibes.model.effect.CanBlockAnyNumberOfCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CombatOpponentReferencingEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyBlockedCreatureAndSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyCombatOpponentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyEquipmentOnEquippedCombatOpponentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentThenEffect;
import com.github.laxika.magicalvibes.model.effect.EachControlledCreatureCanBeBlockedByAtMostNCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalBlockEffect;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalBlockPerEquipmentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnCombatOpponentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedByAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedIfAbleEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.networking.message.AvailableBlockersMessage;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.BlockLegalityContext;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;

/**
 * Handles declare-blockers step: computing legal blockers, validating blocker assignments
 * (evasion, menace, max-blockers, must-block), and collecting ON_BLOCK / ON_BECOMES_BLOCKED triggers.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CombatBlockService {

    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameBroadcastService gameBroadcastService;
    private final CombatAttackService combatAttackService;
    private final CombatTriggerService combatTriggerService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;

    /**
     * Returns the battlefield indices of creatures the given player can legally declare as blockers.
     */
    public List<Integer> getBlockableCreatureIndices(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return List.of();
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < battlefield.size(); i++) {
            if (gameQueryService.canBlock(gameData, battlefield.get(i))) {
                indices.add(i);
            }
        }
        // CR 509.1b: if only one creature can block and it has "can't block alone", remove it
        if (indices.size() == 1) {
            Permanent sole = battlefield.get(indices.getFirst());
            if (hasCantAttackOrBlockAlone(sole)) {
                return List.of();
            }
        }
        return indices;
    }

    /**
     * Computes which attackers each potential blocker can legally block.
     */
    public Map<Integer, List<Integer>> computeLegalBlockPairs(GameData gameData,
                                                              List<Integer> blockerIndices,
                                                              List<Integer> attackerIndices,
                                                              UUID defenderId,
                                                              UUID attackerId) {
        BlockLegalityContext blockContext = gameQueryService.createBlockLegalityContext(
                gameData, gameData.playerBattlefields.get(defenderId));
        return computeLegalBlockPairs(gameData, blockContext, blockerIndices, attackerIndices, defenderId, attackerId);
    }

    private Map<Integer, List<Integer>> computeLegalBlockPairs(GameData gameData,
                                                               BlockLegalityContext blockContext,
                                                               List<Integer> blockerIndices,
                                                               List<Integer> attackerIndices,
                                                               UUID defenderId,
                                                               UUID attackerId) {
        List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(defenderId);
        List<Permanent> attackerBattlefield = gameData.playerBattlefields.get(attackerId);
        Map<Integer, List<Integer>> pairs = new LinkedHashMap<>();
        for (int blockerIdx : blockerIndices) {
            Permanent blocker = defenderBattlefield.get(blockerIdx);
            List<Integer> legalAttackers = new ArrayList<>();
            for (int attackerIdx : attackerIndices) {
                Permanent attacker = attackerBattlefield.get(attackerIdx);
                if (gameQueryService.canBlockAttacker(blockContext, blocker, attacker)) {
                    legalAttackers.add(attackerIdx);
                }
            }
            pairs.put(blockerIdx, legalAttackers);
        }
        return pairs;
    }

    /**
     * Initiates the declare-blockers step. Sends available blockers and legal block pairs
     * to the defending player. Skips if no blockers or no blockable attackers exist.
     */
    /**
     * The attacking creature indices the defender can legally be asked to block: the active
     * player's attackers filtered by every "can't be blocked" condition. Used by both the
     * declare-blockers step and the blocker-declaration prompt (including reconnect replay).
     */
    public List<Integer> getBlockableAttackerIndices(GameData gameData, UUID activeId, UUID defenderId) {
        List<Integer> attackerIndices = combatAttackService.getAttackingCreatureIndices(gameData, activeId);
        List<Permanent> attackerBattlefield = gameData.playerBattlefields.get(activeId);
        List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(defenderId);
        return attackerIndices.stream()
                .filter(idx -> !gameQueryService.hasCantBeBlocked(gameData, attackerBattlefield.get(idx)))
                .filter(idx -> !CombatHelper.isCantBeBlockedDueToDefenderCondition(predicateEvaluationService, gameData, attackerBattlefield.get(idx), defenderBattlefield))
                .filter(idx -> !CombatHelper.isCantBeBlockedDueToHistoricCast(gameQueryService, gameData, attackerBattlefield.get(idx)))
                .filter(idx -> !CombatHelper.isCantBeBlockedDueToAttackingAlone(gameData, attackerBattlefield.get(idx)))
                .toList();
    }

    public CombatResult handleDeclareBlockersStep(GameData gameData) {
        UUID activeId = gameData.activePlayerId;
        UUID defenderId = gameQueryService.getOpponentId(gameData, activeId);
        List<Integer> blockable = getBlockableCreatureIndices(gameData, defenderId);
        List<Integer> attackerIndices = getBlockableAttackerIndices(gameData, activeId, defenderId);

        if (blockable.isEmpty() || attackerIndices.isEmpty()) {
            log.info("Game {} - Defending player has no creatures that can block or no blockable attackers", gameData.id);
            // No blocks are possible, so every attacking creature is unblocked: fire any
            // "attacks and isn't blocked" triggers before advancing to combat damage.
            List<Permanent> attackerBattlefield = gameData.playerBattlefields.get(activeId);
            List<Permanent> unblockedAttackers = new ArrayList<>();
            if (attackerBattlefield != null) {
                for (Permanent attacker : attackerBattlefield) {
                    if (attacker.isAttacking()) {
                        unblockedAttackers.add(attacker);
                    }
                }
            }
            collectUnblockedAttackTriggers(gameData, activeId, defenderId);
            checkUnblockedAttackerTriggers(gameData, activeId, unblockedAttackers);
            // CR 509.4: players still get priority during the declare blockers step even
            // when zero blocks were declared (e.g. the attacker may pump an unblocked
            // creature). AUTO_PASS_ONLY runs that priority round; when nobody can act,
            // auto-pass advances to combat damage exactly as the old direct advance did.
            return CombatResult.AUTO_PASS_ONLY;
        }

        interactionHandlerRegistry.begin(gameData, new PendingInteraction.BlockerDeclaration(defenderId));
        return CombatResult.DONE;
    }

    /**
     * Validates and processes a player's blocker declaration.
     */
    public CombatResult declareBlockers(GameData gameData, Player player, List<BlockerAssignment> blockerAssignments) {
        if (gameData.interaction.activeInteraction(PendingInteraction.BlockerDeclaration.class) == null) {
            throw new IllegalStateException("Not awaiting blocker declaration");
        }

        UUID activeId = gameData.activePlayerId;
        UUID defenderId = gameQueryService.getOpponentId(gameData, activeId);

        if (!player.getId().equals(defenderId)) {
            throw new IllegalStateException("Only the defending player can declare blockers");
        }

        List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(defenderId);
        List<Permanent> attackerBattlefield = gameData.playerBattlefields.get(activeId);
        List<Integer> blockable = getBlockableCreatureIndices(gameData, defenderId);

        // One shared legality context for the whole validation pass (no game-state mutation
        // happens until every check below has passed).
        BlockLegalityContext blockContext = gameQueryService.createBlockLegalityContext(gameData, defenderBattlefield);

        // Validate assignments
        int blockTaxTotal = 0;
        Map<Integer, Integer> blockerUsageCount = new HashMap<>();
        Set<String> blockerAttackerPairs = new HashSet<>();
        Map<Integer, Integer> blockersPerAttacker = new HashMap<>();
        for (BlockerAssignment assignment : blockerAssignments) {
            int blockerIdx = assignment.blockerIndex();
            int attackerIdx = assignment.attackerIndex();

            if (!blockable.contains(blockerIdx)) {
                throw new IllegalStateException("Invalid blocker index: " + blockerIdx);
            }
            int usageCount = blockerUsageCount.merge(blockerIdx, 1, Integer::sum);
            int maxBlocks = getMaxBlocksForCreature(gameData, defenderBattlefield.get(blockerIdx), defenderBattlefield);
            if (usageCount > maxBlocks) {
                throw new IllegalStateException("Blocker " + blockerIdx + " assigned too many times");
            }
            if (!blockerAttackerPairs.add(blockerIdx + ":" + attackerIdx)) {
                throw new IllegalStateException("Duplicate blocker-attacker pair: " + blockerIdx + " -> " + attackerIdx);
            }
            if (attackerIdx < 0 || attackerIdx >= attackerBattlefield.size() || !attackerBattlefield.get(attackerIdx).isAttacking()) {
                throw new IllegalStateException("Invalid attacker index: " + attackerIdx);
            }

            Permanent attacker = attackerBattlefield.get(attackerIdx);
            Permanent blocker = defenderBattlefield.get(blockerIdx);
            gameQueryService.getBlockingIllegalityReason(blockContext, blocker, attacker)
                    .ifPresent(reason -> { throw new IllegalStateException(reason); });

            // Additional cost to declare this block (e.g. Hipparion — {1} to block power 3+).
            blockTaxTotal += blockTaxFor(gameData, blocker, attacker);

            blockersPerAttacker.merge(attackerIdx, 1, Integer::sum);
        }

        // Team-wide "each creature you control can't be blocked by more than N creatures" (Yuan Shao).
        // All attackers belong to the active player, so scan that player's battlefield once.
        int teamMaxBlockers = Integer.MAX_VALUE;
        for (Permanent p : attackerBattlefield) {
            for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof EachControlledCreatureCanBeBlockedByAtMostNCreaturesEffect restriction) {
                    teamMaxBlockers = Math.min(teamMaxBlockers, restriction.maxBlockers());
                }
            }
        }

        for (var entry : blockersPerAttacker.entrySet()) {
            int attackerIdx = entry.getKey();
            int blockerCount = entry.getValue();
            Permanent attacker = attackerBattlefield.get(attackerIdx);
            if (gameQueryService.hasKeyword(gameData, attacker, Keyword.MENACE) && blockerCount == 1) {
                throw new IllegalStateException(attacker.getCard().getName() + " can't be blocked except by two or more creatures");
            }
            if (blockerCount > teamMaxBlockers) {
                throw new IllegalStateException(attacker.getCard().getName()
                        + " can't be blocked by more than " + teamMaxBlockers
                        + " creature" + (teamMaxBlockers == 1 ? "" : "s"));
            }
            for (CardEffect effect : attacker.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof CanBeBlockedByAtMostNCreaturesEffect restriction
                        && blockerCount > restriction.maxBlockers()) {
                    throw new IllegalStateException(attacker.getCard().getName()
                            + " can't be blocked by more than " + restriction.maxBlockers()
                            + " creature" + (restriction.maxBlockers() == 1 ? "" : "s"));
                }
                if (effect instanceof CantBeBlockedByFewerThanNCreaturesEffect restriction
                        && blockerCount < restriction.minBlockers()) {
                    throw new IllegalStateException(attacker.getCard().getName()
                            + " can't be blocked except by " + restriction.minBlockers() + " or more creatures");
                }
            }
        }

        // CR 509.1b: validate "can't block alone" — if any declared blocker has this restriction,
        // there must be at least 2 total blockers
        validateCantBlockAlone(defenderBattlefield, blockerAssignments);

        // Okk: "can't block unless a creature with greater power also blocks"
        validateGreaterPowerAlsoBlocks(gameData, defenderBattlefield, blockerAssignments);

        // Orcish Conscripts: "can't block unless at least N other creatures also block"
        validateCountAlsoBlocks(defenderBattlefield, blockerAssignments);

        validateMaximumBlockRequirements(gameData, blockContext, attackerBattlefield, defenderBattlefield, blockable,
                blockerAssignments);
        validatePerCreatureMustBlockRequirements(gameData, blockContext, attackerBattlefield, defenderBattlefield, blockable,
                blockerAssignments);
        validateMustBeBlockedIfAbleRequirements(gameData, blockContext, attackerBattlefield, defenderBattlefield, blockable,
                blockerAssignments);

        // Block tax (e.g. Hipparion): the block is legal only if its additional cost can be paid.
        if (blockTaxTotal > 0) {
            ManaPool pool = gameData.playerManaPools.get(defenderId);
            if (pool.getTotal() < blockTaxTotal) {
                throw new IllegalStateException("Not enough mana to pay block cost (" + blockTaxTotal + " required)");
            }
        }

        gameData.interaction.clearAwaitingInput();

        // Pay the block tax now that all validation has passed.
        if (blockTaxTotal > 0) {
            combatAttackService.payGenericMana(gameData.playerManaPools.get(defenderId), blockTaxTotal);
        }

        // Mark creatures as blocking
        for (BlockerAssignment assignment : blockerAssignments) {
            Permanent blocker = defenderBattlefield.get(assignment.blockerIndex());
            Permanent attacker = attackerBattlefield.get(assignment.attackerIndex());
            blocker.setBlocking(true);
            blocker.addBlockingTarget(assignment.attackerIndex());
            blocker.addBlockingTargetId(attacker.getId());
        }

        if (!blockerAssignments.isEmpty()) {
            String logEntry = player.getUsername() + " declares " + blockerAssignments.size() +
                    " blocker" + (blockerAssignments.size() > 1 ? "s" : "") + ".";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(logEntry));
        }

        // Collect all blocker-step triggers, then reorder per APNAP (CR 603.3b)
        int stackSizeBeforeBlockerTriggers = gameData.stack.size();

        // Check for "when this creature blocks" triggers (defending player's / NAP's)
        for (BlockerAssignment assignment : blockerAssignments) {
            Permanent blocker = defenderBattlefield.get(assignment.blockerIndex());
            if (!blocker.getCard().getEffects(EffectSlot.ON_BLOCK).isEmpty()) {
                Permanent attacker = attackerBattlefield.get(assignment.attackerIndex());

                // Resolve conditional block effects (e.g. "when blocking a creature with flying")
                List<CardEffect> blockEffects = new ArrayList<>();
                for (CardEffect e : blocker.getCard().getEffects(EffectSlot.ON_BLOCK)) {
                    if (e instanceof BoostSelfWhenBlockingKeywordEffect kwEffect) {
                        if (gameQueryService.hasKeyword(gameData, attacker, kwEffect.requiredKeyword())) {
                            blockEffects.add(new BoostSelfEffect(kwEffect.powerBoost(), kwEffect.toughnessBoost()));
                        }
                    } else if (e instanceof DestroyEquipmentOnEquippedCombatOpponentAtEndOfCombatEffect) {
                        if (hasEquipmentAttached(gameData, attacker)) {
                            blockEffects.add(e);
                        }
                    } else {
                        blockEffects.add(e);
                    }
                }
                if (blockEffects.isEmpty()) continue;

                // Targeted block triggers (e.g. Elite Javelineer's "deals 1 damage to target
                // attacking creature") let the controller choose any legal target rather than
                // referencing the blocked attacker. A card-level target filter is the discriminator;
                // route these through the shared attack-trigger targeting pipeline, which honours the
                // card's PermanentPredicateTargetFilter and drains via the pending-interaction queue.
                boolean targetsChosenPermanent = blocker.getCard().getTargetFilter() != null
                        && blockEffects.stream().anyMatch(e -> e.targetSpec().category().includesPermanents());
                if (targetsChosenPermanent) {
                    gameData.queueInteraction(new PermanentChoiceContext.AttackTriggerTarget(
                            blocker.getCard(), defenderId, new ArrayList<>(blockEffects), blocker.getId()));
                    String triggerLog = blocker.getCard().getName() + "'s block ability triggers.";
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.text(triggerLog));
                    log.info("Game {} - {} block trigger queued for target selection", gameData.id,
                            blocker.getCard().getName());
                    continue;
                }

                // Set target: attacker ID for effects that need it, otherwise blocker's own ID
                boolean needsAttackerTarget = blockEffects.stream()
                        .anyMatch(e -> e instanceof DestroyBlockedCreatureAndSelfEffect
                                || e instanceof DestroyTargetPermanentThenEffect
                                || (e instanceof SkipNextUntapEffect s && s.scope() == TapUntapScope.TARGET)
                                || e instanceof DealDamageToTargetCreatureEffect
                                || e instanceof DestroyCombatOpponentAtEndOfCombatEffect
                                || e instanceof CombatOpponentReferencingEffect
                                || e instanceof PutCounterOnCombatOpponentAtEndOfCombatEffect
                                || e instanceof DestroyEquipmentOnEquippedCombatOpponentAtEndOfCombatEffect
                                || (e instanceof GrantKeywordEffect gk && gk.scope() == GrantScope.TARGET));
                StackEntry blockTrigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        blocker.getCard(),
                        defenderId,
                        blocker.getCard().getName() + "'s block trigger",
                        new ArrayList<>(blockEffects),
                        needsAttackerTarget ? attacker.getId() : blocker.getId(),
                        blocker.getId()
                );
                // Block triggers reference "that creature" but don't target — they can't fizzle
                blockTrigger.setNonTargeting(true);
                gameData.stack.add(blockTrigger);
                String triggerLog = blocker.getCard().getName() + "'s block ability triggers.";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(triggerLog));
                log.info("Game {} - {} block trigger pushed onto stack", gameData.id, blocker.getCard().getName());
            }

            // Check for aura/equipment-based "when enchanted/equipped creature blocks" triggers
            Permanent blockerForAura = defenderBattlefield.get(assignment.blockerIndex());
            Permanent attackerForAura = attackerBattlefield.get(assignment.attackerIndex());
            combatTriggerService.checkAuraTriggersForCreature(gameData, blockerForAura, EffectSlot.ON_BLOCK, attackerForAura);
        }

        // Check for "whenever this creature blocks two or more creatures" triggers (fires once,
        // not per blocker assignment). Defending player's / NAP's triggers.
        Map<Integer, Long> blocksPerBlocker = blockerAssignments.stream()
                .collect(java.util.stream.Collectors.groupingBy(
                        BlockerAssignment::blockerIndex, java.util.stream.Collectors.counting()));
        for (Map.Entry<Integer, Long> entry : blocksPerBlocker.entrySet()) {
            if (entry.getValue() < 2) {
                continue;
            }
            Permanent blocker = defenderBattlefield.get(entry.getKey());
            List<CardEffect> multiBlockEffects = blocker.getCard().getEffects(EffectSlot.ON_BLOCKS_MULTIPLE_CREATURES);
            if (multiBlockEffects.isEmpty()) {
                continue;
            }
            StackEntry multiBlockTrigger = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    blocker.getCard(),
                    defenderId,
                    blocker.getCard().getName() + "'s multi-block trigger",
                    new ArrayList<>(multiBlockEffects),
                    blocker.getId(),
                    blocker.getId()
            );
            multiBlockTrigger.setNonTargeting(true);
            gameData.stack.add(multiBlockTrigger);
            String triggerLog = blocker.getCard().getName() + "'s block ability triggers.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(triggerLog));
            log.info("Game {} - {} multi-block trigger pushed onto stack", gameData.id, blocker.getCard().getName());
        }

        // Check for "when this creature becomes blocked" triggers (active player's / AP's)
        Set<Integer> blockedAttackerIndices = new LinkedHashSet<>();
        for (BlockerAssignment assignment : blockerAssignments) {
            blockedAttackerIndices.add(assignment.attackerIndex());
        }
        for (int atkIdx : blockedAttackerIndices) {
            Permanent attacker = attackerBattlefield.get(atkIdx);
            List<EffectRegistration> becomesBlockedRegs = attacker.getCard().getEffectRegistrations(EffectSlot.ON_BECOMES_BLOCKED);
            if (!becomesBlockedRegs.isEmpty()) {
                List<CardEffect> blockerSpecificEffects = becomesBlockedRegs.stream()
                        .filter(r -> r.triggerMode() == TriggerMode.PER_BLOCKER)
                        .map(EffectRegistration::effect)
                        .toList();
                List<CardEffect> regularEffects = becomesBlockedRegs.stream()
                        .filter(r -> r.triggerMode() != TriggerMode.PER_BLOCKER)
                        .map(EffectRegistration::effect)
                        .toList();

                if (!regularEffects.isEmpty()) {
                    gameData.stack.add(new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            attacker.getCard(),
                            activeId,
                            attacker.getCard().getName() + "'s becomes-blocked trigger",
                            new ArrayList<>(regularEffects),
                            attacker.getId(),
                            attacker.getId()
                    ));
                    String triggerLog = attacker.getCard().getName() + "'s becomes-blocked ability triggers.";
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.text(triggerLog));
                    log.info("Game {} - {} becomes-blocked trigger pushed onto stack", gameData.id, attacker.getCard().getName());
                }

                if (!blockerSpecificEffects.isEmpty()) {
                    for (BlockerAssignment assignment : blockerAssignments) {
                        if (assignment.attackerIndex() != atkIdx) {
                            continue;
                        }
                        Permanent blocker = defenderBattlefield.get(assignment.blockerIndex());

                        // Filter conditional per-blocker effects (e.g. "becomes blocked by an equipped creature")
                        List<CardEffect> filteredEffects = new ArrayList<>();
                        for (CardEffect e : blockerSpecificEffects) {
                            if (e instanceof DestroyEquipmentOnEquippedCombatOpponentAtEndOfCombatEffect) {
                                if (hasEquipmentAttached(gameData, blocker)) {
                                    filteredEffects.add(e);
                                }
                            } else {
                                filteredEffects.add(e);
                            }
                        }
                        if (filteredEffects.isEmpty()) continue;

                        StackEntry trigger = new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                attacker.getCard(),
                                activeId,
                                attacker.getCard().getName() + "'s becomes-blocked trigger",
                                new ArrayList<>(filteredEffects),
                                blocker.getId(),
                                attacker.getId()
                        );
                        // "That creature" wording references a blocker without targeting it.
                        trigger.setNonTargeting(true);
                        gameData.stack.add(trigger);
                        String triggerLog = attacker.getCard().getName() + "'s becomes-blocked ability triggers.";
                        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(triggerLog));
                        log.info("Game {} - {} becomes-blocked trigger pushed onto stack", gameData.id, attacker.getCard().getName());
                    }
                }
            }

            // Check for aura/equipment-based "when enchanted/equipped creature becomes blocked" triggers
            combatTriggerService.checkAuraTriggersForCreature(gameData, attacker, EffectSlot.ON_BECOMES_BLOCKED);
            combatTriggerService.checkAttachedPerBlockerTriggers(gameData, attacker, blockerAssignments, defenderBattlefield, atkIdx);

            // Check for "whenever a creature you control becomes blocked" triggers (active player's / AP's).
            checkAllyBecomesBlockedTriggers(gameData, activeId, attacker);
        }

        // "Whenever this creature attacks and isn't blocked" triggers (ON_ATTACKS_UNBLOCKED,
        // active player's / AP's) for every attacker that ended up unblocked after this declaration.
        collectUnblockedAttackTriggers(gameData, activeId, defenderId);

        // "Whenever a creature you control attacks and isn't blocked" triggers
        // (ON_ALLY_CREATURE_ATTACKS_UNBLOCKED, active player's / AP's). Fires once per attacker that
        // ended up with no blockers assigned.
        List<Permanent> unblockedAttackers = new ArrayList<>();
        for (int i = 0; i < attackerBattlefield.size(); i++) {
            Permanent attacker = attackerBattlefield.get(i);
            if (attacker.isAttacking() && !blockedAttackerIndices.contains(i)) {
                unblockedAttackers.add(attacker);
            }
        }
        checkUnblockedAttackerTriggers(gameData, activeId, unblockedAttackers);

        // APNAP: active player's triggers on bottom, non-active player's on top (resolves first)
        combatTriggerService.reorderTriggersAPNAP(gameData, stackSizeBeforeBlockerTriggers, activeId);

        log.info("Game {} - {} declares {} blockers", gameData.id, player.getUsername(), blockerAssignments.size());
        for (BlockerAssignment assignment : blockerAssignments) {
            Permanent blocker = defenderBattlefield.get(assignment.blockerIndex());
            Permanent attacker = attackerBattlefield.get(assignment.attackerIndex());
            int bp = gameQueryService.getEffectivePower(gameData, blocker);
            int bt = gameQueryService.getEffectiveToughness(gameData, blocker);
            List<String> kws = new ArrayList<>();
            for (Keyword kw : List.of(Keyword.FIRST_STRIKE, Keyword.DOUBLE_STRIKE, Keyword.DEATHTOUCH,
                    Keyword.FLYING, Keyword.REACH, Keyword.INDESTRUCTIBLE)) {
                if (gameQueryService.hasKeyword(gameData, blocker, kw)) kws.add(kw.name().toLowerCase());
            }
            log.info("Game {} -   Blocker [{}]: {} {}/{}{} blocks [{}]: {}", gameData.id,
                    assignment.blockerIndex(), blocker.getCard().getName(), bp, bt,
                    kws.isEmpty() ? "" : " (" + String.join(", ", kws) + ")",
                    assignment.attackerIndex(), attacker.getCard().getName());
        }

        return CombatResult.AUTO_PASS_ONLY;
    }

    /**
     * Builds the AvailableBlockersMessage with all blocking requirement metadata
     * (must-be-blocked, menace, per-blocker must-block).
     */
    public AvailableBlockersMessage buildAvailableBlockersMessage(GameData gameData,
                                                                   List<Integer> blockable,
                                                                   List<Integer> attackerIndices,
                                                                   UUID defenderId,
                                                                   UUID activeId) {
        List<Permanent> attackerBattlefield = gameData.playerBattlefields.get(activeId);
        List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(defenderId);

        BlockLegalityContext blockContext = gameQueryService.createBlockLegalityContext(gameData, defenderBattlefield);
        Map<Integer, List<Integer>> legalPairs = computeLegalBlockPairs(gameData, blockContext, blockable, attackerIndices, defenderId, activeId);

        // Compute "must be blocked if able" attacker indices
        List<Integer> mustBeBlockedIndices = new ArrayList<>();
        for (int idx : attackerIndices) {
            Permanent attacker = attackerBattlefield.get(idx);
            boolean mustBeBlocked = attacker.isMustBeBlockedThisTurn()
                    || attacker.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(MustBeBlockedIfAbleEffect.class::isInstance)
                    || gameQueryService.hasAuraWithEffect(gameData, attacker, MustBeBlockedIfAbleEffect.class);
            if (mustBeBlocked) {
                mustBeBlockedIndices.add(idx);
            }
        }

        // Compute menace attacker indices
        List<Integer> menaceIndices = new ArrayList<>();
        for (int idx : attackerIndices) {
            Permanent attacker = attackerBattlefield.get(idx);
            if (gameQueryService.hasKeyword(gameData, attacker, Keyword.MENACE)) {
                menaceIndices.add(idx);
            }
        }

        // Compute per-blocker must-block requirements (Provoke, MustBlockSource, etc.)
        Map<Integer, List<Integer>> mustBlockReqs = new LinkedHashMap<>();
        for (int blockerIdx : blockable) {
            Permanent blocker = defenderBattlefield.get(blockerIdx);
            if (blocker.getMustBlockIds().isEmpty()) continue;
            List<Integer> requiredAttackerIndices = new ArrayList<>();
            for (UUID mustBlockId : blocker.getMustBlockIds()) {
                for (int atkIdx : attackerIndices) {
                    Permanent attacker = attackerBattlefield.get(atkIdx);
                    if (attacker.getId().equals(mustBlockId)
                            && gameQueryService.canBlockAttacker(blockContext, blocker, attacker)) {
                        requiredAttackerIndices.add(atkIdx);
                    }
                }
            }
            if (!requiredAttackerIndices.isEmpty()) {
                mustBlockReqs.put(blockerIdx, requiredAttackerIndices);
            }
        }

        return new AvailableBlockersMessage(blockable, attackerIndices, legalPairs,
                mustBeBlockedIndices, menaceIndices, mustBlockReqs);
    }


    /**
     * Collects "whenever this creature attacks and isn't blocked" ({@code ON_ATTACKS_UNBLOCKED})
     * triggers for every attacking creature the active player controls that no creature is blocking.
     * Each trigger is the active player's; player-affecting effects (e.g. a discard) read the
     * defending player from the (non-targeting) {@code targetId}. Returns the number pushed.
     */
    private int collectUnblockedAttackTriggers(GameData gameData, UUID activeId, UUID defenderId) {
        List<Permanent> attackerBattlefield = gameData.playerBattlefields.get(activeId);
        if (attackerBattlefield == null) {
            return 0;
        }
        List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(defenderId);
        int pushed = 0;
        for (Permanent attacker : attackerBattlefield) {
            if (!attacker.isAttacking() || isBlocked(defenderBattlefield, attacker)) {
                continue;
            }
            List<CardEffect> effects = attacker.getCard().getEffects(EffectSlot.ON_ATTACKS_UNBLOCKED);
            if (!effects.isEmpty()) {
                StackEntry trigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        attacker.getCard(),
                        activeId,
                        attacker.getCard().getName() + "'s unblocked-attack trigger",
                        new ArrayList<>(effects),
                        defenderId,
                        attacker.getId());
                // "Defending player" is determined by the combat, not chosen — the trigger can't fizzle.
                trigger.setNonTargeting(true);
                gameData.stack.add(trigger);
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(attacker.getCard().getName()
                        + "'s unblocked-attack ability triggers."));
                log.info("Game {} - {} unblocked-attack trigger pushed onto stack", gameData.id, attacker.getCard().getName());
                pushed++;
            }
            // "Whenever enchanted creature attacks and isn't blocked" (aura) triggers on this attacker.
            pushed += collectEnchantedCreatureUnblockedTriggers(gameData, defenderId, attacker);
        }
        return pushed;
    }

    /**
     * Collects "whenever enchanted creature attacks and isn't blocked"
     * ({@link EffectSlot#ON_ENCHANTED_CREATURE_ATTACKS_UNBLOCKED}) triggers for every aura attached to
     * the given unblocked attacker. Like the attacker's own {@code ON_ATTACKS_UNBLOCKED} triggers, the
     * enchanted attacker is baked in as the non-targeting {@code sourcePermanentId} and the defending
     * player as the {@code targetId}; the trigger is the aura's controller's. Used by Cloak of Confusion.
     */
    private int collectEnchantedCreatureUnblockedTriggers(GameData gameData, UUID defenderId, Permanent attacker) {
        int[] pushed = {0};
        gameData.forEachPermanent((auraOwnerId, perm) -> {
            if (!perm.isAttached() || !attacker.getId().equals(perm.getAttachedTo())) {
                return;
            }
            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ENCHANTED_CREATURE_ATTACKS_UNBLOCKED);
            if (effects.isEmpty()) {
                return;
            }
            StackEntry trigger = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    perm.getCard(),
                    auraOwnerId,
                    perm.getCard().getName() + "'s unblocked-attack trigger",
                    new ArrayList<>(effects),
                    defenderId,
                    attacker.getId());
            // Enchanted attacker and defending player are determined by the combat — the trigger can't fizzle.
            trigger.setNonTargeting(true);
            gameData.stack.add(trigger);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(perm.getCard().getName()
                    + "'s ability triggers."));
            log.info("Game {} - {} enchanted-creature unblocked-attack trigger pushed onto stack (enchanted {})",
                    gameData.id, perm.getCard().getName(), attacker.getCard().getName());
            pushed[0]++;
        });
        return pushed[0];
    }

    /**
     * Collects "whenever a creature you control attacks and isn't blocked" triggers
     * ({@link EffectSlot#ON_ALLY_CREATURE_ATTACKS_UNBLOCKED}) for the active player, one per unblocked
     * attacking creature. {@link TriggeringCardConditionalEffect} filters by the unblocked creature.
     * The unblocked creature is set as the trigger's {@code sourcePermanentId} so self-scoped effects
     * (e.g. {@link BoostSelfEffect}) apply to "it". Returns the number of triggers pushed.
     */
    private int checkUnblockedAttackerTriggers(GameData gameData, UUID activeId, List<Permanent> unblockedAttackers) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(activeId);
        if (battlefield == null || unblockedAttackers.isEmpty()) {
            return 0;
        }
        int pushed = 0;
        for (Permanent attacker : unblockedAttackers) {
            for (Permanent perm : battlefield) {
                List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ALLY_CREATURE_ATTACKS_UNBLOCKED);
                if (effects.isEmpty()) continue;

                List<CardEffect> matchingEffects = new ArrayList<>();
                for (CardEffect effect : effects) {
                    if (effect instanceof TriggeringCardConditionalEffect conditional) {
                        if (!predicateEvaluationService.matchesCardPredicate(attacker.getCard(), conditional.predicate(),
                                null, gameData, activeId)) {
                            continue;
                        }
                        matchingEffects.add(conditional.wrapped());
                    } else {
                        matchingEffects.add(effect);
                    }
                }
                if (matchingEffects.isEmpty()) continue;

                StackEntry trigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        perm.getCard(),
                        activeId,
                        perm.getCard().getName() + "'s unblocked-attacker trigger",
                        matchingEffects,
                        attacker.getId(),
                        attacker.getId()
                );
                // "It" references the unblocked creature without targeting it — can't fizzle.
                trigger.setNonTargeting(true);
                gameData.stack.add(trigger);
                pushed++;
                String triggerLog = perm.getCard().getName() + "'s ability triggers.";
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(triggerLog));
                log.info("Game {} - {} ON_ALLY_CREATURE_ATTACKS_UNBLOCKED trigger for {} unblocked",
                        gameData.id, perm.getCard().getName(), attacker.getCard().getName());
            }
        }
        return pushed;
    }

    /**
     * Returns {@code true} if any creature on the defending battlefield is blocking the given attacker.
     */
    private boolean isBlocked(List<Permanent> defenderBattlefield, Permanent attacker) {
        if (defenderBattlefield == null) {
            return false;
        }
        for (Permanent blocker : defenderBattlefield) {
            if (blocker.isBlocking() && blocker.getBlockingTargetIds().contains(attacker.getId())) {
                return true;
            }
        }
        return false;
    }

    /**
     * Fires ON_ALLY_CREATURE_BECOMES_BLOCKED triggers for a single blocked attacker. Scans every
     * permanent with this slot on the blocked creature's controller's battlefield (not just the
     * blocked creature itself). "It" references the blocked creature via the non-targeting
     * sourcePermanentId, so self-scoped effects like {@code BoostSelfEffect} apply to it.
     */
    private void checkAllyBecomesBlockedTriggers(GameData gameData, UUID activeId, Permanent blockedAttacker) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(activeId);
        if (battlefield == null) {
            return;
        }
        for (Permanent perm : battlefield) {
            List<CardEffect> effects = perm.getCard().getEffects(EffectSlot.ON_ALLY_CREATURE_BECOMES_BLOCKED);
            if (effects.isEmpty()) continue;

            List<CardEffect> matchingEffects = new ArrayList<>();
            for (CardEffect effect : effects) {
                if (effect instanceof TriggeringCardConditionalEffect conditional) {
                    if (!predicateEvaluationService.matchesCardPredicate(blockedAttacker.getCard(), conditional.predicate(),
                            null, gameData, activeId)) {
                        continue;
                    }
                    matchingEffects.add(conditional.wrapped());
                } else {
                    matchingEffects.add(effect);
                }
            }
            if (matchingEffects.isEmpty()) continue;

            StackEntry trigger = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    perm.getCard(),
                    activeId,
                    perm.getCard().getName() + "'s becomes-blocked trigger",
                    matchingEffects,
                    blockedAttacker.getId(),
                    blockedAttacker.getId()
            );
            // "It" references the blocked creature without targeting it — can't fizzle.
            trigger.setNonTargeting(true);
            gameData.stack.add(trigger);
            String triggerLog = perm.getCard().getName() + "'s ability triggers.";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(triggerLog));
            log.info("Game {} - {} ON_ALLY_CREATURE_BECOMES_BLOCKED trigger for {} blocked",
                    gameData.id, perm.getCard().getName(), blockedAttacker.getCard().getName());
        }
    }

    private int getMaxBlocksForCreature(GameData gameData, Permanent creature, List<Permanent> battlefield) {
        // Check for "can block any number of creatures" on the creature itself
        for (CardEffect effect : creature.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof CanBlockAnyNumberOfCreaturesEffect) {
                return Integer.MAX_VALUE;
            }
        }

        int additionalBlocks = 0;
        for (Permanent p : battlefield) {
            for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof GrantAdditionalBlockEffect e) {
                    if (e.controlledFilter() != null) {
                        // Grant applies to each of the source controller's permanents matching the filter.
                        FilterContext ctx = FilterContext.of(gameData)
                                .withSourceCardId(p.getCard().getId())
                                .withSourceControllerId(gameQueryService.findPermanentController(gameData, p.getId()));
                        if (predicateEvaluationService.matchesPermanentPredicate(creature, e.controlledFilter(), ctx)) {
                            additionalBlocks += e.additionalBlocks();
                        }
                        continue;
                    }
                    boolean isAttachable = p.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)
                            || p.getCard().isAura();
                    if (isAttachable) {
                        if (creature.getId().equals(p.getAttachedTo())) {
                            additionalBlocks += e.additionalBlocks();
                        }
                    } else if (p.getCard().hasType(CardType.CREATURE)) {
                        // Creature with "can block an additional creature" — self-only
                        if (p.getId().equals(creature.getId())) {
                            additionalBlocks += e.additionalBlocks();
                        }
                    } else {
                        // Non-creature, non-attachable (e.g. enchantment) — global effect
                        additionalBlocks += e.additionalBlocks();
                    }
                } else if (effect instanceof GrantAdditionalBlockPerEquipmentEffect
                        && p.getId().equals(creature.getId())) {
                    for (Permanent eq : battlefield) {
                        if (eq.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)
                                && creature.getId().equals(eq.getAttachedTo())) {
                            additionalBlocks++;
                        }
                    }
                }
            }
        }
        return 1 + additionalBlocks;
    }

    private void validateMaximumBlockRequirements(GameData gameData,
                                                   BlockLegalityContext blockContext,
                                                   List<Permanent> attackerBattlefield,
                                                   List<Permanent> defenderBattlefield,
                                                   List<Integer> blockable,
                                                   List<BlockerAssignment> blockerAssignments) {
        Set<Integer> lureAttackerIndices = new HashSet<>();
        for (int i = 0; i < attackerBattlefield.size(); i++) {
            Permanent attacker = attackerBattlefield.get(i);
            if (!attacker.isAttacking()) continue;
            boolean hasRequirement = attacker.isMustBeBlockedByAllThisTurn()
                    || attacker.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(MustBeBlockedByAllCreaturesEffect.class::isInstance)
                    || gameQueryService.hasAuraWithEffect(gameData, attacker, MustBeBlockedByAllCreaturesEffect.class);
            if (hasRequirement) {
                lureAttackerIndices.add(i);
            }
        }
        if (lureAttackerIndices.isEmpty()) {
            return;
        }

        for (int blockerIdx : blockable) {
            Permanent blocker = defenderBattlefield.get(blockerIdx);
            int currentLureBlocks = 0;
            for (BlockerAssignment assignment : blockerAssignments) {
                if (assignment.blockerIndex() == blockerIdx && lureAttackerIndices.contains(assignment.attackerIndex())) {
                    currentLureBlocks++;
                }
            }

            int possibleLureBlocks = 0;
            for (int attackerIdx : lureAttackerIndices) {
                Permanent attacker = attackerBattlefield.get(attackerIdx);
                if (gameQueryService.canBlockAttacker(blockContext, blocker, attacker)) {
                    possibleLureBlocks++;
                }
            }

            int maxSatisfiable = Math.min(getMaxBlocksForCreature(gameData, blocker, defenderBattlefield), possibleLureBlocks);
            if (currentLureBlocks < maxSatisfiable) {
                throw new IllegalStateException(blocker.getCard().getName() + " must block enchanted creature if able");
            }
        }
    }

    private void validatePerCreatureMustBlockRequirements(GameData gameData,
                                                           BlockLegalityContext blockContext,
                                                           List<Permanent> attackerBattlefield,
                                                           List<Permanent> defenderBattlefield,
                                                           List<Integer> blockable,
                                                           List<BlockerAssignment> blockerAssignments) {
        for (int blockerIdx : blockable) {
            Permanent blocker = defenderBattlefield.get(blockerIdx);
            if (blocker.getMustBlockIds().isEmpty()) {
                continue;
            }

            Set<Integer> requiredAttackerIndices = new HashSet<>();
            for (UUID mustBlockId : blocker.getMustBlockIds()) {
                for (int i = 0; i < attackerBattlefield.size(); i++) {
                    Permanent attacker = attackerBattlefield.get(i);
                    if (attacker.isAttacking() && attacker.getId().equals(mustBlockId)
                            && gameQueryService.canBlockAttacker(blockContext, blocker, attacker)) {
                        requiredAttackerIndices.add(i);
                    }
                }
            }

            if (requiredAttackerIndices.isEmpty()) {
                continue;
            }

            int currentMustBlocks = 0;
            for (BlockerAssignment assignment : blockerAssignments) {
                if (assignment.blockerIndex() == blockerIdx && requiredAttackerIndices.contains(assignment.attackerIndex())) {
                    currentMustBlocks++;
                }
            }

            int maxSatisfiable = Math.min(getMaxBlocksForCreature(gameData, blocker, defenderBattlefield), requiredAttackerIndices.size());
            if (currentMustBlocks < maxSatisfiable) {
                throw new IllegalStateException(blocker.getCard().getName() + " must block target creature this turn if able");
            }
        }
    }

    private void validateMustBeBlockedIfAbleRequirements(GameData gameData,
                                                          BlockLegalityContext blockContext,
                                                          List<Permanent> attackerBattlefield,
                                                          List<Permanent> defenderBattlefield,
                                                          List<Integer> blockable,
                                                          List<BlockerAssignment> blockerAssignments) {
        // Find all attacking creatures with "must be blocked if able" (at least one blocker required)
        Set<Integer> mustBeBlockedAttackerIndices = new HashSet<>();
        for (int i = 0; i < attackerBattlefield.size(); i++) {
            Permanent attacker = attackerBattlefield.get(i);
            if (!attacker.isAttacking()) continue;
            boolean hasRequirement = attacker.isMustBeBlockedThisTurn()
                    || attacker.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(MustBeBlockedIfAbleEffect.class::isInstance)
                    || gameQueryService.hasAuraWithEffect(gameData, attacker, MustBeBlockedIfAbleEffect.class);
            if (hasRequirement) {
                mustBeBlockedAttackerIndices.add(i);
            }
        }
        if (mustBeBlockedAttackerIndices.isEmpty()) {
            return;
        }

        // Collect which blockers are already assigned and which are free
        Set<Integer> assignedBlockerIndices = new HashSet<>();
        Set<Integer> blockedAttackerIndices = new HashSet<>();
        for (BlockerAssignment assignment : blockerAssignments) {
            assignedBlockerIndices.add(assignment.blockerIndex());
            blockedAttackerIndices.add(assignment.attackerIndex());
        }

        // For each "must be blocked if able" attacker that is NOT currently blocked,
        // check if there is a free blocker that could have blocked it
        for (int attackerIdx : mustBeBlockedAttackerIndices) {
            if (blockedAttackerIndices.contains(attackerIdx)) {
                continue; // already blocked by at least one creature
            }
            Permanent attacker = attackerBattlefield.get(attackerIdx);
            for (int blockerIdx : blockable) {
                if (assignedBlockerIndices.contains(blockerIdx)) continue;
                Permanent blocker = defenderBattlefield.get(blockerIdx);
                if (gameQueryService.canBlockAttacker(blockContext, blocker, attacker)) {
                    throw new IllegalStateException(attacker.getCard().getName()
                            + " must be blocked if able");
                }
            }
        }
    }

    private void validateCantBlockAlone(List<Permanent> defenderBattlefield,
                                         List<BlockerAssignment> blockerAssignments) {
        if (blockerAssignments.isEmpty()) return;
        Set<Integer> uniqueBlockerIndices = new HashSet<>();
        for (BlockerAssignment assignment : blockerAssignments) {
            uniqueBlockerIndices.add(assignment.blockerIndex());
        }
        if (uniqueBlockerIndices.size() == 1) {
            int soleIdx = uniqueBlockerIndices.iterator().next();
            Permanent sole = defenderBattlefield.get(soleIdx);
            if (hasCantAttackOrBlockAlone(sole)) {
                throw new IllegalStateException(sole.getCard().getName() + " can't block alone");
            }
        }
    }

    /**
     * Additional generic mana the blocker's controller must pay to declare this block, summed over every
     * {@link BlockCostEffect} the blocker carries against the attacker's effective power (e.g. Hipparion —
     * {1} to block a creature with power 3 or greater).
     */
    private int blockTaxFor(GameData gameData, Permanent blocker, Permanent attacker) {
        int attackerPower = gameQueryService.getEffectivePower(gameData, attacker);
        int tax = 0;
        for (CardEffect effect : blocker.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof BlockCostEffect blockCost) {
                tax += blockCost.blockCost(attackerPower);
            }
        }
        return tax;
    }

    private boolean hasCantAttackOrBlockAlone(Permanent creature) {
        return creature.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(CantAttackOrBlockAloneEffect.class::isInstance);
    }

    /**
     * Okk (CR 509.1a): a creature with "can't block unless a creature with greater power also
     * blocks" may only be declared as a blocker if another declared blocker has strictly greater
     * power. The comparison is checked only at declaration time.
     */
    private void validateGreaterPowerAlsoBlocks(GameData gameData, List<Permanent> defenderBattlefield,
                                                List<BlockerAssignment> blockerAssignments) {
        Set<Integer> uniqueBlockerIndices = new HashSet<>();
        for (BlockerAssignment assignment : blockerAssignments) {
            uniqueBlockerIndices.add(assignment.blockerIndex());
        }
        for (int idx : uniqueBlockerIndices) {
            Permanent restricted = defenderBattlefield.get(idx);
            boolean hasRestriction = restricted.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(CantAttackOrBlockUnlessGreaterPowerAlsoDoesEffect.class::isInstance);
            if (!hasRestriction) {
                continue;
            }
            int power = gameQueryService.getEffectivePower(gameData, restricted);
            boolean greaterPowerAlsoBlocks = uniqueBlockerIndices.stream()
                    .filter(other -> other != idx)
                    .map(defenderBattlefield::get)
                    .anyMatch(other -> gameQueryService.getEffectivePower(gameData, other) > power);
            if (!greaterPowerAlsoBlocks) {
                throw new IllegalStateException(restricted.getCard().getName()
                        + " can't block unless a creature with greater power also blocks");
            }
        }
    }

    /**
     * Orcish Conscripts (CR 509.1b): a creature with "can't block unless at least N other creatures
     * also block" may only be declared as a blocker if at least N other creatures are declared as
     * blockers in the same combat. Checked only at declaration time.
     */
    private void validateCountAlsoBlocks(List<Permanent> defenderBattlefield,
                                         List<BlockerAssignment> blockerAssignments) {
        Set<Integer> uniqueBlockerIndices = new HashSet<>();
        for (BlockerAssignment assignment : blockerAssignments) {
            uniqueBlockerIndices.add(assignment.blockerIndex());
        }
        for (int idx : uniqueBlockerIndices) {
            Permanent restricted = defenderBattlefield.get(idx);
            restricted.getCard().getEffects(EffectSlot.STATIC).stream()
                    .filter(CantAttackOrBlockUnlessCountAlsoDoesEffect.class::isInstance)
                    .map(CantAttackOrBlockUnlessCountAlsoDoesEffect.class::cast)
                    .findFirst()
                    .ifPresent(effect -> {
                        long otherBlockers = uniqueBlockerIndices.stream().filter(other -> other != idx).count();
                        if (otherBlockers < effect.otherCount()) {
                            throw new IllegalStateException(restricted.getCard().getName()
                                    + " can't block unless at least " + effect.otherCount()
                                    + " other creatures block");
                        }
                    });
        }
    }

    /**
     * Returns {@code true} if the given creature has at least one Equipment attached to it.
     */
    private boolean hasEquipmentAttached(GameData gameData, Permanent creature) {
        boolean[] found = {false};
        gameData.forEachPermanent((ownerId, p) -> {
            if (!found[0] && creature.getId().equals(p.getAttachedTo())
                    && p.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)) {
                found[0] = true;
            }
        });
        return found[0];
    }

}
