package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToAndByEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageAndAddMinusCountersEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.Map;
import java.util.Set;
import java.util.UUID;

@Slf4j
@Component
public class DamagePreventionService {

    private final GameQueryService gameQueryService;

    public DamagePreventionService(GameQueryService gameQueryService) {
        this.gameQueryService = gameQueryService;
    }

    int applyGlobalPreventionShield(GameData gameData, int damage) {
        if (!gameQueryService.isDamagePreventable(gameData)) return damage;
        int shield = gameData.globalDamagePreventionShield;
        if (shield <= 0 || damage <= 0) return damage;
        int prevented = Math.min(shield, damage);
        gameData.globalDamagePreventionShield = shield - prevented;
        return damage - prevented;
    }

    public int applyCreaturePreventionShield(GameData gameData, Permanent permanent, int damage) {
        if (gameQueryService.isDamagePreventable(gameData)) {
            if (permanent.getCard().getEffects(EffectSlot.STATIC).stream().anyMatch(e -> e instanceof PreventAllDamageEffect)) return 0;
            if (gameQueryService.hasAuraWithEffect(gameData, permanent, PreventAllDamageToAndByEnchantedCreatureEffect.class)) return 0;
            if (damage > 0 && permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(e -> e instanceof PreventDamageAndAddMinusCountersEffect)) {
                if (!gameQueryService.cantHaveCounters(gameData, permanent)) {
                    permanent.setMinusOneMinusOneCounters(permanent.getMinusOneMinusOneCounters() + damage);
                }
                return 0;
            }
            damage = applyGlobalPreventionShield(gameData, damage);
            int shield = permanent.getDamagePreventionShield();
            if (shield <= 0 || damage <= 0) return damage;
            int prevented = Math.min(shield, damage);
            permanent.setDamagePreventionShield(shield - prevented);
            return damage - prevented;
        }
        return damage;
    }

    public int applyPlayerPreventionShield(GameData gameData, UUID playerId, int damage) {
        if (!gameQueryService.isDamagePreventable(gameData)) return damage;
        damage = applyGlobalPreventionShield(gameData, damage);
        int shield = gameData.playerDamagePreventionShields.getOrDefault(playerId, 0);
        if (shield <= 0 || damage <= 0) return damage;
        int prevented = Math.min(shield, damage);
        gameData.playerDamagePreventionShields.put(playerId, shield - prevented);
        return damage - prevented;
    }

    public boolean isSourceDamagePreventedForPlayer(GameData gameData, UUID playerId, UUID sourcePermanentId) {
        if (!gameQueryService.isDamagePreventable(gameData)) return false;
        if (sourcePermanentId == null) return false;
        Set<UUID> preventedSources = gameData.playerSourceDamagePreventionIds.get(playerId);
        return preventedSources != null && preventedSources.contains(sourcePermanentId);
    }

    public boolean applyColorDamagePreventionForPlayer(GameData gameData, UUID playerId, CardColor sourceColor) {
        if (!gameQueryService.isDamagePreventable(gameData)) return false;
        if (sourceColor == null) return false;
        Map<CardColor, Integer> colorMap = gameData.playerColorDamagePreventionCount.get(playerId);
        if (colorMap == null) return false;
        Integer count = colorMap.get(sourceColor);
        if (count == null || count <= 0) return false;
        colorMap.put(sourceColor, count - 1);
        return true;
    }
}
