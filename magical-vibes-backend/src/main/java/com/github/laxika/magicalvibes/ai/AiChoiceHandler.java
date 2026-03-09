package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.AwaitingInput;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.ColorChoiceContext;
import com.github.laxika.magicalvibes.model.DrawReplacementKind;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.InteractionContext;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.networking.Connection;
import com.github.laxika.magicalvibes.networking.MessageHandler;
import com.github.laxika.magicalvibes.networking.message.BlockerAssignment;
import com.github.laxika.magicalvibes.networking.message.BottomCardsRequest;
import com.github.laxika.magicalvibes.networking.message.CardChosenRequest;
import com.github.laxika.magicalvibes.networking.message.ColorChosenRequest;
import com.github.laxika.magicalvibes.networking.message.CombatDamageAssignedRequest;
import com.github.laxika.magicalvibes.networking.message.GraveyardCardChosenRequest;
import com.github.laxika.magicalvibes.networking.message.HandTopBottomChosenRequest;
import com.github.laxika.magicalvibes.networking.message.LibraryCardChosenRequest;
import com.github.laxika.magicalvibes.networking.message.MayAbilityChosenRequest;
import com.github.laxika.magicalvibes.networking.message.MultipleGraveyardCardsChosenRequest;
import com.github.laxika.magicalvibes.networking.message.MultiplePermanentsChosenRequest;
import com.github.laxika.magicalvibes.networking.message.PermanentChosenRequest;
import com.github.laxika.magicalvibes.networking.message.ReorderLibraryCardsRequest;
import com.github.laxika.magicalvibes.networking.message.XValueChosenRequest;
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
    private final MessageHandler messageHandler;

    @Setter
    private Connection selfConnection;

    AiChoiceHandler(UUID gameId, UUID aiPlayerId, GameQueryService gameQueryService, MessageHandler messageHandler) {
        this.gameId = gameId;
        this.aiPlayerId = aiPlayerId;
        this.gameQueryService = gameQueryService;
        this.messageHandler = messageHandler;
    }

    // ===== Card Choice (discard) =====

    void handleCardChoice(GameData gameData) {
        InteractionContext.CardChoice cardChoice = gameData.interaction.cardChoiceContext();
        if (cardChoice == null) {
            return;
        }
        UUID choicePlayerId = cardChoice.playerId();
        Set<Integer> validIndices = cardChoice.validIndices();

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
        send(() -> messageHandler.handleCardChosen(selfConnection, new CardChosenRequest(bestIndex)));
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
        send(() -> messageHandler.handlePermanentChosen(selfConnection, new PermanentChosenRequest(finalBest)));
    }

    // ===== Multiple Permanent Choice =====

    void handleMultiPermanentChoice(GameData gameData) {
        InteractionContext.MultiPermanentChoice multiPermanentChoice = gameData.interaction.multiPermanentChoiceContext();
        if (multiPermanentChoice == null) {
            return;
        }
        UUID choicePlayerId = multiPermanentChoice.playerId();
        Set<UUID> validIds = multiPermanentChoice.validIds();
        int maxCount = multiPermanentChoice.maxCount();

        if (!aiPlayerId.equals(choicePlayerId)) {
            return;
        }

        if (validIds == null || validIds.isEmpty()) {
            return;
        }

        UUID opponentId = AiUtils.getOpponentId(gameData, aiPlayerId);
        List<Permanent> opponentField = gameData.playerBattlefields.getOrDefault(opponentId, List.of());

        List<UUID> chosen = opponentField.stream()
                .filter(p -> validIds.contains(p.getId()))
                .sorted(Comparator.comparingInt((Permanent p) -> gameQueryService.getEffectivePower(gameData, p)).reversed())
                .limit(maxCount)
                .map(Permanent::getId)
                .toList();

        if (chosen.isEmpty()) {
            chosen = validIds.stream().limit(maxCount).toList();
        }

        log.info("AI: Choosing {} permanents in game {}", chosen.size(), gameId);
        final List<UUID> finalChosen = chosen;
        send(() -> messageHandler.handleMultiplePermanentsChosen(selfConnection, new MultiplePermanentsChosenRequest(finalChosen)));
    }

    // ===== Color Choice =====

    void handleColorChoice(GameData gameData) {
        InteractionContext.ColorChoice colorChoice = gameData.interaction.colorChoiceContextView();
        if (colorChoice == null) {
            return;
        }
        UUID choicePlayerId = colorChoice.playerId();

        if (!aiPlayerId.equals(choicePlayerId)) {
            return;
        }

        if (colorChoice.context() instanceof ColorChoiceContext.DrawReplacementChoice drc
                && drc.kind() == DrawReplacementKind.ABUNDANCE) {
            log.info("AI: Choosing NONLAND for Abundance in game {}", gameId);
            send(() -> messageHandler.handleColorChosen(selfConnection, new ColorChosenRequest(null, "NONLAND")));
            return;
        }

        if (colorChoice.context() instanceof ColorChoiceContext.KeywordGrantChoice kgc) {
            String chosenKeyword = kgc.options().getFirst().name();
            log.info("AI: Choosing keyword {} in game {}", chosenKeyword, gameId);
            send(() -> messageHandler.handleColorChosen(selfConnection, new ColorChosenRequest(null, chosenKeyword)));
            return;
        }

        if (colorChoice.context() instanceof ColorChoiceContext.CardNameChoice) {
            UUID opponentId = AiUtils.getOpponentId(gameData, aiPlayerId);
            List<Permanent> opponentField = gameData.playerBattlefields.getOrDefault(opponentId, List.of());
            String chosenName = opponentField.stream()
                    .filter(p -> !p.getCard().getActivatedAbilities().isEmpty())
                    .map(p -> p.getCard().getName())
                    .findFirst()
                    .orElse(opponentField.isEmpty() ? "Pithing Needle" : opponentField.getFirst().getCard().getName());
            log.info("AI: Choosing card name \"{}\" in game {}", chosenName, gameId);
            final String finalName = chosenName;
            send(() -> messageHandler.handleColorChosen(selfConnection, new ColorChosenRequest(null, finalName)));
            return;
        }

        if (colorChoice.context() instanceof ColorChoiceContext.SubtypeChoice) {
            String chosenSubtype = "HUMAN";
            log.info("AI: Choosing creature type {} in game {}", chosenSubtype, gameId);
            final String subtype = chosenSubtype;
            send(() -> messageHandler.handleColorChosen(selfConnection, new ColorChosenRequest(null, subtype)));
            return;
        }

        if (colorChoice.context() instanceof ColorChoiceContext.ExileByNameChoice ctx) {
            UUID targetId = ctx.targetPlayerId();
            List<Card> targetHand = gameData.playerHands.getOrDefault(targetId, List.of());
            String chosenName = targetHand.stream()
                    .filter(c -> !ctx.excludedTypes().contains(c.getType()))
                    .map(Card::getName)
                    .findFirst()
                    .orElse("Lightning Bolt");
            log.info("AI: Choosing card name \"{}\" for exile in game {}", chosenName, gameId);
            final String name = chosenName;
            send(() -> messageHandler.handleColorChosen(selfConnection, new ColorChosenRequest(null, name)));
            return;
        }

        // Pick the color that appears most on opponent's battlefield
        UUID opponentId = AiUtils.getOpponentId(gameData, aiPlayerId);
        List<Permanent> opponentField = gameData.playerBattlefields.getOrDefault(opponentId, List.of());

        int[] colorCounts = new int[CardColor.values().length];
        for (Permanent perm : opponentField) {
            CardColor color = perm.getCard().getColor();
            if (color != null) {
                colorCounts[color.ordinal()]++;
            }
        }

        CardColor bestColor = CardColor.WHITE;
        int bestCount = 0;
        for (CardColor color : CardColor.values()) {
            if (colorCounts[color.ordinal()] > bestCount) {
                bestCount = colorCounts[color.ordinal()];
                bestColor = color;
            }
        }

        log.info("AI: Choosing color {} in game {}", bestColor.name(), gameId);
        final String colorName = bestColor.name();
        send(() -> messageHandler.handleColorChosen(selfConnection, new ColorChosenRequest(null, colorName)));
    }

    // ===== May Ability Choice =====

    void handleMayAbilityChoice(GameData gameData) {
        InteractionContext.MayAbilityChoice mayAbilityChoice = gameData.interaction.mayAbilityChoiceContext();
        if (mayAbilityChoice == null) {
            return;
        }
        UUID choicePlayerId = mayAbilityChoice.playerId();

        if (!aiPlayerId.equals(choicePlayerId)) {
            return;
        }

        log.info("AI: Accepting may ability in game {}", gameId);
        send(() -> messageHandler.handleMayAbilityChosen(selfConnection, new MayAbilityChosenRequest(null, true)));
    }

    // ===== X Value Choice =====

    void handleXValueChoice(GameData gameData) {
        InteractionContext.XValueChoice xValueChoice = gameData.interaction.xValueChoiceContext();
        if (xValueChoice == null) {
            return;
        }
        UUID choicePlayerId = xValueChoice.playerId();

        if (!aiPlayerId.equals(choicePlayerId)) {
            return;
        }

        int chosenValue = xValueChoice.maxValue();
        log.info("AI: Choosing X={} for {} in game {}", chosenValue, xValueChoice.cardName(), gameId);
        send(() -> messageHandler.handleXValueChosen(selfConnection, new XValueChosenRequest(null, chosenValue)));
    }

    // ===== Reorder Cards =====

    void handleReorderCards(GameData gameData) {
        InteractionContext.LibraryReorder libraryReorder = gameData.interaction.libraryReorderContext();
        if (libraryReorder == null) {
            return;
        }
        UUID choicePlayerId = libraryReorder.playerId();
        List<Card> cards = libraryReorder.cards();

        if (!aiPlayerId.equals(choicePlayerId)) {
            return;
        }

        if (cards == null || cards.isEmpty()) {
            return;
        }

        // Put spells on top, lands on bottom; sort by mana value ascending
        List<int[]> indexedCards = new ArrayList<>();
        for (int i = 0; i < cards.size(); i++) {
            Card card = cards.get(i);
            int priority = card.getType() == CardType.LAND ? 1000 + i : card.getManaValue();
            indexedCards.add(new int[]{i, priority});
        }
        indexedCards.sort(Comparator.comparingInt(a -> a[1]));

        List<Integer> order = indexedCards.stream().map(a -> a[0]).toList();

        log.info("AI: Reordering {} library cards in game {}", order.size(), gameId);
        send(() -> messageHandler.handleLibraryCardsReordered(selfConnection, new ReorderLibraryCardsRequest(order)));
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
            int score = card.getType() == CardType.LAND ? card.getManaValue() : card.getManaValue() * 2 + 10;
            if (score > bestScore) {
                bestScore = score;
                bestIndex = i;
            }
        }

        log.info("AI: Choosing card {} from library in game {}", searchCards.get(bestIndex).getName(), gameId);
        final int idx = bestIndex;
        send(() -> messageHandler.handleLibraryCardChosen(selfConnection, new LibraryCardChosenRequest(idx)));
    }

    // ===== Graveyard Choice =====

    void handleGraveyardChoice(GameData gameData) {
        InteractionContext.GraveyardChoice graveyardChoice = gameData.interaction.graveyardChoiceContext();
        if (graveyardChoice == null) {
            return;
        }
        UUID choicePlayerId = graveyardChoice.playerId();
        Set<Integer> validIndices = graveyardChoice.validIndices();

        if (!aiPlayerId.equals(choicePlayerId)) {
            return;
        }

        if (validIndices == null || validIndices.isEmpty()) {
            return;
        }

        List<Card> graveyard = graveyardChoice.cardPool();
        if (graveyard == null) {
            graveyard = gameData.playerGraveyards.getOrDefault(aiPlayerId, List.of());
        }

        final List<Card> gy = graveyard;
        int bestIndex = validIndices.stream()
                .max(Comparator.comparingInt(i -> i < gy.size() ? gy.get(i).getManaValue() : 0))
                .orElse(validIndices.iterator().next());

        log.info("AI: Choosing graveyard card at index {} in game {}", bestIndex, gameId);
        send(() -> messageHandler.handleGraveyardCardChosen(selfConnection, new GraveyardCardChosenRequest(bestIndex)));
    }

    // ===== Multi-Graveyard Choice =====

    void handleMultiGraveyardChoice(GameData gameData) {
        // Knowledge Pool cast choice
        if (gameData.interaction.awaitingInputType() == AwaitingInput.KNOWLEDGE_POOL_CAST_CHOICE) {
            InteractionContext.KnowledgePoolCastChoice kpc = gameData.interaction.knowledgePoolCastChoiceContext();
            if (kpc != null && aiPlayerId.equals(kpc.playerId())) {
                List<UUID> chosen = kpc.validCardIds().stream().limit(1).toList();
                log.info("AI: Choosing card from Knowledge Pool in game {}", gameId);
                send(() -> messageHandler.handleMultipleGraveyardCardsChosen(selfConnection, new MultipleGraveyardCardsChosenRequest(chosen)));
            }
            return;
        }

        // Multi-zone exile choice (Memoricide, etc.)
        if (gameData.interaction.awaitingInputType() == AwaitingInput.MULTI_ZONE_EXILE_CHOICE) {
            InteractionContext.MultiZoneExileChoice mzec = gameData.interaction.multiZoneExileChoiceContext();
            if (mzec != null && aiPlayerId.equals(mzec.playerId())) {
                List<UUID> chosen = new ArrayList<>(mzec.validCardIds());
                log.info("AI: Exiling {} cards named \"{}\" in game {}", chosen.size(), mzec.cardName(), gameId);
                send(() -> messageHandler.handleMultipleGraveyardCardsChosen(selfConnection, new MultipleGraveyardCardsChosenRequest(chosen)));
            }
            return;
        }

        InteractionContext.MultiGraveyardChoice multiGraveyardChoice = gameData.interaction.multiGraveyardChoiceContext();
        if (multiGraveyardChoice == null) {
            return;
        }
        UUID choicePlayerId = multiGraveyardChoice.playerId();
        Set<UUID> validIds = multiGraveyardChoice.validCardIds();
        int maxCount = multiGraveyardChoice.maxCount();

        if (!aiPlayerId.equals(choicePlayerId)) {
            return;
        }

        if (validIds == null || validIds.isEmpty()) {
            return;
        }

        List<UUID> chosen = validIds.stream().limit(maxCount).toList();

        log.info("AI: Choosing {} graveyard cards in game {}", chosen.size(), gameId);
        send(() -> messageHandler.handleMultipleGraveyardCardsChosen(selfConnection, new MultipleGraveyardCardsChosenRequest(chosen)));
    }

    // ===== Hand Top/Bottom Choice =====

    void handleHandTopBottom(GameData gameData) {
        InteractionContext.HandTopBottomChoice handTopBottomChoice = gameData.interaction.handTopBottomChoiceContext();
        if (handTopBottomChoice == null) {
            return;
        }
        UUID choicePlayerId = handTopBottomChoice.playerId();
        List<Card> cards = handTopBottomChoice.cards();

        if (!aiPlayerId.equals(choicePlayerId)) {
            return;
        }

        if (cards == null || cards.size() < 2) {
            return;
        }

        int handCardIndex = 0;
        int topCardIndex = 1;

        Card card0 = cards.get(0);
        Card card1 = cards.get(1);

        boolean card0IsLand = card0.getType() == CardType.LAND;
        boolean card1IsLand = card1.getType() == CardType.LAND;

        if (card0IsLand && !card1IsLand) {
            handCardIndex = 1;
            topCardIndex = 0;
        } else if (!card0IsLand && card1IsLand) {
            handCardIndex = 0;
            topCardIndex = 1;
        } else {
            if (card1.getManaValue() > card0.getManaValue()) {
                handCardIndex = 1;
                topCardIndex = 0;
            }
        }

        log.info("AI: Choosing hand={} top={} in game {}", handCardIndex, topCardIndex, gameId);
        final int h = handCardIndex;
        final int t = topCardIndex;
        send(() -> messageHandler.handleHandTopBottomChosen(selfConnection, new HandTopBottomChosenRequest(h, t)));
    }

    // ===== Revealed Hand Choice =====

    void handleRevealedHandChoice(GameData gameData) {
        InteractionContext.RevealedHandChoice revealedHandChoice = gameData.interaction.revealedHandChoiceContext();
        if (revealedHandChoice == null) {
            return;
        }
        UUID choosingPlayerId = revealedHandChoice.choosingPlayerId();

        if (!aiPlayerId.equals(choosingPlayerId)) {
            return;
        }

        UUID targetPlayerId = revealedHandChoice.targetPlayerId();
        if (targetPlayerId == null) {
            return;
        }

        List<Card> targetHand = gameData.playerHands.get(targetPlayerId);
        Set<Integer> validIndices = revealedHandChoice.validIndices();
        if (targetHand == null || validIndices == null || validIndices.isEmpty()) {
            return;
        }

        int bestIndex = validIndices.stream()
                .max(Comparator.comparingInt(i -> targetHand.get(i).getManaValue()))
                .orElse(validIndices.iterator().next());

        log.info("AI: Choosing card {} from revealed hand in game {}", bestIndex, gameId);
        send(() -> messageHandler.handleCardChosen(selfConnection, new CardChosenRequest(bestIndex)));
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
        long landCount = hand.stream().filter(c -> c.getType() == CardType.LAND).count();

        for (int i = 0; i < hand.size(); i++) {
            Card card = hand.get(i);
            int score;
            if (card.getType() == CardType.LAND) {
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
        send(() -> messageHandler.handleBottomCards(selfConnection, new BottomCardsRequest(toBottom)));
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
        send(() -> messageHandler.handleCombatDamageAssigned(selfConnection,
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
