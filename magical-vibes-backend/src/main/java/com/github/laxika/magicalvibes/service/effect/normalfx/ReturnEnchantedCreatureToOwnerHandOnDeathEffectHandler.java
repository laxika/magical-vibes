package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnEnchantedCreatureToOwnerHandOnDeathEffect;
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
public class ReturnEnchantedCreatureToOwnerHandOnDeathEffectHandler implements NormalEffectHandlerBean {

    private final PermanentRemovalService permanentRemovalService;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return ReturnEnchantedCreatureToOwnerHandOnDeathEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (ReturnEnchantedCreatureToOwnerHandOnDeathEffect) effect;

        UUID dyingCreatureCardId = e.dyingCreatureCardId();
        if (dyingCreatureCardId == null) {
            log.info("Game {} - {} death trigger fizzles (no dying creature card ID)",
                    gameData.id, entry.getCard().getName());
            return;
        }

        Card creatureCard = gameQueryService.findCardInGraveyardById(gameData, dyingCreatureCardId);
        if (creatureCard == null) {
            String fizzleLog = entry.getCard().getName() + "'s ability fizzles (creature not in graveyard).";
            gameBroadcastService.logAndBroadcast(gameData, fizzleLog);
            log.info("Game {} - {} death trigger fizzles (creature card {} not in graveyard)",
                    gameData.id, entry.getCard().getName(), dyingCreatureCardId);
            return;
        }

        UUID ownerId = gameQueryService.findGraveyardOwnerById(gameData, dyingCreatureCardId);
        permanentRemovalService.removeCardFromGraveyardById(gameData, dyingCreatureCardId);
        gameData.playerHands.get(ownerId).add(creatureCard);

        String ownerName = gameData.playerIdToName.get(ownerId);
        String logEntry = creatureCard.getName() + " returns from graveyard to " + ownerName + "'s hand.";
        gameBroadcastService.logAndBroadcast(gameData, logEntry);
        log.info("Game {} - {} returns {} from graveyard to {}'s hand",
                gameData.id, entry.getCard().getName(), creatureCard.getName(), ownerName);
    }
}
