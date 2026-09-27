package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTargetNonlandCardFromGraveyardWithSuspendEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTargetNonlandCardFromGraveyardWithSuspendEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTargetNonlandCardFromGraveyardWithSuspendEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effectToResolve) {
        var effect = (ExileTargetNonlandCardFromGraveyardWithSuspendEffect) effectToResolve;
        UUID targetCardId = entry.getTargetId();
        if (targetCardId == null && entry.getTargetCardIds() != null
                && !entry.getTargetCardIds().isEmpty()) {
            targetCardId = entry.getTargetCardIds().getFirst();
        }

        Card targetCard = targetCardId == null
                ? null : gameQueryService.findCardInGraveyardById(gameData, targetCardId);
        UUID graveyardOwnerId = targetCard == null
                ? null : gameQueryService.findGraveyardOwnerById(gameData, targetCardId);
        if (targetCard == null || !entry.getControllerId().equals(graveyardOwnerId)
                || targetCard.hasType(CardType.LAND)) {
            gameLogService.append(gameData,
                    GameLog.text(entry.getDescription() + " fizzles (target is no longer legal)."));
            return;
        }

        if (!graveyardReturnSupport.exileCardFromAnyGraveyard(gameData, targetCardId, targetCard)) {
            gameLogService.append(gameData,
                    GameLog.text(entry.getDescription() + " fizzles (target is no longer in a graveyard)."));
            return;
        }

        gameData.exiledCardTimeCounters.put(targetCardId, effect.timeCounters());
        gameLogService.append(gameData, GameLog.cardThen(targetCard,
                " is exiled with " + effect.timeCounters() + " time counters and gains suspend."));
        log.info("Game {} - {} exiled from its controller's graveyard with {} suspend time counters",
                gameData.id, targetCard.getName(), effect.timeCounters());
    }
}
