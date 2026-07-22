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
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.DamageRedirectShield;
import com.github.laxika.magicalvibes.model.SourceDamageRedirectShield;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToControllerAndExileFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.PendingSourceDamage;
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
    private final com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport battleDefeatSupport;

    /** Colours of a non-permanent damage source (spell/ability card), for colour-based prevention. */
    private static Set<CardColor> sourceCardColors(Card card) {
        if (card == null) return Set.of();
        Set<CardColor> colors = new HashSet<>(card.getColors());
        if (card.getColor() != null) colors.add(card.getColor());
        return colors;
    }

    /**
     * Applies damage to a creature, handling prevention shield, recording, logging,
     * and checking for lethal damage (indestructible/regenerate).
     * Returns true if the creature took lethal damage and should be destroyed.
     * Caller is responsible for removal (use {@link #destroyPermanent} for single-target,
     * or batch-collect for multi-target effects).
     */
    public void dealCreatureDamage(GameData gameData, StackEntry entry, Permanent target, int rawDamage) {
        dealCreatureDamage(gameData, entry, target, rawDamage, null);
    }

    /**
     * Overload that accepts an explicit damage source permanent (e.g. the biting creature).
     * When {@code damageSource} is non-null, its ID is used for recording, its name for logging,
     * and keywords are checked directly on it. When null, falls back to entry-based lookup.
     */
    public void dealCreatureDamage(GameData gameData, StackEntry entry, Permanent target, int rawDamage, Permanent damageSource) {
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
        // Saving Grace: redirect all damage this turn to a permanent you control onto the enchanted creature.
        if (targetControllerId != null) {
            rawDamage = damagePreventionService.applyTurnDamageRedirectToCreature(gameData, targetControllerId, target.getId(), rawDamage);
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
        // Swans of Bryn Argoll: prevent all damage to this creature; the source's controller draws that many cards.
        UUID swansSourceControllerId = damageSource != null
                ? gameQueryService.findPermanentController(gameData, damageSource.getId())
                : entry.getControllerId();
        if (damagePreventionService.applySwansSourceControllerDraw(gameData, target, rawDamage, swansSourceControllerId)) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText("Damage to ", target.getCard(), " is prevented."));
            return;
        }
        // Prismatic Ward: prevent all damage to the enchanted creature from sources of the chosen colour.
        Set<CardColor> sourceColors = damageSource != null
                ? gameQueryService.getEffectiveColors(gameData, damageSource)
                : sourceCardColors(entry.getEffectiveDamageSourceCard());
        if (gameQueryService.isColorDamageToEnchantedCreaturePrevented(gameData, target, sourceColors)) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText("Damage to ", target.getCard(), " is prevented."));
            return;
        }
        // Gideon's Intervention: prevent all damage to permanents you control from sources with the chosen name.
        String preventionSourceName = (damageSource != null ? damageSource.getCard() : entry.getEffectiveDamageSourceCard()).getName();
        if (gameQueryService.isDamagePreventable(gameData)
                && gameQueryService.isDamageFromChosenNamePreventedForController(gameData, targetControllerId, preventionSourceName)) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText("Damage to ", target.getCard(), " is prevented."));
            return;
        }
        // Uncle Istvan: "Prevent all damage that would be dealt to this creature by creatures." Noncombat
        // path — combat damage is prevented in DamagePreventionService.applyCreaturePreventionShield.
        if (gameQueryService.isCreatureSourceDamageToSelfPrevented(gameData, target, entry, damageSource)) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText("Damage to ", target.getCard(), " is prevented."));
            return;
        }
        int damage = damagePreventionService.applyCreaturePreventionShield(gameData, target, rawDamage);
        // Djeru, With Eyes Open: "If a source would deal damage to a planeswalker you control, prevent
        // N of that damage." Applied before recording/triggers so reflection and damage-counting see the
        // reduced amount; the loyalty branch below then removes the reduced amount.
        if (target.getCard().hasType(CardType.PLANESWALKER)) {
            damage -= damagePreventionService.applyPlaneswalkerFixedPerSourceDamagePrevention(gameData, targetControllerId, damage);
        }

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
            accumulateSourceDamageForReflection(gameData,
                    damageSource != null ? damageSource.getCard() : entry.getEffectiveDamageSourceCard(),
                    sourceControllerId, damage);
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

        Card sourceCard = damageSource != null ? damageSource.getCard() : entry.getCard();
        String sourceName = sourceCard.getName();

        // CR 120.3c — damage dealt to a planeswalker removes that many loyalty counters
        // (the SBA check reaps it at 0 loyalty). A permanent that is also a creature
        // additionally gets the damage marked below (CR 120.3e).
        if (target.getCard().hasType(CardType.PLANESWALKER)) {
            if (damage > 0) {
                target.setCounterCount(CounterType.LOYALTY, target.getCounterCount(CounterType.LOYALTY) - damage);
                gameBroadcastService.logAndBroadcast(gameData, GameLog.cardTextCard(sourceCard,
                        " deals " + damage + " damage to ", target.getCard(),
                        " (" + target.getCounterCount(CounterType.LOYALTY) + " loyalty remaining)."));
            }
            if (!gameQueryService.isCreature(gameData, target)) {
                if (damage > 0) {
                    checkSpellLifelink(gameData, entry, damage);
                }
                return;
            }
        }

        // CR 310.8 — damage dealt to a battle removes that many defense counters
        if (target.getCard().hasType(CardType.BATTLE)) {
            if (damage > 0) {
                target.setCounterCount(CounterType.DEFENSE, target.getCounterCount(CounterType.DEFENSE) - damage);
                gameBroadcastService.logAndBroadcast(gameData, GameLog.cardTextCard(sourceCard,
                        " deals " + damage + " damage to ", target.getCard(),
                        " (" + target.getCounterCount(CounterType.DEFENSE) + " defense remaining)."));
                battleDefeatSupport.checkAfterDefenseRemoved(gameData, target);
            }
            if (!gameQueryService.isCreature(gameData, target)) {
                if (damage > 0) {
                    checkSpellLifelink(gameData, entry, damage);
                }
                return;
            }
        }

        // CR 702.2b — deathtouch applies only to damage this source actually dealt, so a hit
        // that was fully prevented must not mark the creature for a deathtouch kill.
        boolean sourceHasDeathtouch = damage > 0
                && gameQueryService.sourceHasKeyword(gameData, entry, damageSource, Keyword.DEATHTOUCH);

        // Infect and wither both deal creature damage as -1/-1 counters (CR 702.90 / 702.80).
        // Soul-Scar Mage likewise replaces its controller's noncombat damage to an opponent's
        // creature with that many -1/-1 counters. This helper is the noncombat damage path only
        // (combat damage is handled in CombatDamageService), so the "noncombat" clause is satisfied
        // structurally — no combat check is needed here.
        UUID damageSourceControllerId = damageSource != null
                ? gameQueryService.findPermanentController(gameData, damageSource.getId())
                : entry.getControllerId();
        boolean dealsCounterDamage = gameQueryService.sourceDealsCounterDamageToCreatures(gameData, entry, damageSource)
                || gameQueryService.noncombatDamageToOpponentCreatureAsCounters(gameData, damageSourceControllerId, targetControllerId);

        if (dealsCounterDamage) {
            if (damage > 0 && !gameQueryService.cantHaveCounters(gameData, target)
                    && !gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, target)) {
                // Vizier of Remedies reduces the -1/-1 counters (CR ruling: wither/infect counters
                // count), while the deathtouch marking below still keys off the full damage dealt.
                int counters = gameQueryService.reduceMinusOneMinusOneCounters(gameData, target, damage);
                if (counters > 0) {
                    target.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) + counters);
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.cardTextCard(sourceCard,
                            " puts " + counters + " -1/-1 counters on ", target.getCard(), "."));
                    log.info("Game {} - {} puts {} -1/-1 counters on {}", gameData.id, sourceName, counters, target.getCard().getName());
                    // CR ruling (Nest of Scarabs): the damage source's controller is the player who
                    // "puts" the wither/infect counters, so the controller-restricted watcher keys off it.
                    permanentCounterSupport.fireMinusOneMinusOneCounterPutOnCreatureTriggers(gameData, target, counters, damageSourceControllerId);
                }
            }
            // Counter damage is still damage dealt, so a deathtouch+wither/infect source
            // marks the creature for the CR 704.5h destruction check as well.
            if (sourceHasDeathtouch) {
                target.setDamagedByDeathtouch(true);
            }
            return;
        }

        // Record only — the state-based action check (CR 704.5g/704.5h) is the single place
        // creatures die from damage; it runs after the current resolution completes.
        target.setMarkedDamage(target.getMarkedDamage() + damage);
        if (sourceHasDeathtouch) {
            target.setDamagedByDeathtouch(true);
        }

        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardTextCard(sourceCard,
                " deals " + damage + " damage to ", target.getCard(), "."));
        log.info("Game {} - {} deals {} damage to {}", gameData.id, sourceName, damage, target.getCard().getName());

        if (damage > 0) {
            checkSpellLifelink(gameData, entry, damage);
        }
    }

    /**
     * Deals damage to a creature bypassing all prevention effects (shields, protection, global prevention).
     * Used for effects where "the damage can't be prevented" (e.g. Combust).
     */
    public void dealCreatureDamageUnpreventable(GameData gameData, StackEntry entry, Permanent target, int rawDamage) {
        // Defense in depth: a creature can never deal negative damage. Guards against any upstream
        // computation (e.g. future power-based effects) that might produce a negative value.
        // Skip applyCreaturePreventionShield — damage is unpreventable
        int damage = Math.max(0, rawDamage);

        if (entry.getSourcePermanentId() != null) {
            graveyardService.recordCreatureDamagedByPermanent(gameData, entry.getSourcePermanentId(), target, damage);
        }

        if (damage > 0) {
            accumulateSourceDamageForReflection(gameData, entry.getEffectiveDamageSourceCard(), entry.getControllerId(), damage);
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

        Card sourceCard = entry.getCard();
        String sourceName = sourceCard.getName();

        // Record only (CR 704.5g — unpreventable damage still accumulates as marked damage);
        // the state-based action check performs any resulting destruction.
        target.setMarkedDamage(target.getMarkedDamage() + damage);
        if (damage > 0 && gameQueryService.sourceHasKeyword(gameData, entry, null, Keyword.DEATHTOUCH)) {
            target.setDamagedByDeathtouch(true);
        }

        gameBroadcastService.logAndBroadcast(gameData, GameLog.cardTextCard(sourceCard,
                " deals " + damage + " damage to ", target.getCard(), ". (damage can't be prevented)"));
        log.info("Game {} - {} deals {} unpreventable damage to {}", gameData.id, sourceName, damage, target.getCard().getName());

        if (damage > 0) {
            checkSpellLifelink(gameData, entry, damage);
        }
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

    public boolean isDamageSourcePreventedWithLog(GameData gameData, StackEntry entry) {
        Card source = entry.getEffectiveDamageSourceCard();
        if (gameQueryService.isDamagePreventable(gameData)
                && gameQueryService.isDamageFromSourcePrevented(gameData, source.getColor())) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(source, "'s damage is prevented."));
            return true;
        }
        return false;
    }

    public void resolveCreatureTargetDamage(GameData gameData, StackEntry entry, int damage) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) return;
        if (isDamagePreventedForCreature(gameData, entry, target)) return;
        dealCreatureDamage(gameData, entry, target, damage);
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
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(source, "'s damage is prevented."));
            return true;
        }
        return false;
    }

    public boolean isSourcePermanentPreventedFromDealingDamage(GameData gameData, StackEntry entry) {
        return entry.getSourcePermanentId() != null
                && gameData.isPreventedFromDealingDamage(entry.getSourcePermanentId());
    }

    public void resolveAnyTargetDamage(GameData gameData, StackEntry entry, UUID targetId, int rawDamage, boolean cantRegenerate) {
        Card source = entry.getEffectiveDamageSourceCard();
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
                gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(source, "'s damage is prevented."));
                return;
            }
            if (targetPermanent.getCard().hasType(CardType.PLANESWALKER)) {
                // "Prevent all damage that would be dealt to ~" (e.g. Gideon of the Trials 0) also stops
                // loyalty loss. The creature-damage path applies this set in DamagePreventionService, but
                // the loyalty branch below bypasses it, so guard it here.
                if (gameQueryService.isDamagePreventable(gameData)
                        && gameData.creaturesWithAllDamagePrevented.contains(targetPermanent.getId())) {
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(source, "'s damage is prevented."));
                    return;
                }
                // CR 306.8: damage dealt to a planeswalker removes that many loyalty counters from it
                // (SBAs then move it to the graveyard once it has 0 loyalty). Mirrors the combat path.
                int loyaltyDamage = Math.max(0, rawDamage);
                // Djeru, With Eyes Open: prevent N of the damage dealt to a planeswalker you control.
                UUID pwControllerId = gameQueryService.findPermanentController(gameData, targetPermanent.getId());
                loyaltyDamage -= damagePreventionService.applyPlaneswalkerFixedPerSourceDamagePrevention(gameData, pwControllerId, loyaltyDamage);
                if (loyaltyDamage > 0) {
                    accumulateSourceDamageForReflection(gameData, source, entry.getControllerId(), loyaltyDamage);
                    targetPermanent.setCounterCount(CounterType.LOYALTY,
                            targetPermanent.getCounterCount(CounterType.LOYALTY) - loyaltyDamage);
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.cardTextCard(source,
                            " deals " + loyaltyDamage + " damage to ", targetPermanent.getCard(),
                            " (" + targetPermanent.getCounterCount(CounterType.LOYALTY) + " loyalty remaining)."));
                }
                return;
            }
            if (targetPermanent.getCard().hasType(CardType.BATTLE)) {
                if (gameQueryService.isDamagePreventable(gameData)
                        && gameData.creaturesWithAllDamagePrevented.contains(targetPermanent.getId())) {
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(source, "'s damage is prevented."));
                    return;
                }
                int defenseDamage = Math.max(0, rawDamage);
                if (defenseDamage > 0) {
                    accumulateSourceDamageForReflection(gameData, source, entry.getControllerId(), defenseDamage);
                    targetPermanent.setCounterCount(CounterType.DEFENSE,
                            targetPermanent.getCounterCount(CounterType.DEFENSE) - defenseDamage);
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.cardTextCard(source,
                            " deals " + defenseDamage + " damage to ", targetPermanent.getCard(),
                            " (" + targetPermanent.getCounterCount(CounterType.DEFENSE) + " defense remaining)."));
                    battleDefeatSupport.checkAfterDefenseRemoved(gameData, targetPermanent);
                }
                return;
            }
            if (cantRegenerate) {
                targetPermanent.setCantRegenerateThisTurn(true);
            }
            dealCreatureDamage(gameData, entry, targetPermanent, rawDamage);
        }
    }

    public void damageAllCreaturesOnBattlefield(GameData gameData, StackEntry entry, int damage, Predicate<Permanent> filter) {
        gameData.forEachBattlefield((playerId, battlefield) ->
                damageFilteredCreatures(gameData, entry, damage, battlefield, filter)
        );
    }

    public void damageFilteredCreatures(GameData gameData, StackEntry entry, int damage, Collection<Permanent> permanents, Predicate<Permanent> filter) {
        for (Permanent p : permanents) {
            if (!filter.test(p)) continue;
            if (gameQueryService.isDamagePreventable(gameData) && gameQueryService.hasProtectionFromSource(gameData, p, entry.getCard())) continue;
            dealCreatureDamage(gameData, entry, p, damage);
        }
    }

    public void dealDamageToPlayer(GameData gameData, StackEntry entry, UUID playerId, int rawDamage) {
        Card source = entry.getEffectiveDamageSourceCard();
        String cardName = source.getName();
        // Curse of Bloodletting and similar: double damage dealt to the enchanted player (replacement effect)
        rawDamage *= gameQueryService.getEnchantedPlayerDamageMultiplier(gameData, playerId);
        if (damagePreventionService.isSourceDamagePreventedForPlayer(gameData, playerId, entry.getSourcePermanentId())
                || damagePreventionService.isNoncombatDamageFromAttackerPreventedForPlayer(gameData, playerId, entry.getSourcePermanentId())
                || isSourcePermanentPreventedFromDealingDamage(gameData, entry)) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(source,
                    "'s damage to " + gameData.playerIdToName.get(playerId) + " is prevented."));
            return;
        }
        // Protection from color (e.g. Faith's Shield) prevents all damage from sources of that color.
        if (gameQueryService.isDamagePreventable(gameData)
                && gameQueryService.playerHasProtectionFromColor(gameData, playerId, source.getColor())) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(source,
                    "'s damage to " + gameData.playerIdToName.get(playerId) + " is prevented."));
            return;
        }
        // Protection from card name (Runed Halo) prevents all damage from sources with that name.
        // Gideon's Intervention likewise prevents damage from sources with the chosen name.
        if (gameQueryService.isDamagePreventable(gameData)
                && (gameQueryService.playerHasProtectionFromChosenName(gameData, playerId, cardName)
                        || gameQueryService.isDamageFromChosenNamePreventedForController(gameData, playerId, cardName))) {
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(source,
                    "'s damage to " + gameData.playerIdToName.get(playerId) + " is prevented."));
            return;
        }
        // Apply source-specific redirect shields (e.g. Harm's Way) before general prevention
        rawDamage = damagePreventionService.applySourceRedirectShields(gameData, playerId, entry.getSourcePermanentId(), rawDamage);
        processSourceRedirectDamage(gameData);
        // Saving Grace: redirect all damage this turn to the player onto the enchanted creature.
        rawDamage = damagePreventionService.applyTurnDamageRedirectToCreature(gameData, playerId, null, rawDamage);
        processSourceRedirectDamage(gameData);
        if (rawDamage <= 0) return;
        if (!damagePreventionService.applyColorDamagePreventionForPlayer(gameData, playerId, source.getColor())) {
            rawDamage = damagePreventionService.applyOpponentSourceDamageReduction(gameData, playerId, entry.getControllerId(), rawDamage);
            // Apply target+source-specific prevention shields (e.g. Healing Grace)
            if (entry.getSourcePermanentId() != null) {
                rawDamage = damagePreventionService.applyTargetSourcePreventionShield(gameData, playerId, entry.getSourcePermanentId(), rawDamage);
                // Eye for an Eye: reflect the next damage this source deals to the player back at the
                // source's controller. Does not reduce the damage dealt here; schedules a reflection.
                damagePreventionService.applyEyeForAnEyeReflection(gameData, playerId, entry.getSourcePermanentId(), rawDamage);
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
                gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(battletidePrevented + " of ", source,
                        "'s damage to " + gameData.playerIdToName.get(playerId) + " is prevented."));
            }

            // Urza's Armor: the controller prevents a fixed amount of this source's damage.
            int fixedPrevented = damagePreventionService.applyControllerFixedPerSourceDamagePrevention(gameData, playerId, effectiveDamage);
            if (fixedPrevented > 0) {
                effectiveDamage -= fixedPrevented;
                gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(fixedPrevented + " of ", source,
                        "'s damage to " + gameData.playerIdToName.get(playerId) + " is prevented."));
            }

            // Purity: prevent all remaining noncombat damage to the controller and gain that much life
            int purityPrevented = damagePreventionService.applyControllerNoncombatDamagePrevention(gameData, playerId, effectiveDamage);
            if (purityPrevented > 0) {
                effectiveDamage -= purityPrevented;
                gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(source,
                        "'s " + purityPrevented + " damage to " + gameData.playerIdToName.get(playerId) + " is prevented."));
                lifeSupport.applyGainLife(gameData, playerId, purityPrevented, "prevented damage");
            }

            // Hostility: prevent all remaining damage a spell you control would deal to an opponent and
            // create one token per 1 damage prevented (for the spell's controller).
            var hostility = damagePreventionService.findSpellDamageToOpponentPrevention(gameData, entry, playerId, effectiveDamage);
            if (hostility != null) {
                int hostilityPrevented = effectiveDamage;
                effectiveDamage = 0;
                gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(source,
                        "'s " + hostilityPrevented + " damage to " + gameData.playerIdToName.get(playerId) + " is prevented."));
                permanentControlSupport.applyCreateToken(gameData, entry.getControllerId(),
                        hostility.token(), hostilityPrevented, entry.getCard().getSetCode());
            }

            // Immortal Coil: prevent all remaining damage to the controller and exile a card from
            // their graveyard for each 1 damage prevented this way.
            int coilPrevented = applyImmortalCoilPrevention(gameData, playerId, effectiveDamage);
            if (coilPrevented > 0) {
                effectiveDamage -= coilPrevented;
                gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(source,
                        "'s " + coilPrevented + " damage to " + gameData.playerIdToName.get(playerId) + " is prevented."));
            }

            boolean sourceHasInfect = gameQueryService.sourceHasKeyword(gameData, entry, null, Keyword.INFECT);
            boolean treatAsInfect = sourceHasInfect || gameQueryService.shouldDamageBeDealtAsInfect(gameData, playerId);

            if (treatAsInfect) {
                if (effectiveDamage > 0 && gameQueryService.canPlayerGetPoisonCounters(gameData, playerId)) {
                    int currentPoison = gameData.playerPoisonCounters.getOrDefault(playerId, 0);
                    gameData.playerPoisonCounters.put(playerId, currentPoison + effectiveDamage);
                    String playerName = gameData.playerIdToName.get(playerId);
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(
                            playerName + " gets " + effectiveDamage + " poison counters from ", source, "."));
                }
            } else if (effectiveDamage > 0 && !gameQueryService.canPlayerLifeChange(gameData, playerId)) {
                String playerName = gameData.playerIdToName.get(playerId);
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(playerName + "'s life total can't change."));
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
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(
                            playerName + " takes " + effectiveDamage + " damage from ", source, "."));
                    if (lifeLost > 0) {
                        triggerCollectionService.checkLifeLossTriggers(gameData, playerId, lifeLost);
                    }
                }
            }

            if (effectiveDamage > 0) {
                accumulateSourceDamageForReflection(gameData, source, entry.getControllerId(), effectiveDamage);
                gameData.recordDamageToPlayer(playerId, effectiveDamage);
                gameData.recordNoncombatDamageSourceToPlayer(entry.getSourcePermanentId(), playerId);
                triggerCollectionService.checkDamageDealtToControllerTriggers(gameData, playerId, entry.getSourcePermanentId(), false);
                triggerCollectionService.checkEnchantedCreatureDealtDamageToControllerReflectTriggers(gameData, playerId, entry.getSourcePermanentId(), effectiveDamage);
                // The stack entry's controller is the damage source's controller (caster/activator);
                // used to gate the opponent-only ON_CONTROLLER_DEALT_DAMAGE_BY_OPPONENT slot.
                triggerCollectionService.checkControllerDealtDamageTriggers(gameData, playerId, entry.getControllerId(), effectiveDamage);
                triggerCollectionService.checkNoncombatDamageToOpponentTriggers(gameData, playerId);
                checkSpellLifelink(gameData, entry, effectiveDamage);
            }
        }
        processEyeForAnEyeReflections(gameData);
    }

    /**
     * Processes pending Eye for an Eye reflected damage: deals the reflected amount to the chosen
     * source's controller as a fresh damage event dealt by Eye for an Eye.
     */
    public void processEyeForAnEyeReflections(GameData gameData) {
        if (gameData.pendingEyeForAnEyeReflections.isEmpty()) return;

        List<com.github.laxika.magicalvibes.model.EyeForAnEyeReflection> toProcess =
                new ArrayList<>(gameData.pendingEyeForAnEyeReflections);
        gameData.pendingEyeForAnEyeReflections.clear();

        for (var reflection : toProcess) {
            StackEntry tempEntry = new StackEntry(
                    StackEntryType.TRIGGERED_ABILITY,
                    reflection.eyeCard(),
                    reflection.eyeControllerId(),
                    reflection.eyeCard().getName() + "'s reflection",
                    List.of());
            dealDamageToPlayer(gameData, tempEntry, reflection.targetPlayerId(), reflection.amount());
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

            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(redirect.sourceCard(),
                    " prevents " + damage + " damage to " + protectedName + "."));
            gameBroadcastService.logAndBroadcast(gameData, GameLog.cardThen(redirect.sourceCard(),
                    " deals " + damage + " damage to " + targetName + "."));

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
                gameBroadcastService.logAndBroadcast(gameData, GameLog.text(damage + " damage is redirected to " + targetName + "."));

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

                gameBroadcastService.logAndBroadcast(gameData, GameLog.textCardText(
                        damage + " damage is redirected to ", targetPerm.getCard(), "."));

                int effectiveDamage = damagePreventionService.applyCreaturePreventionShield(gameData, targetPerm, damage);
                if (effectiveDamage > 0) {
                    // Record only — the state-based action check (CR 704.5g) performs any
                    // destruction once the current damage event finishes.
                    targetPerm.setMarkedDamage(targetPerm.getMarkedDamage() + effectiveDamage);
                    gameData.permanentsDealtDamageThisTurn.add(targetPerm.getId());
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
                    dealCreatureDamage(gameData, tempEntry, targetPermanent, rawDamage);
                } else {
                    gameBroadcastService.logAndBroadcast(gameData, GameLog.cardTextCard(sourceCard,
                            "'s damage to ", targetPermanent.getCard(), " is prevented."));
                }
            }
        }

        gameOutcomeService.checkWinCondition(gameData);
        flushSourceDamageReflections(gameData);
    }

    /**
     * Records that {@code sourceCard} (controlled by {@code sourceControllerId}) dealt {@code damage}
     * during the current damage event, batching per source so a global "whenever a [color] source
     * deals damage" watcher (Justice) reflects the summed total once (CR ruling). Consumed by
     * {@link #flushSourceDamageReflections} at the end of the resolution.
     */
    public void accumulateSourceDamageForReflection(GameData gameData, Card sourceCard, UUID sourceControllerId, int damage) {
        if (damage <= 0 || sourceCard == null || sourceControllerId == null) return;
        PendingSourceDamage batch = gameData.pendingSourceDamageForReflection.get(sourceCard.getId());
        if (batch == null) {
            gameData.pendingSourceDamageForReflection.put(sourceCard.getId(),
                    new PendingSourceDamage(sourceCard, sourceControllerId, damage));
        } else {
            batch.add(damage);
        }
    }

    /**
     * Queues the {@code ON_ANY_SOURCE_DEALS_DAMAGE} reflection triggers (Justice) for every source
     * that dealt non-combat damage during the just-finished resolution, then clears the accumulator.
     * Combat damage batches separately in {@code CombatDamageService}.
     */
    public void flushSourceDamageReflections(GameData gameData) {
        if (gameData.pendingSourceDamageForReflection.isEmpty()) return;
        List<PendingSourceDamage> batches = new ArrayList<>(gameData.pendingSourceDamageForReflection.values());
        gameData.pendingSourceDamageForReflection.clear();
        for (PendingSourceDamage batch : batches) {
            triggerCollectionService.queueSourceDealsDamageReflections(gameData,
                    batch.getSourceCard(), batch.getControllerId(), batch.getAmount());
        }
    }


    /**
     * Immortal Coil: "If damage would be dealt to you, prevent that damage. Exile a card from your
     * graveyard for each 1 damage prevented this way." If {@code playerId} controls a permanent with
     * {@link PreventAllDamageToControllerAndExileFromGraveyardEffect}, all of the {@code damage} is
     * prevented and up to that many cards are exiled from their graveyard. Returns the amount
     * prevented (the caller subtracts it); 0 when damage can't be prevented or no such permanent is
     * present. Shared by the noncombat ({@link #dealDamageToPlayer}) and combat
     * ({@code CombatDamageService.applyPlayerDamage}) paths.
     */
    public int applyImmortalCoilPrevention(GameData gameData, UUID playerId, int damage) {
        if (!gameQueryService.isDamagePreventable(gameData)) return 0;
        if (damage <= 0) return 0;

        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return 0;

        boolean hasEffect = battlefield.stream().anyMatch(p ->
                p.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(e -> e instanceof PreventAllDamageToControllerAndExileFromGraveyardEffect));
        if (!hasEffect) return 0;

        graveyardService.exileCardsFromGraveyard(gameData, playerId, damage);
        return damage;
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
