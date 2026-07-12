package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.DamagePreventionService;
import com.github.laxika.magicalvibes.service.GameBroadcastService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.DamageRedirectShield;
import com.github.laxika.magicalvibes.model.SourceDamageRedirectShield;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.CounterType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Predicate;

/**
 * Shared damage helpers used by every "normal" Damage effect handler and by other services
 * (input handlers, combat). Extracted verbatim from {@code DamageResolutionService}; behavior
 * (routing, prevention, lethal-damage deferral, trigger order) is identical.
 */
@Slf4j
@Component
@RequiredArgsConstructor
public class DamageSupport {

    private final GraveyardService graveyardService;
    private final DamagePreventionService damagePreventionService;
    private final GameOutcomeService gameOutcomeService;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final GameBroadcastService gameBroadcastService;
    private final PermanentRemovalService permanentRemovalService;
    private final TriggerCollectionService triggerCollectionService;
    private final LifeSupport lifeSupport;
    private final PermanentControlSupport permanentControlSupport;
    private final PermanentCounterSupport permanentCounterSupport;

    /**
     * Applies damage to a creature, handling prevention shield, recording, logging,
     * and checking for lethal damage (indestructible/regenerate).
     * Returns true if the creature took lethal damage and should be destroyed.
     * Caller is responsible for removal (use {@link #destroyPermanent} for single-target,
     * or batch-collect for multi-target effects).
     */
    public boolean dealCreatureDamage(GameData gameData, StackEntry entry, Permanent target, int rawDamage) {
        return dealCreatureDamage(gameData, entry, target, rawDamage, null);
    }

    /**
     * Overload that accepts an explicit damage source permanent (e.g. the biting creature).
     * When {@code damageSource} is non-null, its ID is used for recording, its name for logging,
     * and keywords are checked directly on it. When null, falls back to entry-based lookup.
     */
    public boolean dealCreatureDamage(GameData gameData, StackEntry entry, Permanent target, int rawDamage, Permanent damageSource) {
        // Defense in depth: a creature can never deal negative damage. Guards against any upstream
        // computation (e.g. future power-based effects) that might produce a negative value.
        rawDamage = Math.max(0, rawDamage);
        // Apply source-specific redirect shields (e.g. Harm's Way) before creature prevention
        UUID targetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        UUID sourcePermId = damageSource != null ? damageSource.getId() : entry.getSourcePermanentId();
        if (targetControllerId != null && sourcePermId != null) {
            rawDamage = damagePreventionService.applySourceRedirectShields(gameData, targetControllerId, sourcePermId, rawDamage);
            processSourceRedirectDamage(gameData);
        }
        // Apply creature-specific redirect shields (e.g. Oracle's Attendants): redirect all damage from
        // a chosen source to the protected creature onto another permanent.
        if (sourcePermId != null) {
            rawDamage = damagePreventionService.applyCreatureRedirectShields(gameData, target.getId(), sourcePermId, rawDamage);
            processSourceRedirectDamage(gameData);
        }
        // Apply target+source-specific prevention shields (e.g. Healing Grace)
        if (sourcePermId != null) {
            rawDamage = damagePreventionService.applyTargetSourcePreventionShield(gameData, target.getId(), sourcePermId, rawDamage);
            // Apply one-shot Sanctum Guardian shields (prevent the next damage from the chosen source to any target)
            rawDamage = damagePreventionService.applyChosenSourceNextDamageToAnyTargetShield(gameData, sourcePermId, rawDamage);
        }
        int damage = damagePreventionService.applyCreaturePreventionShield(gameData, target, rawDamage);

        if (damageSource != null) {
            graveyardService.recordCreatureDamagedByPermanent(gameData, damageSource.getId(), target, damage);
        } else if (entry.getSourcePermanentId() != null) {
            graveyardService.recordCreatureDamagedByPermanent(gameData, entry.getSourcePermanentId(), target, damage);
        }

        // Fire ON_DEALT_DAMAGE triggers (e.g. Nested Ghoul, Phyrexian Obliterator)
        if (damage > 0) {
            gameData.permanentsDealtDamageThisTurn.add(target.getId());

            UUID sourceControllerId = damageSource != null
                    ? gameQueryService.findPermanentController(gameData, damageSource.getId())
                    : entry.getControllerId();
            triggerCollectionService.checkDealtDamageToCreatureTriggers(gameData, target, damage, sourceControllerId);

            // Fire ON_OPPONENT_CREATURE_DEALT_DAMAGE triggers (e.g. Kazarov)
            UUID damagedCreatureControllerId = gameQueryService.findPermanentController(gameData, target.getId());
            if (damagedCreatureControllerId != null) {
                triggerCollectionService.checkOpponentCreatureDealtDamageTriggers(gameData, damagedCreatureControllerId);
            }

            // Fire ON_ANY_CREATURE_DEALT_DAMAGE triggers (e.g. Death Pits of Rath)
            triggerCollectionService.checkAnyCreatureDealtDamageTriggers(gameData, target);

            // Fire ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE reflection triggers (e.g. Greatbow Doyen)
            Permanent reflectionSource = damageSource != null
                    ? damageSource
                    : (sourcePermId != null ? gameQueryService.findPermanentById(gameData, sourcePermId) : null);
            triggerCollectionService.checkAllyDealtDamageToCreatureTriggers(gameData, reflectionSource, sourceControllerId, damagedCreatureControllerId, damage);
        }

        String sourceName = damageSource != null ? damageSource.getCard().getName() : entry.getCard().getName();

        // Infect and wither both deal creature damage as -1/-1 counters (CR 702.90 / 702.80).
        boolean dealsCounterDamage = gameQueryService.sourceDealsCounterDamageToCreatures(gameData, entry, damageSource);

        if (dealsCounterDamage) {
            if (damage > 0 && !gameQueryService.cantHaveCounters(gameData, target)
                    && !gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, target)) {
                target.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) + damage);
                gameBroadcastService.logAndBroadcast(gameData,
                        sourceName + " puts " + damage + " -1/-1 counters on " + target.getCard().getName() + ".");
                log.info("Game {} - {} puts {} -1/-1 counters on {}", gameData.id, sourceName, damage, target.getCard().getName());
                permanentCounterSupport.fireMinusOneMinusOneCounterPutOnCreatureTriggers(gameData, target, damage);
            }
            // CR 704.5f: 0 toughness from -1/-1 counters — dies regardless of indestructible
            return gameQueryService.getEffectiveToughness(gameData, target) <= 0;
        }

        // Accumulate damage on creature (CR 704.5g — lethal when total marked damage >= toughness)
        target.setMarkedDamage(target.getMarkedDamage() + damage);

        gameBroadcastService.logAndBroadcast(gameData,
                sourceName + " deals " + damage + " damage to " + target.getCard().getName() + ".");
        log.info("Game {} - {} deals {} damage to {}", gameData.id, sourceName, damage, target.getCard().getName());

        if (damage > 0) {
            checkSpellLifelink(gameData, entry, damage);
        }

        boolean sourceHasDeathtouch = gameQueryService.sourceHasKeyword(gameData, entry, damageSource, Keyword.DEATHTOUCH);
        boolean isLethal = gameQueryService.isLethalDamage(target.getMarkedDamage(), gameQueryService.getEffectiveToughness(gameData, target), sourceHasDeathtouch);
        if (isLethal) {
            if (gameQueryService.hasKeyword(gameData, target, Keyword.INDESTRUCTIBLE)) {
                gameBroadcastService.logAndBroadcast(gameData,
                        target.getCard().getName() + " is indestructible and survives.");
                return false;
            }
            return !graveyardService.tryRegenerate(gameData, target);
        }
        return false;
    }

    /**
     * Deals damage to a creature bypassing all prevention effects (shields, protection, global prevention).
     * Used for effects where "the damage can't be prevented" (e.g. Combust).
     */
    public boolean dealCreatureDamageUnpreventable(GameData gameData, StackEntry entry, Permanent target, int rawDamage) {
        // Defense in depth: a creature can never deal negative damage. Guards against any upstream
        // computation (e.g. future power-based effects) that might produce a negative value.
        // Skip applyCreaturePreventionShield — damage is unpreventable
        int damage = Math.max(0, rawDamage);

        if (entry.getSourcePermanentId() != null) {
            graveyardService.recordCreatureDamagedByPermanent(gameData, entry.getSourcePermanentId(), target, damage);
        }

        if (damage > 0) {
            triggerCollectionService.checkDealtDamageToCreatureTriggers(gameData, target, damage, entry.getControllerId());

            // Fire ON_OPPONENT_CREATURE_DEALT_DAMAGE triggers (e.g. Kazarov)
            UUID damagedCreatureControllerId = gameQueryService.findPermanentController(gameData, target.getId());
            if (damagedCreatureControllerId != null) {
                triggerCollectionService.checkOpponentCreatureDealtDamageTriggers(gameData, damagedCreatureControllerId);
            }

            // Fire ON_ANY_CREATURE_DEALT_DAMAGE triggers (e.g. Death Pits of Rath)
            triggerCollectionService.checkAnyCreatureDealtDamageTriggers(gameData, target);

            // Fire ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE reflection triggers (e.g. Greatbow Doyen)
            Permanent reflectionSource = entry.getSourcePermanentId() != null
                    ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                    : null;
            UUID reflectionTargetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
            triggerCollectionService.checkAllyDealtDamageToCreatureTriggers(gameData, reflectionSource, entry.getControllerId(), reflectionTargetControllerId, damage);
        }

        String sourceName = entry.getCard().getName();

        gameBroadcastService.logAndBroadcast(gameData,
                sourceName + " deals " + damage + " damage to " + target.getCard().getName() + ". (damage can't be prevented)");
        log.info("Game {} - {} deals {} unpreventable damage to {}", gameData.id, sourceName, damage, target.getCard().getName());

        if (damage > 0) {
            checkSpellLifelink(gameData, entry, damage);
        }

        boolean sourceHasDeathtouch = gameQueryService.sourceHasKeyword(gameData, entry, null, Keyword.DEATHTOUCH);
        boolean isLethal = gameQueryService.isLethalDamage(damage, gameQueryService.getEffectiveToughness(gameData, target), sourceHasDeathtouch);
        if (isLethal) {
            if (gameQueryService.hasKeyword(gameData, target, Keyword.INDESTRUCTIBLE)) {
                gameBroadcastService.logAndBroadcast(gameData,
                        target.getCard().getName() + " is indestructible and survives.");
                return false;
            }
            return !graveyardService.tryRegenerate(gameData, target);
        }
        return false;
    }

    /**
     * If the stack entry represents a spell that should have lifelink (via
     * {@link com.github.laxika.magicalvibes.model.effect.GrantLifelinkToControllerSpellsByColorEffect}),
     * the controller gains life equal to the effective damage dealt.
     */
    public void checkSpellLifelink(GameData gameData, StackEntry entry, int effectiveDamage) {
        if (effectiveDamage <= 0) return;
        if (!gameQueryService.shouldControllerSpellHaveLifelink(gameData, entry)) return;
        lifeSupport.applyGainLife(gameData, entry.getControllerId(), effectiveDamage,
                "spell lifelink", entry.getCard(), entry.getEntryType());
    }

    public void destroyPermanent(GameData gameData, Permanent target) {
        permanentRemovalService.removePermanentToGraveyard(gameData, target);
        gameBroadcastService.logAndBroadcast(gameData, target.getCard().getName() + " is destroyed.");
        log.info("Game {} - {} is destroyed", gameData.id, target.getCard().getName());
    }

    public void destroyAllLethal(GameData gameData, List<Permanent> destroyed) {
        for (Permanent target : destroyed) {
            destroyPermanent(gameData, target);
        }
        if (!destroyed.isEmpty()) {
            permanentRemovalService.removeOrphanedAuras(gameData);
        }
    }

    public void dealDamageAndDestroyIfLethal(GameData gameData, StackEntry entry, Permanent target, int rawDamage) {
        dealDamageAndDestroyIfLethal(gameData, entry, target, rawDamage, null);
    }

    public void dealDamageAndDestroyIfLethal(GameData gameData, StackEntry entry, Permanent target, int rawDamage, Permanent damageSource) {
        if (dealCreatureDamage(gameData, entry, target, rawDamage, damageSource)) {
            gameData.pendingLethalDamageDestructions.add(target);
        }
    }

    public void dealDamageAndDestroyIfLethalUnpreventable(GameData gameData, StackEntry entry, Permanent target, int rawDamage) {
        if (dealCreatureDamageUnpreventable(gameData, entry, target, rawDamage)) {
            destroyPermanent(gameData, target);
            permanentRemovalService.removeOrphanedAuras(gameData);
        }
    }

    public boolean isDamageSourcePreventedWithLog(GameData gameData, StackEntry entry) {
        Card source = entry.getEffectiveDamageSourceCard();
        if (gameQueryService.isDamagePreventable(gameData)
                && gameQueryService.isDamageFromSourcePrevented(gameData, source.getColor())) {
            gameBroadcastService.logAndBroadcast(gameData, source.getName() + "'s damage is prevented.");
            return true;
        }
        return false;
    }

    public void resolveCreatureTargetDamage(GameData gameData, StackEntry entry, int damage) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) return;
        if (isDamagePreventedForCreature(gameData, entry, target)) return;
        dealDamageAndDestroyIfLethal(gameData, entry, target, damage);
    }

    /**
     * Excess damage dealt to a creature: damage beyond what was needed for lethal damage,
     * accounting for damage already marked and deathtouch (CR 120.10).
     */
    public int computeExcessDamageToCreature(GameData gameData, Permanent target, int damageDealt,
                                             int markedDamageBefore, boolean sourceHasDeathtouch) {
        if (damageDealt <= 0) {
            return 0;
        }
        if (sourceHasDeathtouch) {
            return Math.max(0, damageDealt - 1);
        }
        int toughness = gameQueryService.getEffectiveToughness(gameData, target);
        int lethalNeeded = Math.max(0, toughness - markedDamageBefore);
        return Math.max(0, damageDealt - lethalNeeded);
    }

    public boolean isDamagePreventedForCreature(GameData gameData, StackEntry entry, Permanent target) {
        Card source = entry.getEffectiveDamageSourceCard();
        if (gameQueryService.isDamagePreventable(gameData)
                && (gameQueryService.isDamageFromSourcePrevented(gameData, source.getColor())
                    || gameQueryService.hasProtectionFromSource(gameData, target, source))) {
            gameBroadcastService.logAndBroadcast(gameData,
                    source.getName() + "'s damage is prevented.");
            return true;
        }
        return false;
    }

    public boolean isSourcePermanentPreventedFromDealingDamage(GameData gameData, StackEntry entry) {
        return entry.getSourcePermanentId() != null
                && gameData.permanentsPreventedFromDealingDamage.contains(entry.getSourcePermanentId());
    }

    public void resolveAnyTargetDamage(GameData gameData, StackEntry entry, UUID targetId, int rawDamage, boolean cantRegenerate) {
        Card source = entry.getEffectiveDamageSourceCard();
        String cardName = source.getName();
        boolean targetIsPlayer = gameData.playerIds.contains(targetId);
        Permanent targetPermanent = targetIsPlayer ? null : gameQueryService.findPermanentById(gameData, targetId);

        if (!targetIsPlayer && targetPermanent == null) return;

        if (isDamageSourcePreventedWithLog(gameData, entry)) return;

        if (targetIsPlayer) {
            // dealDamageToPlayer handles per-permanent prevention (permanentsPreventedFromDealingDamage)
            dealDamageToPlayer(gameData, entry, targetId, rawDamage);
        } else {
            if (gameQueryService.isDamagePreventable(gameData)
                    && (isSourcePermanentPreventedFromDealingDamage(gameData, entry)
                        || gameQueryService.hasProtectionFromSource(gameData, targetPermanent, source))) {
                gameBroadcastService.logAndBroadcast(gameData, cardName + "'s damage is prevented.");
                return;
            }
            if (targetPermanent.getCard().hasType(CardType.PLANESWALKER)) {
                // CR 306.8: damage dealt to a planeswalker removes that many loyalty counters from it
                // (SBAs then move it to the graveyard once it has 0 loyalty). Mirrors the combat path.
                int loyaltyDamage = Math.max(0, rawDamage);
                if (loyaltyDamage > 0) {
                    targetPermanent.setCounterCount(CounterType.LOYALTY,
                            targetPermanent.getCounterCount(CounterType.LOYALTY) - loyaltyDamage);
                    gameBroadcastService.logAndBroadcast(gameData, cardName + " deals " + loyaltyDamage
                            + " damage to " + targetPermanent.getCard().getName() + " ("
                            + targetPermanent.getCounterCount(CounterType.LOYALTY) + " loyalty remaining).");
                }
                return;
            }
            if (cantRegenerate) {
                targetPermanent.setCantRegenerateThisTurn(true);
            }
            dealDamageAndDestroyIfLethal(gameData, entry, targetPermanent, rawDamage);
        }
    }

    public void damageAllCreaturesOnBattlefield(GameData gameData, StackEntry entry, int damage, Predicate<Permanent> filter) {
        List<Permanent> destroyed = new ArrayList<>();
        gameData.forEachBattlefield((playerId, battlefield) ->
                destroyed.addAll(damageFilteredCreatures(gameData, entry, damage, battlefield, filter))
        );
        destroyAllLethal(gameData, destroyed);
    }

    public List<Permanent> damageFilteredCreatures(GameData gameData, StackEntry entry, int damage, Collection<Permanent> permanents, Predicate<Permanent> filter) {
        List<Permanent> destroyed = new ArrayList<>();
        for (Permanent p : permanents) {
            if (!filter.test(p)) continue;
            if (gameQueryService.isDamagePreventable(gameData) && gameQueryService.hasProtectionFromSource(gameData, p, entry.getCard())) continue;
            if (dealCreatureDamage(gameData, entry, p, damage)) {
                destroyed.add(p);
            }
        }
        return destroyed;
    }

    public void dealDamageToPlayer(GameData gameData, StackEntry entry, UUID playerId, int rawDamage) {
        Card source = entry.getEffectiveDamageSourceCard();
        String cardName = source.getName();
        // Curse of Bloodletting and similar: double damage dealt to the enchanted player (replacement effect)
        rawDamage *= gameQueryService.getEnchantedPlayerDamageMultiplier(gameData, playerId);
        if (damagePreventionService.isSourceDamagePreventedForPlayer(gameData, playerId, entry.getSourcePermanentId())
                || damagePreventionService.isNoncombatDamageFromAttackerPreventedForPlayer(gameData, playerId, entry.getSourcePermanentId())
                || isSourcePermanentPreventedFromDealingDamage(gameData, entry)) {
            gameBroadcastService.logAndBroadcast(gameData, cardName + "'s damage to " + gameData.playerIdToName.get(playerId) + " is prevented.");
            return;
        }
        // Protection from color (e.g. Faith's Shield) prevents all damage from sources of that color.
        if (gameQueryService.isDamagePreventable(gameData)
                && gameQueryService.playerHasProtectionFromColor(gameData, playerId, source.getColor())) {
            gameBroadcastService.logAndBroadcast(gameData, cardName + "'s damage to " + gameData.playerIdToName.get(playerId) + " is prevented.");
            return;
        }
        // Protection from card name (Runed Halo) prevents all damage from sources with that name.
        if (gameQueryService.isDamagePreventable(gameData)
                && gameQueryService.playerHasProtectionFromChosenName(gameData, playerId, cardName)) {
            gameBroadcastService.logAndBroadcast(gameData, cardName + "'s damage to " + gameData.playerIdToName.get(playerId) + " is prevented.");
            return;
        }
        // Apply source-specific redirect shields (e.g. Harm's Way) before general prevention
        rawDamage = damagePreventionService.applySourceRedirectShields(gameData, playerId, entry.getSourcePermanentId(), rawDamage);
        processSourceRedirectDamage(gameData);
        if (rawDamage <= 0) return;
        if (!damagePreventionService.applyColorDamagePreventionForPlayer(gameData, playerId, source.getColor())) {
            rawDamage = damagePreventionService.applyOpponentSourceDamageReduction(gameData, playerId, entry.getControllerId(), rawDamage);
            // Apply target+source-specific prevention shields (e.g. Healing Grace)
            if (entry.getSourcePermanentId() != null) {
                rawDamage = damagePreventionService.applyTargetSourcePreventionShield(gameData, playerId, entry.getSourcePermanentId(), rawDamage);
                // Apply one-shot Circle-of-Protection shields (prevent the next damage event from the chosen source)
                rawDamage = damagePreventionService.applyPlayerNextSourceDamageShield(gameData, playerId, entry.getSourcePermanentId(), rawDamage);
                // Apply one-shot Sanctum Guardian shields (prevent the next damage from the chosen source to any target)
                rawDamage = damagePreventionService.applyChosenSourceNextDamageToAnyTargetShield(gameData, entry.getSourcePermanentId(), rawDamage);
            }
            int effectiveDamage = damagePreventionService.applyPlayerPreventionShield(gameData, playerId, rawDamage);
            processPendingRedirectDamage(gameData);
            effectiveDamage = permanentRemovalService.redirectPlayerDamageToEnchantedCreature(gameData, playerId, effectiveDamage, cardName);

            // Battletide Alchemist: the controller prevents up to (Clerics they control) of this source's damage.
            int battletidePrevented = damagePreventionService.applyControllerPerClericDamagePrevention(gameData, playerId, effectiveDamage);
            if (battletidePrevented > 0) {
                effectiveDamage -= battletidePrevented;
                gameBroadcastService.logAndBroadcast(gameData,
                        battletidePrevented + " of " + cardName + "'s damage to " + gameData.playerIdToName.get(playerId) + " is prevented.");
            }

            // Urza's Armor: the controller prevents a fixed amount of this source's damage.
            int fixedPrevented = damagePreventionService.applyControllerFixedPerSourceDamagePrevention(gameData, playerId, effectiveDamage);
            if (fixedPrevented > 0) {
                effectiveDamage -= fixedPrevented;
                gameBroadcastService.logAndBroadcast(gameData,
                        fixedPrevented + " of " + cardName + "'s damage to " + gameData.playerIdToName.get(playerId) + " is prevented.");
            }

            // Purity: prevent all remaining noncombat damage to the controller and gain that much life
            int purityPrevented = damagePreventionService.applyControllerNoncombatDamagePrevention(gameData, playerId, effectiveDamage);
            if (purityPrevented > 0) {
                effectiveDamage -= purityPrevented;
                gameBroadcastService.logAndBroadcast(gameData,
                        cardName + "'s " + purityPrevented + " damage to " + gameData.playerIdToName.get(playerId) + " is prevented.");
                lifeSupport.applyGainLife(gameData, playerId, purityPrevented, "prevented damage");
            }

            // Hostility: prevent all remaining damage a spell you control would deal to an opponent and
            // create one token per 1 damage prevented (for the spell's controller).
            var hostility = damagePreventionService.findSpellDamageToOpponentPrevention(gameData, entry, playerId, effectiveDamage);
            if (hostility != null) {
                int hostilityPrevented = effectiveDamage;
                effectiveDamage = 0;
                gameBroadcastService.logAndBroadcast(gameData,
                        cardName + "'s " + hostilityPrevented + " damage to " + gameData.playerIdToName.get(playerId) + " is prevented.");
                permanentControlSupport.applyCreateToken(gameData, entry.getControllerId(),
                        hostility.token(), hostilityPrevented, entry.getCard().getSetCode());
            }

            boolean sourceHasInfect = gameQueryService.sourceHasKeyword(gameData, entry, null, Keyword.INFECT);
            boolean treatAsInfect = sourceHasInfect || gameQueryService.shouldDamageBeDealtAsInfect(gameData, playerId);

            if (treatAsInfect) {
                if (effectiveDamage > 0 && gameQueryService.canPlayerGetPoisonCounters(gameData, playerId)) {
                    int currentPoison = gameData.playerPoisonCounters.getOrDefault(playerId, 0);
                    gameData.playerPoisonCounters.put(playerId, currentPoison + effectiveDamage);
                    String playerName = gameData.playerIdToName.get(playerId);
                    gameBroadcastService.logAndBroadcast(gameData,
                            playerName + " gets " + effectiveDamage + " poison counters from " + cardName + ".");
                }
            } else if (effectiveDamage > 0 && !gameQueryService.canPlayerLifeChange(gameData, playerId)) {
                String playerName = gameData.playerIdToName.get(playerId);
                gameBroadcastService.logAndBroadcast(gameData,
                        playerName + "'s life total can't change.");
            } else {
                int currentLife = gameData.getLife(playerId);
                int newLife = currentLife - effectiveDamage;
                // Worship: damage can't reduce the controller's life total below 1 while they control a creature.
                // The full damage is still dealt (lifelink/damage triggers see the full amount); only the life
                // total reduction is capped. Does nothing if the player is already at 0 or less life.
                if (currentLife >= 1 && newLife < 1
                        && gameQueryService.damageCantReduceLifeBelowOne(gameData, playerId)) {
                    newLife = 1;
                }
                gameData.playerLifeTotals.put(playerId, newLife);
                int lifeLost = currentLife - newLife;

                if (effectiveDamage > 0) {
                    String playerName = gameData.playerIdToName.get(playerId);
                    gameBroadcastService.logAndBroadcast(gameData,
                            playerName + " takes " + effectiveDamage + " damage from " + cardName + ".");
                    if (lifeLost > 0) {
                        triggerCollectionService.checkLifeLossTriggers(gameData, playerId, lifeLost);
                    }
                }
            }

            if (effectiveDamage > 0) {
                gameData.recordDamageToPlayer(playerId, effectiveDamage);
                triggerCollectionService.checkDamageDealtToControllerTriggers(gameData, playerId, entry.getSourcePermanentId(), false);
                triggerCollectionService.checkNoncombatDamageToOpponentTriggers(gameData, playerId);
                checkSpellLifelink(gameData, entry, effectiveDamage);
            }
        }
    }

    /**
     * Processes pending redirect damage entries populated by {@link DamagePreventionService}
     * when damage redirect shields (e.g. Vengeful Archon) prevent damage. The source permanent
     * deals the prevented amount to the redirect target player.
     */
    public void processPendingRedirectDamage(GameData gameData) {
        if (gameData.pendingRedirectDamage.isEmpty()) return;

        List<DamageRedirectShield> toProcess = new ArrayList<>(gameData.pendingRedirectDamage);
        gameData.pendingRedirectDamage.clear();

        for (DamageRedirectShield redirect : toProcess) {
            UUID targetId = redirect.redirectTargetPlayerId();
            int damage = redirect.remainingAmount();
            String targetName = gameData.playerIdToName.get(targetId);
            String protectedName = gameData.playerIdToName.get(redirect.protectedPlayerId());

            gameBroadcastService.logAndBroadcast(gameData,
                    redirect.sourceCard().getName() + " prevents " + damage + " damage to " + protectedName + ".");
            gameBroadcastService.logAndBroadcast(gameData,
                    redirect.sourceCard().getName() + " deals " + damage + " damage to " + targetName + ".");

            // Apply prevention shields on the redirect target (they may also have shields)
            int redirectEffective = damagePreventionService.applyPlayerPreventionShield(gameData, targetId, damage);
            // Recursively process any redirects triggered by the target's shields
            processPendingRedirectDamage(gameData);

            if (redirectEffective > 0) {
                if (gameQueryService.canPlayerLifeChange(gameData, targetId)) {
                    int currentLife = gameData.getLife(targetId);
                    gameData.playerLifeTotals.put(targetId, currentLife - redirectEffective);
                }
                gameData.recordDamageToPlayer(targetId, redirectEffective);
            }
        }
    }

    /**
     * Processes pending source-specific redirect damage entries (e.g. Harm's Way).
     * The prevented damage is dealt to the redirect target, which can be a player or permanent.
     */
    public void processSourceRedirectDamage(GameData gameData) {
        if (gameData.pendingSourceRedirectDamage.isEmpty()) return;

        List<SourceDamageRedirectShield> toProcess = new ArrayList<>(gameData.pendingSourceRedirectDamage);
        gameData.pendingSourceRedirectDamage.clear();

        for (SourceDamageRedirectShield redirect : toProcess) {
            UUID targetId = redirect.redirectTargetId();
            int damage = redirect.remainingAmount();
            boolean targetIsPlayer = gameData.playerIds.contains(targetId);

            if (targetIsPlayer) {
                String targetName = gameData.playerIdToName.get(targetId);
                gameBroadcastService.logAndBroadcast(gameData,
                        damage + " damage is redirected to " + targetName + ".");

                int redirectEffective = damagePreventionService.applyPlayerPreventionShield(gameData, targetId, damage);
                processPendingRedirectDamage(gameData);

                if (redirectEffective > 0) {
                    if (gameQueryService.canPlayerLifeChange(gameData, targetId)) {
                        int currentLife = gameData.getLife(targetId);
                        gameData.playerLifeTotals.put(targetId, currentLife - redirectEffective);
                    }
                    gameData.recordDamageToPlayer(targetId, redirectEffective);
                }
            } else {
                Permanent targetPerm = gameQueryService.findPermanentById(gameData, targetId);
                if (targetPerm == null) continue;

                gameBroadcastService.logAndBroadcast(gameData,
                        damage + " damage is redirected to " + targetPerm.getCard().getName() + ".");

                int effectiveDamage = damagePreventionService.applyCreaturePreventionShield(gameData, targetPerm, damage);
                if (effectiveDamage > 0) {
                    targetPerm.setMarkedDamage(targetPerm.getMarkedDamage() + effectiveDamage);
                    gameData.permanentsDealtDamageThisTurn.add(targetPerm.getId());
                    int effToughness = gameQueryService.getEffectiveToughness(gameData, targetPerm);
                    if (gameQueryService.isLethalDamage(targetPerm.getMarkedDamage(), effToughness, false)
                            && !gameQueryService.hasKeyword(gameData, targetPerm, Keyword.INDESTRUCTIBLE)) {
                        permanentRemovalService.removePermanentToGraveyard(gameData, targetPerm);
                    }
                }
            }
        }
    }


    /**
     * Deals divided damage to any number of targets (creatures and/or players) according
     * to the supplied assignments map. Called by {@code PermanentChoiceHandlerService}
     * after the player sacrifices an artifact for a divided-damage effect.
     */
    public void dealDividedDamageToAnyTargets(GameData gameData, Card sourceCard, UUID controllerId,
                                               Map<UUID, Integer> assignments) {
        if (assignments == null || assignments.isEmpty()) return;

        // Find source permanent on battlefield for damage tracking
        UUID sourcePermanentId = null;
        List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
        if (bf != null) {
            for (Permanent p : bf) {
                if (p.getCard() == sourceCard) {
                    sourcePermanentId = p.getId();
                    break;
                }
            }
        }

        // Create a temporary stack entry for the private damage helpers
        StackEntry tempEntry = new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                sourceCard,
                controllerId,
                sourceCard.getName() + "'s ability",
                List.of(),
                null,
                sourcePermanentId
        );

        if (isDamageSourcePreventedWithLog(gameData, tempEntry)) return;

        List<Permanent> destroyed = new ArrayList<>();

        for (Map.Entry<UUID, Integer> assignment : assignments.entrySet()) {
            UUID targetId = assignment.getKey();
            int rawDamage = gameQueryService.applyDamageMultiplier(gameData, assignment.getValue(), tempEntry);

            boolean targetIsPlayer = gameData.playerIds.contains(targetId);
            Permanent targetPermanent = targetIsPlayer ? null : gameQueryService.findPermanentById(gameData, targetId);

            if (!targetIsPlayer && targetPermanent == null) continue;

            // Divided damage only ever targets creatures, planeswalkers, or players — a permanent
            // that is none of those (e.g. a land) is an illegal target and isn't affected.
            if (!targetIsPlayer
                    && !gameQueryService.isCreature(gameData, targetPermanent)
                    && !targetPermanent.getCard().hasType(CardType.PLANESWALKER)) {
                continue;
            }

            if (targetIsPlayer) {
                dealDamageToPlayer(gameData, tempEntry, targetId, rawDamage);
            } else {
                if (!(gameQueryService.isDamagePreventable(gameData) && gameQueryService.hasProtectionFromSource(gameData, targetPermanent, sourceCard))) {
                    if (dealCreatureDamage(gameData, tempEntry, targetPermanent, rawDamage)) {
                        destroyed.add(targetPermanent);
                    }
                } else {
                    gameBroadcastService.logAndBroadcast(gameData,
                            sourceCard.getName() + "'s damage to " + targetPermanent.getCard().getName() + " is prevented.");
                }
            }
        }

        destroyAllLethal(gameData, destroyed);
        gameOutcomeService.checkWinCondition(gameData);
    }


    /**
     * Counts the permanents currently attached to the given player that match the predicate
     * (e.g. Curses attached to that player for Curse of Thirst).
     */
    public int countPermanentsAttachedToPlayer(GameData gameData, UUID playerId, PermanentPredicate predicate) {
        int[] count = {0};
        gameData.forEachPermanent((ownerId, perm) -> {
            if (perm.isAttached() && playerId.equals(perm.getAttachedTo())
                    && predicateEvaluationService.matchesPermanentPredicate(gameData, perm, predicate)) {
                count[0]++;
            }
        });
        return count[0];
    }


}
