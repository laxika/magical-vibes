package com.github.laxika.magicalvibes.service.combat;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectRegistration;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TriggerMode;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.BoostSelfWhenBlockingKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.CanBeBlockedByAtMostNCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockAloneEffect;
import com.github.laxika.magicalvibes.model.effect.CanBlockAnyNumberOfCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyBlockedCreatureAndSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyEquipmentOnEquippedCombatOpponentAtEndOfCombatEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetCreatureAndGainLifeEqualToToughnessEffect;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalBlockEffect;
import com.github.laxika.magicalvibes.model.effect.GrantAdditionalBlockPerEquipmentEffect;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedByAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedIfAbleEffect;
import com.github.laxika.magicalvibes.model.effect.SkipNextUntapOnTargetEffect;
import com.github.laxika.magicalvibes.networking.SessionManager;
import com.github.laxika.magicalvibes.networking.message.AvailableBlockersMessage;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
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
    private final GameBroadcastService gameBroadcastService;
    private final SessionManager sessionManager;
    private final CombatAttackService combatAttackService;
    private final CombatTriggerService combatTriggerService;

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
        List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(defenderId);
        List<Permanent> attackerBattlefield = gameData.playerBattlefields.get(attackerId);
        Map<Integer, List<Integer>> pairs = new LinkedHashMap<>();
        for (int blockerIdx : blockerIndices) {
            Permanent blocker = defenderBattlefield.get(blockerIdx);
            List<Integer> legalAttackers = new ArrayList<>();
            for (int attackerIdx : attackerIndices) {
                Permanent attacker = attackerBattlefield.get(attackerIdx);
                if (canBlockAttacker(gameData, blocker, attacker, defenderBattlefield)) {
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
    public CombatResult handleDeclareBlockersStep(GameData gameData) {
        UUID activeId = gameData.activePlayerId;
        UUID defenderId = gameQueryService.getOpponentId(gameData, activeId);
        List<Integer> blockable = getBlockableCreatureIndices(gameData, defenderId);
        List<Integer> attackerIndices = combatAttackService.getAttackingCreatureIndices(gameData, activeId);

        // Filter out attackers that can't be blocked
        List<Permanent> attackerBattlefield = gameData.playerBattlefields.get(activeId);
        List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(defenderId);
        attackerIndices = attackerIndices.stream()
                .filter(idx -> !gameQueryService.hasCantBeBlocked(gameData, attackerBattlefield.get(idx)))
                .filter(idx -> !CombatHelper.isCantBeBlockedDueToDefenderCondition(gameQueryService, gameData, attackerBattlefield.get(idx), defenderBattlefield))
                .filter(idx -> !CombatHelper.isCantBeBlockedDueToHistoricCast(gameQueryService, gameData, attackerBattlefield.get(idx)))
                .toList();

        if (blockable.isEmpty() || attackerIndices.isEmpty()) {
            log.info("Game {} - Defending player has no creatures that can block or no blockable attackers", gameData.id);
            return CombatResult.ADVANCE_ONLY;
        }

        AvailableBlockersMessage message = buildAvailableBlockersMessage(gameData, blockable, attackerIndices, defenderId, activeId);

        gameData.interaction.beginBlockerDeclaration(defenderId);
        sessionManager.sendToPlayer(CombatHelper.getEffectiveRecipient(gameData, defenderId), message);
        return CombatResult.DONE;
    }

    /**
     * Validates and processes a player's blocker declaration.
     */
    public CombatResult declareBlockers(GameData gameData, Player player, List<BlockerAssignment> blockerAssignments) {
        if (!gameData.interaction.isAwaitingInput(AwaitingInput.BLOCKER_DECLARATION)) {
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

        // Validate assignments
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
            int maxBlocks = getMaxBlocksForCreature(defenderBattlefield.get(blockerIdx), defenderBattlefield);
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
            gameQueryService.getBlockingIllegalityReason(gameData, blocker, attacker, defenderBattlefield)
                    .ifPresent(reason -> { throw new IllegalStateException(reason); });

            blockersPerAttacker.merge(attackerIdx, 1, Integer::sum);
        }

        for (var entry : blockersPerAttacker.entrySet()) {
            int attackerIdx = entry.getKey();
            int blockerCount = entry.getValue();
            Permanent attacker = attackerBattlefield.get(attackerIdx);
            if (gameQueryService.hasKeyword(gameData, attacker, Keyword.MENACE) && blockerCount == 1) {
                throw new IllegalStateException(attacker.getCard().getName() + " can't be blocked except by two or more creatures");
            }
            for (CardEffect effect : attacker.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof CanBeBlockedByAtMostNCreaturesEffect restriction
                        && blockerCount > restriction.maxBlockers()) {
                    throw new IllegalStateException(attacker.getCard().getName()
                            + " can't be blocked by more than " + restriction.maxBlockers()
                            + " creature" + (restriction.maxBlockers() == 1 ? "" : "s"));
                }
            }
        }

        // CR 509.1b: validate "can't block alone" — if any declared blocker has this restriction,
        // there must be at least 2 total blockers
        validateCantBlockAlone(defenderBattlefield, blockerAssignments);

        validateMaximumBlockRequirements(gameData, attackerBattlefield, defenderBattlefield, blockable,
                blockerAssignments);
        validatePerCreatureMustBlockRequirements(gameData, attackerBattlefield, defenderBattlefield, blockable,
                blockerAssignments);
        validateMustBeBlockedIfAbleRequirements(gameData, attackerBattlefield, defenderBattlefield, blockable,
                blockerAssignments);

        gameData.interaction.clearAwaitingInput();

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
            gameBroadcastService.logAndBroadcast(gameData, logEntry);
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

                // Set target: attacker ID for effects that need it, otherwise blocker's own ID
                boolean needsAttackerTarget = blockEffects.stream()
                        .anyMatch(e -> e instanceof DestroyBlockedCreatureAndSelfEffect
                                || e instanceof DestroyTargetCreatureAndGainLifeEqualToToughnessEffect
                                || e instanceof SkipNextUntapOnTargetEffect
                                || e instanceof DealDamageToTargetCreatureEffect
                                || e instanceof DestroyEquipmentOnEquippedCombatOpponentAtEndOfCombatEffect);
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
                gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                log.info("Game {} - {} block trigger pushed onto stack", gameData.id, blocker.getCard().getName());
            }

            // Check for aura/equipment-based "when enchanted/equipped creature blocks" triggers
            Permanent blockerForAura = defenderBattlefield.get(assignment.blockerIndex());
            Permanent attackerForAura = attackerBattlefield.get(assignment.attackerIndex());
            combatTriggerService.checkAuraTriggersForCreature(gameData, blockerForAura, EffectSlot.ON_BLOCK, attackerForAura);
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
                    gameBroadcastService.logAndBroadcast(gameData, triggerLog);
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
                        gameBroadcastService.logAndBroadcast(gameData, triggerLog);
                        log.info("Game {} - {} becomes-blocked trigger pushed onto stack", gameData.id, attacker.getCard().getName());
                    }
                }
            }

            // Check for aura/equipment-based "when enchanted/equipped creature becomes blocked" triggers
            combatTriggerService.checkAuraTriggersForCreature(gameData, attacker, EffectSlot.ON_BECOMES_BLOCKED);
            combatTriggerService.checkAttachedPerBlockerTriggers(gameData, attacker, blockerAssignments, defenderBattlefield, atkIdx);
        }

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

        Map<Integer, List<Integer>> legalPairs = computeLegalBlockPairs(gameData, blockable, attackerIndices, defenderId, activeId);

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
                            && canBlockAttacker(gameData, blocker, attacker, defenderBattlefield)) {
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

    // ===== Private helpers =====

    private boolean canBlockAttacker(GameData gameData, Permanent blocker,
                                      Permanent attacker, List<Permanent> defenderBattlefield) {
        return gameQueryService.canBlockAttacker(gameData, blocker, attacker, defenderBattlefield);
    }

    private int getMaxBlocksForCreature(Permanent creature, List<Permanent> battlefield) {
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
                    boolean isAttachable = p.getCard().getSubtypes().contains(CardSubtype.EQUIPMENT)
                            || p.getCard().isAura();
                    if (isAttachable) {
                        if (creature.getId().equals(p.getAttachedTo())) {
                            additionalBlocks += e.additionalBlocks();
                        }
                    } else {
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
                                                   List<Permanent> attackerBattlefield,
                                                   List<Permanent> defenderBattlefield,
                                                   List<Integer> blockable,
                                                   List<BlockerAssignment> blockerAssignments) {
        Set<Integer> lureAttackerIndices = new HashSet<>();
        for (int i = 0; i < attackerBattlefield.size(); i++) {
            Permanent attacker = attackerBattlefield.get(i);
            if (!attacker.isAttacking()) continue;
            boolean hasRequirement = attacker.getCard().getEffects(EffectSlot.STATIC).stream()
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
                if (canBlockAttacker(gameData, blocker, attacker, defenderBattlefield)) {
                    possibleLureBlocks++;
                }
            }

            int maxSatisfiable = Math.min(getMaxBlocksForCreature(blocker, defenderBattlefield), possibleLureBlocks);
            if (currentLureBlocks < maxSatisfiable) {
                throw new IllegalStateException(blocker.getCard().getName() + " must block enchanted creature if able");
            }
        }
    }

    private void validatePerCreatureMustBlockRequirements(GameData gameData,
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
                            && canBlockAttacker(gameData, blocker, attacker, defenderBattlefield)) {
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

            int maxSatisfiable = Math.min(getMaxBlocksForCreature(blocker, defenderBattlefield), requiredAttackerIndices.size());
            if (currentMustBlocks < maxSatisfiable) {
                throw new IllegalStateException(blocker.getCard().getName() + " must block target creature this turn if able");
            }
        }
    }

    private void validateMustBeBlockedIfAbleRequirements(GameData gameData,
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
                if (canBlockAttacker(gameData, blocker, attacker, defenderBattlefield)) {
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

    private boolean hasCantAttackOrBlockAlone(Permanent creature) {
        return creature.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(CantAttackOrBlockAloneEffect.class::isInstance);
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
