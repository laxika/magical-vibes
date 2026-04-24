package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectResolution;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TargetType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.filter.PlayerPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockAloneEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ReturnCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedByAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedIfAbleEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeArtifactCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.networking.MessageHandler;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.networking.message.CardChosenRequest;
import com.github.laxika.magicalvibes.networking.message.DeclareAttackersRequest;
import com.github.laxika.magicalvibes.networking.message.DeclareBlockersRequest;
import com.github.laxika.magicalvibes.networking.message.PassPriorityRequest;
import com.github.laxika.magicalvibes.networking.message.PlayCardRequest;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameRegistry;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.combat.CombatAttackService;
import com.github.laxika.magicalvibes.service.effect.TargetValidationService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.UUID;

/**
 * AI that makes purely random decisions from legal options. Designed for fuzz
 * testing — exercises far more code paths than heuristic-based AIs because it
 * plays unusual lines the smart AIs would never consider.
 *
 * <p>Accepts a {@link Random} instance so tests can fix the seed for
 * reproducible failures.</p>
 */
class RandomAiDecisionEngine extends AiDecisionEngine {

    private static final Logger log = LoggerFactory.getLogger(RandomAiDecisionEngine.class);

    private final Random rng;

    RandomAiDecisionEngine(UUID gameId, Player aiPlayer, GameRegistry gameRegistry,
                           MessageHandler messageHandler, GameQueryService gameQueryService,
                           CombatAttackService combatAttackService,
                           GameBroadcastService gameBroadcastService,
                           TargetValidationService targetValidationService,
                           TargetLegalityService targetLegalityService, Random rng) {
        super(gameId, aiPlayer, gameRegistry, messageHandler, gameQueryService, combatAttackService, gameBroadcastService, targetValidationService, targetLegalityService);
        this.rng = rng;
    }

    // ===== Priority / Main Phase =====

    @Override
    protected void handleGameState(GameData gameData) {
        if (!hasPriority(gameData)) {
            return;
        }

        boolean isMainPhase = gameData.currentStep == TurnStep.PRECOMBAT_MAIN
                || gameData.currentStep == TurnStep.POSTCOMBAT_MAIN;
        boolean isActivePlayer = aiPlayer.getId().equals(gameData.activePlayerId);

        if (isMainPhase && isActivePlayer && gameData.stack.isEmpty()) {
            // Always try to play a land (maximizes mana for more interesting games)
            tryPlayLand(gameData);

            // Re-check priority: playing a land can trigger abilities that set
            // awaiting input (e.g. a queued death trigger needing target selection).
            if (!hasPriority(gameData)) {
                return;
            }

            if (gameData.stack.isEmpty() && tryCastRandomSpell(gameData, false)) {
                return;
            }
        }

        // Re-check priority: casting a sorcery-speed spell above may have triggered
        // abilities that set awaiting input.
        if (!hasPriority(gameData)) {
            return;
        }

        // Outside main phase (or after failing to cast a sorcery), try an instant
        if (tryCastRandomSpell(gameData, true)) {
            return;
        }

        send(() -> messageHandler.handlePassPriority(selfConnection, new PassPriorityRequest()));
    }

    // ===== Random Spell Casting =====

    private boolean tryCastRandomSpell(GameData gameData, boolean instantsOnly) {
        List<Card> hand = gameData.playerHands.get(aiPlayer.getId());
        if (hand == null) {
            return false;
        }

        ManaPool virtualPool = manaManager.buildVirtualManaPool(gameData, aiPlayer.getId());
        List<Card> graveyard = gameData.playerGraveyards.getOrDefault(aiPlayer.getId(), List.of());

        // Collect all castable spell indices
        List<Integer> castableIndices = new ArrayList<>();
        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (card.hasType(CardType.LAND)) {
                continue;
            }
            if (instantsOnly && !card.hasType(CardType.INSTANT)) {
                continue;
            }
            if (card.getManaCost() == null) {
                continue;
            }

            // Skip spells that target spells on the stack (e.g. Twincast) — AI can't pick spell targets
            if (EffectResolution.needsSpellTarget(card)) {
                continue;
            }

            if (!isSpellCastable(gameData, card, virtualPool)) {
                continue;
            }
            castableIndices.add(i);
        }

        if (castableIndices.isEmpty()) {
            return false;
        }

        // Shuffle to pick a random castable spell
        Collections.shuffle(castableIndices, rng);

        for (int cardIndex : castableIndices) {
            Card card = hand.get(cardIndex);

            // Handle modal spells (ChooseOneEffect)
            ModalCastPlan modalPlan = prepareModalSpellCast(gameData, card);
            if (modalPlan == null && findChooseOneEffect(card) != null) {
                continue; // No valid mode for this modal spell
            }

            // Build damage assignments for divided damage spells
            Map<UUID, Integer> damageAssignments = null;
            if (modalPlan == null && EffectResolution.needsDamageDistribution(card)) {
                damageAssignments = targetSelector.buildDamageAssignments(gameData, card, aiPlayer.getId());
                if (damageAssignments == null) {
                    continue; // No valid targets for damage distribution
                }
            }

            // Determine target if needed (skip for modal and damage distribution spells)
            UUID targetId = modalPlan != null ? modalPlan.targetId() : null;
            List<UUID> multiTargetIds = null;
            boolean isMultiTarget = card.getSpellTargets().size() > 1;
            if (isMultiTarget && modalPlan == null) {
                multiTargetIds = targetSelector.chooseMultiTargets(gameData, card, aiPlayer.getId());
                if (multiTargetIds == null) {
                    continue; // Can't satisfy mandatory targets, try next spell
                }
            } else if (modalPlan == null && !EffectResolution.needsDamageDistribution(card) && (EffectResolution.needsTarget(card) || card.isAura())) {
                targetId = pickRandomTarget(gameData, card);
                if (targetId == null) {
                    continue; // No valid target, try next spell
                }
            }

            // Check targeting tax (e.g. Kopala, Warden of Waves)
            int targetingTax = computeTargetingTax(gameData, targetId, multiTargetIds);
            if (targetingTax > 0 && !canAffordSpell(gameData, card, virtualPool, targetingTax)) {
                continue; // Can't afford with targeting tax, try next spell
            }

            // Determine exile graveyard card index if needed (single card exile)
            Integer exileGraveyardCardIndex = null;
            ExileCardFromGraveyardCost exileCost = findExileGraveyardCost(card);
            if (exileCost != null) {
                exileGraveyardCardIndex = findValidGraveyardIndex(graveyard, exileCost);
                if (exileGraveyardCardIndex == null) {
                    continue; // No valid graveyard card, try next spell
                }
            }

            // Determine exile graveyard card indices if needed (X cards exile, e.g. Harvest Pyre)
            List<Integer> exileGraveyardCardIndices = null;
            if (findExileXGraveyardCost(card) != null) {
                List<Integer> allIndices = selectAllGraveyardIndices(gameData);
                if (allIndices.isEmpty()) {
                    continue; // No graveyard cards, try next spell
                }
                Collections.shuffle(allIndices, rng);
                int count = rng.nextInt(allIndices.size()) + 1;
                exileGraveyardCardIndices = new ArrayList<>(allIndices.subList(0, count));
            } else if (findExileNGraveyardCost(card) != null) {
                exileGraveyardCardIndices = selectNGraveyardIndicesToExile(gameData, findExileNGraveyardCost(card));
                if (exileGraveyardCardIndices == null) {
                    continue; // Not enough matching graveyard cards, try next spell
                }
            }

            // Select sacrifice target if the spell has a sacrifice cost
            UUID sacrificePermanentId = selectRandomSacrificeTarget(gameData, card);

            // Calculate X value (for modal spells, xValue is the mode index)
            ManaCost castCost = new ManaCost(card.getManaCost());
            Integer xValue = modalPlan != null ? modalPlan.modeIndex() : null;
            if (castCost.hasX() && xValue == null) {
                int costModifier = gameBroadcastService.getCastCostModifier(gameData, aiPlayer.getId(), card) + targetingTax;
                int maxX = manaManager.calculateMaxAffordableX(card, virtualPool, costModifier);
                maxX = Math.min(maxX, getMaxXForGraveyardRequirements(gameData, card));
                if (maxX <= 0) {
                    continue;
                }
                // For requiresManaValueEqualsX spells (e.g. Postmortem Lunge), X must match the
                // graveyard target's mana value — re-pick an affordable target and set X accordingly.
                if (hasRequiresManaValueEqualsX(card)) {
                    List<Card> validTargets = targetSelector.findValidGraveyardTargets(
                            gameData, card, aiPlayer.getId(), maxX);
                    if (validTargets.isEmpty()) {
                        continue;
                    }
                    Card chosen = validTargets.get(rng.nextInt(validTargets.size()));
                    targetId = chosen.getId();
                    xValue = chosen.getManaValue();
                } else if (hasPermanentManaValueEqualsXTarget(card)) {
                    // For PermanentManaValueEqualsXPredicate spells (e.g. Entrancing Melody),
                    // X must match the target permanent's mana value — co-select target and X.
                    List<Permanent> validTargets = targetSelector.findValidPermanentTargetsForManaValueX(
                            gameData, card, aiPlayer.getId(), maxX);
                    if (validTargets.isEmpty()) {
                        continue;
                    }
                    Permanent chosen = validTargets.get(rng.nextInt(validTargets.size()));
                    targetId = chosen.getId();
                    xValue = chosen.getCard().getManaValue();
                } else {
                    // Pick a random X between 1 and maxX
                    xValue = rng.nextInt(maxX) + 1;
                }
            }

            log.info("Random AI: Casting {}{} in game {}", card.getName(),
                    xValue != null ? " (X=" + xValue + ")" : "", gameId);
            if (tapManaForSpell(gameData, card, xValue, targetingTax)) {
                return true; // Mana ability triggered a pending choice; will resume after it resolves
            }
            final UUID finalTargetId = targetId;
            final Integer finalXValue = xValue;
            final Integer finalExileGraveyardCardIndex = exileGraveyardCardIndex;
            final List<Integer> finalExileGraveyardCardIndices = exileGraveyardCardIndices;
            final UUID finalSacrificePermanentId = sacrificePermanentId;
            final Map<UUID, Integer> finalDamageAssignments = damageAssignments;
            final List<UUID> finalMultiTargetIds = multiTargetIds;
            send(() -> messageHandler.handlePlayCard(selfConnection,
                    new PlayCardRequest(cardIndex, finalXValue, finalTargetId, finalDamageAssignments, finalMultiTargetIds, null, null, finalSacrificePermanentId, null, null, null, null, finalExileGraveyardCardIndex, finalExileGraveyardCardIndices, null, null, null)));

            // Identity check: hand size alone is unreliable because ETB/cast triggers
            // can add cards back to hand (e.g. Explore revealing a land), masking a
            // successful cast.
            if (hand.contains(card)) {
                ManaPool actualPool = gameData.playerManaPools.get(aiPlayer.getId());
                log.warn("Random AI: PlayCard failed silently in game {}. Card='{}' index={} step={} activePlayer={} isActive={} stackEmpty={} actualPool={} virtualPool={} priorityPassed={}",
                        gameId, card.getName(), cardIndex, gameData.currentStep,
                        gameData.activePlayerId, aiPlayer.getId().equals(gameData.activePlayerId),
                        gameData.stack.isEmpty(), actualPool != null ? actualPool.toMap() : "null",
                        virtualPool.toMap(), gameData.priorityPassedBy);
                continue; // Try next spell
            }
            return true;
        }
        return false;
    }

    /**
     * Finds an ExileCardFromGraveyardCost in the card's SPELL effects, if any.
     */
    private ExileCardFromGraveyardCost findExileGraveyardCost(Card card) {
        for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
            if (effect instanceof ExileCardFromGraveyardCost cost) {
                return cost;
            }
        }
        return null;
    }

    /**
     * Finds a random valid graveyard card index matching the exile cost's required type.
     * Returns null if no valid card exists.
     */
    private Integer findValidGraveyardIndex(List<Card> graveyard, ExileCardFromGraveyardCost cost) {
        List<Integer> validIndices = new ArrayList<>();
        for (int i = 0; i < graveyard.size(); i++) {
            Card graveyardCard = graveyard.get(i);
            if (cost.requiredType() == null || graveyardCard.hasType(cost.requiredType())) {
                validIndices.add(i);
            }
        }
        if (validIndices.isEmpty()) {
            return null;
        }
        return validIndices.get(rng.nextInt(validIndices.size()));
    }

    /**
     * Returns true if the card has a ReturnCardFromGraveyardEffect with requiresManaValueEqualsX,
     * meaning X must match the graveyard target's mana value (e.g. Postmortem Lunge).
     */
    private boolean hasRequiresManaValueEqualsX(Card card) {
        return card.getEffects(EffectSlot.SPELL).stream()
                .anyMatch(e -> e instanceof ReturnCardFromGraveyardEffect rge && rge.requiresManaValueEqualsX());
    }

    // ===== Random Sacrifice Target Selection =====

    /**
     * Selects a random valid permanent to sacrifice for the card's sacrifice cost.
     * Returns null if the card has no sacrifice cost.
     */
    private UUID selectRandomSacrificeTarget(GameData gameData, Card card) {
        List<Permanent> battlefield = gameData.playerBattlefields.getOrDefault(aiPlayer.getId(), List.of());
        for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
            if (effect instanceof SacrificeCreatureCost) {
                List<Permanent> creatures = battlefield.stream()
                        .filter(p -> gameQueryService.isCreature(gameData, p))
                        .toList();
                return creatures.isEmpty() ? null : creatures.get(rng.nextInt(creatures.size())).getId();
            } else if (effect instanceof SacrificeArtifactCost) {
                List<Permanent> artifacts = battlefield.stream()
                        .filter(p -> gameQueryService.isArtifact(gameData, p))
                        .toList();
                return artifacts.isEmpty() ? null : artifacts.get(rng.nextInt(artifacts.size())).getId();
            } else if (effect instanceof SacrificePermanentCost sacCost) {
                List<Permanent> matching = battlefield.stream()
                        .filter(p -> gameQueryService.matchesPermanentPredicate(gameData, p, sacCost.filter()))
                        .toList();
                return matching.isEmpty() ? null : matching.get(rng.nextInt(matching.size())).getId();
            }
        }
        return null;
    }

    // ===== Random Target Selection =====

    private UUID pickRandomTarget(GameData gameData, Card card) {
        List<UUID> validTargets = new ArrayList<>();
        UUID opponentId = AiUtils.getOpponentId(gameData, aiPlayer.getId());

        // Use base-mode targeting since AI never kicks spells
        Set<TargetType> allowed = targetSelector.computeBaseAllowedTargets(card);

        // Add players as targets if allowed, respecting player relation predicates and hexproof/shroud
        if (allowed.contains(TargetType.PLAYER)) {
            PlayerRelation relation = PlayerRelation.ANY;
            if (card.getTargetFilter() instanceof PlayerPredicateTargetFilter ptf
                    && ptf.predicate() instanceof PlayerRelationPredicate prp) {
                relation = prp.relation();
            }
            if (relation != PlayerRelation.OPPONENT
                    && !gameQueryService.playerHasShroud(gameData, aiPlayer.getId())) {
                validTargets.add(aiPlayer.getId());
            }
            if (relation != PlayerRelation.SELF && opponentId != null
                    && !gameQueryService.playerHasShroud(gameData, opponentId)
                    && !gameQueryService.playerHasHexproof(gameData, opponentId)) {
                validTargets.add(opponentId);
            }
        }

        // Add permanents as targets (unless it only targets players)
        if (!card.isEnchantPlayer() && (!allowed.contains(TargetType.PLAYER) || allowed.contains(TargetType.PERMANENT) || card.isAura())) {
            for (UUID playerId : gameData.orderedPlayerIds) {
                List<Permanent> field = gameData.playerBattlefields.getOrDefault(playerId, List.of());
                for (Permanent p : field) {
                    if (targetSelector.isValidPermanentTarget(gameData, card, p, aiPlayer.getId())) {
                        validTargets.add(p.getId());
                    }
                }
            }
        }

        // Add graveyard cards as targets if allowed
        if (allowed.contains(TargetType.GRAVEYARD)) {
            for (Card c : targetSelector.findValidGraveyardTargets(gameData, card, aiPlayer.getId())) {
                validTargets.add(c.getId());
            }
        }

        if (validTargets.isEmpty()) {
            return null;
        }
        return validTargets.get(rng.nextInt(validTargets.size()));
    }

    // ===== Combat: Random Attackers =====

    @Override
    protected void handleAttackers(GameData gameData) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(aiPlayer.getId());
        List<Integer> availableIndices = combatAttackService.getAttackableCreatureIndices(gameData, aiPlayer.getId());
        if (battlefield == null || availableIndices.isEmpty()) {
            send(() -> messageHandler.handleDeclareAttackers(selfConnection, new DeclareAttackersRequest(List.of(), null)));
            return;
        }

        // Each available attacker has a 50% chance of attacking
        List<Integer> attackerIndices = new ArrayList<>();
        for (int i : availableIndices) {
            if (rng.nextBoolean()) {
                attackerIndices.add(i);
            }
        }

        // Ensure creatures with "attacks each combat if able" are included
        List<Integer> mustAttackIndices = combatAttackService.getMustAttackIndices(gameData, aiPlayer.getId(), availableIndices);
        attackerIndices = enforceMustAttack(attackerIndices, mustAttackIndices);

        // CR 508.1b: if only one attacker selected and it can't attack alone, try to
        // pair it with another available attacker before tax prep. prepareAttackersForTax
        // applies a final safety net if it can't.
        if (attackerIndices.size() == 1) {
            Permanent sole = battlefield.get(attackerIndices.getFirst());
            if (sole.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(CantAttackOrBlockAloneEffect.class::isInstance)) {
                List<Integer> others = new ArrayList<>(availableIndices);
                others.removeAll(attackerIndices);
                if (!others.isEmpty()) {
                    attackerIndices.add(others.get(rng.nextInt(others.size())));
                } else {
                    attackerIndices.clear();
                }
            }
        }

        // Ensure at least one attacker when forced (e.g. Trove of Temptation)
        attackerIndices = enforceMustAttackWithAtLeastOne(gameData, attackerIndices, availableIndices);

        // Cap attackers to what we can afford given attack tax, and tap mana to pay
        attackerIndices = prepareAttackersForTax(gameData, attackerIndices);

        log.info("Random AI: Declaring {} of {} attackers in game {}",
                attackerIndices.size(), availableIndices.size(), gameId);
        final List<Integer> finalAttackerIndices = attackerIndices;
        send(() -> messageHandler.handleDeclareAttackers(selfConnection, new DeclareAttackersRequest(finalAttackerIndices, null)));
    }

    // ===== Combat: Random Blockers =====

    @Override
    protected void handleBlockers(GameData gameData) {
        try {
            handleBlockersInternal(gameData);
        } catch (Exception e) {
            log.error("Random AI: Error in handleBlockers in game {}, sending empty blockers", gameId, e);
            sendBlockerDeclaration(gameData, new DeclareBlockersRequest(List.of()));
        }
    }

    private void handleBlockersInternal(GameData gameData) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(aiPlayer.getId());
        UUID opponentId = AiUtils.getOpponentId(gameData, aiPlayer.getId());
        List<Permanent> opponentBattlefield = gameData.playerBattlefields.getOrDefault(opponentId, List.of());

        if (battlefield == null || opponentBattlefield == null) {
            sendBlockerDeclaration(gameData, new DeclareBlockersRequest(List.of()));
            return;
        }

        List<Integer> attackerIndices = new ArrayList<>();
        for (int i = 0; i < opponentBattlefield.size(); i++) {
            Permanent perm = opponentBattlefield.get(i);
            if (perm.isAttacking() && !gameQueryService.hasCantBeBlocked(gameData, perm)) {
                attackerIndices.add(i);
            }
        }
        if (attackerIndices.isEmpty()) {
            sendBlockerDeclaration(gameData, new DeclareBlockersRequest(List.of()));
            return;
        }

        List<Integer> availableBlockerIndices = new ArrayList<>();
        for (int j = 0; j < battlefield.size(); j++) {
            Permanent blocker = battlefield.get(j);
            if (gameQueryService.canBlock(gameData, blocker)) {
                availableBlockerIndices.add(j);
            }
        }

        // Resolve per-creature mustBlockIds (Provoke, etc.): blocker → attacker pairs it's
        // obligated to attempt. Collected per-attacker so menace/lure logic can fold them
        // in atomically.
        Map<Integer, List<Integer>> provokedBlockersByAttacker = new HashMap<>();
        for (int blockerIdx : availableBlockerIndices) {
            Permanent blocker = battlefield.get(blockerIdx);
            if (blocker.getMustBlockIds().isEmpty()) continue;
            for (UUID mustBlockId : blocker.getMustBlockIds()) {
                for (int attackerIdx : attackerIndices) {
                    Permanent attacker = opponentBattlefield.get(attackerIdx);
                    if (attacker.getId().equals(mustBlockId) && canBlock(gameData, blocker, attacker)) {
                        provokedBlockersByAttacker.computeIfAbsent(attackerIdx, k -> new ArrayList<>()).add(blockerIdx);
                        break;
                    }
                }
            }
        }

        Set<Integer> lureAttackerIndices = findLureAttackers(gameData, opponentBattlefield);
        Set<Integer> mustBeBlockedAttackerIndices = findMustBeBlockedAttackers(gameData, opponentBattlefield);

        // Sort by priority group: lure → menace-lure → mustBlockIfAble → provoked → regular.
        // Random within each group (Random AI preserves randomness but respects constraint priority).
        List<Integer> sortedAttackers = new ArrayList<>(attackerIndices);
        Collections.shuffle(sortedAttackers, rng);
        sortedAttackers.sort((a, b) -> Integer.compare(priorityGroup(gameData, opponentBattlefield, b,
                lureAttackerIndices, mustBeBlockedAttackerIndices, provokedBlockersByAttacker),
                priorityGroup(gameData, opponentBattlefield, a,
                        lureAttackerIndices, mustBeBlockedAttackerIndices, provokedBlockersByAttacker)));

        List<BlockerAssignment> assignments = new ArrayList<>();
        boolean[] blockerUsed = new boolean[battlefield.size()];

        for (int attackerIdx : sortedAttackers) {
            Permanent attacker = opponentBattlefield.get(attackerIdx);
            boolean menace = gameQueryService.hasKeyword(gameData, attacker, Keyword.MENACE);
            boolean lure = lureAttackerIndices.contains(attackerIdx);
            boolean mustBlock = mustBeBlockedAttackerIndices.contains(attackerIdx);
            List<Integer> provoked = provokedBlockersByAttacker.getOrDefault(attackerIdx, List.of());

            List<Integer> candidates = new ArrayList<>();
            for (int blockerIdx : availableBlockerIndices) {
                if (blockerUsed[blockerIdx]) continue;
                Permanent blocker = battlefield.get(blockerIdx);
                if (canBlock(gameData, blocker, attacker)) {
                    candidates.add(blockerIdx);
                }
            }
            if (candidates.isEmpty()) continue;
            // Menace: no creature is "able to block" alone — with <2 candidates, skip.
            if (menace && candidates.size() < 2) continue;

            List<Integer> chosen;
            if (lure) {
                // Every able blocker must block this attacker.
                chosen = new ArrayList<>(candidates);
            } else if (!provoked.isEmpty()) {
                // Provoked blockers must block; add a menace partner if required.
                List<Integer> provokedUnused = new ArrayList<>();
                for (int p : provoked) {
                    if (!blockerUsed[p] && candidates.contains(p)) provokedUnused.add(p);
                }
                if (provokedUnused.isEmpty()) {
                    chosen = List.of();
                } else if (menace && provokedUnused.size() == 1) {
                    int partner = -1;
                    for (int c : candidates) {
                        if (c != provokedUnused.get(0)) { partner = c; break; }
                    }
                    chosen = partner != -1 ? List.of(provokedUnused.get(0), partner) : List.of();
                } else {
                    chosen = new ArrayList<>(provokedUnused);
                }
            } else if (mustBlock) {
                List<Integer> shuffled = new ArrayList<>(candidates);
                Collections.shuffle(shuffled, rng);
                int needed = menace ? 2 : 1;
                chosen = shuffled.subList(0, Math.min(needed, shuffled.size()));
            } else {
                // Voluntary block: 50% to try, randomly pick 1 (or 2 with menace).
                if (!rng.nextBoolean()) continue;
                List<Integer> shuffled = new ArrayList<>(candidates);
                Collections.shuffle(shuffled, rng);
                int needed = menace ? 2 : 1;
                chosen = shuffled.subList(0, Math.min(needed, shuffled.size()));
            }

            if (chosen.isEmpty()) continue;

            for (int blockerIdx : chosen) {
                assignments.add(new BlockerAssignment(blockerIdx, attackerIdx));
                blockerUsed[blockerIdx] = true;
            }
        }

        // CR 509.1b: if only one unique blocker and it can't block alone, remove it.
        Set<Integer> uniqueBlockerIndices = new HashSet<>();
        for (BlockerAssignment a : assignments) {
            uniqueBlockerIndices.add(a.blockerIndex());
        }
        if (uniqueBlockerIndices.size() == 1) {
            Permanent sole = battlefield.get(uniqueBlockerIndices.iterator().next());
            if (sole.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(CantAttackOrBlockAloneEffect.class::isInstance)) {
                assignments.clear();
            }
        }

        log.info("Random AI: Declaring {} blockers in game {}", assignments.size(), gameId);
        sendBlockerDeclaration(gameData, new DeclareBlockersRequest(assignments));
    }

    /**
     * Priority rank for attacker iteration. Higher ranks are processed first so the most
     * constrained attackers claim their required blockers before less-constrained ones drain
     * the pool.
     */
    private int priorityGroup(GameData gameData, List<Permanent> opponentBattlefield, int attackerIdx,
                              Set<Integer> lureAttackers, Set<Integer> mustBlockAttackers,
                              Map<Integer, List<Integer>> provokedByAttacker) {
        Permanent attacker = opponentBattlefield.get(attackerIdx);
        boolean lure = lureAttackers.contains(attackerIdx);
        boolean menace = gameQueryService.hasKeyword(gameData, attacker, Keyword.MENACE);
        if (lure && menace) return 5;
        if (lure) return 4;
        if (mustBlockAttackers.contains(attackerIdx)) return 3;
        if (provokedByAttacker.containsKey(attackerIdx)) return 2;
        return 1;
    }

    // ===== Block legality check =====

    private boolean canBlock(GameData gameData, Permanent blocker, Permanent attacker) {
        List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(aiPlayer.getId());
        return gameQueryService.canBlockAttacker(gameData, blocker, attacker, defenderBattlefield);
    }

    private Set<Integer> findLureAttackers(GameData gameData, List<Permanent> opponentBattlefield) {
        Set<Integer> lureIndices = new HashSet<>();
        for (int i = 0; i < opponentBattlefield.size(); i++) {
            Permanent attacker = opponentBattlefield.get(i);
            if (!attacker.isAttacking()) continue;
            boolean hasLure = attacker.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(MustBeBlockedByAllCreaturesEffect.class::isInstance)
                    || gameQueryService.hasAuraWithEffect(gameData, attacker, MustBeBlockedByAllCreaturesEffect.class);
            if (hasLure) {
                lureIndices.add(i);
            }
        }
        return lureIndices;
    }

    private Set<Integer> findMustBeBlockedAttackers(GameData gameData, List<Permanent> opponentBattlefield) {
        Set<Integer> indices = new HashSet<>();
        for (int i = 0; i < opponentBattlefield.size(); i++) {
            Permanent attacker = opponentBattlefield.get(i);
            if (!attacker.isAttacking()) continue;
            boolean mustBeBlocked = attacker.isMustBeBlockedThisTurn()
                    || attacker.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(MustBeBlockedIfAbleEffect.class::isInstance)
                    || gameQueryService.hasAuraWithEffect(gameData, attacker, MustBeBlockedIfAbleEffect.class);
            if (mustBeBlocked) {
                indices.add(i);
            }
        }
        return indices;
    }

    // ===== Card Choice (random discard) =====

    @Override
    protected void handleCardChoice(GameData gameData) {
        var cardChoice = gameData.interaction.cardChoiceContext();
        if (cardChoice == null) {
            return;
        }
        UUID choicePlayerId = cardChoice.playerId();
        Set<Integer> validIndices = cardChoice.validIndices();

        if (!aiPlayer.getId().equals(choicePlayerId)) {
            return;
        }

        if (validIndices == null || validIndices.isEmpty()) {
            return;
        }

        // Pick a random valid index
        List<Integer> indices = new ArrayList<>(validIndices);
        int chosen = indices.get(rng.nextInt(indices.size()));

        log.info("Random AI: Choosing card at index {} in game {}", chosen, gameId);
        send(() -> messageHandler.handleCardChosen(selfConnection, new CardChosenRequest(chosen)));
    }

    // ===== Mulligan: always keep (speeds up games) =====

    @Override
    protected boolean shouldKeepHand(GameData gameData) {
        return true;
    }
}
