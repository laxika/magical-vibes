package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class LoyaltyDamageReplacementHandlerRegistry {

    private final Map<Class<? extends CardEffect>, LoyaltyDamageReplacementHandler> handlers;
    private final GameQueryService gameQueryService;

    public LoyaltyDamageReplacementHandlerRegistry(List<LoyaltyDamageReplacementHandler> handlerBeans,
                                                   GameQueryService gameQueryService) {
        handlers = new HashMap<>();
        this.gameQueryService = gameQueryService;
        for (LoyaltyDamageReplacementHandler handler : handlerBeans) {
            handlers.put(handler.handledEffect(), handler);
        }
    }

    public int apply(GameData gameData, Permanent target, int damage) {
        if (damage <= 0) return damage;

        int[] result = {damage};
        gameData.forEachBattlefield((controllerId, battlefield) -> {
            for (Permanent source : battlefield) {
                for (CardEffect effect : source.getCard().getEffects(EffectSlot.STATIC)) {
                    LoyaltyDamageReplacementHandler handler = handlers.get(effect.getClass());
                    if (handler == null || source.isStaticEffectSuppressed(effect.getClass())
                            || gameQueryService.hasLostAllAbilities(gameData, source)) continue;
                    result[0] = handler.apply(gameData, source, target, result[0]);
                }
            }
        });
        return result[0];
    }
}
