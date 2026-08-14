package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileDiscardedCardFromGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileDiscardedCardFromGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final PermanentRemovalService permanentRemovalService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileDiscardedCardFromGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExileDiscardedCardFromGraveyardEffect exileEffect = (ExileDiscardedCardFromGraveyardEffect) effect;
        if (entry.getTriggeringCardId() == null
                || (exileEffect.trackWithSource() && entry.getSourcePermanentId() == null)) {
            return;
        }

        UUID ownerId = null;
        Card discarded = null;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) continue;
            discarded = graveyard.stream()
                    .filter(card -> entry.getTriggeringCardId().equals(card.getId()))
                    .findFirst()
                    .orElse(null);
            if (discarded != null) {
                ownerId = playerId;
                break;
            }
        }
        if (discarded == null) {
            return;
        }

        permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, discarded.getId());
        if (exileEffect.addStashCounter()) {
            gameData.addToExileWithStashCounter(ownerId, discarded);
        } else if (exileEffect.trackWithSource()) {
            gameData.addToExile(ownerId, discarded, entry.getSourcePermanentId());
        } else {
            gameData.addToExile(ownerId, discarded);
        }
        gameLogService.append(gameData, GameLog.cardTextCard(entry.getCard(), " exiles ", discarded,
                " from " + gameData.playerIdToName.get(ownerId) + "'s graveyard."));
        log.info("Game {} - {} exiles discarded card {} from graveyard{}",
                gameData.id, entry.getCard().getName(), discarded.getName(),
                exileEffect.trackWithSource() ? " with the source" : "");
    }
}
