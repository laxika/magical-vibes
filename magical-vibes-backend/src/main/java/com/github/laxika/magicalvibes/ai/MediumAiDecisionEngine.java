package com.github.laxika.magicalvibes.ai;

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
 * Medium difficulty AI that uses board evaluation, spell evaluation, and exhaustive
 * combat search to make smarter decisions than the base AiDecisionEngine.
 */
@Slf4j
public class MediumAiDecisionEngine extends AiDecisionEngine {

    private final BoardEvaluator boardEvaluator;
    private final SpellEvaluator spellEvaluator;
    private final CombatSimulator combatSimulator;

    public MediumAiDecisionEngine(UUID gameId, Player aiPlayer, GameRegistry gameRegistry,
                                  MessageHandler messageHandler, GameQueryService gameQueryService,
                                  CombatAttackService combatAttackService) {
        super(gameId, aiPlayer, gameRegistry, messageHandler, gameQueryService, combatAttackService);
        this.boardEvaluator = new BoardEvaluator(gameQueryService);
        this.spellEvaluator = new SpellEvaluator(gameQueryService, boardEvaluator);
        this.combatSimulator = new CombatSimulator(gameQueryService, boardEvaluator);
    }

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

            if (tryCastSpell(gameData)) {
                return;
            }
        }

        // Pass priority
        send(() -> messageHandler.handlePassPriority(selfConnection, new PassPriorityRequest()));
    }

    protected boolean tryCastSpell(GameData gameData) {
        List<Card> hand = gameData.playerHands.get(aiPlayer.getId());
        if (hand == null) {
            return false;
        }

        ManaPool virtualPool = manaManager.buildVirtualManaPool(gameData, aiPlayer.getId());

        // Evaluate all castable spells using SpellEvaluator
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

        // Cast the highest-value spell
        candidates.sort(Comparator.comparingDouble(CastCandidate::value).reversed());
        CastCandidate best = candidates.getFirst();
        Card card = hand.get(best.index);

        // Determine target if needed
        UUID targetId = null;
        if (card.isNeedsTarget() || card.isAura()) {
            targetId = targetSelector.chooseTarget(gameData, card, aiPlayer.getId());
            if (targetId == null) {
                return false;
            }
        }

        // Calculate X value and tap lands
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

        log.info("AI (Medium): Casting {}{} (value={}) in game {}", card.getName(),
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
            log.warn("AI (Medium): PlayCard failed silently in game {}", gameId);
            return false;
        }
        return true;
    }

    @Override
    protected void handleAttackers(GameData gameData) {
        List<Integer> availableIndices = combatAttackService.getAttackableCreatureIndices(gameData, aiPlayer.getId());
        List<Integer> mustAttackIndices = combatAttackService.getMustAttackIndices(gameData, aiPlayer.getId(), availableIndices);

        List<Integer> attackerIndices = combatSimulator.findBestAttackers(
                gameData, aiPlayer.getId(), availableIndices, mustAttackIndices);

        log.info("AI (Medium): Declaring {} attackers in game {}", attackerIndices.size(), gameId);
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

        // Find attacker indices
        List<Integer> attackerIndices = new ArrayList<>();
        for (int i = 0; i < opponentBattlefield.size(); i++) {
            Permanent perm = opponentBattlefield.get(i);
            if (perm.isAttacking()) {
                attackerIndices.add(i);
            }
        }

        // Find available blocker indices
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

        log.info("AI (Medium): Declaring {} blockers in game {}", blockerAssignments.size(), gameId);
        sendBlockerDeclaration(gameData, new DeclareBlockersRequest(blockerAssignments));
    }

    @Override
    protected void handleCardChoice(GameData gameData) {
        var cardChoice = gameData.interaction.cardChoiceContext();
        if (cardChoice == null) return;

        UUID choicePlayerId = cardChoice.playerId();
        Set<Integer> validIndices = cardChoice.validIndices();

        if (!aiPlayer.getId().equals(choicePlayerId)) return;

        List<Card> hand = gameData.playerHands.get(aiPlayer.getId());
        if (hand == null || validIndices == null || validIndices.isEmpty()) return;

        // Discard the card with the lowest spell value instead of highest mana cost
        int bestIndex = validIndices.stream()
                .min(Comparator.comparingDouble(i ->
                        spellEvaluator.estimateSpellValue(gameData, hand.get(i), aiPlayer.getId())))
                .orElse(validIndices.iterator().next());

        log.info("AI (Medium): Discarding card at index {} in game {}", bestIndex, gameId);
        send(() -> messageHandler.handleCardChosen(selfConnection, new CardChosenRequest(bestIndex)));
    }

    @Override
    protected boolean shouldKeepHand(GameData gameData) {
        List<Card> hand = gameData.playerHands.get(aiPlayer.getId());
        if (hand == null || hand.isEmpty()) return true;

        int mulliganCount = gameData.mulliganCounts.getOrDefault(aiPlayer.getId(), 0);
        if (mulliganCount >= 3) return true;

        long landCount = hand.stream().filter(c -> c.hasType(CardType.LAND)).count();

        // Basic land check first
        if (landCount == 0 && mulliganCount < 2) return false;
        if (landCount > 5) return false;

        // Score hand by counting playable spells in turns 1-3
        double handScore = 0;
        for (Card card : hand) {
            if (card.hasType(CardType.LAND)) {
                handScore += 1.5; // Lands have base value
                continue;
            }
            int mv = card.getManaValue();
            if (mv <= landCount + 1) {
                // Playable in first few turns
                handScore += 3.0;
            } else if (mv <= landCount + 3) {
                // Playable soon
                handScore += 1.5;
            } else {
                // Expensive, low value early
                handScore += 0.5;
            }
        }

        // Threshold scales with mulligan count (more lenient as we mulligan more)
        double threshold = 12.0 - mulliganCount * 3.0;
        return handScore >= threshold;
    }

}
