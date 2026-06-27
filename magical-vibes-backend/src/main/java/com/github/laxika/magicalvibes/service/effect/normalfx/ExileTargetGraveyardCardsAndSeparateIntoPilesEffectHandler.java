package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetGraveyardCardsAndSeparateIntoPilesEffect;
import com.github.laxika.magicalvibes.networking.model.CardView;
import com.github.laxika.magicalvibes.networking.service.CardViewFactory;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.UUID;

import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class ExileTargetGraveyardCardsAndSeparateIntoPilesEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final PlayerInputService playerInputService;
    private final CardViewFactory cardViewFactory;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetGraveyardCardsAndSeparateIntoPilesEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ExileTargetGraveyardCardsAndSeparateIntoPilesEffect) effect;

        UUID controllerId = entry.getControllerId();
        List<UUID> targetCardIds = entry.getTargetCardIds();

        if (targetCardIds == null || targetCardIds.isEmpty()) {
            return;
        }

        // Exile targeted cards from their graveyards
        List<Card> exiledCards = new ArrayList<>();
        Map<UUID, UUID> cardOwners = new HashMap<>();

        for (UUID cardId : targetCardIds) {
            UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, cardId);
            Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
            if (card != null && ownerId != null) {
                List<Card> ownerGraveyard = gameData.playerGraveyards.get(ownerId);
                if (ownerGraveyard != null && ownerGraveyard.removeIf(c -> c.getId().equals(cardId))) {
                    exiledCards.add(card);
                    cardOwners.put(cardId, ownerId);
                }
            }
        }

        if (exiledCards.isEmpty()) {
            String playerName = gameData.playerIdToName.get(controllerId);
            gameBroadcastService.logAndBroadcast(gameData, playerName + "'s " + entry.getCard().getName() + " fizzles — no valid targets.");
            return;
        }

        // Log the exile
        String exiledNames = exiledCards.stream().map(Card::getName).collect(java.util.stream.Collectors.joining(", "));
        String playerName = gameData.playerIdToName.get(controllerId);
        gameBroadcastService.logAndBroadcast(gameData, playerName + " exiles " + exiledNames + " from graveyards.");

        // Determine opponent (in 2-player, it's the other player)
        UUID opponentId = gameData.orderedPlayerIds.stream()
                .filter(id -> !id.equals(controllerId))
                .findFirst()
                .orElseThrow();

        // Store pile separation state (reusing shared pile separation fields)
        gameData.pendingPileSeparation = true;
        gameData.pendingPileSeparationControllerId = controllerId;
        gameData.pendingPileSeparationTargetPlayerId = opponentId;
        gameData.pendingPileSeparationCards.clear();
        gameData.pendingPileSeparationCards.addAll(exiledCards);
        gameData.pendingPileSeparationCardOwners.clear();
        gameData.pendingPileSeparationCardOwners.putAll(cardOwners);
        gameData.pendingPileSeparationAllPermanentIds.clear();
        gameData.pendingPileSeparationPile1Ids.clear();
        gameData.pendingPileSeparationPile2Ids.clear();

        // Prompt opponent to separate into two piles
        List<UUID> cardIds = exiledCards.stream().map(Card::getId).toList();
        List<CardView> cardViews = exiledCards.stream().map(cardViewFactory::create).toList();
        playerInputService.beginMultiGraveyardChoice(gameData, opponentId, cardIds, cardViews, cardIds.size(),
                "Separate the exiled cards into two piles. Select cards for Pile 1 (unselected form Pile 2).");
    }
}
