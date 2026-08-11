package com.github.laxika.magicalvibes.service.combat.block;

import com.github.laxika.magicalvibes.service.GameLogService;

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
import com.github.laxika.magicalvibes.model.effect.BlockPairConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.BlockParticipant;
import com.github.laxika.magicalvibes.model.effect.BlockedCreatureTriggerEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfWhenBlockingKeywordEffect;
import com.github.laxika.magicalvibes.model.action.DelayedBlockerBoost;
import com.github.laxika.magicalvibes.model.action.DelayedBlockerDeclarationControl;
import com.github.laxika.magicalvibes.model.action.DelayedUnblockedAttackerPowerDamage;
import com.github.laxika.magicalvibes.model.action.DelayedUnblockedAttackerUntapRemoveFromCombat;
import com.github.laxika.magicalvibes.model.effect.RemoveTargetFromCombatEffect;
import com.github.laxika.magicalvibes.model.effect.TargetPredicate;
import com.github.laxika.magicalvibes.model.effect.UntapPermanentsEffect;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.AssignNoCombatDamageEffect;
import com.github.laxika.magicalvibes.model.effect.BlockerDeclarationControlEffect;
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
import com.github.laxika.magicalvibes.model.effect.GraveyardCardChoosingEffect;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalBlockEffect;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalBlockPerEquipmentEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.GlobalMustBlockEachCombatEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutCounterOnCombatOpponentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedIfAbleEffect;
import com.github.laxika.magicalvibes.model.effect.MustBlockEachCombatEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapEffect;
import com.github.laxika.magicalvibes.model.effect.TapUntapScope;
import com.github.laxika.magicalvibes.model.effect.TriggeringCardConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.GraveyardTargetingService;
import com.github.laxika.magicalvibes.service.combat.attack.CombatAttackService;
import com.github.laxika.magicalvibes.service.combat.CombatHelper;
import com.github.laxika.magicalvibes.service.combat.CombatResult;
import com.github.laxika.magicalvibes.service.combat.CombatTriggerService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.effect.staticfx.StaticEffectConditionResolver;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

/**
 * Handles declare-blockers step: computing legal blockers, validating blocker assignments
 * (evasion, menace, max-blockers, must-block), and collecting ON_BLOCK / ON_BECOMES_BLOCKED triggers.
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class CombatBlockService {

    private final GameQueryService gameQueryService;
    private final BlockLegalityService blockLegalityService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameLogService gameLogService;
    private final CombatAttackService combatAttackService;
    private final CombatTriggerService combatTriggerService;
    private final InteractionHandlerRegistry interactionHandlerRegistry;
    private final GraveyardTargetingService graveyardTargetingService;
    private final StaticEffectConditionResolver staticEffectConditionResolver;

    /**
     * Returns the battlefield indices of creatures the given player can legally declare as blockers.
     */
    public List<Integer> getBlockableCreatureIndices(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return List.of();
        BlockLegalityContext blockContext = blockLegalityService.createBlockLegalityContext(gameData, battlefield);
        List<Integer> indices = new ArrayList<>();
        for (int i = 0; i < battlefield.size(); i++) {
            if (blockLegalityService.canBlock(blockContext, battlefield.get(i))) {
                indices.add(i);
            }
        }
        // CR 509.1a: if only one creature can block and it has "can't block alone", remove it
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
        BlockLegalityContext blockContext = blockLegalityService.createBlockLegalityContext(
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
                if (blockLegalityService.canBlockAttacker(blockContext, blocker, attacker)) {
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
            processDelayedUnblockedAttackerPowerDamageTriggers(gameData, activeId, unblockedAttackers);
            processDelayedUnblockedAttackerUntapRemoveTriggers(gameData, unblockedAttackers);
            // CR 509.4: players still get priority during the declare blockers step even
            // when zero blocks were declared (e.g. the attacker may pump an unblocked
            // creature). AUTO_PASS_ONLY runs that priority round; when nobody can act,
            // auto-pass advances to combat damage exactly as the old direct advance did.
            return CombatResult.AUTO_PASS_ONLY;
        }

        interactionHandlerRegistry.begin(gameData, buildBlockerDeclaration(
                gameData, blockable, attackerIndices, defenderId, activeId,
                blockerDeclarationChooser(gameData, defenderId)));
        return CombatResult.DONE;
    }

    /**
     * Validates and processes a player's blocker declaration.
     */
    public CombatResult declareBlockers(GameData gameData, Player player, List<BlockerAssignment> blockerAssignments) {
        PendingInteraction.BlockerDeclaration pending =
                gameData.interaction.activeInteraction(PendingInteraction.BlockerDeclaration.class);
        if (pending == null) {
            throw new IllegalStateException("Not awaiting blocker declaration");
        }

        UUID activeId = gameData.activePlayerId;
        UUID defenderId = gameQueryService.getOpponentId(gameData, activeId);

        // Normally the defending player declares; Melee hands the declaration to its controller.
        if (!player.getId().equals(pending.chooserId())) {
            throw new IllegalStateException(pending.choosingForOpponent()
                    ? "Only the player choosing blockers can declare blockers"
                    : "Only the defending player can declare blockers");
        }

        List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(defenderId);
        List<Permanent> attackerBattlefield = gameData.playerBattlefields.get(activeId);
        List<Integer> blockable = getBlockableCreatureIndices(gameData, defenderId);

        // One shared legality context for the whole validation pass (no game-state mutation
        // happens until every check below has passed).
        BlockLegalityContext blockContext = blockLegalityService.createBlockLegalityContext(gameData, defenderBattlefield);

        // Validate assignments
        int blockTaxTotal = 0;
        Map<UUID, Integer> blockLifeTaxByBlocker = new HashMap<>();
        Map<UUID, Integer> globalBlockManaTaxByBlocker = new HashMap<>();
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
            blockLegalityService.getBlockingIllegalityReason(blockContext, blocker, attacker)
                    .ifPresent(reason -> { throw new IllegalStateException(reason); });

            // Additional cost to declare this block (e.g. Hipparion — {1} to block power 3+).
            blockTaxTotal += gameQueryService.getBlockManaTax(gameData, blocker, attacker);

            // Board-wide mana tax to block at all (Archangel of Tithes): once per unique blocker,
            // however many attackers it blocks.
            globalBlockManaTaxByBlocker.computeIfAbsent(blocker.getId(),
                    ignored -> gameQueryService.getGlobalBlockManaTax(gameData, blocker));

            // Board-wide life tax (Heat Wave): once per unique qualifying blocker.
            int lifeTax = gameQueryService.getGlobalBlockLifeTax(gameData, blocker, attacker);
            if (lifeTax > 0) {
                blockLifeTaxByBlocker.merge(blocker.getId(), lifeTax, Math::max);
            }

            blockersPerAttacker.merge(attackerIdx, 1, Integer::sum);
        }
        int blockLifeTaxTotal = blockLifeTaxByBlocker.values().stream().mapToInt(Integer::intValue).sum();
        blockTaxTotal += globalBlockManaTaxByBlocker.values().stream().mapToInt(Integer::intValue).sum();

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
            int maxBlockers = gameQueryService.getMaxBlockersAllowed(gameData, attacker);
            if (blockerCount > maxBlockers) {
                throw new IllegalStateException(attacker.getCard().getName()
                        + " can't be blocked by more than " + maxBlockers
                        + " creature" + (maxBlockers == 1 ? "" : "s"));
            }
            for (CardEffect effect : attacker.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof CantBeBlockedByFewerThanNCreaturesEffect restriction
                        && blockerCount < restriction.minBlockers()) {
                    throw new IllegalStateException(attacker.getCard().getName()
                            + " can't be blocked except by " + restriction.minBlockers() + " or more creatures");
                }
            }
        }

        // CR 509.1a: validate "can't block alone" — if any declared blocker has this restriction,
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
        validateMustBlockIfAbleRequirements(gameData, blockContext, attackerBattlefield, defenderBattlefield, blockable,
                blockerAssignments);

        // Block tax (e.g. Hipparion): the block is legal only if its additional cost can be paid.
        if (blockTaxTotal > 0) {
            ManaPool pool = gameData.playerManaPools.get(defenderId);
            if (pool.getTotal() < blockTaxTotal) {
                throw new IllegalStateException("Not enough mana to pay block cost (" + blockTaxTotal + " required)");
            }
        }
        if (blockLifeTaxTotal > 0) {
            if (!gameQueryService.canPlayerLifeChange(gameData, defenderId)) {
                throw new IllegalStateException("Life total can't change to pay block life cost");
            }
            int currentLife = gameData.playerLifeTotals.getOrDefault(defenderId, 0);
            if (currentLife < blockLifeTaxTotal) {
                throw new IllegalStateException("Not enough life to pay block cost ("
                        + blockLifeTaxTotal + " required)");
            }
        }

        gameData.interaction.clearAwaitingInput();

        // Pay the block tax now that all validation has passed.
        if (blockTaxTotal > 0) {
            combatAttackService.payGenericMana(gameData.playerManaPools.get(defenderId), blockTaxTotal);
        }
        if (blockLifeTaxTotal > 0) {
            int currentLife = gameData.playerLifeTotals.get(defenderId);
            gameData.playerLifeTotals.put(defenderId, currentLife - blockLifeTaxTotal);
            gameData.lifeLostThisTurn.merge(defenderId, blockLifeTaxTotal, Integer::sum);
            gameLogService.append(gameData, GameLog.text(
                    player.getUsername() + " pays " + blockLifeTaxTotal + " life to declare blockers."));
        }

        // Mark creatures as blocking, and record turn-scoped combat-block opponent subtypes so
        // "target creature that blocked or was blocked by a [subtype] this turn" spells (Time to
        // Reflect) can find their targets even after combat ends.
        for (BlockerAssignment assignment : blockerAssignments) {
            Permanent blocker = defenderBattlefield.get(assignment.blockerIndex());
            Permanent attacker = attackerBattlefield.get(assignment.attackerIndex());
            blocker.setBlocking(true);
            blocker.setBlockedThisTurn(true);
            blocker.addBlockingTarget(assignment.attackerIndex());
            blocker.addBlockingTargetId(attacker.getId());
            recordCombatBlockOpponentSubtypes(gameData, blocker, attacker);
        }

        // CR 702.22h: when a blocker blocks one member of an attacking band, every other creature in
        // that band becomes blocked by that same blocker (even one it couldn't otherwise block, e.g.
        // a flyer). Applied after validation, so these consequential blocks don't count against
        // max-blocks or menace.
        applyBandSharedBlocking(gameData, attackerBattlefield, defenderBattlefield, blockerAssignments);

        if (!blockerAssignments.isEmpty()) {
            String logEntry = player.getUsername() + " declares " + blockerAssignments.size() +
                    " blocker" + (blockerAssignments.size() > 1 ? "s" : "") + ".";
            gameLogService.append(gameData, GameLog.text(logEntry));
        }

        // Collect all blocker-step triggers, then reorder per APNAP (CR 603.3b)
        int stackSizeBeforeBlockerTriggers = gameData.stack.size();
        Set<Integer> blockersWithOncePerBlockTrigger = new HashSet<>();

        // Check for "when this creature blocks" triggers (defending player's / NAP's)
        for (BlockerAssignment assignment : blockerAssignments) {
            Permanent blocker = defenderBattlefield.get(assignment.blockerIndex());
            List<CardEffect> blockEffects = new ArrayList<>(blocker.getCard().getEffects(EffectSlot.ON_BLOCK));
            blockEffects.addAll(blocker.getTemporaryTriggeredEffects(EffectSlot.ON_BLOCK));
            blockEffects.addAll(blocker.getPersistentTriggeredEffects(EffectSlot.ON_BLOCK));
            boolean hasOncePerBlockEffect = blocker.getCard().getEffectRegistrations(EffectSlot.ON_BLOCK).stream()
                    .anyMatch(registration -> registration.triggerMode() == TriggerMode.ONCE_PER_BLOCK);
            boolean collectBlockTrigger = !hasOncePerBlockEffect
                    || blockersWithOncePerBlockTrigger.add(assignment.blockerIndex());
            if (collectBlockTrigger && !blockEffects.isEmpty()) {
                Permanent attacker = attackerBattlefield.get(assignment.attackerIndex());

                // Resolve conditional block effects (e.g. "when blocking a creature with flying")
                List<CardEffect> resolvedBlockEffects = new ArrayList<>();
                for (CardEffect e : blockEffects) {
                    if (e instanceof BoostSelfWhenBlockingKeywordEffect kwEffect) {
                        if (gameQueryService.hasKeyword(gameData, attacker, kwEffect.requiredKeyword())) {
                            resolvedBlockEffects.add(new BoostSelfEffect(kwEffect.powerBoost(), kwEffect.toughnessBoost()));
                        }
                    } else if (e instanceof DestroyEquipmentOnEquippedCombatOpponentAtEndOfCombatEffect) {
                        if (hasEquipmentAttached(gameData, attacker)) {
                            resolvedBlockEffects.add(e);
                        }
                    } else {
                        resolvedBlockEffects.add(e);
                    }
                }
                if (resolvedBlockEffects.isEmpty()) continue;

                // Targeted block triggers (e.g. Elite Javelineer's "deals 1 damage to target
                // attacking creature") let the controller choose any legal target rather than
                // referencing the blocked attacker. A card-level target filter is the discriminator;
                // route these through the shared attack-trigger targeting pipeline, which honours the
                // card's PermanentPredicateTargetFilter and drains via the pending-interaction queue.
                boolean targetsChosenPermanent = blocker.getCard().getTargetFilter() != null
                        && resolvedBlockEffects.stream().anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.PERMANENT));
                if (targetsChosenPermanent) {
                    gameData.queueInteraction(new PermanentChoiceContext.AttackTriggerTarget(
                            blocker.getCard(), defenderId, new ArrayList<>(resolvedBlockEffects), blocker.getId()));
                    gameLogService.append(gameData, GameLog.cardThen(blocker.getCard(),
                            "'s block ability triggers."));
                    log.info("Game {} - {} block trigger queued for target selection", gameData.id,
                            blocker.getCard().getName());
                    continue;
                }

                // Set target: attacker ID for effects that need it, otherwise blocker's own ID
                boolean needsAttackerTarget = resolvedBlockEffects.stream()
                        .anyMatch(e -> e instanceof DestroyBlockedCreatureAndSelfEffect
                                || e instanceof DestroyTargetPermanentThenEffect
                                || (e instanceof SkipNextUntapEffect s && s.scope() == TapUntapScope.TARGET)
                                || e instanceof DealDamageToTargetCreatureEffect
                                || e instanceof DestroyCombatOpponentAtEndOfCombatEffect
                                || (e instanceof CombatOpponentReferencingEffect c && c.referencesCombatOpponent())
                                || e instanceof PutCounterOnCombatOpponentAtEndOfCombatEffect
                                || e instanceof DestroyEquipmentOnEquippedCombatOpponentAtEndOfCombatEffect
                                || (e instanceof GrantKeywordEffect gk && gk.scope() == GrantScope.TARGET));
                StackEntry blockTrigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        blocker.getCard(),
                        defenderId,
                        blocker.getCard().getName() + "'s block trigger",
                        new ArrayList<>(resolvedBlockEffects),
                        needsAttackerTarget ? attacker.getId() : blocker.getId(),
                        blocker.getId()
                );
                // Block triggers reference "that creature" but don't target — they can't fizzle
                blockTrigger.setNonTargeting(true);
                gameData.stack.add(blockTrigger);
                gameLogService.append(gameData, GameLog.cardThen(blocker.getCard(),
                        "'s block ability triggers."));
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
            gameLogService.append(gameData, GameLog.cardThen(blocker.getCard(),
                    "'s block ability triggers."));
            log.info("Game {} - {} multi-block trigger pushed onto stack", gameData.id, blocker.getCard().getName());
        }

        // Global "whenever a creature blocks" watchers, once per blocking creature.
        checkAnyCreatureBlocksTriggers(gameData, defenderBattlefield, blockerAssignments);

        // Check for "when this creature becomes blocked" triggers (active player's / AP's)
        Set<Integer> blockedAttackerIndices = new LinkedHashSet<>();
        for (BlockerAssignment assignment : blockerAssignments) {
            blockedAttackerIndices.add(assignment.attackerIndex());
        }
        // CR 702.22h: a band-mate of any blocked attacker also became blocked.
        addBandMatesOfBlockedAttackers(attackerBattlefield, blockedAttackerIndices);
        for (int atkIdx : blockedAttackerIndices) {
            Permanent attacker = attackerBattlefield.get(atkIdx);
            List<EffectRegistration> becomesBlockedRegs = attacker.getCard().getEffectRegistrations(EffectSlot.ON_BECOMES_BLOCKED);
            List<CardEffect> grantedBecomesBlockedEffects = new ArrayList<>(
                    attacker.getTemporaryTriggeredEffects(EffectSlot.ON_BECOMES_BLOCKED));
            grantedBecomesBlockedEffects.addAll(attacker.getPersistentTriggeredEffects(EffectSlot.ON_BECOMES_BLOCKED));
            if (!becomesBlockedRegs.isEmpty() || !grantedBecomesBlockedEffects.isEmpty()) {
                List<CardEffect> blockerSpecificEffects = becomesBlockedRegs.stream()
                        .filter(r -> r.triggerMode() == TriggerMode.PER_BLOCKER)
                        .map(EffectRegistration::effect)
                        .toList();
                List<CardEffect> regularEffects = new ArrayList<>(becomesBlockedRegs.stream()
                        .filter(r -> r.triggerMode() != TriggerMode.PER_BLOCKER)
                        .map(EffectRegistration::effect)
                        .toList());
                regularEffects.addAll(grantedBecomesBlockedEffects);

                pushRegularBecomesBlockedTriggers(gameData, attacker, activeId, regularEffects);

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
                            } else if (e instanceof TriggeringPermanentConditionalEffect permConditional) {
                                // "becomes blocked by a [filter] creature" — the blocker is the event subject
                                // (e.g. Catacomb Dragon's nonartifact, non-Dragon blocker).
                                if (predicateEvaluationService.matchesPermanentPredicate(gameData, blocker, permConditional.predicate())) {
                                    filteredEffects.add(permConditional.wrapped());
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
                        gameLogService.append(gameData, GameLog.cardThen(attacker.getCard(),
                                "'s becomes-blocked ability triggers."));
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

        // Global "whenever a creature becomes blocked / blocks a creature" watchers
        // (ON_ANY_CREATURE_BECOMES_BLOCKED) on every battlefield, once per attacker/blocker pair.
        checkAnyCreatureBecomesBlockedTriggers(gameData, attackerBattlefield, defenderBattlefield,
                blockerAssignments, blockedAttackerIndices);

        // Global "whenever one or more creatures block" watchers (ON_ANY_CREATURES_BLOCK) on every
        // battlefield, once per declaration no matter how many creatures blocked.
        checkAnyCreaturesBlockTriggers(gameData, blockerAssignments);

        // Engine-level flanking triggers (CR 702.25a): whenever a creature with flanking becomes
        // blocked by a creature without flanking, that blocker gets -1/-1 until end of turn. Each
        // instance of flanking triggers separately (CR 702.25b), but a card can only carry the
        // Scryfall-loaded keyword once, so one trigger per blocker.
        for (BlockerAssignment assignment : blockerAssignments) {
            Permanent attacker = attackerBattlefield.get(assignment.attackerIndex());
            if (!gameQueryService.hasKeyword(gameData, attacker, Keyword.FLANKING)) {
                continue;
            }
            Permanent blocker = defenderBattlefield.get(assignment.blockerIndex());
            if (gameQueryService.hasKeyword(gameData, blocker, Keyword.FLANKING)) {
                continue;
            }
            StackEntry flankingTrigger = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    attacker.getCard(),
                    activeId,
                    attacker.getCard().getName() + "'s flanking trigger",
                    List.of(new BoostTargetCreatureEffect(-1, -1)),
                    blocker.getId(),
                    attacker.getId()
            );
            // Flanking references the blocking creature without targeting it.
            flankingTrigger.setNonTargeting(true);
            gameData.stack.add(flankingTrigger);
            gameLogService.append(gameData, GameLog.cardThen(attacker.getCard(), "'s flanking triggers."));
            log.info("Game {} - {} flanking trigger pushed onto stack", gameData.id, attacker.getCard().getName());
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

        // "Whenever a creature you control attacks and isn't blocked, you may have it deal damage
        // equal to its power to a target creature. If you do, it assigns no combat damage"
        // (Gaze of Pain delayed trigger).
        processDelayedUnblockedAttackerPowerDamageTriggers(gameData, activeId, unblockedAttackers);

        // "Whenever a creature attacks and isn't blocked this combat, untap it and remove it from
        // combat" delayed triggers (Melee).
        processDelayedUnblockedAttackerUntapRemoveTriggers(gameData, unblockedAttackers);

        // "Whenever a creature blocks this turn, it gets +X/+Y" delayed triggers (Battle Cry).
        // Once per unique blocker, not once per attacker blocked.
        processDelayedBlockerBoostTriggers(gameData, blockerAssignments, defenderBattlefield);

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
     * Fires delayed "whenever a creature you control attacks and isn't blocked, you may have it
     * deal damage equal to its power to a target creature; if you do, it assigns no combat damage"
     * triggers (Gaze of Pain). One may-trigger per unblocked attacker per registered delayed action
     * whose controller is the attacking player.
     */
    private void processDelayedUnblockedAttackerPowerDamageTriggers(GameData gameData,
                                                                    UUID activeId,
                                                                    List<Permanent> unblockedAttackers) {
        if (unblockedAttackers.isEmpty()
                || !gameData.hasDelayedAction(DelayedUnblockedAttackerPowerDamage.class)) {
            return;
        }
        for (DelayedUnblockedAttackerPowerDamage delayed
                : gameData.getDelayedActions(DelayedUnblockedAttackerPowerDamage.class)) {
            if (!delayed.controllerId().equals(activeId)) {
                continue;
            }
            for (Permanent attacker : unblockedAttackers) {
                MayEffect may = new MayEffect(
                        SequenceEffect.of(
                                new DealDamageToTargetCreatureEffect(new SourcePower()),
                                new AssignNoCombatDamageEffect()),
                        "have it deal damage equal to its power to a target creature?");
                // CR 603.5: source permanent is the unblocked attacker ("it"); source card is Gaze
                // of Pain (governs targeting via effect targetSpec → any creature).
                gameData.queueMayAbility(delayed.sourceCard(), delayed.controllerId(), may,
                        null, attacker.getId());
                gameLogService.append(gameData, GameLog.cardTextCard(
                        delayed.sourceCard(), " — ", attacker.getCard(),
                        " attacks unblocked."));
                log.info("Game {} - {} delayed unblocked-attacker power damage fires for {}",
                        gameData.id, delayed.sourceCard().getName(), attacker.getCard().getName());
            }
        }
    }

    /**
     * Fires delayed "whenever a creature attacks and isn't blocked this combat, untap it and remove
     * it from combat" triggers (Melee). One trigger per unblocked attacker per registered delayed
     * action; unlike the Gaze of Pain family this is not restricted to the registering player's
     * creatures, because Melee's trigger reads "whenever a creature attacks".
     */
    private void processDelayedUnblockedAttackerUntapRemoveTriggers(GameData gameData,
                                                                    List<Permanent> unblockedAttackers) {
        if (unblockedAttackers.isEmpty()
                || !gameData.hasDelayedAction(DelayedUnblockedAttackerUntapRemoveFromCombat.class)) {
            return;
        }
        for (DelayedUnblockedAttackerUntapRemoveFromCombat delayed
                : gameData.getDelayedActions(DelayedUnblockedAttackerUntapRemoveFromCombat.class)) {
            for (Permanent attacker : unblockedAttackers) {
                StackEntry trigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        delayed.sourceCard(),
                        delayed.controllerId(),
                        delayed.sourceCard().getName() + "'s delayed trigger",
                        List.of(new UntapPermanentsEffect(TapUntapScope.TARGET),
                                new RemoveTargetFromCombatEffect()),
                        attacker.getId(),
                        attacker.getId());
                trigger.setNonTargeting(true);
                gameData.stack.add(trigger);
                gameLogService.append(gameData, GameLog.cardTextCard(
                        delayed.sourceCard(), " — ", attacker.getCard(),
                        " is untapped and removed from combat."));
                log.info("Game {} - {} delayed untap-and-remove-from-combat fires for {}",
                        gameData.id, delayed.sourceCard().getName(), attacker.getCard().getName());
            }
        }
    }

    /**
     * Fires delayed "whenever a creature blocks this turn, it gets +X/+Y" triggers (Battle Cry).
     * One trigger per unique blocker per registered delayed action.
     */
    private void processDelayedBlockerBoostTriggers(GameData gameData,
                                                    List<BlockerAssignment> blockerAssignments,
                                                    List<Permanent> defenderBattlefield) {
        if (blockerAssignments.isEmpty() || !gameData.hasDelayedAction(DelayedBlockerBoost.class)) {
            return;
        }
        LinkedHashSet<Integer> uniqueBlockerIndices = new LinkedHashSet<>();
        for (BlockerAssignment assignment : blockerAssignments) {
            uniqueBlockerIndices.add(assignment.blockerIndex());
        }
        for (DelayedBlockerBoost boost : gameData.getDelayedActions(DelayedBlockerBoost.class)) {
            for (int blockerIdx : uniqueBlockerIndices) {
                Permanent blocker = defenderBattlefield.get(blockerIdx);
                StackEntry se = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        boost.sourceCard(),
                        boost.controllerId(),
                        boost.sourceCard().getName() + "'s delayed trigger",
                        List.of(new BoostSelfEffect(boost.power(), boost.toughness())),
                        blocker.getId(),
                        blocker.getId());
                se.setNonTargeting(true);
                gameData.stack.add(se);
                gameLogService.append(gameData, GameLog.cardTextCard(
                        boost.sourceCard(), " — ", blocker.getCard(),
                        " gets +" + boost.power() + "/+" + boost.toughness() + " until end of turn."));
                log.info("Game {} - {} delayed blocker boost fires for {}",
                        gameData.id, boost.sourceCard().getName(), blocker.getCard().getName());
            }
        }
    }

    /**
     * Captures all finalized blocker legality metadata (must-be-blocked, menace, and
     * per-blocker must-block requirements) in the pending domain interaction.
     */
    private PendingInteraction.BlockerDeclaration buildBlockerDeclaration(
            GameData gameData,
            List<Integer> blockable,
            List<Integer> attackerIndices,
            UUID defenderId,
            UUID activeId,
            UUID chooserId) {
        List<Permanent> attackerBattlefield = gameData.playerBattlefields.get(activeId);
        List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(defenderId);

        BlockLegalityContext blockContext = blockLegalityService.createBlockLegalityContext(gameData, defenderBattlefield);
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
                            && blockLegalityService.canBlockAttacker(blockContext, blocker, attacker)) {
                        requiredAttackerIndices.add(atkIdx);
                    }
                }
            }
            if (!requiredAttackerIndices.isEmpty()) {
                mustBlockReqs.put(blockerIdx, requiredAttackerIndices);
            }
        }

        return new PendingInteraction.BlockerDeclaration(
                defenderId, blockable, attackerIndices, legalPairs,
                mustBeBlockedIndices, menaceIndices, mustBlockReqs, chooserId);
    }

    /**
     * Returns the player who declares this combat's blocks: the defending player, unless a
     * "you choose which creatures block this combat" effect (Melee) is in force, in which case the
     * most recently registered chooser takes over.
     */
    private UUID blockerDeclarationChooser(GameData gameData, UUID defenderId) {
        if (hasGlobalBlockerDeclarationControl(gameData)) {
            return gameData.activePlayerId;
        }
        List<DelayedBlockerDeclarationControl> controls =
                gameData.getDelayedActions(DelayedBlockerDeclarationControl.class);
        return controls.isEmpty() ? defenderId : controls.getLast().chooserId();
    }

    private boolean hasGlobalBlockerDeclarationControl(GameData gameData) {
        return gameData.playerBattlefields.values().stream()
                .flatMap(Collection::stream)
                .flatMap(permanent -> permanent.getCard().getEffects(EffectSlot.STATIC).stream())
                .anyMatch(BlockerDeclarationControlEffect.class::isInstance);
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
            GraveyardCardChoosingEffect graveyardChoice = effects.stream()
                    .filter(GraveyardCardChoosingEffect.class::isInstance)
                    .map(GraveyardCardChoosingEffect.class::cast)
                    .findFirst()
                    .orElse(null);
            if (graveyardChoice != null) {
                // "you may exile up to two target creature cards from defending player's graveyard"
                // (Rysorian Badger): the targets are chosen as the trigger goes on the stack, so the
                // service owns pushing the entry (and its own logging).
                graveyardTargetingService.handleUnblockedAttackGraveyardChoiceTargeting(gameData, activeId,
                        attacker.getCard(), effects, attacker.getId(), defenderId, graveyardChoice);
                pushed++;
            } else if (!effects.isEmpty() && attacker.getCard().getSpellTargets().size() > 1) {
                // "destroy target creature and target land" (Goblin Grenadiers): two positional target
                // groups, so the single-target may/attack pipelines can't collect them. Reuse the ETB
                // slot-by-slot picker — the targets are chosen as the trigger goes on the stack
                // (CR 603.3d) and the "you may sacrifice it" is still made at resolution.
                gameData.queueInteraction(new PermanentChoiceContext.ETBTokenMultiTargetTrigger(
                        attacker.getCard(), activeId, effects, attacker.getId(), List.of(), 0, 0));
                gameLogService.append(gameData, GameLog.cardThen(attacker.getCard(),
                        "'s unblocked-attack ability triggers."));
                log.info("Game {} - {} unblocked-attack multi-target trigger queued", gameData.id,
                        attacker.getCard().getName());
                pushed++;
            } else if (!effects.isEmpty()) {
                // Permanent-targeting "you may" (Dwarven Vigilantes / Gaze of Pain shape): the may's
                // creature target is chosen at resolution after accepting. Push via queueMayAbility
                // with null targetId so the defender baked into other unblocked-attack triggers does
                // not look like an already-chosen creature target. Non-targeting mays (Stromgald Spy)
                // and mandatory effects keep the defending player as targetId.
                List<CardEffect> targetingMayEffects = effects.stream()
                        .filter(e -> e instanceof MayEffect
                                && e.targetSpec().admits(TargetPredicate.Kind.PERMANENT))
                        .toList();
                List<CardEffect> otherEffects = effects.stream()
                        .filter(e -> !targetingMayEffects.contains(e))
                        .toList();
                for (CardEffect effect : targetingMayEffects) {
                    gameData.queueMayAbility(attacker.getCard(), activeId, (MayEffect) effect,
                            null, attacker.getId());
                    gameLogService.append(gameData, GameLog.cardThen(attacker.getCard(),
                            "'s unblocked-attack ability triggers."));
                    log.info("Game {} - {} unblocked-attack targeting-may trigger pushed onto stack",
                            gameData.id, attacker.getCard().getName());
                    pushed++;
                }
                if (!otherEffects.isEmpty()) {
                    StackEntry trigger = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            attacker.getCard(),
                            activeId,
                            attacker.getCard().getName() + "'s unblocked-attack trigger",
                            new ArrayList<>(otherEffects),
                            defenderId,
                            attacker.getId());
                    // "Defending player" is determined by the combat, not chosen — the trigger can't fizzle.
                    trigger.setNonTargeting(true);
                    gameData.stack.add(trigger);
                    gameLogService.append(gameData, GameLog.cardThen(attacker.getCard(),
                            "'s unblocked-attack ability triggers."));
                    log.info("Game {} - {} unblocked-attack trigger pushed onto stack", gameData.id, attacker.getCard().getName());
                    pushed++;
                }
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
            gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
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
                gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
                log.info("Game {} - {} ON_ALLY_CREATURE_ATTACKS_UNBLOCKED trigger for {} unblocked",
                        gameData.id, perm.getCard().getName(), attacker.getCard().getName());
            }
        }
        return pushed;
    }

    /**
     * CR 702.22h: for each declared block of a band member, marks the blocker as also blocking every
     * other member of that band. These consequential blocks bypass block legality (a non-flyer can end
     * up "blocking" a flying band-mate) and are applied only after all declared blocks have been
     * validated, so they never count toward max-blocks or menace requirements.
     */
    private void applyBandSharedBlocking(GameData gameData,
                                         List<Permanent> attackerBattlefield,
                                         List<Permanent> defenderBattlefield,
                                         List<BlockerAssignment> blockerAssignments) {
        Map<UUID, List<Integer>> bandMembers = groupAttackingBands(attackerBattlefield);
        if (bandMembers.isEmpty()) {
            return;
        }
        for (BlockerAssignment assignment : blockerAssignments) {
            Permanent attacker = attackerBattlefield.get(assignment.attackerIndex());
            UUID bandId = attacker.getBandId();
            if (bandId == null) {
                continue;
            }
            Permanent blocker = defenderBattlefield.get(assignment.blockerIndex());
            for (int memberIdx : bandMembers.getOrDefault(bandId, List.of())) {
                if (memberIdx == assignment.attackerIndex()) {
                    continue;
                }
                Permanent member = attackerBattlefield.get(memberIdx);
                if (!blocker.getBlockingTargetIds().contains(member.getId())) {
                    blocker.setBlocking(true);
                    blocker.addBlockingTarget(memberIdx);
                    blocker.addBlockingTargetId(member.getId());
                    member.setBlockedOrWasBlockedSinceLastUpkeep(true);
                    recordCombatBlockOpponentSubtypes(gameData, blocker, member);
                }
            }
        }
    }

    /**
     * CR 702.22h: expands {@code blockedAttackerIndices} to include every band-mate of an already
     * blocked attacker, since blocking one member of a band blocks the whole band.
     */
    private void addBandMatesOfBlockedAttackers(List<Permanent> attackerBattlefield,
                                                Set<Integer> blockedAttackerIndices) {
        Map<UUID, List<Integer>> bandMembers = groupAttackingBands(attackerBattlefield);
        if (bandMembers.isEmpty()) {
            return;
        }
        Set<Integer> additions = new LinkedHashSet<>();
        for (int idx : blockedAttackerIndices) {
            if (idx < 0 || idx >= attackerBattlefield.size()) {
                continue;
            }
            UUID bandId = attackerBattlefield.get(idx).getBandId();
            if (bandId != null) {
                additions.addAll(bandMembers.getOrDefault(bandId, List.of()));
            }
        }
        blockedAttackerIndices.addAll(additions);
    }

    /** Groups the currently attacking creatures by their band id (CR 702.22). */
    private Map<UUID, List<Integer>> groupAttackingBands(List<Permanent> attackerBattlefield) {
        Map<UUID, List<Integer>> bandMembers = new HashMap<>();
        for (int i = 0; i < attackerBattlefield.size(); i++) {
            Permanent atk = attackerBattlefield.get(i);
            if (atk.isAttacking() && atk.getBandId() != null) {
                bandMembers.computeIfAbsent(atk.getBandId(), k -> new ArrayList<>()).add(i);
            }
        }
        return bandMembers;
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

    /** Makes an attacking creature blocked without adding a creature that blocks it. */
    public void makeAttackingCreatureBlockedWithoutBlockers(GameData gameData, Permanent attacker) {
        if (attacker == null || !attacker.isAttacking() || attacker.isBlockedWithoutBlockers()
                || gameQueryService.isBlockedByAnyCreature(gameData, attacker)) {
            return;
        }

        attacker.setBlockedWithoutBlockers(true);
        gameLogService.append(gameData, GameLog.cardThen(attacker.getCard(), " becomes blocked."));
        log.info("Game {} - {} becomes blocked with no blockers", gameData.id, attacker.getCard().getName());
        fireBecomesBlockedTriggersWithoutBlockers(gameData, attacker);
    }

    /**
     * Fires the "becomes blocked" triggers for an attacker that an effect made blocked without any
     * creature blocking it (CR 509.1h, e.g. Dazzling Beauty). Only triggers that don't reference a
     * blocker fire — PER_BLOCKER registrations and per-blocker attached triggers are skipped, since
     * there is no blocker.
     */
    public void fireBecomesBlockedTriggersWithoutBlockers(GameData gameData, Permanent attacker) {
        UUID controllerId = gameData.activePlayerId;
        List<CardEffect> regularEffects = new ArrayList<>(attacker.getCard().getEffectRegistrations(EffectSlot.ON_BECOMES_BLOCKED)
                .stream()
                .filter(r -> r.triggerMode() != TriggerMode.PER_BLOCKER)
                .map(EffectRegistration::effect)
                .toList());
        regularEffects.addAll(attacker.getTemporaryTriggeredEffects(EffectSlot.ON_BECOMES_BLOCKED));
        regularEffects.addAll(attacker.getPersistentTriggeredEffects(EffectSlot.ON_BECOMES_BLOCKED));
        pushRegularBecomesBlockedTriggers(gameData, attacker, controllerId, regularEffects);

        combatTriggerService.checkAuraTriggersForCreature(gameData, attacker, EffectSlot.ON_BECOMES_BLOCKED);
        checkAllyBecomesBlockedTriggers(gameData, controllerId, attacker);
        checkBlockedCreatureTriggers(gameData, attacker);
    }

    /**
     * Pushes the non-PER_BLOCKER becomes-blocked effects of a blocked attacker.
     *
     * <p>Graveyard-targeting effects are queued for the shared graveyard target selector. Permanent-
     * targeting "you may" effects (Rust Scarab's "you may destroy target artifact or enchantment
     * defending player controls") go through {@code queueMayAbility} with a {@code null}
     * target so the target is chosen after the controller accepts, honouring the card's target
     * filter. Baking the attacker in as {@code targetId} — what the plain trigger entry does so
     * self-scoped effects see their own source — would otherwise look like an already-chosen target.
     * Everything else keeps the plain entry, whose {@code attackedTargetId} lets defending-player
     * effects (Vedalken Ghoul's life loss) read the attacked player or planeswalker's controller.
     */
    private void pushRegularBecomesBlockedTriggers(GameData gameData, Permanent attacker, UUID controllerId,
                                                   List<CardEffect> regularEffects) {
        if (regularEffects.isEmpty()) {
            return;
        }
        boolean needsGraveyardTarget = regularEffects.stream()
                .anyMatch(e -> e.targetSpec().admits(TargetPredicate.Kind.GRAVEYARD_CARD));
        if (needsGraveyardTarget) {
            gameData.queueInteraction(new PermanentChoiceContext.SpellGraveyardTargetTrigger(
                    attacker.getCard(), controllerId, new ArrayList<>(regularEffects)));
            gameLogService.append(gameData, GameLog.cardThen(attacker.getCard(),
                    "'s becomes-blocked ability triggers."));
            log.info("Game {} - {} becomes-blocked graveyard-target trigger queued",
                    gameData.id, attacker.getCard().getName());
            return;
        }
        List<CardEffect> targetingMayEffects = regularEffects.stream()
                .filter(e -> e instanceof MayEffect && e.targetSpec().admits(TargetPredicate.Kind.PERMANENT))
                .toList();
        for (CardEffect effect : targetingMayEffects) {
            gameData.queueMayAbility(attacker.getCard(), controllerId, (MayEffect) effect,
                    null, attacker.getId());
            gameLogService.append(gameData, GameLog.cardThen(attacker.getCard(),
                    "'s becomes-blocked ability triggers."));
            log.info("Game {} - {} becomes-blocked targeting-may trigger pushed onto stack",
                    gameData.id, attacker.getCard().getName());
        }
        List<CardEffect> otherEffects = regularEffects.stream()
                .filter(e -> !targetingMayEffects.contains(e))
                .toList();
        if (otherEffects.isEmpty()) {
            return;
        }
        StackEntry trigger = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                attacker.getCard(),
                controllerId,
                attacker.getCard().getName() + "'s becomes-blocked trigger",
                new ArrayList<>(otherEffects),
                attacker.getId(),
                attacker.getId()
        );
        trigger.setAttackedTargetId(attacker.getAttackTarget());
        gameData.stack.add(trigger);
        gameLogService.append(gameData, GameLog.cardThen(attacker.getCard(),
                "'s becomes-blocked ability triggers."));
        log.info("Game {} - {} becomes-blocked trigger pushed onto stack", gameData.id, attacker.getCard().getName());
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
            gameLogService.append(gameData, GameLog.abilityTriggers(perm.getCard()));
            log.info("Game {} - {} ON_ALLY_CREATURE_BECOMES_BLOCKED trigger for {} blocked",
                    gameData.id, perm.getCard().getName(), blockedAttacker.getCard().getName());
        }
    }

    /**
     * Fires ON_ANY_CREATURE_BECOMES_BLOCKED watchers once per declared attacker/blocker pair, scanning
     * every battlefield (the watcher's controller need not control either creature). Effects
     * implementing {@link BlockPairConditionalEffect} are filtered against the pair's effective powers
     * here — the comparison is a trigger condition, so it is never re-checked on resolution — and the
     * participant the effect acts on is baked in as the non-targeting target.
     */
    private void checkAnyCreatureBecomesBlockedTriggers(GameData gameData,
                                                        List<Permanent> attackerBattlefield,
                                                        List<Permanent> defenderBattlefield,
                                                        List<BlockerAssignment> blockerAssignments,
                                                        Set<Integer> blockedAttackerIndices) {
        for (int attackerIndex : blockedAttackerIndices) {
            Permanent attacker = attackerBattlefield.get(attackerIndex);
            checkBlockedCreatureTriggers(gameData, attacker);
        }

        for (BlockerAssignment assignment : blockerAssignments) {
            Permanent attacker = attackerBattlefield.get(assignment.attackerIndex());
            Permanent blocker = defenderBattlefield.get(assignment.blockerIndex());
            int attackerPower = gameQueryService.getEffectivePower(gameData, attacker);
            int blockerPower = gameQueryService.getEffectivePower(gameData, blocker);

            for (Map.Entry<UUID, List<Permanent>> battlefield : gameData.playerBattlefields.entrySet()) {
                for (Permanent watcher : List.copyOf(battlefield.getValue())) {
                    for (CardEffect effect : watcher.getCard().getEffects(EffectSlot.ON_ANY_CREATURE_BECOMES_BLOCKED)) {
                        if (!(effect instanceof BlockPairConditionalEffect pairEffect)) {
                            continue;
                        }
                        if (!pairEffect.firesForPair(attackerPower, blockerPower)) {
                            continue;
                        }
                        Permanent subject = pairEffect.actsOn() == BlockParticipant.BLOCKER ? blocker : attacker;
                        StackEntry trigger = new StackEntry(
                                StackEntryType.TRIGGERED_ABILITY,
                                watcher.getCard(),
                                battlefield.getKey(),
                                watcher.getCard().getName() + "'s block trigger",
                                List.of(effect),
                                subject.getId(),
                                attacker.getId()
                        );
                        // "That creature" wording references a combatant without targeting it.
                        trigger.setNonTargeting(true);
                        gameData.stack.add(trigger);
                        gameLogService.append(gameData, GameLog.abilityTriggers(watcher.getCard()));
                        log.info("Game {} - {} block-pair trigger pushed onto stack for {}",
                                gameData.id, watcher.getCard().getName(), subject.getCard().getName());
                    }
                }
            }
        }
    }

    /**
     * Fires global card-conditional becomes-blocked triggers once for each matching blocked
     * attacker, including attackers controlled by the opponent of the watcher.
     */
    private void checkBlockedCreatureTriggers(GameData gameData, Permanent attacker) {
        UUID attackerControllerId = gameData.findControllerOf(attacker);
        for (Map.Entry<UUID, List<Permanent>> battlefield : gameData.playerBattlefields.entrySet()) {
            for (Permanent watcher : List.copyOf(battlefield.getValue())) {
                List<CardEffect> matchingEffects = new ArrayList<>();
                for (CardEffect effect : watcher.getCard().getEffects(EffectSlot.ON_ANY_CREATURE_BECOMES_BLOCKED)) {
                    if (!(effect instanceof BlockedCreatureTriggerEffect conditional)) {
                        continue;
                    }
                    if (!predicateEvaluationService.matchesCardPredicate(attacker.getCard(), conditional.predicate(),
                            null, gameData, attackerControllerId)) {
                        continue;
                    }
                    matchingEffects.add(conditional.wrapped());
                }
                if (matchingEffects.isEmpty()) {
                    continue;
                }

                StackEntry trigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        watcher.getCard(),
                        battlefield.getKey(),
                        watcher.getCard().getName() + "'s becomes-blocked trigger",
                        matchingEffects,
                        attacker.getId(),
                        attacker.getId()
                );
                trigger.setNonTargeting(true);
                gameData.stack.add(trigger);
                gameLogService.append(gameData, GameLog.abilityTriggers(watcher.getCard()));
                log.info("Game {} - {} global becomes-blocked trigger for {}",
                        gameData.id, watcher.getCard().getName(), attacker.getCard().getName());
            }
        }
    }

    /**
     * Fires ON_ANY_CREATURES_BLOCK watchers once for the whole declaration when at least one creature
     * blocked, scanning every battlefield (the watcher's controller need not control any of the
     * creatures involved). No combatant is baked into the entry — the effects read the board's
     * blocking state themselves at resolution.
     */
    private void checkAnyCreaturesBlockTriggers(GameData gameData, List<BlockerAssignment> blockerAssignments) {
        if (blockerAssignments.isEmpty()) {
            return;
        }
        for (Map.Entry<UUID, List<Permanent>> battlefield : gameData.playerBattlefields.entrySet()) {
            for (Permanent watcher : List.copyOf(battlefield.getValue())) {
                List<CardEffect> effects = watcher.getCard().getEffects(EffectSlot.ON_ANY_CREATURES_BLOCK);
                if (effects.isEmpty()) {
                    continue;
                }
                StackEntry trigger = new StackEntry(
                        StackEntryType.TRIGGERED_ABILITY,
                        watcher.getCard(),
                        battlefield.getKey(),
                        watcher.getCard().getName() + "'s block trigger",
                        new ArrayList<>(effects),
                        null,
                        watcher.getId()
                );
                trigger.setNonTargeting(true);
                gameData.stack.add(trigger);
                gameLogService.append(gameData, GameLog.abilityTriggers(watcher.getCard()));
                log.info("Game {} - {} creatures-block trigger pushed onto stack", gameData.id,
                        watcher.getCard().getName());
            }
        }
    }

    /**
     * Fires ON_ANY_CREATURE_BLOCKS watchers once per blocking creature (a creature that blocks
     * several attackers still blocks only once), scanning every battlefield — the watcher's
     * controller need not control the blocker. The blocking creature is baked in as the trigger's
     * non-targeting {@code targetId} so "that creature['s controller]" effects can read it.
     */
    private void checkAnyCreatureBlocksTriggers(GameData gameData,
                                                List<Permanent> defenderBattlefield,
                                                List<BlockerAssignment> blockerAssignments) {
        Set<Integer> blockerIndices = new LinkedHashSet<>();
        for (BlockerAssignment assignment : blockerAssignments) {
            blockerIndices.add(assignment.blockerIndex());
        }
        for (int blockerIndex : blockerIndices) {
            Permanent blocker = defenderBattlefield.get(blockerIndex);
            for (Map.Entry<UUID, List<Permanent>> battlefield : gameData.playerBattlefields.entrySet()) {
                for (Permanent watcher : List.copyOf(battlefield.getValue())) {
                    List<CardEffect> effects = watcher.getCard().getEffects(EffectSlot.ON_ANY_CREATURE_BLOCKS);
                    if (effects.isEmpty()) {
                        continue;
                    }
                    FilterContext watcherContext = FilterContext.of(gameData)
                            .withSourceCardId(watcher.getCard().getId())
                            .withSourceControllerId(battlefield.getKey());
                    List<CardEffect> filteredEffects = new ArrayList<>();
                    for (CardEffect effect : effects) {
                        if (effect instanceof TriggeringPermanentConditionalEffect conditional) {
                            if (!predicateEvaluationService.matchesPermanentPredicate(
                                    blocker, conditional.predicate(), watcherContext)) {
                                continue;
                            }
                            filteredEffects.add(conditional.wrapped());
                        } else {
                            filteredEffects.add(effect);
                        }
                    }
                    if (filteredEffects.isEmpty()) {
                        continue;
                    }
                    StackEntry trigger = new StackEntry(
                            StackEntryType.TRIGGERED_ABILITY,
                            watcher.getCard(),
                            battlefield.getKey(),
                            watcher.getCard().getName() + "'s block trigger",
                            filteredEffects,
                            blocker.getId(),
                            watcher.getId()
                    );
                    // "That creature" wording references the blocker without targeting it.
                    trigger.setNonTargeting(true);
                    gameData.stack.add(trigger);
                    gameLogService.append(gameData, GameLog.abilityTriggers(watcher.getCard()));
                    log.info("Game {} - {} any-creature-blocks trigger pushed onto stack for {}",
                            gameData.id, watcher.getCard().getName(), blocker.getCard().getName());
                }
            }
        }
    }

    private int getMaxBlocksForCreature(GameData gameData, Permanent creature, List<Permanent> battlefield) {
        // Check for "can block any number of creatures" on the creature itself
        for (CardEffect effect : creature.getCard().getEffects(EffectSlot.STATIC)) {
            if (effect instanceof CanBlockAnyNumberOfCreaturesEffect) {
                return Integer.MAX_VALUE;
            }
        }

        // One-shot "can block an additional creature this turn" grants (e.g. Act of Heroism).
        int additionalBlocks = creature.getAdditionalBlocksUntilEndOfTurn();
        for (Permanent p : battlefield) {
            for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
                CardEffect effectiveEffect = staticEffectConditionResolver.resolve(gameData, p,
                        gameQueryService.findPermanentController(gameData, p.getId()), effect);
                if (effectiveEffect == null) {
                    continue;
                }
                if (effectiveEffect instanceof GrantAdditionalBlockEffect e) {
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
                } else if (effectiveEffect instanceof GrantAdditionalBlockPerEquipmentEffect
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
        for (int blockerIdx : blockable) {
            Permanent blocker = defenderBattlefield.get(blockerIdx);

            Set<Integer> requiredAttackerIndices = new HashSet<>();
            for (int i = 0; i < attackerBattlefield.size(); i++) {
                Permanent attacker = attackerBattlefield.get(i);
                if (!attacker.isAttacking()) {
                    continue;
                }
                if (!gameQueryService.isRequiredToBlockByLure(gameData, attacker, blocker)) {
                    continue;
                }
                if (blockLegalityService.canBlockAttacker(blockContext, blocker, attacker)) {
                    requiredAttackerIndices.add(i);
                }
            }
            if (requiredAttackerIndices.isEmpty()) {
                continue;
            }

            int currentLureBlocks = 0;
            for (BlockerAssignment assignment : blockerAssignments) {
                if (assignment.blockerIndex() == blockerIdx
                        && requiredAttackerIndices.contains(assignment.attackerIndex())) {
                    currentLureBlocks++;
                }
            }

            int maxSatisfiable = Math.min(
                    getMaxBlocksForCreature(gameData, blocker, defenderBattlefield),
                    requiredAttackerIndices.size());
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
                            && blockLegalityService.canBlockAttacker(blockContext, blocker, attacker)) {
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
                if (blockLegalityService.canBlockAttacker(blockContext, blocker, attacker)) {
                    throw new IllegalStateException(attacker.getCard().getName()
                            + " must be blocked if able");
                }
            }
        }
    }

    /**
     * "Target creature blocks this turn if able" (Nacatl Hunt-Pride): a creature flagged with
     * {@link Permanent#isMustBlockThisTurnIfAble()} must be declared as a blocker of at least one
     * attacker if it is able to block any of them. The requirement is satisfied vacuously when the
     * creature can't legally block any declared attacker (evasion, tapped, etc.). The permanent
     * static form, {@link MustBlockEachCombatEffect} ("this creature blocks each combat if able" —
     * Watchdog), is enforced on the same path.
     */
    private void validateMustBlockIfAbleRequirements(GameData gameData,
                                                     BlockLegalityContext blockContext,
                                                     List<Permanent> attackerBattlefield,
                                                     List<Permanent> defenderBattlefield,
                                                     List<Integer> blockable,
                                                     List<BlockerAssignment> blockerAssignments) {
        Set<Integer> assignedBlockerIndices = new HashSet<>();
        for (BlockerAssignment assignment : blockerAssignments) {
            assignedBlockerIndices.add(assignment.blockerIndex());
        }

        for (int blockerIdx : blockable) {
            Permanent blocker = defenderBattlefield.get(blockerIdx);
            if (assignedBlockerIndices.contains(blockerIdx) || !mustBlockIfAble(gameData, blocker)) {
                continue;
            }
            for (Permanent attacker : attackerBattlefield) {
                if (attacker.isAttacking() && blockLegalityService.canBlockAttacker(blockContext, blocker, attacker)) {
                    throw new IllegalStateException(blocker.getCard().getName() + " must block this turn if able");
                }
            }
        }
    }

    /**
     * A creature carries a "blocks if able" requirement either from a one-shot effect this turn
     * ({@link Permanent#isMustBlockThisTurnIfAble()}) or from a permanent static
     * {@link MustBlockEachCombatEffect} on itself or on an Aura attached to it.
     */
    private boolean mustBlockIfAble(GameData gameData, Permanent blocker) {
        return blocker.isMustBlockThisTurnIfAble()
                || blocker.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(MustBlockEachCombatEffect.class::isInstance)
                || gameQueryService.hasAuraWithEffect(gameData, blocker, MustBlockEachCombatEffect.class)
                || hasGlobalMustBlockEachCombat(gameData);
    }

    private boolean hasGlobalMustBlockEachCombat(GameData gameData) {
        return gameData.playerBattlefields.values().stream()
                .flatMap(Collection::stream)
                .flatMap(permanent -> permanent.getCard().getEffects(EffectSlot.STATIC).stream())
                .anyMatch(GlobalMustBlockEachCombatEffect.class::isInstance);
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

    private boolean hasCantAttackOrBlockAlone(Permanent creature) {
        return creature.getCard().getEffects(EffectSlot.STATIC).stream()
                .filter(CantAttackOrBlockAloneEffect.class::isInstance)
                .map(CantAttackOrBlockAloneEffect.class::cast)
                .anyMatch(CantAttackOrBlockAloneEffect::restrictsBlocking);
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
     * Orcish Conscripts (CR 509.1a): a creature with "can't block unless at least N other creatures
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

    /**
     * Records, for both creatures in a declared block, the subtypes of the other creature at block time
     * into the turn-scoped {@link GameData#combatBlockOpponentSubtypesThisTurn} map (plus the Changeling
     * set). The blocker "blocked" the attacker and the attacker "was blocked by" the blocker, so both
     * directions are recorded. Time to Reflect reads this to target a creature that blocked or was
     * blocked by a Zombie this turn, even after combat ends or the other creature leaves / changes types.
     * The opponent's ID is also recorded into {@link GameData#combatBlockOpponentIdsThisTurn} for
     * Venomous Breath's "destroy all creatures that blocked or were blocked by it this turn".
     * <p>
     * The attacker alone is additionally recorded into {@link GameData#creaturesBlockedThisTurn}, the
     * one-directional "was blocked this turn" set that Fyndhorn Druid's dies trigger reads.
     */
    private void recordCombatBlockOpponentSubtypes(GameData gameData, Permanent blocker, Permanent attacker) {
        recordCombatOpponentSubtypes(gameData, blocker, attacker);
        recordCombatOpponentSubtypes(gameData, attacker, blocker);
        gameData.combatOpponentIdsBlockedByThisTurn
                .computeIfAbsent(blocker.getId(), k -> ConcurrentHashMap.newKeySet())
                .add(attacker.getId());
        gameData.creaturesBlockedThisTurn.add(attacker.getId());
    }

    private void recordCombatOpponentSubtypes(GameData gameData, Permanent creature, Permanent opponent) {
        creature.setBlockedOrWasBlockedSinceLastUpkeep(true);
        Set<CardSubtype> subtypes = gameData.combatBlockOpponentSubtypesThisTurn
                .computeIfAbsent(creature.getId(), k -> ConcurrentHashMap.newKeySet());
        subtypes.addAll(opponent.getCard().getSubtypes());
        subtypes.addAll(opponent.getGrantedSubtypes());
        subtypes.addAll(opponent.getTransientSubtypes());
        gameData.combatBlockOpponentColorsThisTurn
                .computeIfAbsent(creature.getId(), k -> ConcurrentHashMap.newKeySet())
                .addAll(gameQueryService.getEffectiveColors(gameData, opponent));
        gameData.combatBlockOpponentIdsThisTurn
                .computeIfAbsent(creature.getId(), k -> ConcurrentHashMap.newKeySet())
                .add(opponent.getId());
        if (gameQueryService.hasKeyword(gameData, opponent, Keyword.CHANGELING)) {
            gameData.creaturesInCombatWithChangelingThisTurn.add(creature.getId());
        }
    }

}
