package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnEnchantedCreatureToBattlefieldUnderOwnersControlOnDeathEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import java.util.UUID;

import lombok.extern.slf4j.Slf4j;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class ReturnEnchantedCreatureToBattlefieldUnderOwnersControlOnDeathEffectHandler implements NormalEffectHandlerBean {

    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final GraveyardReturnSupport graveyardReturnSupport;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnEnchantedCreatureToBattlefieldUnderOwnersControlOnDeathEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnEnchantedCreatureToBattlefieldUnderOwnersControlOnDeathEffect) effect;

        UUID dyingCreatureCardId = e.dyingCreatureCardId();
        if (dyingCreatureCardId == null) {
            log.info("Game {} - {} death trigger fizzles (no dying creature card ID)",
                    gameData.id, entry.getCard().getName());
            return;
        }

        Card creatureCard = gameQueryService.findCardInGraveyardById(gameData, dyingCreatureCardId);
        if (creatureCard == null) {
            String fizzleLog = entry.getCard().getName() + "'s ability fizzles (creature not in graveyard).";
            gameBroadcastService.logAndBroadcast(gameData, GameLog.text(fizzleLog));
            log.info("Game {} - {} death trigger fizzles (creature card {} not in graveyard)",
                    gameData.id, entry.getCard().getName(), dyingCreatureCardId);
            return;
        }

        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, dyingCreatureCardId);
        permanentRemovalService.removeCardFromGraveyardById(gameData, dyingCreatureCardId);
        graveyardReturnSupport.putCardOntoBattlefield(gameData, ownerId, creatureCard);
    }
}
