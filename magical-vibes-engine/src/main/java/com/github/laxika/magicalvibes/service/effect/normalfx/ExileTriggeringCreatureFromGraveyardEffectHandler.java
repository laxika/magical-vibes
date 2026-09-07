package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringCreatureFromGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

/** Resolves the exile half of Illicit Masquerade's impostor death trigger. */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTriggeringCreatureFromGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;
    private final ExileService exileService;
    private final GameLogService gameLogService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTriggeringCreatureFromGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID dyingCardId = ((ExileTriggeringCreatureFromGraveyardEffect) effect).dyingCardId();
        if (dyingCardId == null) {
            return;
        }

        Card dyingCard = gameQueryService.findCardInGraveyardById(gameData, dyingCardId);
        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, dyingCardId);
        if (dyingCard == null || ownerId == null) {
            return;
        }

        permanentRemovalService.removeCardFromGraveyardByIdForExile(gameData, dyingCardId);
        exileService.exileCard(gameData, ownerId, dyingCard);
        gameLogService.append(gameData, GameLog.cardThen(dyingCard, " is exiled."));
        log.info("Game {} - {} exiled from its owner's graveyard", gameData.id, dyingCard.getName());
    }
}
