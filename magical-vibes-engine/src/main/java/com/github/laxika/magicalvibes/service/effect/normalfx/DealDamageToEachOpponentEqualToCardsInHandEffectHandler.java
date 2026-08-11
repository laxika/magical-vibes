package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToEachOpponentEqualToCardsInHandEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DealDamageToEachOpponentEqualToCardsInHandEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToEachOpponentEqualToCardsInHandEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        UUID controllerId = entry.getControllerId();
        if (controllerId == null) return;

        for (UUID playerId : gameData.orderedPlayerIds) {
            if (playerId.equals(controllerId)) continue;

            int handSize = gameData.playerHands.getOrDefault(playerId, List.of()).size();
            if (handSize == 0 || gameQueryService.isDamageFromSourcePrevented(gameData, entry.getCard().getColor())) {
                continue;
            }

            int rawDamage = gameQueryService.applyDamageMultiplier(gameData, handSize, entry);
            damageSupport.dealDamageToPlayer(gameData, entry, playerId, rawDamage);
        }

        gameOutcomeService.checkWinCondition(gameData);
    }
}
