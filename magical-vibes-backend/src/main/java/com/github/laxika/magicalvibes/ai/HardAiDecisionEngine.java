package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.ai.simulation.GameSimulator;
import com.github.laxika.magicalvibes.ai.simulation.MCTSEngine;
import com.github.laxika.magicalvibes.ai.simulation.SimulationAction;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameStatus;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Player;
import com.github.laxika.magicalvibes.model.TurnStep;
import com.github.laxika.magicalvibes.networking.MessageHandler;
import com.github.laxika.magicalvibes.networking.message.DeclareAttackersRequest;
import com.github.laxika.magicalvibes.networking.message.PassPriorityRequest;
import com.github.laxika.magicalvibes.networking.message.PlayCardRequest;
import com.github.laxika.magicalvibes.service.GameQueryService;
import com.github.laxika.magicalvibes.service.GameRegistry;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Hard difficulty AI that uses Information Set Monte Carlo Tree Search (IS-MCTS)
 * to make decisions. Extends MediumAiDecisionEngine, overriding spell casting
 * and attacker declaration to use MCTS when multiple options exist.
 *
 * Inherited from Medium: blocker assignment (exhaustive search), card discard
 * (lowest value), mulligan scoring, and all utility decision handlers.
 */
@Slf4j
public class HardAiDecisionEngine extends MediumAiDecisionEngine {

    private static final int MCTS_BUDGET = 50000;

    private final GameSimulator simulator;
    private final MCTSEngine mctsEngine;

    public HardAiDecisionEngine(UUID gameId, Player aiPlayer, GameRegistry gameRegistry,
                                MessageHandler messageHandler, GameQueryService gameQueryService) {
        super(gameId, aiPlayer, gameRegistry, messageHandler, gameQueryService);
        this.simulator = new GameSimulator(gameQueryService);
        this.mctsEngine = new MCTSEngine(simulator);
    }

    @Override
    protected void handleGameState(GameData gameData) {
        if (gameData.status != GameStatus.RUNNING) {
            return;
        }

        boolean awaitingInput;
        UUID priorityHolder;
        synchronized (gameData) {
            awaitingInput = gameData.interaction.isAwaitingInput();
            priorityHolder = getPriorityPlayerId(gameData);
        }

        if (priorityHolder == null || !priorityHolder.equals(aiPlayer.getId())) {
            return;
        }

        if (awaitingInput) {
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
     * Falls back to Medium AI logic for 0-1 castable options.
     */
    private boolean tryCastSpellMCTS(GameData gameData) {
        List<Card> hand = gameData.playerHands.get(aiPlayer.getId());
        if (hand == null) return false;

        ManaPool virtualPool = buildVirtualManaPool(gameData);

        // Count castable spells
        int castableCount = 0;
        for (Card card : hand) {
            if (card.getType() == CardType.LAND || card.getType() == CardType.INSTANT) continue;
            if (card.getManaCost() == null) continue;
            ManaCost cost = new ManaCost(card.getManaCost());
            if (cost.hasX()) {
                if (cost.canPay(virtualPool, 1)) castableCount++;
            } else {
                if (cost.canPay(virtualPool)) castableCount++;
            }
        }

        // If 0-1 options, use Medium AI logic (no need for MCTS)
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
                if (castCost.hasX()) {
                    int smartX = calculateSmartX(gameData, card, pc.targetPermanentId(), virtualPool);
                    if (smartX <= 0) {
                        return false;
                    }
                    xValue = smartX;
                    tapLandsForXSpell(gameData, card, smartX);
                } else {
                    tapLandsForCost(gameData, card.getManaCost());
                }
                log.info("AI (Hard/MCTS): Casting {}{} in game {}", card.getName(),
                        xValue != null ? " (X=" + xValue + ")" : "", gameId);
                final int cardIndex = pc.handIndex();
                final UUID targetId = pc.targetPermanentId();
                final Integer finalXValue = xValue;
                send(() -> messageHandler.handlePlayCard(selfConnection,
                        new PlayCardRequest(cardIndex, finalXValue, targetId, null, null, null, null)));
                return true;
            }

            if (bestAction instanceof SimulationAction.PassPriority) {
                log.info("AI (Hard/MCTS): MCTS recommends passing in game {}", gameId);
                return false; // Let handleGameState send the pass
            }
        } catch (Exception e) {
            log.warn("AI (Hard): MCTS failed, falling back to Medium logic in game {}", gameId, e);
            return tryCastSpell(gameData);
        }

        return false;
    }

    @Override
    protected void handleAttackers(GameData gameData) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(aiPlayer.getId());
        if (battlefield == null) {
            send(() -> messageHandler.handleDeclareAttackers(selfConnection,
                    new DeclareAttackersRequest(List.of())));
            return;
        }

        // Count available attackers
        List<Integer> availableIndices = new ArrayList<>();
        for (int i = 0; i < battlefield.size(); i++) {
            Permanent perm = battlefield.get(i);
            if (!gameQueryService.isCreature(gameData, perm)) continue;
            if (perm.isTapped()) continue;
            if (perm.isSummoningSick() && !gameQueryService.hasKeyword(gameData, perm, Keyword.HASTE)) continue;
            if (gameQueryService.hasKeyword(gameData, perm, Keyword.DEFENDER)) continue;
            availableIndices.add(i);
        }

        // If 0-1 attackers, use Medium AI logic
        if (availableIndices.size() <= 1) {
            super.handleAttackers(gameData);
            return;
        }

        // Use MCTS for attacker selection
        try {
            SimulationAction bestAction = mctsEngine.search(gameData, aiPlayer.getId(), MCTS_BUDGET);

            if (bestAction instanceof SimulationAction.DeclareAttackers da) {
                log.info("AI (Hard/MCTS): Declaring {} attackers in game {}", da.attackerIndices().size(), gameId);
                send(() -> messageHandler.handleDeclareAttackers(selfConnection,
                        new DeclareAttackersRequest(da.attackerIndices())));
                return;
            }
        } catch (Exception e) {
            log.warn("AI (Hard): MCTS attacker search failed, falling back to Medium logic", e);
        }

        // Fall back to Medium AI
        super.handleAttackers(gameData);
    }
}
