package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.DamageRedirectShield;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.SourceDamageRedirectShield;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllCombatDamageToAndByEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToAndByEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DelayedPlusOnePlusOneCounterRegrowthEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllNoncombatDamageToAttachedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageAndAddMinusCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageAndRemovePlusOnePlusOneCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromOpponentSourcesEffect;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
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
        return applyCreaturePreventionShield(gameData, permanent, damage, false);
    }

    public int applyCreaturePreventionShield(GameData gameData, Permanent permanent, int damage, boolean isCombatDamage) {
        // Safe Passage: prevent all damage to creatures controlled by a player with full prevention
        if (gameQueryService.isDamagePreventable(gameData)) {
            UUID controllerId = gameQueryService.findPermanentController(gameData, permanent.getId());
            if (controllerId != null && gameData.playersWithAllDamagePrevented.contains(controllerId)) return 0;
        }
        // Protean Hydra / Unbreathing Horde: "If damage would be dealt to this creature, prevent that
        // damage and remove +1/+1 counters." Counters are removed regardless of whether damage is preventable.
        // When removeOneOnly=true (Unbreathing Horde), exactly one counter is removed per damage event.
        // When removeOneOnly=false (Protean Hydra), counters equal to the damage are removed.
        var preventRemoveEffect = permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof PreventDamageAndRemovePlusOnePlusOneCountersEffect)
                .map(e -> (PreventDamageAndRemovePlusOnePlusOneCountersEffect) e)
                .findFirst().orElse(null);
        if (damage > 0 && preventRemoveEffect != null) {
            int countersToRemove = preventRemoveEffect.removeOneOnly()
                    ? Math.min(1, permanent.getPlusOnePlusOneCounters())
                    : Math.min(damage, permanent.getPlusOnePlusOneCounters());
            if (countersToRemove > 0 && !gameQueryService.cantHaveCounters(gameData, permanent)) {
                permanent.setPlusOnePlusOneCounters(permanent.getPlusOnePlusOneCounters() - countersToRemove);
                registerDelayedRegrowth(gameData, permanent, countersToRemove);
            }
            // Prevention only applies if damage is preventable
            if (gameQueryService.isDamagePreventable(gameData)) {
                return 0;
            }
            return damage;
        }
        if (gameQueryService.isDamagePreventable(gameData)) {
            if (permanent.getCard().getEffects(EffectSlot.STATIC).stream().anyMatch(e -> e instanceof PreventAllDamageEffect)) return 0;
            if (gameQueryService.hasAuraWithEffect(gameData, permanent, PreventAllDamageToAndByEnchantedCreatureEffect.class)) return 0;
            if (isCombatDamage && gameQueryService.hasAuraWithEffect(gameData, permanent, PreventAllCombatDamageToAndByEnchantedCreatureEffect.class)) return 0;
            if (!isCombatDamage && gameQueryService.hasAuraWithEffect(gameData, permanent, PreventAllNoncombatDamageToAttachedCreatureEffect.class)) return 0;
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

    /**
     * Registers delayed +1/+1 counter regrowth triggers for Protean Hydra-style effects.
     * Each removed counter creates a separate delayed trigger that adds 2 +1/+1 counters
     * at the beginning of the next end step (ruling: "its last ability will trigger that many times").
     */
    void registerDelayedRegrowth(GameData gameData, Permanent permanent, int countersRemoved) {
        if (permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(e -> e instanceof DelayedPlusOnePlusOneCounterRegrowthEffect)) {
            int pending = gameData.pendingDelayedPlusOnePlusOneCounters.getOrDefault(permanent.getId(), 0);
            gameData.pendingDelayedPlusOnePlusOneCounters.put(permanent.getId(), pending + countersRemoved * 2);
        }
    }

    public int applyPlayerPreventionShield(GameData gameData, UUID playerId, int damage) {
        if (!gameQueryService.isDamagePreventable(gameData)) return damage;
        if (gameData.playersWithAllDamagePrevented.contains(playerId)) return 0;
        // Process redirect shields first (e.g. Vengeful Archon)
        damage = applyRedirectShields(gameData, playerId, damage);
        damage = applyGlobalPreventionShield(gameData, damage);
        int shield = gameData.playerDamagePreventionShields.getOrDefault(playerId, 0);
        if (shield <= 0 || damage <= 0) return damage;
        int prevented = Math.min(shield, damage);
        gameData.playerDamagePreventionShields.put(playerId, shield - prevented);
        return damage - prevented;
    }

    /**
     * Consumes damage redirect shields for the given player. Prevented damage is tracked
     * in {@link GameData#pendingRedirectDamage} for the caller to deal after damage processing.
     * Returns the remaining damage after redirect shield prevention.
     */
    private int applyRedirectShields(GameData gameData, UUID playerId, int damage) {
        if (damage <= 0 || gameData.damageRedirectShields.isEmpty()) return damage;

        int remaining = damage;
        List<DamageRedirectShield> toReAdd = new ArrayList<>();
        Iterator<DamageRedirectShield> it = gameData.damageRedirectShields.iterator();

        while (it.hasNext() && remaining > 0) {
            DamageRedirectShield shield = it.next();
            if (!shield.protectedPlayerId().equals(playerId)) continue;

            int prevented = Math.min(shield.remainingAmount(), remaining);
            remaining -= prevented;
            it.remove();

            // If shield is not fully consumed, save reduced version to re-add after iteration
            if (prevented < shield.remainingAmount()) {
                toReAdd.add(shield.withReducedAmount(prevented));
            }

            if (prevented > 0) {
                gameData.pendingRedirectDamage.add(new DamageRedirectShield(
                        playerId, prevented, shield.sourcePermanentId(), shield.sourceCard(), shield.redirectTargetPlayerId()));
            }
        }

        // Re-add partially consumed shields after iteration is complete
        gameData.damageRedirectShields.addAll(toReAdd);

        return remaining;
    }

    public boolean isSourceDamagePreventedForPlayer(GameData gameData, UUID playerId, UUID sourcePermanentId) {
        if (!gameQueryService.isDamagePreventable(gameData)) return false;
        if (sourcePermanentId == null) return false;
        Set<UUID> preventedSources = gameData.playerSourceDamagePreventionIds.get(playerId);
        return preventedSources != null && preventedSources.contains(sourcePermanentId);
    }

    /**
     * Checks source-specific damage redirect shields (e.g. Harm's Way) for damage dealt to a player
     * or permanents they control. This is a redirection effect (replacement), NOT a prevention effect,
     * so it applies even when damage can't be prevented (e.g. Leyline of Punishment).
     * If a matching shield is found, consumes up to the shield's remaining amount, stores
     * the redirect damage in {@link GameData#pendingSourceRedirectDamage}, and returns the remaining damage.
     *
     * @param protectedPlayerId the player (or permanent's controller) receiving damage
     * @param sourcePermanentId the permanent dealing the damage
     * @param damage            the raw damage amount
     * @return the remaining damage after redirection
     */
    public int applySourceRedirectShields(GameData gameData, UUID protectedPlayerId, UUID sourcePermanentId, int damage) {
        // No isDamagePreventable check — this is redirection (replacement), not prevention
        if (damage <= 0 || sourcePermanentId == null || gameData.sourceDamageRedirectShields.isEmpty()) return damage;

        int remaining = damage;
        List<SourceDamageRedirectShield> toReAdd = new ArrayList<>();
        Iterator<SourceDamageRedirectShield> it = gameData.sourceDamageRedirectShields.iterator();

        while (it.hasNext() && remaining > 0) {
            SourceDamageRedirectShield shield = it.next();
            if (!shield.protectedPlayerId().equals(protectedPlayerId) || !shield.damageSourceId().equals(sourcePermanentId))
                continue;

            int prevented = Math.min(shield.remainingAmount(), remaining);
            remaining -= prevented;
            it.remove();

            if (prevented < shield.remainingAmount()) {
                toReAdd.add(shield.withReducedAmount(prevented));
            }

            if (prevented > 0) {
                gameData.pendingSourceRedirectDamage.add(new SourceDamageRedirectShield(
                        protectedPlayerId, sourcePermanentId, prevented, shield.redirectTargetId()));
            }
        }

        gameData.sourceDamageRedirectShields.addAll(toReAdd);
        return remaining;
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

    /**
     * Applies static damage reduction from permanents with {@link PreventDamageFromOpponentSourcesEffect}
     * on the receiving player's battlefield (e.g. Guardian Seraph).
     * Only reduces damage from opponent-controlled sources. Returns the damage after reduction (min 0).
     */
    public int applyOpponentSourceDamageReduction(GameData gameData, UUID playerId, UUID sourceControllerId, int damage) {
        if (!gameQueryService.isDamagePreventable(gameData)) return damage;
        if (damage <= 0) return damage;
        if (sourceControllerId == null || sourceControllerId.equals(playerId)) return damage;

        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return damage;

        int totalReduction = 0;
        for (Permanent permanent : battlefield) {
            for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof PreventDamageFromOpponentSourcesEffect e) {
                    totalReduction += e.amount();
                }
            }
        }

        if (totalReduction <= 0) return damage;
        return Math.max(0, damage - totalReduction);
    }
}
