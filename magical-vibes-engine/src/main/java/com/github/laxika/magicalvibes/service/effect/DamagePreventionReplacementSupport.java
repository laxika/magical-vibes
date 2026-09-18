package com.github.laxika.magicalvibes.service.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.effect.PreventDamageByRemovingCountersOrSacrificeEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToControllerAndPutCounterOnSelfEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.normalfx.DestructionSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentCounterSupport;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.UUID;

@Component
@RequiredArgsConstructor
public class DamagePreventionReplacementSupport {

    private final GameQueryService gameQueryService;
    private final PermanentCounterSupport permanentCounterSupport;
    private final ObjectProvider<DestructionSupport> destructionSupportProvider;

    public int preventDamageToControllerAndPutCounterOnSelf(GameData gameData, UUID playerId, int damage) {
        return preventDamageToControllerAndPutCounterOnSelf(gameData, playerId, damage, false);
    }

    public int preventDamageToControllerAndPutCounterOnSelf(GameData gameData, UUID playerId, int damage,
                                                             boolean combatDamage) {
        if (damage <= 0 || !gameQueryService.isDamagePreventable(gameData, combatDamage)) return 0;

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

    /** Applies a permanent's damage replacement that consumes counters or sacrifices it. */
    public boolean applyDamageToPermanentByRemovingCountersOrSacrificing(
            GameData gameData, Permanent permanent, int damage) {
        if (damage <= 0 || permanent == null
                || !gameQueryService.hasActiveStaticEffect(
                gameData, permanent, PreventDamageByRemovingCountersOrSacrificeEffect.class)) {
            return false;
        }

        PreventDamageByRemovingCountersOrSacrificeEffect effect = permanent.getCard()
                .getEffects(EffectSlot.STATIC).stream()
                .filter(PreventDamageByRemovingCountersOrSacrificeEffect.class::isInstance)
                .map(PreventDamageByRemovingCountersOrSacrificeEffect.class::cast)
                .findFirst()
                .orElse(null);
        if (effect == null) {
            return false;
        }

        int counters = permanent.getCounterCount(effect.counterType());
        if (counters >= damage) {
            permanent.setCounterCount(effect.counterType(), counters - damage);
            return true;
        }

        if (gameQueryService.cantBeSacrificed(gameData, permanent)) {
            return false;
        }
        UUID controllerId = gameQueryService.findPermanentController(gameData, permanent.getId());
        if (controllerId == null) {
            return false;
        }
        destructionSupportProvider.getObject().sacrificeAndLog(gameData, permanent, controllerId);
        return true;
    }
}
