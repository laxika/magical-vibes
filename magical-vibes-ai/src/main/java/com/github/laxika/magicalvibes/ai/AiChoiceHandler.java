package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.ai.interaction.AiInteractionContext;
import com.github.laxika.magicalvibes.ai.interaction.AiInteractionStrategies;
import com.github.laxika.magicalvibes.ai.interaction.AiInteractionStrategy;
import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.DrawReplacementKind;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.InteractionContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.Connection;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.networking.message.BottomCardsRequest;
import com.github.laxika.magicalvibes.networking.message.CardChosenRequest;
import com.github.laxika.magicalvibes.networking.message.ChosenFromListRequest;
import com.github.laxika.magicalvibes.networking.message.CombatDamageAssignedRequest;
import com.github.laxika.magicalvibes.networking.message.GraveyardCardChosenRequest;
import com.github.laxika.magicalvibes.networking.message.LibraryCardChosenRequest;
import com.github.laxika.magicalvibes.networking.message.MultipleCardsChosenRequest;
import com.github.laxika.magicalvibes.networking.message.PermanentChosenRequest;
import com.github.laxika.magicalvibes.networking.message.ScryCompletedRequest;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.Setter;
import lombok.extern.slf4j.Slf4j;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;

/**
 * Handles all interactive choice prompts for AI: card selection, permanent
 * selection, color choice, mulligan bottoming, combat damage assignment, etc.
 */
@Slf4j
class AiChoiceHandler {

    private final UUID gameId;
    private final UUID aiPlayerId;
    private final GameQueryService gameQueryService;
    private final AiGameActions gameActions;

    @Setter
    private Connection selfConnection;

    AiChoiceHandler(UUID gameId, UUID aiPlayerId, GameQueryService gameQueryService, AiGameActions gameActions) {
        this.gameId = gameId;
        this.aiPlayerId = aiPlayerId;
        this.gameQueryService = gameQueryService;
        this.gameActions = gameActions;
    }

    // ===== Card Choice (discard) =====

    void handleCardChoice(GameData gameData) {
        if (!(gameData.interaction.activeInteraction() instanceof PendingInteraction.HandChoice cardChoice)) {
            return;
        }
        UUID choicePlayerId = cardChoice.playerId();
        List<Integer> validIndices = cardChoice.validIndices();

        if (!aiPlayerId.equals(choicePlayerId)) {
            return;
        }

        List<Card> hand = gameData.playerHands.get(aiPlayerId);
        if (hand == null || validIndices == null || validIndices.isEmpty()) {
            return;
        }

        // Discard: pick highest mana cost card
        int bestIndex = validIndices.stream()
                .max(Comparator.comparingInt(i -> hand.get(i).getManaValue()))
                .orElse(validIndices.iterator().next());

        log.info("AI: Choosing card at index {} in game {}", bestIndex, gameId);
        send(() -> gameActions.handleCardChosen(selfConnection, new CardChosenRequest(bestIndex)));
    }

    // ===== Permanent Choice =====

    void handlePermanentChoice(GameData gameData) {
        InteractionContext.PermanentChoice permanentChoice = gameData.interaction.permanentChoiceContextView();
        if (permanentChoice == null) {
            return;
        }
        UUID choicePlayerId = permanentChoice.playerId();
        Set<UUID> validIds = permanentChoice.validIds();

        if (!aiPlayerId.equals(choicePlayerId)) {
            return;
        }

        if (validIds == null || validIds.isEmpty()) {
            return;
        }

        UUID opponentId = AiUtils.getOpponentId(gameData, aiPlayerId);
        List<Permanent> opponentField = gameData.playerBattlefields.getOrDefault(opponentId, List.of());
        List<Permanent> ownField = gameData.playerBattlefields.getOrDefault(aiPlayerId, List.of());

        // Try opponent's best creature first
        UUID best = opponentField.stream()
                .filter(p -> validIds.contains(p.getId()))
                .filter(p -> gameQueryService.isCreature(gameData, p))
                .max(Comparator.comparingInt(p -> gameQueryService.getEffectivePower(gameData, p)))
                .map(Permanent::getId)
                .orElse(null);

        if (best == null) {
            best = opponentField.stream()
                    .filter(p -> validIds.contains(p.getId()))
                    .max(Comparator.comparingInt(p -> p.getCard().getManaValue()))
                    .map(Permanent::getId)
                    .orElse(null);
        }

        if (best == null) {
            best = ownField.stream()
                    .filter(p -> validIds.contains(p.getId()))
                    .min(Comparator.comparingInt(p -> p.getCard().getManaValue()))
                    .map(Permanent::getId)
                    .orElse(validIds.iterator().next());
        }

        log.info("AI: Choosing permanent {} in game {}", best, gameId);
        final UUID finalBest = best;
        send(() -> gameActions.handlePermanentChosen(selfConnection, new PermanentChosenRequest(finalBest)));
    }

    // ===== Multiple Permanent Choice =====

    void handleMultiPermanentChoice(GameData gameData) {
        handleActiveInteraction(gameData);
    }

    // ===== Color Choice =====

    /** Baseline color/list-choice answer via {@code ColorChoiceAiStrategy}; Hard AI's override falls back here. */
    void handleColorChoice(GameData gameData) {
        handleActiveInteraction(gameData);
    }

    // ===== May Ability Choice =====

    /** Baseline may-ability answer via {@code MayAbilityChoiceAiStrategy}; Hard AI's override falls back here. */
    void handleMayAbilityChoice(GameData gameData) {
        handleActiveInteraction(gameData);
    }

    // ===== Registry-managed interactions (per-kind strategies) =====

    /**
     * Answers the active registry-managed interaction via its per-kind
     * {@link AiInteractionStrategy}. No-ops when no strategy matches (e.g. the
     * interaction kind has not been migrated yet).
     */
    void handleActiveInteraction(GameData gameData) {
        PendingInteraction active = gameData.interaction.activeInteraction();
        if (active == null) {
            return;
        }
        AiInteractionStrategy<PendingInteraction> strategy = AiInteractionStrategies.forInteraction(active);
        if (strategy == null) {
            return;
        }
        send(() -> strategy.answer(active, new AiInteractionContext(
                gameData, gameId, aiPlayerId, gameQueryService, gameActions, selfConnection)));
    }

    // ===== Scry =====

    /** Baseline scry answer via {@code ScryAiStrategy}; Hard AI's override falls back here. */
    void handleScry(GameData gameData) {
        handleActiveInteraction(gameData);
    }

    // ===== Library Search =====

    void handleLibrarySearch(GameData gameData) {
        InteractionContext.LibrarySearch librarySearch = gameData.interaction.librarySearchContext();
        if (librarySearch == null) {
            return;
        }
        UUID choicePlayerId = librarySearch.playerId();
        List<Card> searchCards = librarySearch.cards();

        if (!aiPlayerId.equals(choicePlayerId)) {
            return;
        }

        if (searchCards == null || searchCards.isEmpty()) {
            return;
        }

        // Pick highest value non-land, or first card
        int bestIndex = 0;
        int bestScore = -1;
        for (int i = 0; i < searchCards.size(); i++) {
            Card card = searchCards.get(i);
            int score = card.hasType(CardType.LAND) ? card.getManaValue() : card.getManaValue() * 2 + 10;
            if (score > bestScore) {
                bestScore = score;
                bestIndex = i;
            }
        }

        log.info("AI: Choosing card {} from library in game {}", searchCards.get(bestIndex).getName(), gameId);
        final int idx = bestIndex;
        send(() -> gameActions.handleLibraryCardChosen(selfConnection, new LibraryCardChosenRequest(idx)));
    }

    // ===== Graveyard Choice =====

    /** Baseline graveyard answer via {@code GraveyardChoiceAiStrategy} / {@code GraveyardExileCostChoiceAiStrategy}. */
    void handleGraveyardChoice(GameData gameData) {
        handleActiveInteraction(gameData);
    }

    // ===== Multi-Graveyard Choice =====

    /** All multi-card wire answers are registry-managed (KP / MoF / multi-zone exile / multi-graveyard / library reveal). */
    void handleMultiCardChoice(GameData gameData) {
        handleActiveInteraction(gameData);
    }

    // ===== Revealed Hand Choice =====

    /** Baseline revealed-hand answer via {@code RevealedHandChoiceAiStrategy}. */
    void handleRevealedHandChoice(GameData gameData) {
        handleActiveInteraction(gameData);
    }

    // ===== Bottom Cards (mulligan) =====

    void handleBottomCards(GameData gameData) {
        Integer needsToBottom = gameData.playerNeedsToBottom.get(aiPlayerId);
        if (needsToBottom == null || needsToBottom <= 0) {
            return;
        }

        List<Card> hand = gameData.playerHands.get(aiPlayerId);
        if (hand == null) {
            return;
        }

        List<int[]> scoredIndices = new ArrayList<>();
        long landCount = hand.stream().filter(c -> c.hasType(CardType.LAND)).count();

        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            int score;
            if (card.hasType(CardType.LAND)) {
                score = landCount > 2 ? 1000 : -1000;
                if (landCount > 2) {
                    landCount--;
                }
            } else {
                score = card.getManaValue();
            }
            scoredIndices.add(new int[]{i, score});
        }

        scoredIndices.sort((a, b) -> Integer.compare(b[1], a[1]));

        List<Integer> toBottom = new ArrayList<>();
        for (int i = 0; i < needsToBottom && i < scoredIndices.size(); i++) {
            toBottom.add(scoredIndices.get(i)[0]);
        }

        log.info("AI: Bottoming {} cards in game {}", toBottom.size(), gameId);
        send(() -> gameActions.handleBottomCards(selfConnection, new BottomCardsRequest(toBottom)));
    }

    // ===== Combat Damage Assignment =====

    void handleCombatDamageAssignment(GameData gameData) {
        InteractionContext.CombatDamageAssignment cda;
        synchronized (gameData) {
            cda = gameData.interaction.combatDamageAssignmentContext();
        }
        if (cda == null || !aiPlayerId.equals(cda.playerId())) {
            log.warn("AI: No combat damage assignment context for player {} in game {} (cda={})",
                    aiPlayerId, gameId, cda);
            return;
        }

        int atkIdx = cda.attackerIndex();
        int totalDamage = cda.totalDamage();
        var targets = cda.validTargets();

        Map<String, Integer> assignments = new HashMap<>();
        int remaining = totalDamage;

        for (var target : targets) {
            if (target.isPlayer()) continue;
            int lethal = target.effectiveToughness() - target.currentDamage();
            int dmg = Math.min(remaining, lethal);
            if (dmg > 0) {
                assignments.put(target.id().toString(), dmg);
                remaining -= dmg;
            }
        }

        if (remaining > 0) {
            for (var target : targets) {
                if (target.isPlayer()) {
                    assignments.put(target.id().toString(), remaining);
                    remaining = 0;
                    break;
                }
            }
        }

        if (remaining > 0 && !targets.isEmpty()) {
            var firstBlocker = targets.stream().filter(t -> !t.isPlayer()).findFirst().orElse(targets.get(0));
            assignments.merge(firstBlocker.id().toString(), remaining, Integer::sum);
        }

        log.info("AI: Assigning combat damage for attacker {} in game {}: {}", atkIdx, gameId, assignments);
        send(() -> gameActions.handleCombatDamageAssigned(selfConnection,
                new CombatDamageAssignedRequest(atkIdx, assignments)));
    }

    // ===== Internal =====

    @FunctionalInterface
    private interface SendAction {
        void execute() throws Exception;
    }

    private void send(SendAction action) {
        try {
            action.execute();
        } catch (Exception e) {
            log.error("AI: Error sending message in game {}", gameId, e);
        }
    }
}
