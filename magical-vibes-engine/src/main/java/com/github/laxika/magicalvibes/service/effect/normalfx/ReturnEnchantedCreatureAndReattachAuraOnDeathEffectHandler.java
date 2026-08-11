package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.action.DelayedReturnAuraAttachedToPermanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnEnchantedCreatureAndReattachAuraOnDeathEffect;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.UUID;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnEnchantedCreatureAndReattachAuraOnDeathEffectHandler implements NormalEffectHandlerBean {

    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final GameLogService gameLogService;
    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnEnchantedCreatureAndReattachAuraOnDeathEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnEnchantedCreatureAndReattachAuraOnDeathEffect) effect;
        UUID dyingCreatureCardId = e.dyingCreatureCardId();
        if (dyingCreatureCardId == null) {
            log.info("Game {} - {} death trigger fizzles (no dying creature card)",
                    gameData.id, entry.getCard().getName());
            return;
        }

        Card creatureCard = gameQueryService.findCardInGraveyardById(gameData, dyingCreatureCardId);
        if (creatureCard == null) {
            gameLogService.append(gameData,
                    GameLog.cardThen(entry.getCard(), "'s ability fizzles (creature not in graveyard)."));
            return;
        }

        UUID creatureOwnerId = gameQueryService.findGraveyardOwnerById(gameData, dyingCreatureCardId);
        if (creatureOwnerId == null) {
            return;
        }

        permanentRemovalService.removeCardFromGraveyardById(gameData, dyingCreatureCardId);
        Permanent returnedCreature = graveyardReturnSupport.putCardOntoBattlefield(
                gameData, creatureOwnerId, creatureCard, null, null, false, false, null);
        if (returnedCreature == null) {
            return;
        }

        UUID auraCardId = entry.getCard().getId();
        if (gameQueryService.findCardInGraveyardById(gameData, auraCardId) != null) {
            UUID auraOwnerId = entry.getCard().getOwnerId();
            if (auraOwnerId == null) {
                auraOwnerId = gameQueryService.findGraveyardOwnerById(gameData, auraCardId);
            }
            if (auraOwnerId == null) {
                return;
            }
            gameData.queueDelayedAction(new DelayedReturnAuraAttachedToPermanent(
                    auraCardId, auraOwnerId, returnedCreature.getId()));
        }
    }
}
