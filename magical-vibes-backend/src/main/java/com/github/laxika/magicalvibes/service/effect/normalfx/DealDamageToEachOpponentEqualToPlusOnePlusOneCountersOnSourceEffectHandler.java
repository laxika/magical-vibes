package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachOpponentEqualToPlusOnePlusOneCountersOnSourceEffect;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CounterType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class DealDamageToEachOpponentEqualToPlusOnePlusOneCountersOnSourceEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameBroadcastService gameBroadcastService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToEachOpponentEqualToPlusOnePlusOneCountersOnSourceEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {

        if (entry.getSourcePermanentId() == null) return;

        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) return;

        int counters = source.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE);
        if (counters <= 0) return;

        if (damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) return;

        UUID controllerId = entry.getControllerId();
        String cardName = entry.getCard().getName();

        int damage = gameQueryService.applyDamageMultiplier(gameData, counters, entry);
        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId)) continue;
            damageSupport.dealDamageToPlayer(gameData, entry, playerId, damage);
        }

        gameBroadcastService.logAndBroadcast(gameData,
                cardName + " deals " + damage + " damage to each opponent.");
        log.info("Game {} - {} deals {} damage to each opponent (from +1/+1 counters)",
                gameData.id, cardName, damage);

        gameOutcomeService.checkWinCondition(gameData);
    
    }
}
