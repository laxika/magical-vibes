package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetPlayerEqualToCardTypeCountInGraveyardEffect;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import java.util.UUID;
import java.util.ArrayList;
import java.util.List;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardType;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

@Component
@RequiredArgsConstructor
public class DealDamageToTargetPlayerEqualToCardTypeCountInGraveyardEffectHandler implements NormalEffectHandlerBean {

    private final DamageSupport damageSupport;
    private final GameQueryService gameQueryService;
    private final GameOutcomeService gameOutcomeService;

    @Override
    public Class<? extends CardEffect> handledEffect() {
        return DealDamageToTargetPlayerEqualToCardTypeCountInGraveyardEffect.class;
    }

    @Override
    public void resolve(GameData gameData, StackEntry entry, CardEffect effect) {
        var e = (DealDamageToTargetPlayerEqualToCardTypeCountInGraveyardEffect) effect;

        UUID targetId = entry.getTargetId();
        if (!gameData.playerIds.contains(targetId)) return;

        if (!damageSupport.isDamageSourcePreventedWithLog(gameData, entry)) {
            List<Card> graveyard = gameData.playerGraveyards.get(entry.getControllerId());
            int count = 0;
            if (graveyard != null) {
                for (Card card : graveyard) {
                    if (card.hasType(e.cardType())) {
                        count++;
                    }
                }
            }

            int rawDamage = gameQueryService.applyDamageMultiplier(gameData, count, entry);
            damageSupport.dealDamageToPlayer(gameData, entry, targetId, rawDamage);
        }

        gameOutcomeService.checkWinCondition(gameData);
    
    }
}
