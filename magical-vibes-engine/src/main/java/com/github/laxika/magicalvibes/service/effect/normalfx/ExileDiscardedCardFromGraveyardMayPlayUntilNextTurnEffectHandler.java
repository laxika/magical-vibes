package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileDiscardedCardFromGraveyardMayPlayUntilNextTurnEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileDiscardedCardFromGraveyardMayPlayUntilNextTurnEffectHandler
        implements NormalEffectHandlerBean {

    private final PermanentRemovalService permanentRemovalService;
    private final ExileService exileService;
    private final ExileSupport exileSupport;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileDiscardedCardFromGraveyardMayPlayUntilNextTurnEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID discardedCardId = entry.getTriggeringCardId();
        if (discardedCardId == null) {
            return;
        }

        UUID graveyardOwnerId = null;
        Card discardedCard = null;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) {
                continue;
            }
            discardedCard = graveyard.stream()
                    .filter(card -> discardedCardId.equals(card.getId()))
                    .findFirst()
                    .orElse(null);
            if (discardedCard != null) {
                graveyardOwnerId = playerId;
                break;
            }
        }

        if (discardedCard == null) {
            return;
        }

        permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, discardedCardId);
        exileService.exileCard(gameData, graveyardOwnerId, discardedCard);
        exileSupport.grantPlayUntilOwnersNextTurn(gameData, discardedCardId, entry.getControllerId());

        gameLogService.append(gameData, GameLog.cardTextCard(entry.getCard(), " exiles ", discardedCard,
                " from the graveyard; its controller may play it until the end of their next turn."));
        log.info("Game {} - {} exiles discarded card {} and grants play permission until next turn",
                gameData.id, entry.getCard().getName(), discardedCard.getName());
    }
}
