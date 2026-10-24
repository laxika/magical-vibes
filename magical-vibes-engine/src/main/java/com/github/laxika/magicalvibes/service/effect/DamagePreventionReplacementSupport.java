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

        List<Permanent> sources = damageToControllerCounterOnSelfSources(gameData, playerId);
        return sources.isEmpty() ? 0
                : preventDamageToControllerAndPutCounterOnSelf(gameData, sources.getFirst(), damage, true);
    }

    /** The permanents {@code playerId} controls that prevent damage to them and take counters for it. */
    public List<Permanent> damageToControllerCounterOnSelfSources(GameData gameData, UUID playerId) {
        return gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                .filter(permanent -> findDamagePreventionEffect(gameData, permanent) != null)
                .toList();
    }

    /**
     * Applies {@code source}'s prevention to {@code damage} and puts its counters on it. Unpreventable
     * damage still gets the counters, but nothing is prevented (CR 615.12).
     *
     * @param preventable whether the damage can be prevented
     * @return the damage prevented
     */
    public int preventDamageToControllerAndPutCounterOnSelf(GameData gameData, Permanent source, int damage,
                                                             boolean preventable) {
        PreventDamageToControllerAndPutCounterOnSelfEffect effect = findDamagePreventionEffect(gameData, source);
        if (damage <= 0 || effect == null) return 0;

        int counterCount = effect.putThatManyCounters() ? damage : 1;
        permanentCounterSupport.placeCounterOnPermanent(
                gameData, null, source, effect.counterType(), counterCount);
        return preventable ? damage : 0;
    }

    private PreventDamageToControllerAndPutCounterOnSelfEffect findDamagePreventionEffect(
            GameData gameData, Permanent permanent) {
        if (gameQueryService.hasActiveStaticEffect(gameData, permanent,
                PreventDamageToControllerAndPutCounterOnSelfEffect.class)) {
            PreventDamageToControllerAndPutCounterOnSelfEffect printedEffect = permanent.getCard()
                    .getEffects(EffectSlot.STATIC).stream()
                    .filter(PreventDamageToControllerAndPutCounterOnSelfEffect.class::isInstance)
                    .map(PreventDamageToControllerAndPutCounterOnSelfEffect.class::cast)
                    .findFirst()
                    .orElse(null);
            if (printedEffect != null) {
                return printedEffect;
            }
        }

        return gameQueryService.getGrantedEffects(gameData, permanent).stream()
                .filter(PreventDamageToControllerAndPutCounterOnSelfEffect.class::isInstance)
                .map(PreventDamageToControllerAndPutCounterOnSelfEffect.class::cast)
                .findFirst()
                .orElse(null);
    }

    /**
     * Whether {@link #applyDamageToPermanentByRemovingCountersOrSacrificing} would replace
     * {@code damage} to {@code permanent}.
     */
    public boolean removesCountersOrSacrificesForDamage(GameData gameData, Permanent permanent, int damage) {
        PreventDamageByRemovingCountersOrSacrificeEffect effect = activeEffect(gameData, permanent, damage);
        return effect != null && (permanent.getCounterCount(effect.counterType()) >= damage
                || !gameQueryService.cantBeSacrificed(gameData, permanent)
                && gameQueryService.findPermanentController(gameData, permanent.getId()) != null);
    }

    /**
     * Whether {@link #applyDamageToPermanentByRemovingCountersOrSacrificing} would sacrifice {@code permanent}
     * for {@code damage} (it has too few counters to remove), without changing the game.
     */
    public boolean sacrificesForDamage(GameData gameData, Permanent permanent, int damage) {
        PreventDamageByRemovingCountersOrSacrificeEffect effect = activeEffect(gameData, permanent, damage);
        return effect != null && permanent.getCounterCount(effect.counterType()) < damage
                && !gameQueryService.cantBeSacrificed(gameData, permanent)
                && gameQueryService.findPermanentController(gameData, permanent.getId()) != null;
    }

    private PreventDamageByRemovingCountersOrSacrificeEffect activeEffect(GameData gameData, Permanent permanent,
                                                                          int damage) {
        if (damage <= 0 || permanent == null
                || !gameQueryService.hasActiveStaticEffect(
                gameData, permanent, PreventDamageByRemovingCountersOrSacrificeEffect.class)) {
            return null;
        }
        return permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                .filter(PreventDamageByRemovingCountersOrSacrificeEffect.class::isInstance)
                .map(PreventDamageByRemovingCountersOrSacrificeEffect.class::cast)
                .findFirst()
                .orElse(null);
    }

    /** Applies a permanent's damage replacement that consumes counters or sacrifices it. */
    public boolean applyDamageToPermanentByRemovingCountersOrSacrificing(
            GameData gameData, Permanent permanent, int damage) {
        PreventDamageByRemovingCountersOrSacrificeEffect effect = activeEffect(gameData, permanent, damage);
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
