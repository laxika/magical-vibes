package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.HauntEffect;
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
public class HauntEffectHandler implements NormalEffectHandlerBean {

    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return HauntEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID cardId = entry.getCard().getId();
        Card sourceCard = gameQueryService.findCardInGraveyardById(gameData, cardId);
        if (sourceCard == null) {
            return;
        }

        UUID targetId = entry.targetsForEffect(effect).stream()
                .findFirst()
                .orElse(entry.getTargetId());
        Permanent target = targetId == null ? null : gameQueryService.findPermanentById(gameData, targetId);
        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, cardId);
        if (target == null || ownerId == null) {
            return;
        }

        permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, cardId);
        exileService.exileCard(gameData, ownerId, sourceCard);
        gameData.hauntingCardToPermanentId.put(cardId, target.getId());

        gameLogService.append(gameData,
                GameLog.cardThen(sourceCard, " is exiled haunting " + target.getCard().getName() + "."));
        log.info("Game {} - {} is exiled haunting {}", gameData.id,
                sourceCard.getName(), target.getCard().getName());
    }
}
