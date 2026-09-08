package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringCardFromGraveyardEffect;
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
public class ExileTriggeringCardFromGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTriggeringCardFromGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID triggeringCardId = entry.getTriggeringCardId();
        if (triggeringCardId == null) {
            return;
        }

        Card triggeringCard = gameQueryService.findCardInGraveyardById(gameData, triggeringCardId);
        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, triggeringCardId);
        if (triggeringCard == null || ownerId == null) {
            return;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, triggeringCardId);
        exileService.exileCard(gameData, ownerId, triggeringCard);
        gameLogService.append(gameData, GameLog.cardTextCard(triggeringCard, " is exiled by ", entry.getCard(), "."));
        log.info("Game {} - {} exiles {} from a graveyard", gameData.id,
                entry.getCard().getName(), triggeringCard.getName());
    }
}
