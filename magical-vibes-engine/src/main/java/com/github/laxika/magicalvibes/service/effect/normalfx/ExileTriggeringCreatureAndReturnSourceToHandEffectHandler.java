package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileTriggeringCreatureAndReturnSourceToHandEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.exile.ExileService;
import java.util.UUID;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

/**
 * Resolves Diabolic Servitude's linked-creature death trigger. It also handles the case where the
 * source has already left the battlefield and is still in its owner's graveyard.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class ExileTriggeringCreatureAndReturnSourceToHandEffectHandler implements NormalEffectHandlerBean {

    private final ExileService exileService;
    private final GameLogService gameLogService;
    private final GameQueryService gameQueryService;
    private final PermanentRemovalService permanentRemovalService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ExileTriggeringCreatureAndReturnSourceToHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var linkedEffect = (ExileTriggeringCreatureAndReturnSourceToHandEffect) effect;
        Card dyingCard = linkedEffect.dyingCardId() == null
                ? null
                : gameQueryService.findCardInGraveyardById(gameData, linkedEffect.dyingCardId());
        if (dyingCard != null) {
            UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, dyingCard.getId());
            permanentRemovalService.removeCardFromGraveyardById(gameData, dyingCard.getId());
            exileService.exileCard(gameData, ownerId, dyingCard);
            gameLogService.append(gameData, GameLog.cardTextCard(dyingCard, " is exiled by ", entry.getCard(), "."));
            log.info("Game {} - {} is exiled by {}", gameData.id, dyingCard.getName(), entry.getCard().getName());
        }

        Permanent source = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source != null) {
            source.setChosenPermanentId(null);
            permanentRemovalService.removePermanentToHand(gameData, source);
            return;
        }

        Card sourceCard = gameQueryService.findCardInGraveyardById(gameData, entry.getCard().getId());
        if (sourceCard == null) {
            return;
        }
        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, sourceCard.getId());
        permanentRemovalService.removeCardFromGraveyardById(gameData, sourceCard.getId());
        gameData.addCardToHand(ownerId, sourceCard);
        gameLogService.append(gameData, GameLog.cardThen(sourceCard, " returns to its owner's hand."));
        log.info("Game {} - {} returns from its graveyard to its owner's hand", gameData.id, sourceCard.getName());
    }
}
