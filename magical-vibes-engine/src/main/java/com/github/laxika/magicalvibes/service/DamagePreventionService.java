package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CreatureDamageRedirectShield;
import com.github.laxika.magicalvibes.model.DamageRedirectShield;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.SourceDamageRedirectShield;
import com.github.laxika.magicalvibes.model.TargetSourceDamagePreventionShield;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllCombatDamageToAndByEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToAndByEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DelayedPlusOnePlusOneCounterRegrowthEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllNoncombatDamageToAttachedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageAndAddMinusCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PreventCombatDamageToAttackingCreaturesYouControlEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageAndRemovePlusOnePlusOneCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromOpponentSourcesEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToOtherCreaturesAndAddPlusCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToControllerPerClericEffect;
import com.github.laxika.magicalvibes.model.effect.PreventFixedDamagePerSourceToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.PreventNoncombatDamageToControllerAndGainLifeEffect;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.PreventSpellDamageToOpponentAndCreateTokensEffect;
import com.github.laxika.magicalvibes.model.effect.PreventXDamageFromEachSourceToAttachedCreatureEffect;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.UUID;
import com.github.laxika.magicalvibes.model.CounterType;

@Slf4j
@Component
public class DamagePreventionService {

    private final GameQueryService gameQueryService;
    private final LifeSupport lifeSupport;

    public DamagePreventionService(GameQueryService gameQueryService, LifeSupport lifeSupport) {
        this.gameQueryService = gameQueryService;
        this.lifeSupport = lifeSupport;
    }

    int applyGlobalPreventionShield(GameData gameData, int damage) {
        if (!gameQueryService.isDamagePreventable(gameData)) return damage;
        int shield = gameData.globalDamagePreventionShield;
        if (shield <= 0 || damage <= 0) return damage;
        int prevented = Math.min(shield, damage);
        gameData.globalDamagePreventionShield = shield - prevented;
        return damage - prevented;
    }

    /**
     * Applies target+source-specific prevention shields (e.g. Healing Grace).
     * Only prevents damage from the chosen source to the chosen target.
     * Returns the remaining damage after prevention.
     */
    public int applyTargetSourcePreventionShield(GameData gameData, UUID targetId, UUID sourceId, int damage) {
        if (!gameQueryService.isDamagePreventable(gameData)) return damage;
        if (damage <= 0 || targetId == null || sourceId == null || gameData.targetSourceDamagePreventionShields.isEmpty())
            return damage;

        int remaining = damage;
        List<TargetSourceDamagePreventionShield> toReAdd = new ArrayList<>();
        Iterator<TargetSourceDamagePreventionShield> it = gameData.targetSourceDamagePreventionShields.iterator();

        while (it.hasNext() && remaining > 0) {
            TargetSourceDamagePreventionShield shield = it.next();
            if (!shield.targetId().equals(targetId) || !shield.sourceId().equals(sourceId)) continue;

            int prevented = Math.min(shield.remainingAmount(), remaining);
            remaining -= prevented;
            it.remove();

            if (prevented < shield.remainingAmount()) {
                toReAdd.add(shield.withReducedAmount(prevented));
            }
        }

        gameData.targetSourceDamagePreventionShields.addAll(toReAdd);
        return remaining;
    }

    public int applyCreaturePreventionShield(GameData gameData, Permanent permanent, int damage) {
        return applyCreaturePreventionShield(gameData, permanent, damage, false);
    }

    public int applyCreaturePreventionShield(GameData gameData, Permanent permanent, int damage, boolean isCombatDamage) {
        // Blinding Fog: prevent all damage to all creatures
        if (gameQueryService.isDamagePreventable(gameData) && gameData.preventAllDamageToAllCreatures) return 0;
        // Wellgabber Apothecary: prevent all damage to specific target creatures this turn
        if (gameQueryService.isDamagePreventable(gameData) && gameData.creaturesWithAllDamagePrevented.contains(permanent.getId())) return 0;
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
                    ? Math.min(1, permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                    : Math.min(damage, permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE));
            if (countersToRemove > 0 && !gameQueryService.cantHaveCounters(gameData, permanent)) {
                permanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) - countersToRemove);
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
            // Dolmen Gate: "Prevent all combat damage that would be dealt to attacking creatures you control."
            if (isCombatDamage && permanent.isAttacking() && hasAttackingCreatureCombatDamagePreventionSource(gameData, permanent)) return 0;
            if (!isCombatDamage && gameQueryService.hasAuraWithEffect(gameData, permanent, PreventAllNoncombatDamageToAttachedCreatureEffect.class)) return 0;
            // Shield of the Realm: "If a source would deal damage to equipped creature, prevent N of that damage."
            damage = applyAttachedPerSourceDamageReduction(gameData, permanent, damage);
            if (damage <= 0) return 0;
            if (damage > 0 && permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(e -> e instanceof PreventDamageAndAddMinusCountersEffect)) {
                if (!gameQueryService.cantHaveCounters(gameData, permanent)) {
                    permanent.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, permanent.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) + damage);
                }
                return 0;
            }
            // Vigor: "If damage would be dealt to another creature you control, prevent that damage.
            // Put a +1/+1 counter on that creature for each 1 damage prevented this way." The effect
            // lives on a different permanent (Vigor) controlled by this creature's controller.
            if (damage > 0 && hasOtherCreatureDamagePreventionSource(gameData, permanent)) {
                if (!gameQueryService.cantHaveCounters(gameData, permanent)) {
                    permanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + damage);
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
     * Vigor-style protection: returns true when the given creature's controller controls some other
     * permanent (i.e. not the creature itself — "another creature you control") carrying
     * {@link PreventDamageToOtherCreaturesAndAddPlusCountersEffect}. Such damage is fully prevented and
     * replaced with +1/+1 counters by the caller.
     */
    private boolean hasOtherCreatureDamagePreventionSource(GameData gameData, Permanent creature) {
        UUID controllerId = gameQueryService.findPermanentController(gameData, creature.getId());
        if (controllerId == null) return false;
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return false;
        return battlefield.stream()
                .filter(p -> !p.getId().equals(creature.getId()))
                .flatMap(p -> p.getCard().getEffects(EffectSlot.STATIC).stream())
                .anyMatch(e -> e instanceof PreventDamageToOtherCreaturesAndAddPlusCountersEffect);
    }

    /**
     * Dolmen Gate-style protection: returns true when the given attacking creature's controller controls
     * a permanent carrying {@link PreventCombatDamageToAttackingCreaturesYouControlEffect}. Combat damage
     * dealt to such a creature is fully prevented by the caller.
     */
    private boolean hasAttackingCreatureCombatDamagePreventionSource(GameData gameData, Permanent creature) {
        UUID controllerId = gameQueryService.findPermanentController(gameData, creature.getId());
        if (controllerId == null) return false;
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return false;
        return battlefield.stream()
                .flatMap(p -> p.getCard().getEffects(EffectSlot.STATIC).stream())
                .anyMatch(e -> e instanceof PreventCombatDamageToAttackingCreaturesYouControlEffect);
    }

    /**
     * Registers delayed +1/+1 counter regrowth triggers for Protean Hydra-style effects.
     * Each removed counter creates a separate delayed trigger that adds 2 +1/+1 counters
     * at the beginning of the next end step (ruling: "its last ability will trigger that many times").
     */
    void registerDelayedRegrowth(GameData gameData, Permanent permanent, int countersRemoved) {
        if (permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(e -> e instanceof DelayedPlusOnePlusOneCounterRegrowthEffect)) {
            gameData.addDelayedPlusOneCounters(permanent.getId(), countersRemoved * 2);
        }
    }

    /**
     * Deep Wood: whether combat damage dealt to the given player by attacking creatures is prevented this
     * turn. Combat damage to a defending player always originates from attacking creatures, so this needs
     * only the player flag.
     */
    public boolean isCombatDamageFromAttackersPreventedForPlayer(GameData gameData, UUID playerId) {
        if (!gameQueryService.isDamagePreventable(gameData)) return false;
        return gameData.playersWithDamageFromAttackersPrevented.contains(playerId);
    }

    /**
     * Deep Wood: whether noncombat damage dealt to the given player is prevented this turn because its
     * source permanent is currently an attacking creature.
     */
    public boolean isNoncombatDamageFromAttackerPreventedForPlayer(GameData gameData, UUID playerId, UUID sourcePermanentId) {
        if (!isCombatDamageFromAttackersPreventedForPlayer(gameData, playerId)) return false;
        if (sourcePermanentId == null) return false;
        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        return source != null && source.isAttacking();
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
     * Applies one-shot Circle-of-Protection shields: if a shield matches this (player, source), the
     * entire next damage event is prevented and the shield is consumed. Returns the remaining damage.
     */
    public int applyPlayerNextSourceDamageShield(GameData gameData, UUID playerId, UUID sourcePermanentId, int damage) {
        if (!gameQueryService.isDamagePreventable(gameData)) return damage;
        if (damage <= 0 || playerId == null || sourcePermanentId == null
                || gameData.playerSourceNextDamageShields.isEmpty()) {
            return damage;
        }
        var it = gameData.playerSourceNextDamageShields.iterator();
        while (it.hasNext()) {
            var shield = it.next();
            if (shield.playerId().equals(playerId) && shield.sourceId().equals(sourcePermanentId)) {
                it.remove();
                // Reverse Damage: gain life equal to the damage prevented this way.
                if (shield.gainLife()) {
                    lifeSupport.applyGainLife(gameData, playerId, damage, "prevented damage");
                }
                return 0;
            }
        }
        return damage;
    }

    /**
     * Applies one-shot Sanctum Guardian shields: if a shield matches this source, the entire next
     * damage event it would deal to any target (player, planeswalker, or creature) is prevented and
     * the shield is consumed. Returns the remaining damage.
     */
    public int applyChosenSourceNextDamageToAnyTargetShield(GameData gameData, UUID sourcePermanentId, int damage) {
        if (!gameQueryService.isDamagePreventable(gameData)) return damage;
        if (damage <= 0 || sourcePermanentId == null || gameData.sourceNextDamageToAnyTargetShields.isEmpty()) {
            return damage;
        }
        // List.remove(Object) removes the first matching entry — a single shield is consumed per event.
        return gameData.sourceNextDamageToAnyTargetShields.remove(sourcePermanentId) ? 0 : damage;
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

    /**
     * Checks creature-specific damage redirect shields (e.g. Oracle's Attendants) for damage dealt to a
     * specific creature by a chosen source. This is a redirection (replacement) effect: when the chosen
     * source would deal damage to the protected creature this turn, ALL of it (no amount limit) is
     * redirected to the shield's redirect target. Reuses {@link GameData#pendingSourceRedirectDamage}
     * so callers deal the redirected damage via their existing {@code processSourceRedirectDamage}.
     *
     * @param protectedPermanentId the creature receiving damage
     * @param sourcePermanentId    the permanent dealing the damage
     * @param damage               the raw damage amount
     * @return the remaining damage after redirection (0 if a shield matched)
     */
    public int applyCreatureRedirectShields(GameData gameData, UUID protectedPermanentId, UUID sourcePermanentId, int damage) {
        // No isDamagePreventable check — this is redirection (replacement), not prevention.
        if (damage <= 0 || protectedPermanentId == null || sourcePermanentId == null
                || gameData.creatureDamageRedirectShields.isEmpty()) return damage;

        int remaining = damage;
        List<CreatureDamageRedirectShield> toReAdd = new ArrayList<>();
        Iterator<CreatureDamageRedirectShield> it = gameData.creatureDamageRedirectShields.iterator();

        while (it.hasNext() && remaining > 0) {
            CreatureDamageRedirectShield shield = it.next();
            if (!shield.protectedPermanentId().equals(protectedPermanentId)) continue;
            // A null source matches any source (e.g. Zealous Inquisitor); otherwise it must match exactly.
            if (shield.damageSourceId() != null && !shield.damageSourceId().equals(sourcePermanentId)) continue;

            if (shield.isUnlimited()) {
                // Unlimited (Oracle's Attendants): redirect all remaining damage; the shield persists.
                gameData.pendingSourceRedirectDamage.add(new SourceDamageRedirectShield(
                        protectedPermanentId, sourcePermanentId, remaining, shield.redirectTargetId()));
                remaining = 0;
            } else {
                // Amount-limited (Zealous Inquisitor): redirect up to the remaining amount, then consume.
                int redirected = Math.min(shield.remainingAmount(), remaining);
                remaining -= redirected;
                it.remove();
                if (redirected < shield.remainingAmount()) {
                    toReAdd.add(shield.withReducedAmount(redirected));
                }
                if (redirected > 0) {
                    gameData.pendingSourceRedirectDamage.add(new SourceDamageRedirectShield(
                            protectedPermanentId, sourcePermanentId, redirected, shield.redirectTargetId()));
                }
            }
        }

        gameData.creatureDamageRedirectShields.addAll(toReAdd);
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

    /**
     * Purity-style prevention: if the given player controls a permanent with
     * {@link PreventNoncombatDamageToControllerAndGainLifeEffect}, all noncombat damage that
     * would be dealt to them is prevented. Returns the amount prevented (the caller gains that
     * much life). Returns 0 when damage can't be prevented or no such permanent is present.
     */
    public int applyControllerNoncombatDamagePrevention(GameData gameData, UUID playerId, int damage) {
        if (!gameQueryService.isDamagePreventable(gameData)) return 0;
        if (damage <= 0) return 0;

        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return 0;

        boolean hasEffect = battlefield.stream().anyMatch(p ->
                p.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(e -> e instanceof PreventNoncombatDamageToControllerAndGainLifeEffect));
        return hasEffect ? damage : 0;
    }

    /**
     * Battletide Alchemist-style prevention: "If a source would deal damage to a player, you may prevent
     * X of that damage, where X is the number of Clerics you control." Modeled on the controller of the
     * permanent (the "you may" choice would never prevent damage dealt to an opponent). Prevents up to
     * X = (Clerics that player controls) from each source, multiplied by the number of Battletide-style
     * permanents they control (each is a separate "you may prevent X"). Returns the amount prevented
     * (the caller subtracts it); 0 when damage can't be prevented or no such permanent is present.
     */
    public int applyControllerPerClericDamagePrevention(GameData gameData, UUID playerId, int damage) {
        if (!gameQueryService.isDamagePreventable(gameData)) return 0;
        if (damage <= 0) return 0;

        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return 0;

        long shields = battlefield.stream().filter(p ->
                p.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(e -> e instanceof PreventDamageToControllerPerClericEffect)).count();
        if (shields == 0) return 0;

        long clerics = gameQueryService.countControlledSubtypePermanents(gameData, playerId, CardSubtype.CLERIC);
        return (int) Math.min(damage, clerics * shields);
    }

    /**
     * Urza's Armor-style prevention: "If a source would deal damage to you, prevent N of that damage."
     * Modeled on the controller of the permanent. Prevents up to the summed {@code amount} of every such
     * permanent they control from each source that would deal damage to them (combat and noncombat).
     * Returns the amount prevented (the caller subtracts it); 0 when damage can't be prevented or no such
     * permanent is present.
     */
    public int applyControllerFixedPerSourceDamagePrevention(GameData gameData, UUID playerId, int damage) {
        if (!gameQueryService.isDamagePreventable(gameData)) return 0;
        if (damage <= 0) return 0;

        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return 0;

        int reduction = battlefield.stream()
                .flatMap(p -> p.getCard().getEffects(EffectSlot.STATIC).stream())
                .filter(e -> e instanceof PreventFixedDamagePerSourceToControllerEffect)
                .mapToInt(e -> ((PreventFixedDamagePerSourceToControllerEffect) e).amount())
                .sum();
        return Math.min(damage, reduction);
    }

    /**
     * Hostility-style prevention: if the damage source is a spell controlled by a player who controls a
     * permanent with {@link PreventSpellDamageToOpponentAndCreateTokensEffect}, and the damaged player is
     * an opponent of that controller, all of that damage is prevented. Returns the matching effect (whose
     * token blueprint the caller uses to create one token per 1 damage prevented), or {@code null} when it
     * doesn't apply (damage can't be prevented, the source isn't a spell, or no such permanent is present).
     */
    public PreventSpellDamageToOpponentAndCreateTokensEffect findSpellDamageToOpponentPrevention(
            GameData gameData, StackEntry entry, UUID playerId, int damage) {
        if (!gameQueryService.isDamagePreventable(gameData)) return null;
        if (damage <= 0 || entry == null) return null;

        // Only damage dealt by a spell qualifies (not abilities or combat).
        StackEntryType type = entry.getEntryType();
        if (type == StackEntryType.TRIGGERED_ABILITY || type == StackEntryType.ACTIVATED_ABILITY) return null;

        // The damaged player must be an opponent of the spell's controller.
        UUID spellControllerId = entry.getControllerId();
        if (spellControllerId == null || spellControllerId.equals(playerId)) return null;

        List<Permanent> battlefield = gameData.playerBattlefields.get(spellControllerId);
        if (battlefield == null) return null;

        return battlefield.stream()
                .flatMap(p -> p.getCard().getEffects(EffectSlot.STATIC).stream())
                .filter(e -> e instanceof PreventSpellDamageToOpponentAndCreateTokensEffect)
                .map(e -> (PreventSpellDamageToOpponentAndCreateTokensEffect) e)
                .findFirst().orElse(null);
    }

    /**
     * Applies per-source damage reduction from attached permanents with
     * {@link PreventXDamageFromEachSourceToAttachedCreatureEffect}
     * (e.g. Shield of the Realm: "If a source would deal damage to equipped creature, prevent 2 of that damage.").
     * Sums the reduction from all such attached permanents and reduces the damage accordingly (min 0).
     */
    private int applyAttachedPerSourceDamageReduction(GameData gameData, Permanent creature, int damage) {
        if (damage <= 0) return damage;

        int totalReduction = 0;
        for (UUID playerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
            if (battlefield == null) continue;
            for (Permanent p : battlefield) {
                if (p.isAttached() && p.getAttachedTo().equals(creature.getId())) {
                    for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
                        if (effect instanceof PreventXDamageFromEachSourceToAttachedCreatureEffect e) {
                            totalReduction += e.amount();
                        }
                    }
                }
            }
        }

        if (totalReduction <= 0) return damage;
        return Math.max(0, damage - totalReduction);
    }
}
