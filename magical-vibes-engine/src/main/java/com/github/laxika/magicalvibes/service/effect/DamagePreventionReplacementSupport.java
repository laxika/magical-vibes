package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToControllerAndPutCounterOnSelfEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentCounterSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DamagePreventionReplacementSupport {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;

    public int preventDamageToControllerAndPutCounterOnSelf(GameData gameData, UUID playerId, int damage) {
        if (damage <= 0 || !gameQueryService.isDamagePreventable(gameData)) return 0;

        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return 0;

        for (Permanent permanent : battlefield) {
            PreventDamageToControllerAndPutCounterOnSelfEffect effect = permanent.getCard()
                    .getEffects(EffectSlot.STATIC).stream()
                    .filter(PreventDamageToControllerAndPutCounterOnSelfEffect.class::isInstance)
                    .map(PreventDamageToControllerAndPutCounterOnSelfEffect.class::cast)
                    .findFirst()
                    .orElse(null);
            if (effect == null) continue;

            permanentCounterSupport.placeCounterOnPermanent(gameData, null, permanent, effect.counterType(), 1);
            return damage;
        }
        return 0;
    }
}
