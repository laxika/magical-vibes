package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardsForEachOpponentOfTriggeringPlayerEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DrawCardsForEachOpponentOfTriggeringPlayerEffectHandler implements NormalEffectHandlerBean {

    private final PlayerInteractionSupport playerInteractionSupport;
    private final GameQueryService gameQueryService;
    private final AmountEvaluationService amountEvaluationService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DrawCardsForEachOpponentOfTriggeringPlayerEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DrawCardsForEachOpponentOfTriggeringPlayerEffect) effect;
        UUID triggeringPlayerId = findTriggeringPlayerId(gameData, entry);
        if (triggeringPlayerId == null) {
            return;
        }

        Permanent source = entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                : null;
        if (source == null) {
            source = entry.getSourcePermanentSnapshot();
        }
        AmountContext base = AmountContext.forStackEntry(entry, source);

        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(triggeringPlayerId)) {
                continue;
            }
            int amount = amountEvaluationService.evaluate(gameData, e.amount(),
                    new AmountContext(playerId, source, base.targetPermanentId(), base.xValue(), base.eventValue()));
            playerInteractionSupport.applyDrawCards(gameData, playerId, amount);
        }
    }

    private UUID findTriggeringPlayerId(GameData gameData, StackEntry entry) {
        UUID triggeringCardId = entry.getTriggeringCardId();
        if (triggeringCardId != null) {
            for (StackEntry stackEntry : gameData.stack) {
                if (stackEntry.getCard().getId().equals(triggeringCardId)) {
                    return stackEntry.getControllerId();
                }
            }
        }
        return entry.getTargetId();
    }
}
