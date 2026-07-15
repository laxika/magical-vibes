package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.PendingPileSeparation;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetGraveyardCardsAndSeparateIntoPilesEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
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
    private final GraveyardService graveyardService;

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

        graveyardService.beginGraveyardLeaveBatch(gameData);
        try {
            for (UUID cardId : targetCardIds) {
                UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, cardId);
                Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
                if (card != null && ownerId != null) {
                    List<Card> ownerGraveyard = gameData.playerGraveyards.get(ownerId);
                    if (ownerGraveyard != null && ownerGraveyard.removeIf(c -> c.getId().equals(cardId))) {
                        exiledCards.add(card);
                        cardOwners.put(cardId, ownerId);
                        graveyardService.notifyCardsLeftGraveyard(gameData, ownerId);
                    }
                }
            }
        } finally {
            graveyardService.endGraveyardLeaveBatch(gameData);
        }

        if (exiledCards.isEmpty()) {
            String playerName = gameData.playerIdToName.get(controllerId);
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + "'s " + entry.getCard().getName() + " fizzles — no valid targets."));
            return;
        }

        // Log the exile
        String exiledNames = exiledCards.stream().map(Card::getName).collect(java.util.stream.Collectors.joining(", "));
        String playerName = gameData.playerIdToName.get(controllerId);
        gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + " exiles " + exiledNames + " from graveyards."));

        // Determine opponent (in 2-player, it's the other player)
        UUID opponentId = gameData.orderedPlayerIds.stream()
                .filter(id -> !id.equals(controllerId))
                .findFirst()
                .orElseThrow();

        // Store pile separation state (card-pile mode)
        gameData.queueInteraction(new PendingPileSeparation(controllerId, opponentId,
                List.of(), exiledCards, cardOwners, List.of(), List.of()));

        // Prompt opponent to separate into two piles
        playerInputService.beginMultiGraveyardChoice(gameData, opponentId, exiledCards, exiledCards.size(),
                "Separate the exiled cards into two piles. Select cards for Pile 1 (unselected form Pile 2).");
    }
}
