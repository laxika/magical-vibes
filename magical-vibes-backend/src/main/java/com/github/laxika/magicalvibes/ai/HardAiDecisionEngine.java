package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.ai.simulation.GameSimulator;
import com.github.laxika.magicalvibes.ai.simulation.MCTSEngine;
import com.github.laxika.magicalvibes.ai.simulation.SimulationAction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.MessageHandler;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.networking.message.CardChosenRequest;
import com.github.laxika.magicalvibes.networking.message.DeclareAttackersRequest;
import com.github.laxika.magicalvibes.networking.message.DeclareBlockersRequest;
import com.github.laxika.magicalvibes.networking.message.PassPriorityRequest;
import com.github.laxika.magicalvibes.networking.message.PlayCardRequest;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.combat.CombatAttackService;
import com.github.laxika.magicalvibes.service.GameRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import java.util.function.IntConsumer;

/**
 * Hard difficulty AI that uses Information Set Monte Carlo Tree Search (IS-MCTS)
 * to make decisions. Falls back to SpellEvaluator/CombatSimulator-based logic
 * (same as Medium) when MCTS is not applicable (0-1 options) or fails.
 */
@Slf4j
public class HardAiDecisionEngine extends AiDecisionEngine {

    private static final int MCTS_BUDGET = 50000;

    private final SpellEvaluator spellEvaluator;
    private final CombatSimulator combatSimulator;
    private final MCTSEngine mctsEngine;

    public HardAiDecisionEngine(UUID gameId, Player aiPlayer, GameRegistry gameRegistry,
                                MessageHandler messageHandler, GameQueryService gameQueryService,
                                CombatAttackService combatAttackService) {
        super(gameId, aiPlayer, gameRegistry, messageHandler, gameQueryService, combatAttackService);
        BoardEvaluator boardEvaluator = new BoardEvaluator(gameQueryService);
        this.spellEvaluator = new SpellEvaluator(gameQueryService, boardEvaluator);
        this.combatSimulator = new CombatSimulator(gameQueryService, boardEvaluator);
        this.mctsEngine = new MCTSEngine(new GameSimulator(gameQueryService));
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
            if (tryPlayLand(gameData)) {
                return;
            }

            if (tryCastSpellMCTS(gameData)) {
                return;
            }
        }

        send(() -> messageHandler.handlePassPriority(selfConnection, new PassPriorityRequest()));
    }

    /**
     * Uses MCTS to decide which spell to cast (or whether to pass).
     * Falls back to evaluator-based logic for 0-1 castable options.
     */
    private boolean tryCastSpellMCTS(GameData gameData) {
        List<Card> hand = gameData.playerHands.get(aiPlayer.getId());
        if (hand == null) return false;

        ManaPool virtualPool = manaManager.buildVirtualManaPool(gameData, aiPlayer.getId());

        // Count castable spells
        int castableCount = 0;
        for (Card card : hand) {
            if (card.hasType(CardType.LAND) || card.hasType(CardType.INSTANT)) continue;
            if (card.getManaCost() == null) continue;
            ManaCost cost = new ManaCost(card.getManaCost());
            if (cost.hasX()) {
                if (cost.canPay(virtualPool, 1)) castableCount++;
            } else {
                if (cost.canPay(virtualPool)) castableCount++;
            }
        }

        // If 0-1 options, use evaluator-based logic (no need for MCTS)
        if (castableCount <= 1) {
            return tryCastSpell(gameData);
        }

        // Use MCTS to decide
        try {
            SimulationAction bestAction = mctsEngine.search(gameData, aiPlayer.getId(), MCTS_BUDGET);

            if (bestAction instanceof SimulationAction.PlayCard pc) {
                Card card = hand.get(pc.handIndex());
                ManaCost castCost = new ManaCost(card.getManaCost());
                Integer xValue = null;
                var tapAction = tapPermanentAction();
                if (castCost.hasX()) {
                    int smartX = manaManager.calculateSmartX(gameData, card, pc.targetId(), virtualPool);
                    if (smartX <= 0) {
                        return false;
                    }
                    xValue = smartX;
                    manaManager.tapLandsForXSpell(gameData, aiPlayer.getId(), card, smartX, tapAction);
                } else {
                    manaManager.tapLandsForCost(gameData, aiPlayer.getId(), card.getManaCost(), tapAction);
                }
                log.info("AI (Hard/MCTS): Casting {}{} in game {}", card.getName(),
                        xValue != null ? " (X=" + xValue + ")" : "", gameId);
                int handSizeBefore = hand.size();
                final int cardIndex = pc.handIndex();
                final UUID targetId = pc.targetId();
                final Integer finalXValue = xValue;
                send(() -> messageHandler.handlePlayCard(selfConnection,
                        new PlayCardRequest(cardIndex, finalXValue, targetId, null, null, null, null, null, null, null, null, null, null, null, null)));
                // Verify the spell was actually cast — handlePlayCard silently
                // swallows errors, so we must confirm the state actually changed.
                if (hand.size() >= handSizeBefore) {
                    log.warn("AI (Hard/MCTS): PlayCard failed silently in game {}", gameId);
                    return false;
                }
                return true;
            }

            if (bestAction instanceof SimulationAction.PassPriority) {
                log.info("AI (Hard/MCTS): MCTS recommends passing in game {}", gameId);
                return false; // Let handleGameState send the pass
            }
        } catch (Exception e) {
            log.warn("AI (Hard): MCTS failed, falling back to evaluator logic in game {}", gameId, e);
            return tryCastSpell(gameData);
        }

        return false;
    }

    // ===== Spell Casting (evaluator-based fallback) =====

    private boolean tryCastSpell(GameData gameData) {
        List<Card> hand = gameData.playerHands.get(aiPlayer.getId());
        if (hand == null) {
            return false;
        }

        ManaPool virtualPool = manaManager.buildVirtualManaPool(gameData, aiPlayer.getId());

        record CastCandidate(int index, double value) {}
        List<CastCandidate> candidates = new ArrayList<>();

        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            if (card.hasType(CardType.LAND)) continue;
            if (card.hasType(CardType.INSTANT)) continue;
            if (card.getManaCost() == null) continue;

            ManaCost cost = new ManaCost(card.getManaCost());
            if (cost.hasX()) {
                if (!cost.canPay(virtualPool, 1)) continue;
            } else {
                if (!cost.canPay(virtualPool)) continue;
            }

            double value = spellEvaluator.estimateSpellValue(gameData, card, aiPlayer.getId());
            if (value > 0) {
                candidates.add(new CastCandidate(i, value));
            }
        }

        if (candidates.isEmpty()) {
            return false;
        }

        candidates.sort(Comparator.comparingDouble(CastCandidate::value).reversed());
        CastCandidate best = candidates.getFirst();
        Card card = hand.get(best.index);

        UUID targetId = null;
        if (card.isNeedsTarget() || card.isAura()) {
            targetId = targetSelector.chooseTarget(gameData, card, aiPlayer.getId());
            if (targetId == null) {
                return false;
            }
        }

        ManaCost castCost = new ManaCost(card.getManaCost());
        Integer xValue = null;
        IntConsumer tapAction = tapPermanentAction();
        if (castCost.hasX()) {
            int smartX = manaManager.calculateSmartX(gameData, card, targetId, virtualPool);
            if (smartX <= 0) {
                return false;
            }
            xValue = smartX;
            manaManager.tapLandsForXSpell(gameData, aiPlayer.getId(), card, smartX, tapAction);
        } else {
            manaManager.tapLandsForCost(gameData, aiPlayer.getId(), card.getManaCost(), tapAction);
        }

        log.info("AI (Hard): Casting {}{} (value={}) in game {}", card.getName(),
                xValue != null ? " (X=" + xValue + ")" : "",
                String.format("%.1f", best.value), gameId);
        int handSizeBefore = hand.size();
        final UUID finalTargetId = targetId;
        final int cardIndex = best.index;
        final Integer finalXValue = xValue;
        send(() -> messageHandler.handlePlayCard(selfConnection,
                new PlayCardRequest(cardIndex, finalXValue, finalTargetId, null, null, null, null, null, null, null, null, null, null, null, null)));
        // Verify the spell was actually cast — handlePlayCard silently
        // swallows errors, so we must confirm the state actually changed.
        if (hand.size() >= handSizeBefore) {
            log.warn("AI (Hard): PlayCard failed silently in game {}", gameId);
            return false;
        }
        return true;
    }

    // ===== Combat =====

    @Override
    protected void handleAttackers(GameData gameData) {
        List<Integer> availableIndices = combatAttackService.getAttackableCreatureIndices(gameData, aiPlayer.getId());
        if (availableIndices.isEmpty()) {
            send(() -> messageHandler.handleDeclareAttackers(selfConnection,
                    new DeclareAttackersRequest(List.of(), null)));
            return;
        }

        List<Integer> mustAttackIndices = combatAttackService.getMustAttackIndices(gameData, aiPlayer.getId(), availableIndices);

        // If 0-1 attackers, use CombatSimulator (no need for MCTS)
        if (availableIndices.size() <= 1) {
            handleAttackersWithSimulator(gameData, availableIndices, mustAttackIndices);
            return;
        }

        // Use MCTS for attacker selection
        try {
            SimulationAction bestAction = mctsEngine.search(gameData, aiPlayer.getId(), MCTS_BUDGET);

            if (bestAction instanceof SimulationAction.DeclareAttackers da) {
                // Ensure must-attack creatures are included in the MCTS result
                List<Integer> attackerIndices = enforceMustAttack(da.attackerIndices(), mustAttackIndices);
                log.info("AI (Hard/MCTS): Declaring {} attackers in game {}", attackerIndices.size(), gameId);
                send(() -> messageHandler.handleDeclareAttackers(selfConnection,
                        new DeclareAttackersRequest(attackerIndices, null)));
                return;
            }
        } catch (Exception e) {
            log.warn("AI (Hard): MCTS attacker search failed, falling back to evaluator logic", e);
        }

        // Fall back to CombatSimulator
        handleAttackersWithSimulator(gameData, availableIndices, mustAttackIndices);
    }

    private void handleAttackersWithSimulator(GameData gameData, List<Integer> availableIndices,
                                              List<Integer> mustAttackIndices) {
        List<Integer> attackerIndices = combatSimulator.findBestAttackers(
                gameData, aiPlayer.getId(), availableIndices, mustAttackIndices);

        log.info("AI (Hard): Declaring {} attackers in game {}", attackerIndices.size(), gameId);
        send(() -> messageHandler.handleDeclareAttackers(selfConnection,
                new DeclareAttackersRequest(attackerIndices, null)));
    }

    @Override
    protected void handleBlockers(GameData gameData) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(aiPlayer.getId());
        UUID opponentId = AiUtils.getOpponentId(gameData, aiPlayer.getId());
        List<Permanent> opponentBattlefield = gameData.playerBattlefields.getOrDefault(opponentId, List.of());

        if (battlefield == null) {
            send(() -> messageHandler.handleDeclareBlockers(selfConnection,
                    new DeclareBlockersRequest(List.of())));
            return;
        }

        List<Integer> attackerIndices = new ArrayList<>();
        for (int i = 0; i < opponentBattlefield.size(); i++) {
            Permanent perm = opponentBattlefield.get(i);
            if (perm.isAttacking()) {
                attackerIndices.add(i);
            }
        }

        List<Integer> blockerIndices = new ArrayList<>();
        for (int i = 0; i < battlefield.size(); i++) {
            if (gameQueryService.canBlock(gameData, battlefield.get(i))) {
                blockerIndices.add(i);
            }
        }

        List<int[]> assignments = combatSimulator.findBestBlockers(
                gameData, aiPlayer.getId(), attackerIndices, blockerIndices);

        List<BlockerAssignment> blockerAssignments = assignments.stream()
                .map(a -> new BlockerAssignment(a[0], a[1]))
                .toList();

        log.info("AI (Hard): Declaring {} blockers in game {}", blockerAssignments.size(), gameId);
        sendBlockerDeclaration(gameData, new DeclareBlockersRequest(blockerAssignments));
    }

    // ===== Card Choice (discard lowest spell value) =====

    @Override
    protected void handleCardChoice(GameData gameData) {
        var cardChoice = gameData.interaction.cardChoiceContext();
        if (cardChoice == null) return;

        UUID choicePlayerId = cardChoice.playerId();
        Set<Integer> validIndices = cardChoice.validIndices();

        if (!aiPlayer.getId().equals(choicePlayerId)) return;

        List<Card> hand = gameData.playerHands.get(aiPlayer.getId());
        if (hand == null || validIndices == null || validIndices.isEmpty()) return;

        int bestIndex = validIndices.stream()
                .min(Comparator.comparingDouble(i ->
                        spellEvaluator.estimateSpellValue(gameData, hand.get(i), aiPlayer.getId())))
                .orElse(validIndices.iterator().next());

        log.info("AI (Hard): Discarding card at index {} in game {}", bestIndex, gameId);
        send(() -> messageHandler.handleCardChosen(selfConnection, new CardChosenRequest(bestIndex)));
    }

    // ===== Mulligan (scoring-based) =====

    @Override
    protected boolean shouldKeepHand(GameData gameData) {
        List<Card> hand = gameData.playerHands.get(aiPlayer.getId());
        if (hand == null || hand.isEmpty()) return true;

        int mulliganCount = gameData.mulliganCounts.getOrDefault(aiPlayer.getId(), 0);
        if (mulliganCount >= 3) return true;

        long landCount = hand.stream().filter(c -> c.hasType(CardType.LAND)).count();

        if (landCount == 0 && mulliganCount < 2) return false;
        if (landCount > 5) return false;

        double handScore = 0;
        for (Card card : hand) {
            if (card.hasType(CardType.LAND)) {
                handScore += 1.5;
                continue;
            }
            int mv = card.getManaValue();
            if (mv <= landCount + 1) {
                handScore += 3.0;
            } else if (mv <= landCount + 3) {
                handScore += 1.5;
            } else {
                handScore += 0.5;
            }
        }

        double threshold = 12.0 - mulliganCount * 3.0;
        return handScore >= threshold;
    }
}
