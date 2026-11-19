package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringCreatureAndTrackWithSourceEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Death-trigger resolution for Colfenor's Urn: exile the dying creature card from the graveyard it
 * went to and track it as "exiled with" the source permanent (via source permanent ID on the stack
 * entry), so the source can later count and return those cards. A card that left the graveyard and came back is a new
 * object the ability no longer refers to (CR 400.7), so it stays.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTriggeringCreatureAndTrackWithSourceEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTriggeringCreatureAndTrackWithSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID dyingCardId = entry.getTriggeringCardId();
        UUID sourcePermanentId = entry.getSourcePermanentId();
        if (sourcePermanentId == null && entry.getSourcePlanarObject() != null && entry.getCard() != null) {
            sourcePermanentId = entry.getCard().getId();
        }
        if (dyingCardId == null || sourcePermanentId == null) {
            return;
        }

        Card card = gameQueryService.findCardInGraveyardById(gameData, dyingCardId);
        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, dyingCardId);
        if (card == null || ownerId == null
                || !entry.isTriggeringCardIncarnation(gameData.graveyardEntryVersion(dyingCardId))) {
            return;
        }

        permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, dyingCardId);
        exileService.exileCard(gameData, ownerId, card, sourcePermanentId);
        gameLogService.append(gameData, GameLog.cardTextCard(card, " is exiled with ", entry.getCard(), "."));
        log.info("Game {} - {} exiled with {} (creature put into graveyard from battlefield)",
                gameData.id, card.getName(), entry.getCard().getName());
    }
}
