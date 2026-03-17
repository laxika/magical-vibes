package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TargetType;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.model.effect.CantAttackOrBlockAloneEffect;
import com.github.laxika.magicalvibes.model.effect.MustBeBlockedByAllCreaturesEffect;
import com.github.laxika.magicalvibes.networking.MessageHandler;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.networking.message.CardChosenRequest;
import com.github.laxika.magicalvibes.networking.message.DeclareAttackersRequest;
import com.github.laxika.magicalvibes.networking.message.DeclareBlockersRequest;
import com.github.laxika.magicalvibes.networking.message.PassPriorityRequest;
import com.github.laxika.magicalvibes.networking.message.PlayCardRequest;
import com.github.laxika.magicalvibes.service.GameRegistry;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.combat.CombatAttackService;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
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
                           CombatAttackService combatAttackService, Random rng) {
        super(gameId, aiPlayer, gameRegistry, messageHandler, gameQueryService, combatAttackService);
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

            if (tryCastRandomSpell(gameData, false)) {
                return;
            }
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
            if (card.isNeedsSpellTarget()) {
                continue;
            }

            ManaCost cost = new ManaCost(card.getManaCost());
            if (cost.hasX()) {
                if (!cost.canPay(virtualPool, 1)) continue;
            } else {
                if (!cost.canPay(virtualPool)) continue;
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

            // Determine target if needed
            UUID targetId = null;
            if (card.isNeedsTarget() || card.isAura()) {
                targetId = pickRandomTarget(gameData, card);
                if (targetId == null) {
                    continue; // No valid target, try next spell
                }
            }

            // Calculate X value and tap lands
            ManaCost castCost = new ManaCost(card.getManaCost());
            Integer xValue = null;
            if (castCost.hasX()) {
                int maxX = manaManager.calculateMaxAffordableX(card, virtualPool);
                if (maxX <= 0) {
                    continue;
                }
                // Pick a random X between 1 and maxX
                xValue = rng.nextInt(maxX) + 1;
                manaManager.tapLandsForXSpell(gameData, aiPlayer.getId(), card, xValue, tapPermanentAction());
            } else {
                manaManager.tapLandsForCost(gameData, aiPlayer.getId(), card.getManaCost(), tapPermanentAction());
            }

            log.info("Random AI: Casting {}{} in game {}", card.getName(),
                    xValue != null ? " (X=" + xValue + ")" : "", gameId);
            int handSizeBefore = hand.size();
            final UUID finalTargetId = targetId;
            final Integer finalXValue = xValue;
            send(() -> messageHandler.handlePlayCard(selfConnection,
                    new PlayCardRequest(cardIndex, finalXValue, finalTargetId, null, null, null, null, null, null, null, null, null, null)));

            if (hand.size() >= handSizeBefore) {
                log.warn("Random AI: PlayCard failed silently in game {}", gameId);
                continue; // Try next spell
            }
            return true;
        }
        return false;
    }

    // ===== Random Target Selection =====

    private UUID pickRandomTarget(GameData gameData, Card card) {
        List<UUID> validTargets = new ArrayList<>();
        UUID opponentId = AiUtils.getOpponentId(gameData, aiPlayer.getId());

        Set<TargetType> allowed = card.getAllowedTargets();

        // Add players as targets if allowed
        if (allowed.contains(TargetType.PLAYER)) {
            validTargets.add(aiPlayer.getId());
            if (opponentId != null) {
                validTargets.add(opponentId);
            }
        }

        // Add permanents as targets (unless it only targets players)
        if (!allowed.contains(TargetType.PLAYER) || allowed.contains(TargetType.PERMANENT) || card.isAura()) {
            for (UUID playerId : gameData.orderedPlayerIds) {
                List<Permanent> field = gameData.playerBattlefields.getOrDefault(playerId, List.of());
                for (Permanent p : field) {
                    if (targetSelector.passesTargetFilter(gameData, card, p, aiPlayer.getId())) {
                        validTargets.add(p.getId());
                    }
                }
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

        // CR 508.1b: if only one attacker selected and it can't attack alone, fix it
        if (attackerIndices.size() == 1) {
            Permanent sole = battlefield.get(attackerIndices.getFirst());
            if (sole.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(CantAttackOrBlockAloneEffect.class::isInstance)) {
                // Try to add another random attacker; if none available, remove the lone attacker
                List<Integer> others = new ArrayList<>(availableIndices);
                others.removeAll(attackerIndices);
                if (!others.isEmpty()) {
                    attackerIndices.add(others.get(rng.nextInt(others.size())));
                } else {
                    attackerIndices.clear();
                }
            }
        }

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

        // Find all attacking creatures that can be blocked
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

        // Find all available blockers using the same canBlock() check as the game engine
        List<Integer> availableBlockerIndices = new ArrayList<>();
        for (int j = 0; j < battlefield.size(); j++) {
            Permanent blocker = battlefield.get(j);
            if (gameQueryService.canBlock(gameData, blocker)) {
                availableBlockerIndices.add(j);
            }
        }

        List<BlockerAssignment> assignments = new ArrayList<>();
        boolean[] blockerUsed = new boolean[battlefield.size()];

        // Phase 1: Satisfy lure (MustBeBlockedByAllCreatures) requirements.
        // Every creature that CAN block a lure attacker MUST do so.
        Set<Integer> lureAttackerIndices = findLureAttackers(gameData, opponentBattlefield);
        if (!lureAttackerIndices.isEmpty()) {
            for (int blockerIdx : availableBlockerIndices) {
                if (blockerUsed[blockerIdx]) continue;
                Permanent blocker = battlefield.get(blockerIdx);
                for (int attackerIdx : lureAttackerIndices) {
                    Permanent attacker = opponentBattlefield.get(attackerIdx);
                    if (canBlock(gameData, blocker, attacker)) {
                        assignments.add(new BlockerAssignment(blockerIdx, attackerIdx));
                        blockerUsed[blockerIdx] = true;
                        break;
                    }
                }
            }
        }

        // Phase 2: Satisfy per-creature mustBlockIds requirements (Provoke, etc.)
        for (int blockerIdx : availableBlockerIndices) {
            if (blockerUsed[blockerIdx]) continue;
            Permanent blocker = battlefield.get(blockerIdx);
            if (blocker.getMustBlockIds().isEmpty()) continue;

            for (UUID mustBlockId : blocker.getMustBlockIds()) {
                for (int attackerIdx : attackerIndices) {
                    Permanent attacker = opponentBattlefield.get(attackerIdx);
                    if (attacker.getId().equals(mustBlockId) && canBlock(gameData, blocker, attacker)) {
                        assignments.add(new BlockerAssignment(blockerIdx, attackerIdx));
                        blockerUsed[blockerIdx] = true;
                        break;
                    }
                }
                if (blockerUsed[blockerIdx]) break;
            }
        }

        // Phase 3: Randomly assign remaining blockers to remaining attackers
        List<Integer> remainingAttackers = new ArrayList<>(attackerIndices);
        Collections.shuffle(remainingAttackers, rng);

        for (int attackerIdx : remainingAttackers) {
            // 50% chance to try blocking this attacker
            if (!rng.nextBoolean()) {
                continue;
            }

            Permanent attacker = opponentBattlefield.get(attackerIdx);

            // Collect unused blockers that can legally block this attacker, in random order
            List<Integer> candidates = new ArrayList<>();
            for (int blockerIdx : availableBlockerIndices) {
                if (blockerUsed[blockerIdx]) continue;
                Permanent blocker = battlefield.get(blockerIdx);
                if (canBlock(gameData, blocker, attacker)) {
                    candidates.add(blockerIdx);
                }
            }
            if (candidates.isEmpty()) continue;

            // Menace requires at least 2 blockers — skip if we don't have enough candidates
            boolean hasMenace = gameQueryService.hasKeyword(gameData, attacker, Keyword.MENACE);
            if (hasMenace && candidates.size() < 2) {
                continue;
            }

            Collections.shuffle(candidates, rng);
            if (hasMenace) {
                // Assign exactly 2 blockers for menace creatures
                assignments.add(new BlockerAssignment(candidates.get(0), attackerIdx));
                blockerUsed[candidates.get(0)] = true;
                assignments.add(new BlockerAssignment(candidates.get(1), attackerIdx));
                blockerUsed[candidates.get(1)] = true;
            } else {
                assignments.add(new BlockerAssignment(candidates.get(0), attackerIdx));
                blockerUsed[candidates.get(0)] = true;
            }
        }

        // CR 509.1b: if only one unique blocker and it can't block alone, remove it
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
