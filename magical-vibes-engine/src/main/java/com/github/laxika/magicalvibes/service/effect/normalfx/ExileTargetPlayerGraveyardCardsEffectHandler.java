package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPlayerGraveyardCardsEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTargetPlayerGraveyardCardsEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GraveyardReturnSupport graveyardReturnSupport;
    private final LifeSupport lifeSupport;
    private final TargetLegalityService targetLegalityService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetPlayerGraveyardCardsEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var exileEffect = (ExileTargetPlayerGraveyardCardsEffect) effect;
        UUID targetPlayerId = entry.getTargetId();
        boolean targetPlayerIsLegal = targetPlayerId != null
                && targetLegalityService.isPrimaryTargetLegalOnResolution(gameData, entry, targetPlayerId);
        List<Card> targetGraveyard = targetPlayerId == null
                ? List.of()
                : gameData.playerGraveyards.getOrDefault(targetPlayerId, List.of());

        List<String> exiledNames = new ArrayList<>();
        for (UUID cardId : entry.getTargetCardIds()) {
            Card card = gameQueryService.findCardInGraveyardById(gameData, cardId);
            boolean isStillInTargetGraveyard = targetGraveyard.stream()
                    .anyMatch(graveyardCard -> graveyardCard.getId().equals(cardId));
            if (card != null && isStillInTargetGraveyard
                    && graveyardReturnSupport.exileCardFromAnyGraveyard(gameData, cardId, card)) {
                exiledNames.add(card.getName());
            }
        }

        int exiledCount = exiledNames.size();
        if (!exiledNames.isEmpty()) {
            String targetPlayerName = gameData.playerIdToName.get(targetPlayerId);
            gameLogService.append(gameData, GameLog.text(targetPlayerName + " exiles "
                    + String.join(", ", exiledNames) + " from their graveyard."));
            log.info("Game {} - {} exiled {} cards from their graveyard", gameData.id,
                    targetPlayerName, exiledCount);
        }

        if (exiledCount > 0 && targetPlayerIsLegal && exileEffect.lifeLossPerExiledCard() > 0) {
            lifeSupport.applyLifeLoss(gameData, targetPlayerId,
                    exiledCount * exileEffect.lifeLossPerExiledCard(), entry.getCard().getName());
        }
        if (exiledCount > 0 && exileEffect.lifeGainPerExiledCard() > 0) {
            lifeSupport.applyGainLife(gameData, entry.getControllerId(),
                    exiledCount * exileEffect.lifeGainPerExiledCard(), entry.getCard().getName());
        }
    }
}
