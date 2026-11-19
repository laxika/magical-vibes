package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringCreatureWithHitCounterEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves Mari's opponent-creature death trigger. A card that left the graveyard and came back is a new object the
 * ability no longer refers to (CR 400.7), so it stays.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTriggeringCreatureWithHitCounterEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTriggeringCreatureWithHitCounterEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID dyingCardId = entry.getTriggeringCardId();
        if (dyingCardId == null) {
            return;
        }

        Card dyingCard = gameQueryService.findCardInGraveyardById(gameData, dyingCardId);
        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, dyingCardId);
        if (dyingCard == null || ownerId == null
                || !entry.isTriggeringCardIncarnation(gameData.graveyardEntryVersion(dyingCardId))) {
            return;
        }

        permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, dyingCardId);
        exileService.exileCard(gameData, ownerId, dyingCard);
        gameData.exiledCardHitCounters.merge(dyingCardId, 1, Integer::sum);
        gameLogService.append(gameData, GameLog.cardThen(dyingCard,
                " is exiled with a hit counter on it."));
        log.info("Game {} - {} exiled with a hit counter", gameData.id, dyingCard.getName());
    }
}
