package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileSourceCardWithSuspendCountersEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileSourceCardWithSuspendCountersEffectHandler implements NormalEffectHandlerBean {

    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final ExileService exileService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileSourceCardWithSuspendCountersEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        ExileSourceCardWithSuspendCountersEffect suspendEffect =
                (ExileSourceCardWithSuspendCountersEffect) effect;
        UUID cardId = entry.getCard().getId();
        Card sourceCard = gameQueryService.findCardInGraveyardById(gameData, cardId);
        if (sourceCard == null) {
            gameLogService.append(gameData,
                    GameLog.cardThen(entry.getCard(), "'s ability fizzles (card not in graveyard)."));
            log.info("Game {} - {} suspend-on-death trigger fizzles (card {} not in graveyard)",
                    gameData.id, entry.getCard().getName(), cardId);
            return;
        }

        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, cardId);
        if (ownerId == null) {
            return;
        }

        permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, cardId);
        exileService.exileCard(gameData, ownerId, sourceCard);
        gameData.exiledCardTimeCounters.put(cardId, suspendEffect.timeCounters());
        gameLogService.append(gameData, GameLog.cardThen(sourceCard,
                " is exiled with " + suspendEffect.timeCounters()
                        + " time counters and gains suspend."));
        log.info("Game {} - {} exiled from graveyard with {} suspend time counters",
                gameData.id, sourceCard.getName(), suspendEffect.timeCounters());
    }
}
