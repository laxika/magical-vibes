package com.github.laxika.magicalvibes.service.effect.normalfx;

import com.github.laxika.magicalvibes.service.DamagePreventionService;
import com.github.laxika.magicalvibes.service.GameLogService;
import com.github.laxika.magicalvibes.service.GameOutcomeService;
import com.github.laxika.magicalvibes.service.graveyard.GraveyardService;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.battlefield.PermanentRemovalService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import com.github.laxika.magicalvibes.service.outcome.LossOutcome;
import com.github.laxika.magicalvibes.service.outcome.LossReason;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.DamageRedirectShield;
import com.github.laxika.magicalvibes.model.SourceDamageRedirectShield;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageDealtByEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToAndByEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToControllerAndExileFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.DelayingShieldDamageReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CrumblingSanctuaryDamageReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.NefariousLichDamageReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectChosenColorSpellDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.PendingSourceDamage;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.model.CounterType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.ToIntFunction;

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
    private final GameLogService gameLogService;
    private final PermanentRemovalService permanentRemovalService;
    private final TriggerCollectionService triggerCollectionService;
    private final LifeSupport lifeSupport;
    private final PermanentControlSupport permanentControlSupport;
    private final PermanentCounterSupport permanentCounterSupport;
    private final com.github.laxika.magicalvibes.service.battle.BattleDefeatSupport battleDefeatSupport;
    private final ConditionEvaluationService conditionEvaluationService;

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
     * Returns the amount of damage actually dealt to the target.
     */
    public int dealCreatureDamage(GameData gameData, StackEntry entry, Permanent target, int rawDamage) {
        return dealCreatureDamage(gameData, entry, target, rawDamage, null);
    }

    public int dealCreatureDamage(GameData gameData, StackEntry entry, Permanent target, int rawDamage,
                                  boolean cantBeRedirected) {
        return dealCreatureDamage(gameData, entry, target, rawDamage, null, cantBeRedirected);
    }

    /**
     * Overload that accepts an explicit damage source permanent (e.g. the biting creature).
     * When {@code damageSource} is non-null, its ID is used for recording, its name for logging,
     * and keywords are checked directly on it. When null, falls back to entry-based lookup.
     */
    public int dealCreatureDamage(GameData gameData, StackEntry entry, Permanent target, int rawDamage, Permanent damageSource) {
        return dealCreatureDamage(gameData, entry, target, rawDamage, damageSource, false);
    }

    private int dealCreatureDamage(GameData gameData, StackEntry entry, Permanent target, int rawDamage,
                                   Permanent damageSource, boolean cantBeRedirected) {
        Permanent source = damageSource;
        if (source == null && entry != null && entry.getSourcePermanentId() != null) {
            source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        }
        UUID sourceControllerId = source == null
                ? entry == null ? null : entry.getControllerId()
                : gameQueryService.findPermanentController(gameData, source.getId());
        if (sourceControllerId == null && entry != null) {
            sourceControllerId = entry.getControllerId();
        }
        if (damageSource != null && (entry == null || entry.getSourcePermanentId() == null
                || !damageSource.getId().equals(entry.getSourcePermanentId()))) {
            rawDamage *= gameQueryService.getSourceDamageMultiplier(gameData, sourceControllerId, damageSource);
        }
        if (target.isDamageCantBePreventedOrRedirectedThisTurn()) {
            boolean previous = gameData.damageCantBePreventedThisTurn;
            gameData.damageCantBePreventedThisTurn = true;
            try {
                return dealCreatureDamageFromSource(gameData, entry, target, rawDamage, damageSource,
                        true, true);
            } finally {
                gameData.damageCantBePreventedThisTurn = previous;
            }
        }
        boolean sourceDamagePrevented = source != null
                ? gameQueryService.isDamageFromPermanentSourcePrevented(gameData, source)
                : gameQueryService.isDamageFromStackEntryPrevented(gameData, entry);
        if (sourceDamagePrevented) {
            Card sourceCard = source != null ? source.getCard() : entry.getEffectiveDamageSourceCard();
            gameLogService.append(gameData, GameLog.cardThen(sourceCard, "'s damage is prevented."));
            return 0;
        }
        if (gameQueryService.isDamageByCreaturePrevented(gameData, source)) {
            gameLogService.append(gameData, GameLog.textCardText("Damage dealt by ", source.getCard(), " is prevented."));
            return 0;
        }
        // Malignus: "Damage that would be dealt by this creature can't be prevented." Suppress every
        // prevention path (all gated on isDamagePreventable) for this one event, then restore — the
        // same shape DealDamageToAnyTargetEffectHandler uses for Banefire.
        if (damageSource != null && gameQueryService.damageCantBePreventedFromSource(gameData, damageSource)) {
            boolean previous = gameData.damageCantBePreventedThisTurn;
            gameData.damageCantBePreventedThisTurn = true;
            try {
                return dealCreatureDamageFromSource(gameData, entry, target, rawDamage, damageSource,
                        cantBeRedirected, false);
            } finally {
                gameData.damageCantBePreventedThisTurn = previous;
            }
        }
        return dealCreatureDamageFromSource(gameData, entry, target, rawDamage, damageSource,
                cantBeRedirected, false);
    }

    private int dealCreatureDamageFromSource(GameData gameData, StackEntry entry, Permanent target,
                                             int rawDamage, Permanent damageSource,
                                             boolean cantBeRedirected, boolean targetDamageUnpreventable) {
        // Defense in depth: a creature can never deal negative damage. Guards against any upstream
        // computation (e.g. future power-based effects) that might produce a negative value.
        rawDamage = Math.max(0, rawDamage);
        if (damageSource != null
                && (entry == null || !damageSource.getId().equals(entry.getSourcePermanentId()))) {
            rawDamage *= gameQueryService.getPermanentDamageMultiplier(gameData, damageSource.getId());
        }
        // Energy Storm and Hidden Retreat: prevent damage dealt by instant/sorcery spells themselves
        // (not fight/bite damage from permanents that a spell merely caused to deal damage).
        if (!targetDamageUnpreventable && damageSource == null
                && gameQueryService.isDamageFromInstantOrSorcerySpellPrevented(gameData, entry)) {
            gameLogService.append(gameData, GameLog.cardThen(entry.getEffectiveDamageSourceCard(),
                    "'s damage is prevented."));
            return 0;
        }
        if (!targetDamageUnpreventable && damageSource == null
                && gameQueryService.isDamageFromTargetSpellPrevented(gameData, entry)) {
            gainLifeForTargetSpellDamage(gameData, entry, rawDamage);
            gameLogService.append(gameData, GameLog.cardThen(entry.getEffectiveDamageSourceCard(),
                    "'s damage is prevented."));
            return 0;
        }
        if (!targetDamageUnpreventable && damageSource == null
                && gameQueryService.isDamageFromTargetingSpellPrevented(gameData, entry, target)) {
            gameLogService.append(gameData, GameLog.textCardText("Damage to ", target.getCard(), " is prevented."));
            return 0;
        }
        // Benevolent Unicorn: a spell dealing damage as itself deals that much damage minus N.
        if (damageSource == null) {
            rawDamage = Math.max(0, rawDamage - gameQueryService.getSpellDamageReduction(gameData, entry));
        }
        // Apply source-specific redirect shields (e.g. Harm's Way) before creature prevention
        UUID targetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        UUID recipientSourceControllerId = damageSource == null
                ? entry == null ? null : entry.getControllerId()
                : gameQueryService.findPermanentController(gameData, damageSource.getId());
        if (recipientSourceControllerId == null && entry != null) {
            recipientSourceControllerId = entry.getControllerId();
        }
        Permanent sourcePermanentForBonus = damageSource != null
                ? damageSource
                : (entry == null || entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId()));
        UUID bonusSourceControllerId = sourcePermanentForBonus == null
                ? entry == null ? null : entry.getControllerId()
                : gameQueryService.findPermanentController(gameData, sourcePermanentForBonus.getId());
        Card sourceCardForBonus = sourcePermanentForBonus == null
                ? entry == null ? null : entry.getEffectiveDamageSourceCard()
                : sourcePermanentForBonus.getCard();
        if (rawDamage > 0) {
            rawDamage += gameQueryService.getAdditionalDamageToOpponentsBonus(
                    gameData, bonusSourceControllerId, sourceCardForBonus, sourcePermanentForBonus, targetControllerId);
        }
        UUID sourceControllerId = damageSource != null
                ? gameQueryService.findPermanentController(gameData, damageSource.getId())
                : entry != null && entry.getSourcePermanentId() != null
                ? gameQueryService.findPermanentController(gameData, entry.getSourcePermanentId())
                : entry == null ? null : entry.getControllerId();
        if (rawDamage > 0) {
            rawDamage += gameQueryService.getControllerDamageToOpponentBonus(
                    gameData, sourceControllerId, targetControllerId);
        }
        // Gisela, Blade of Goldnight: double the damage dealt to a permanent an opponent controls. The
        // combat counterpart lives in GameQueryService.applyCombatDamageMultiplier.
        rawDamage *= gameQueryService.getDamageToRecipientMultiplier(gameData, targetControllerId,
                recipientSourceControllerId, target.getId());
        rawDamage = gameQueryService.applyDamageReplacementEffects(gameData, rawDamage);
        UUID sourcePermId = damageSource != null ? damageSource.getId() : entry.getSourcePermanentId();
        if (!cantBeRedirected) {
            if (targetControllerId != null && sourcePermId != null) {
                rawDamage = damagePreventionService.applySourceRedirectShields(gameData, targetControllerId, sourcePermId, rawDamage);
                processSourceRedirectDamage(gameData);
            }
            // Reflect Damage: the chosen source's next damage is dealt to that source's controller instead.
            if (sourcePermId != null) {
                rawDamage = damagePreventionService.applyReflectDamageToSourceControllerShield(gameData, sourcePermId, rawDamage);
                processEyeForAnEyeReflections(gameData);
                if (rawDamage <= 0) return 0;
                // Opal-Eye: the chosen source's next damage is dealt to a fixed creature instead.
                rawDamage = damagePreventionService.applySourceNextDamageRedirectToPermanent(
                        gameData, sourcePermId, target.getId(), rawDamage);
                processSourceRedirectDamage(gameData);
                if (rawDamage <= 0) return 0;
            }
            // Saving Grace: redirect all damage this turn to a permanent you control onto the enchanted creature.
            if (targetControllerId != null) {
                rawDamage = damagePreventionService.applyTurnDamageRedirectToCreature(gameData, targetControllerId, target.getId(), rawDamage);
                processSourceRedirectDamage(gameData);
            }
            // Palisade Giant: damage to other permanents its controller controls is dealt to it instead.
            if (targetControllerId != null) {
                rawDamage = damagePreventionService.applyStaticPermanentDamageRedirectToSelf(gameData, targetControllerId, target.getId(), rawDamage);
                processSourceRedirectDamage(gameData);
            }
            rawDamage = damagePreventionService.applyCreatureControllerDamageRedirectUntilNextTurn(
                    gameData, targetControllerId, target, sourcePermId, rawDamage);
            processSourceRedirectDamage(gameData);
            rawDamage = damagePreventionService.applyAllCreatureDamageRedirectToController(
                    gameData, target, sourcePermId, rawDamage);
            processSourceRedirectDamage(gameData);
            rawDamage = damagePreventionService.applyEnchantedCreatureDamageRedirectToController(
                    gameData, target, sourcePermId, rawDamage);
            processSourceRedirectDamage(gameData);
            if (rawDamage <= 0) return 0;
            // Apply creature-specific redirect shields (e.g. Oracle's Attendants): redirect all damage from
            // a chosen source to the protected creature onto another permanent.
            rawDamage = damagePreventionService.applyCreatureRedirectShields(gameData, target.getId(), sourcePermId, rawDamage);
            processSourceRedirectDamage(gameData);
        }
        // Apply target+source-specific prevention shields (e.g. Healing Grace)
        if (!targetDamageUnpreventable && sourcePermId != null) {
            rawDamage = damagePreventionService.applyTargetSourcePreventionShield(gameData, target.getId(), sourcePermId, rawDamage);
            // Apply one-shot Sanctum Guardian / Honorable Passage shields (prevent the next damage from
            // the chosen source to any target; red rider queues reflected damage)
            rawDamage = damagePreventionService.applyChosenSourceNextDamageToAnyTargetShield(gameData, sourcePermId, rawDamage, target.getId());
            processEyeForAnEyeReflections(gameData);
            // Shadowbane: the chosen source's next damage to the protected player's creatures.
            rawDamage = damagePreventionService.applyControllerCreaturesNextSourceDamageShield(
                    gameData, targetControllerId, sourcePermId, rawDamage);
        }
        // Swans of Bryn Argoll: prevent all damage to this creature; the source's controller draws that many cards.
        UUID swansSourceControllerId = damageSource != null
                ? gameQueryService.findPermanentController(gameData, damageSource.getId())
                : entry.getControllerId();
        if (!targetDamageUnpreventable
                && damagePreventionService.applySwansSourceControllerDraw(gameData, target, rawDamage, swansSourceControllerId)) {
            gameLogService.append(gameData, GameLog.textCardText("Damage to ", target.getCard(), " is prevented."));
            return 0;
        }
        // Prismatic Ward: prevent all damage to the enchanted creature from sources of the chosen colour.
        Set<CardColor> sourceColors = damageSource != null
                ? gameQueryService.getEffectiveColors(gameData, damageSource)
                : sourceCardColors(entry.getEffectiveDamageSourceCard());
        if (!targetDamageUnpreventable
                && damagePreventionService.isColorDamagePreventedForTarget(gameData, target.getId(), sourceColors)) {
            gameLogService.append(gameData, GameLog.textCardText("Damage to ", target.getCard(), " is prevented."));
            return 0;
        }
        if (!targetDamageUnpreventable
                && gameQueryService.isColorDamageToEnchantedCreaturePrevented(gameData, target, sourceColors)) {
            gameLogService.append(gameData, GameLog.textCardText("Damage to ", target.getCard(), " is prevented."));
            return 0;
        }
        Permanent sharedColorSource = damageSource;
        if (sharedColorSource == null && entry != null && entry.getSourcePermanentId() != null) {
            sharedColorSource = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        }
        if (!targetDamageUnpreventable
                && gameQueryService.isDamageBetweenCreaturesOfSharedColorPrevented(gameData, target, sharedColorSource)) {
            gameLogService.append(gameData, GameLog.textCardText("Damage to ", target.getCard(), " is prevented."));
            return 0;
        }
        // Gideon's Intervention: prevent all damage to permanents you control from sources with the chosen name.
        String preventionSourceName = (damageSource != null ? damageSource.getCard() : entry.getEffectiveDamageSourceCard()).getName();
        if (!targetDamageUnpreventable
                && gameQueryService.isDamagePreventable(gameData)
                && gameQueryService.isDamageFromChosenNamePreventedForController(gameData, targetControllerId, preventionSourceName)) {
            gameLogService.append(gameData, GameLog.textCardText("Damage to ", target.getCard(), " is prevented."));
            return 0;
        }
        // Uncle Istvan: "Prevent all damage that would be dealt to this creature by creatures." Noncombat
        // path — combat damage is prevented in DamagePreventionService.applyCreaturePreventionShield.
        if (!targetDamageUnpreventable
                && gameQueryService.isCreatureSourceDamageToSelfPrevented(gameData, target, entry, damageSource)) {
            gameLogService.append(gameData, GameLog.textCardText("Damage to ", target.getCard(), " is prevented."));
            return 0;
        }
        Permanent effectiveDamageSource = damageSource;
        if (effectiveDamageSource == null && entry != null && entry.getSourcePermanentId() != null) {
            effectiveDamageSource = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        }
        int damage = targetDamageUnpreventable
                ? rawDamage
                : damagePreventionService.applyCreaturePreventionShield(
                gameData, target, rawDamage, false, effectiveDamageSource);
        // Djeru, With Eyes Open: "If a source would deal damage to a planeswalker you control, prevent
        // N of that damage." Applied before recording/triggers so reflection and damage-counting see the
        // reduced amount; the loyalty branch below then removes the reduced amount.
        if (!targetDamageUnpreventable && target.getCard().hasType(CardType.PLANESWALKER)) {
            damage -= damagePreventionService.applyPlaneswalkerFixedPerSourceDamagePrevention(gameData, targetControllerId, damage);
            damage -= damagePreventionService.applyAllButOneDamagePrevention(gameData, targetControllerId, damage);
        }

        if (damageSource != null) {
            graveyardService.recordCreatureDamagedByPermanent(gameData, damageSource.getId(), target, damage);
        } else if (entry.getSourcePermanentId() != null) {
            graveyardService.recordCreatureDamagedByPermanent(gameData, entry.getSourcePermanentId(), target, damage);
        }

        // Fire ON_DEALT_DAMAGE triggers (e.g. Nested Ghoul, Phyrexian Obliterator)
        if (damage > 0) {
            gameData.recordDamageToPermanent(target.getId(), damage);
            if (entry.getEntryType() == StackEntryType.INSTANT_SPELL
                    || entry.getEntryType() == StackEntryType.SORCERY_SPELL) {
                gameData.recordQualifyingDamageControllerToPermanent(target.getId(), sourceControllerId);
            }
            gameData.recordDamageDealtBySource(
                    damageSource != null ? damageSource.getId() : entry.getSourcePermanentId(), damage);
            gameData.recordDamageRecipientBySource(sourcePermId, target.getId());

            accumulateSourceDamageForReflection(gameData,
                    damageSource != null ? damageSource.getCard() : entry.getEffectiveDamageSourceCard(),
                    sourceControllerId,
                    damageSource != null ? damageSource.getId() : entry.getSourcePermanentId(), damage,
                    null, targetControllerId);
            triggerCollectionService.checkDealtDamageToCreatureTriggers(gameData, target, damage, sourceControllerId);

            UUID damagedCreatureControllerId = gameQueryService.findPermanentController(gameData, target.getId());
            Permanent reflectionSource = damageSource != null
                    ? damageSource
                    : (sourcePermId != null ? gameQueryService.findPermanentById(gameData, sourcePermId) : null);

            if (target.getCard().hasType(CardType.PLANESWALKER)) {
                triggerCollectionService.checkAllyDealtDamageToPlaneswalkerTriggers(
                        gameData, reflectionSource, sourceControllerId, target.getId(), damage, false, null);
            }

            // All three slots below trigger on damage dealt *to a creature* — "whenever a creature …
            // is dealt damage" (Kazarov, Sengir Pureblood; Death Pits of Rath) and "whenever … deals
            // damage to a creature" (Greatbow Doyen, Bellowing Fiend, Cruel Deceiver's granted
            // ability) — so a planeswalker or battle that is not also a creature must not fire them:
            // CR 603.2, an ability triggers only when the event matches its trigger event. The gate is
            // layer-aware (CR 613.1d), unlike the printed type lines the CR 120.3c / CR 120.3h
            // branches below key off, because "creature" is the trigger condition here rather than a
            // choice of damage destination.
            if (gameQueryService.isCreature(gameData, target)) {
                // Fire ON_OPPONENT_CREATURE_DEALT_DAMAGE triggers (e.g. Kazarov)
                if (damagedCreatureControllerId != null) {
                    triggerCollectionService.checkOpponentCreatureDealtDamageTriggers(gameData, damagedCreatureControllerId);
                }

                // Fire ON_ANY_CREATURE_DEALT_DAMAGE triggers (e.g. Death Pits of Rath)
                triggerCollectionService.checkAnyCreatureDealtDamageTriggers(gameData, target, damage);

                // Fire ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE reflection triggers (e.g. Greatbow Doyen)
                triggerCollectionService.queueEnchantedCreatureDealsDamageToCreatureTriggers(
                        gameData, reflectionSource, target.getId(), damage);
                triggerCollectionService.checkAllyDealtDamageToCreatureTriggers(gameData, reflectionSource,
                        sourceControllerId, damagedCreatureControllerId, target.getId(), target, damage, false);
            }

            // Mangara's Equity: "…or a white creature you control" — deliberately outside the gate.
            // It also covers damage to the player, and the effect's own damagedPermanentFilter does
            // the narrowing in DamageTriggerCollectorService.
            triggerCollectionService.checkCreatureDamageToYouOrYourPermanentTriggers(
                    gameData, damagedCreatureControllerId, target, reflectionSource, damage);
        }

        Card sourceCard = damageSource != null ? damageSource.getCard() : entry.getCard();
        String sourceName = sourceCard.getName();

        boolean sourceHasDeathtouch = damage > 0
                && gameQueryService.sourceHasKeyword(gameData, entry, damageSource, Keyword.DEATHTOUCH);
        int excessDamage = target.getCard().hasType(CardType.PLANESWALKER)
                ? Math.max(0, damage - target.getCounterCount(CounterType.LOYALTY))
                : computeExcessDamageToCreature(gameData, target, damage,
                        target.getMarkedDamage(), sourceHasDeathtouch);
        if (excessDamage > 0) {
            triggerCollectionService.checkOpponentPermanentDealtExcessDamageTriggers(
                    gameData, target, targetControllerId, excessDamage);
        }

        // CR 120.3c — damage dealt to a planeswalker removes that many loyalty counters
        // (the SBA check reaps it at 0 loyalty). A permanent that is also a creature
        // additionally gets the damage marked below (CR 120.3e).
        if (target.getCard().hasType(CardType.PLANESWALKER)) {
            if (damage > 0) {
                target.setCounterCount(CounterType.LOYALTY, target.getCounterCount(CounterType.LOYALTY) - damage);
                queueEnchantedCreatureDealsDamageTrigger(gameData, entry, damageSource, damage);
                gameLogService.append(gameData, GameLog.cardTextCard(sourceCard,
                        " deals " + damage + " damage to ", target.getCard(),
                        " (" + target.getCounterCount(CounterType.LOYALTY) + " loyalty remaining)."));
            }
            if (!gameQueryService.isCreature(gameData, target)) {
                if (damage > 0) {
                    checkSpellLifelink(gameData, entry, damage);
                }
                processPendingRedirectDamage(gameData);
                return damage;
            }
        }

        // CR 120.3h — damage dealt to a battle removes that many defense counters
        // (the state-based action check reaps it at 0 defense). A permanent that is
        // also a creature additionally gets the damage marked below (CR 120.3e).
        if (target.getCard().hasType(CardType.BATTLE)) {
            if (damage > 0) {
                target.setCounterCount(CounterType.DEFENSE, target.getCounterCount(CounterType.DEFENSE) - damage);
                queueEnchantedCreatureDealsDamageTrigger(gameData, entry, damageSource, damage);
                gameLogService.append(gameData, GameLog.cardTextCard(sourceCard,
                        " deals " + damage + " damage to ", target.getCard(),
                        " (" + target.getCounterCount(CounterType.DEFENSE) + " defense remaining)."));
                battleDefeatSupport.checkAfterDefenseRemoved(gameData, target);
            }
            if (!gameQueryService.isCreature(gameData, target)) {
                if (damage > 0) {
                    checkSpellLifelink(gameData, entry, damage);
                }
                processPendingRedirectDamage(gameData);
                return damage;
            }
        }

        // CR 702.2b — deathtouch applies only to damage this source actually dealt, so a hit
        // that was fully prevented must not mark the creature for a deathtouch kill.
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
                    permanentCounterSupport.notifyCountersPlaced(gameData, entry, target, counters);
                    gameLogService.append(gameData, GameLog.cardTextCard(sourceCard,
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
            queueEnchantedCreatureDealsDamageTrigger(gameData, entry, damageSource, damage);
            processPendingRedirectDamage(gameData);
            return damage;
        }

        // Record only — the state-based action check (CR 704.5g/704.5h) is the single place
        // creatures die from damage; it runs after the current resolution completes.
        target.addMarkedDamage(damageSourceKey(entry, damageSource), damage);
        if (sourceHasDeathtouch) {
            target.setDamagedByDeathtouch(true);
        }

        gameLogService.append(gameData, GameLog.cardTextCard(sourceCard,
                " deals " + damage + " damage to ", target.getCard(), "."));
        log.info("Game {} - {} deals {} damage to {}", gameData.id, sourceName, damage, target.getCard().getName());

        if (damage > 0) {
            checkSpellLifelink(gameData, entry, damage);
            queueEnchantedCreatureDealsDamageTrigger(gameData, entry, damageSource, damage);
        }
        processPendingRedirectDamage(gameData);
        return damage;
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
        Permanent sourcePermanent = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        UUID sourceControllerId = sourcePermanent == null
                ? entry.getControllerId()
                : gameQueryService.findPermanentController(gameData, sourcePermanent.getId());
        if (damage > 0) {
            damage += gameQueryService.getAdditionalDamageToOpponentsBonus(
                    gameData, sourceControllerId,
                    sourcePermanent == null ? entry.getEffectiveDamageSourceCard() : sourcePermanent.getCard(),
                    sourcePermanent,
                    gameQueryService.findPermanentController(gameData, target.getId()));
        }

        if (!target.isDamageCantBePreventedOrRedirectedThisTurn()) {
            damage = damagePreventionService.applyEnchantedCreatureDamageRedirectToController(
                    gameData, target, entry.getSourcePermanentId(), damage);
            processSourceRedirectDamage(gameData);
        }
        if (damage <= 0) return;

        if (entry.getSourcePermanentId() != null) {
            graveyardService.recordCreatureDamagedByPermanent(gameData, entry.getSourcePermanentId(), target, damage);
        }

        if (damage > 0) {
            accumulateSourceDamageForReflection(gameData, entry.getEffectiveDamageSourceCard(),
                    entry.getControllerId(), entry.getSourcePermanentId(), damage,
                    null, gameQueryService.findPermanentController(gameData, target.getId()));
            triggerCollectionService.checkDealtDamageToCreatureTriggers(gameData, target, damage, entry.getControllerId());

            // Fire ON_OPPONENT_CREATURE_DEALT_DAMAGE triggers (e.g. Kazarov)
            UUID damagedCreatureControllerId = gameQueryService.findPermanentController(gameData, target.getId());
            if (damagedCreatureControllerId != null) {
                triggerCollectionService.checkOpponentCreatureDealtDamageTriggers(gameData, damagedCreatureControllerId);
            }

            // Fire ON_ANY_CREATURE_DEALT_DAMAGE triggers (e.g. Death Pits of Rath)
            triggerCollectionService.checkAnyCreatureDealtDamageTriggers(gameData, target, damage);

            // Fire ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE reflection triggers (e.g. Greatbow Doyen)
            Permanent reflectionSource = entry.getSourcePermanentId() != null
                    ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                    : null;
            UUID reflectionTargetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
            triggerCollectionService.queueEnchantedCreatureDealsDamageToCreatureTriggers(
                    gameData, reflectionSource, target.getId(), damage);
            triggerCollectionService.checkAllyDealtDamageToCreatureTriggers(gameData, reflectionSource,
                    entry.getControllerId(), reflectionTargetControllerId, target.getId(), target, damage, false);

            // Mangara's Equity: "…or a white creature you control"
            triggerCollectionService.checkCreatureDamageToYouOrYourPermanentTriggers(
                    gameData, reflectionTargetControllerId, target, reflectionSource, damage);
        }

        Card sourceCard = entry.getCard();
        String sourceName = sourceCard.getName();

        // Record only (CR 704.5g — unpreventable damage still accumulates as marked damage);
        // the state-based action check performs any resulting destruction.
        target.addMarkedDamage(damageSourceKey(entry, null), damage);
        gameData.recordDamageToPermanent(target.getId(), damage);
        if (damage > 0 && gameQueryService.sourceHasKeyword(gameData, entry, null, Keyword.DEATHTOUCH)) {
            target.setDamagedByDeathtouch(true);
        }

        gameLogService.append(gameData, GameLog.cardTextCard(sourceCard,
                " deals " + damage + " damage to ", target.getCard(), ". (damage can't be prevented)"));
        log.info("Game {} - {} deals {} unpreventable damage to {}", gameData.id, sourceName, damage, target.getCard().getName());

        if (damage > 0) {
            checkSpellLifelink(gameData, entry, damage);
            queueEnchantedCreatureDealsDamageTrigger(gameData, entry, null, damage);
        }
    }

    private void queueEnchantedCreatureDealsDamageTrigger(GameData gameData, StackEntry entry,
                                                          Permanent damageSource, int damageDealt) {
        if (damageDealt <= 0) return;
        Permanent sourceCreature = damageSource;
        if (sourceCreature == null && entry != null && entry.getSourcePermanentId() != null) {
            sourceCreature = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        }
        triggerCollectionService.queueEnchantedCreatureDealsDamageTriggers(gameData, sourceCreature, damageDealt);
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
        if (gameQueryService.isDamageFromStackEntryPrevented(gameData, entry)) {
            gameLogService.append(gameData, GameLog.cardThen(source, "'s damage is prevented."));
            return true;
        }
        if (gameQueryService.isDamageFromInstantOrSorcerySpellPrevented(gameData, entry)) {
            gameLogService.append(gameData, GameLog.cardThen(source, "'s damage is prevented."));
            return true;
        }
        if (gameQueryService.isDamageFromTargetSpellPrevented(gameData, entry)
                && gameQueryService.getTargetSpellDamagePreventionShield(gameData, entry).lifeGainPlayerId() == null) {
            gameLogService.append(gameData, GameLog.cardThen(source, "'s damage is prevented."));
            return true;
        }
        return false;
    }

    private void gainLifeForTargetSpellDamage(GameData gameData, StackEntry entry, int damage) {
        if (damage <= 0) return;
        var shield = gameQueryService.getTargetSpellDamagePreventionShield(gameData, entry);
        if (shield != null && shield.lifeGainPlayerId() != null) {
            lifeSupport.applyGainLife(gameData, shield.lifeGainPlayerId(), damage, "prevented spell damage");
        }
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
        if (!target.isDamageCantBePreventedOrRedirectedThisTurn()
                && gameQueryService.isDamagePreventable(gameData)
                && (gameQueryService.isDamageFromStackEntryPrevented(gameData, entry)
                    || gameQueryService.hasProtectionFromDamageSource(gameData, target, source,
                        entry.getControllerId()))) {
            gameLogService.append(gameData, GameLog.cardThen(source, "'s damage is prevented."));
            return true;
        }
        return false;
    }

    public boolean isSourcePermanentPreventedFromDealingDamage(GameData gameData, StackEntry entry) {
        if (entry.getSourcePermanentId() == null) return false;
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (gameQueryService.isDamageFromPermanentSourcePrevented(gameData, source)
                || gameQueryService.isDamageByCreaturePrevented(gameData, source)
                || gameData.isPreventedFromDealingDamage(entry.getSourcePermanentId())) return true;
        // Defang / Heart of Light: an aura can blank all damage dealt by the enchanted permanent,
        // including damage from its own activated and triggered abilities.
        return source != null
                && (gameQueryService.hasAuraWithEffect(gameData, source, PreventAllDamageDealtByEnchantedCreatureEffect.class)
                    || gameQueryService.hasAuraWithEffect(gameData, source, PreventAllDamageToAndByEnchantedCreatureEffect.class));
    }

    /**
     * Whether {@code permanent} is one of the permanent kinds "any target" damage can be dealt to —
     * a creature, a planeswalker or a battle (CR 115.4). A permanent that is none of them was never
     * a legal target, or stopped being one before resolution (CR 608.2b), and the divided-damage
     * loops skip it rather than burning a land.
     *
     * <p>The planeswalker and battle halves read the printed type line rather than the layer-aware
     * {@code GameQueryService.isPlaneswalker} / {@code isBattle} on purpose: the destinations that
     * consume this answer — {@link #dealCreatureDamage}'s CR 120.3c loyalty branch and CR 120.3h
     * defense branch — key off the printed line too, so a layered question here would let damage
     * through to a branch that would then do nothing with it.</p>
     */
    public boolean isAnyTargetDamageRecipient(GameData gameData, Permanent permanent) {
        return gameQueryService.isCreature(gameData, permanent)
                || permanent.getCard().hasType(CardType.PLANESWALKER)
                || permanent.getCard().hasType(CardType.BATTLE);
    }

    public void resolveAnyTargetDamage(GameData gameData, StackEntry entry, UUID targetId, int rawDamage, boolean cantRegenerate) {
        resolveAnyTargetDamage(gameData, entry, targetId, rawDamage, cantRegenerate, false);
    }

    public void resolveAnyTargetDamage(GameData gameData, StackEntry entry, UUID targetId, int rawDamage,
                                       boolean cantRegenerate, boolean cantBeRedirected) {
        Card source = entry.getEffectiveDamageSourceCard();
        boolean targetIsPlayer = gameData.playerIds.contains(targetId);
        Permanent targetPermanent = targetIsPlayer ? null : gameQueryService.findPermanentById(gameData, targetId);

        if (!targetIsPlayer && targetPermanent == null) return;

        if (targetIsPlayer) {
            UUID redirectedPlayerId = cantBeRedirected ? null
                    : damagePreventionService.applyNextInstantOrSorceryDamageRedirectShield(
                            gameData, entry, targetId, rawDamage);
            if (redirectedPlayerId != null && !redirectedPlayerId.equals(targetId)) {
                dealDamageToPlayer(gameData, entry, redirectedPlayerId, rawDamage);
                return;
            }
            if (isDamageSourcePreventedWithLog(gameData, entry)) return;
            // dealDamageToPlayer handles per-permanent prevention (permanentsPreventedFromDealingDamage)
            dealDamageToPlayer(gameData, entry, targetId, rawDamage);
        } else {
            if (!targetPermanent.isDamageCantBePreventedOrRedirectedThisTurn()
                    && isDamageSourcePreventedWithLog(gameData, entry)) return;
            if (!targetPermanent.isDamageCantBePreventedOrRedirectedThisTurn()
                    && gameQueryService.isDamagePreventable(gameData)
                    && (isSourcePermanentPreventedFromDealingDamage(gameData, entry)
                        || gameQueryService.hasProtectionFromDamageSource(gameData, targetPermanent, source,
                            entry.getControllerId()))) {
                gameLogService.append(gameData, GameLog.cardThen(source, "'s damage is prevented."));
                return;
            }
            if (targetPermanent.getCard().hasType(CardType.PLANESWALKER)
                    && !(targetPermanent.isDamageCantBePreventedOrRedirectedThisTurn()
                    && gameQueryService.isCreature(gameData, targetPermanent))) {
                Permanent damageSourcePermanent = entry.getSourcePermanentId() == null
                        ? null
                        : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
                UUID sourceControllerId = damageSourcePermanent == null
                        ? entry.getControllerId()
                        : gameQueryService.findPermanentController(gameData, damageSourcePermanent.getId());
                if (sourceControllerId == null) {
                    sourceControllerId = entry.getControllerId();
                }
                rawDamage *= gameQueryService.getDamageToRecipientMultiplier(
                        gameData, gameQueryService.findPermanentController(gameData, targetPermanent.getId()), sourceControllerId);
                if (gameQueryService.isDamageFromTargetSpellPrevented(gameData, entry)) {
                    gainLifeForTargetSpellDamage(gameData, entry, rawDamage);
                    gameLogService.append(gameData, GameLog.cardThen(source, "'s damage is prevented."));
                    return;
                }
                // "Prevent all damage that would be dealt to ~" (e.g. Gideon of the Trials 0) also stops
                // loyalty loss. The creature-damage path applies this set in DamagePreventionService, but
                // the loyalty branch below bypasses it, so guard it here.
                if (gameQueryService.isDamagePreventable(gameData)
                        && gameData.creaturesWithAllDamagePrevented.contains(targetPermanent.getId())) {
                    gameLogService.append(gameData, GameLog.cardThen(source, "'s damage is prevented."));
                    return;
                }
                // CR 306.8: damage dealt to a planeswalker removes that many loyalty counters from it
                // (SBAs then move it to the graveyard once it has 0 loyalty). Mirrors the combat path.
                int loyaltyDamage = Math.max(0, rawDamage);
                // Djeru, With Eyes Open: prevent N of the damage dealt to a planeswalker you control.
                UUID pwControllerId = gameQueryService.findPermanentController(gameData, targetPermanent.getId());
                Permanent sourcePermanent = entry.getSourcePermanentId() == null
                        ? null
                        : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
                Set<CardColor> sourceColors = sourcePermanent == null
                        ? sourceCardColors(source)
                        : gameQueryService.getEffectiveColors(gameData, sourcePermanent);
                if (damagePreventionService.isColorDamagePreventedForTarget(gameData, targetPermanent.getId(), sourceColors)) {
                    gameLogService.append(gameData, GameLog.textCardText("Damage to ", targetPermanent.getCard(), " is prevented."));
                    return;
                }
                loyaltyDamage = damagePreventionService.applyPermanentDamagePreventionShield(
                        gameData, targetPermanent, loyaltyDamage);
                loyaltyDamage = damagePreventionService.applyControllerAndPermanentsNoncombatDamagePrevention(
                        gameData, targetPermanent, loyaltyDamage);
                loyaltyDamage -= damagePreventionService.applyPlaneswalkerFixedPerSourceDamagePrevention(gameData, pwControllerId, loyaltyDamage);
                loyaltyDamage -= damagePreventionService.applyAllButOneDamagePrevention(gameData, pwControllerId, loyaltyDamage);
                if (loyaltyDamage > 0) {
                    triggerCollectionService.checkAllyDealtDamageToPlaneswalkerTriggers(
                            gameData, sourcePermanent, entry.getControllerId(), targetPermanent.getId(),
                            loyaltyDamage, false, null);
                    accumulateSourceDamageForReflection(gameData, source, entry.getControllerId(),
                            entry.getSourcePermanentId(), loyaltyDamage, null, pwControllerId);
                    queueEnchantedCreatureDealsDamageTrigger(gameData, entry, sourcePermanent, loyaltyDamage);
                    gameData.recordDamageDealtBySource(entry.getSourcePermanentId(), loyaltyDamage);
                    gameData.recordDamageRecipientBySource(entry.getSourcePermanentId(), targetPermanent.getId());
                    targetPermanent.setCounterCount(CounterType.LOYALTY,
                            targetPermanent.getCounterCount(CounterType.LOYALTY) - loyaltyDamage);
                    gameLogService.append(gameData, GameLog.cardTextCard(source,
                            " deals " + loyaltyDamage + " damage to ", targetPermanent.getCard(),
                            " (" + targetPermanent.getCounterCount(CounterType.LOYALTY) + " loyalty remaining)."));
                }
                return;
            }
            // A battle deliberately has no arm of its own here: it falls through to
            // dealCreatureDamage, whose CR 120.3h branch removes the defense counters after the
            // shared pipeline has applied prevention shields, redirects, damage multipliers and
            // spell lifelink (CR 702.15b). The planeswalker arm above predates that pipeline and
            // still open-codes its own; the two must not diverge again.
            int damageDealt = dealCreatureDamage(gameData, entry, targetPermanent, rawDamage,
                    cantBeRedirected);
            if (cantRegenerate && damageDealt > 0) {
                targetPermanent.setCantRegenerateThisTurn(true);
            }
        }
    }

    public void damageAllCreaturesOnBattlefield(GameData gameData, StackEntry entry, int damage, Predicate<Permanent> filter) {
        damageAllCreaturesOnBattlefield(gameData, entry, damage, filter, false);
    }

    /**
     * Variant that marks every creature actually dealt damage so that if it would die this turn it
     * is exiled instead (Yamabushi's Storm). Creatures the damage never reaches — protection,
     * prevention — are left unmarked, as they were not "dealt damage this way".
     */
    public void damageAllCreaturesOnBattlefield(GameData gameData, StackEntry entry, int damage,
                                                Predicate<Permanent> filter, boolean exileInsteadOfDie) {
        damageAllCreaturesOnBattlefield(gameData, entry, damage, filter, exileInsteadOfDie, false);
    }

    public void damageAllCreaturesOnBattlefield(GameData gameData, StackEntry entry, int damage,
                                                Predicate<Permanent> filter, boolean exileInsteadOfDie,
                                                boolean cantRegenerate) {
        gameData.forEachBattlefield((playerId, battlefield) ->
                damageFilteredCreatures(gameData, entry, p -> damage, battlefield, filter,
                        exileInsteadOfDie, cantRegenerate)
        );
    }

    public void damageFilteredCreatures(GameData gameData, StackEntry entry, int damage, Collection<Permanent> permanents, Predicate<Permanent> filter) {
        damageFilteredCreatures(gameData, entry, p -> damage, permanents, filter);
    }

    /**
     * Variant whose damage is computed per creature, for amounts that describe the creature being
     * damaged (Baki's Curse: 2 damage per Aura attached to that creature).
     */
    public void damageAllCreaturesOnBattlefield(GameData gameData, StackEntry entry, ToIntFunction<Permanent> damage, Predicate<Permanent> filter) {
        gameData.forEachBattlefield((playerId, battlefield) ->
                damageFilteredCreatures(gameData, entry, damage, battlefield, filter)
        );
    }

    public void damageAllCreaturesOnBattlefield(GameData gameData, StackEntry entry, ToIntFunction<Permanent> damage,
                                                Predicate<Permanent> filter, boolean exileInsteadOfDie,
                                                boolean cantRegenerate) {
        gameData.forEachBattlefield((playerId, battlefield) ->
                damageFilteredCreatures(gameData, entry, damage, battlefield, filter,
                        exileInsteadOfDie, cantRegenerate)
        );
    }

    public void damageFilteredCreatures(GameData gameData, StackEntry entry, ToIntFunction<Permanent> damage, Collection<Permanent> permanents, Predicate<Permanent> filter) {
        damageFilteredCreatures(gameData, entry, damage, permanents, filter, false);
    }

    public void damageFilteredCreatures(GameData gameData, StackEntry entry, ToIntFunction<Permanent> damage,
                                        Collection<Permanent> permanents, Predicate<Permanent> filter,
                                        boolean exileInsteadOfDie) {
        damageFilteredCreatures(gameData, entry, damage, permanents, filter, exileInsteadOfDie, false);
    }

    public void damageFilteredCreatures(GameData gameData, StackEntry entry, ToIntFunction<Permanent> damage,
                                        Collection<Permanent> permanents, Predicate<Permanent> filter,
                                        boolean exileInsteadOfDie, boolean cantRegenerate) {
        for (Permanent p : permanents) {
            if (!filter.test(p)) continue;
            if (gameQueryService.isDamagePreventable(gameData) && gameQueryService.hasProtectionFromDamageSource(gameData, p, entry.getCard(), entry.getControllerId())) continue;
            // Mark before the damage lands so lethal damage is replaced by exile straight away.
            if (exileInsteadOfDie) {
                p.setExileInsteadOfDieThisTurn(true);
            }
            int damageDealt = dealCreatureDamage(gameData, entry, p, damage.applyAsInt(p));
            if (cantRegenerate && damageDealt > 0) {
                p.setCantRegenerateThisTurn(true);
            }
        }
    }

    public void dealDamageToPlayer(GameData gameData, StackEntry entry, UUID playerId, int rawDamage) {
        Card source = entry.getEffectiveDamageSourceCard();
        if (gameQueryService.isDamageFromStackEntryPrevented(gameData, entry)) {
            gameLogService.append(gameData, GameLog.cardThen(source,
                    "'s damage to " + gameData.playerIdToName.get(playerId) + " is prevented."));
            return;
        }
        Permanent sourcePermanent = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        UUID sourceControllerId = sourcePermanent == null
                ? entry.getControllerId()
                : gameQueryService.findPermanentController(gameData, sourcePermanent.getId());
        if (sourceControllerId == null) {
            sourceControllerId = entry.getControllerId();
        }
        UUID redirectedTarget = findHarshJudgmentRedirectTarget(gameData, entry, playerId);
        if (rawDamage > 0 && redirectedTarget != null) {
            gameLogService.append(gameData, GameLog.cardThen(source,
                    "'s damage to " + gameData.playerIdToName.get(playerId)
                            + " is dealt to its controller instead."));
            dealDamageToPlayer(gameData, entry, redirectedTarget, rawDamage);
            return;
        }
        String cardName = source.getName();
        while (true) {
            UUID redirectedPlayerId = damagePreventionService.applyNextInstantOrSorceryDamageRedirectShield(
                    gameData, entry, playerId, rawDamage);
            if (redirectedPlayerId == null) {
                break;
            }
            playerId = redirectedPlayerId;
        }
        // Curse of Bloodletting and similar: double damage dealt to the enchanted player (replacement effect)
        rawDamage *= gameQueryService.getEnchantedPlayerDamageMultiplier(gameData, playerId);
        // Gisela, Blade of Goldnight: double the damage dealt to an opponent of her controller.
        rawDamage *= gameQueryService.getDamageToRecipientMultiplier(gameData, playerId, sourceControllerId);
        if (rawDamage > 0) {
            rawDamage += gameQueryService.getControllerDamageToOpponentBonus(
                    gameData, sourceControllerId, playerId);
        }
        // Energy Storm and Hidden Retreat: prevent all damage dealt by instant and sorcery spells.
        if (gameQueryService.isDamageFromInstantOrSorcerySpellPrevented(gameData, entry)) {
            gameLogService.append(gameData, GameLog.cardThen(source,
                    "'s damage to " + gameData.playerIdToName.get(playerId) + " is prevented."));
            return;
        }
        if (gameQueryService.isDamageFromTargetSpellPrevented(gameData, entry)) {
            gainLifeForTargetSpellDamage(gameData, entry, rawDamage);
            gameLogService.append(gameData, GameLog.cardThen(source,
                    "'s damage to " + gameData.playerIdToName.get(playerId) + " is prevented."));
            return;
        }
        // Benevolent Unicorn: a spell dealing damage to a player deals that much damage minus N.
        rawDamage = Math.max(0, rawDamage - gameQueryService.getSpellDamageReduction(gameData, entry));
        Set<CardColor> sourceColors = sourcePermanent == null
                ? sourceCardColors(source)
                : gameQueryService.getEffectiveColors(gameData, sourcePermanent);
        UUID damageSourceId = damageSourceKey(entry, sourcePermanent);
        // Tok-Tok, Volcano Born: a source of a matching colour deals that much damage plus N instead.
        if (rawDamage > 0) {
            rawDamage += gameQueryService.getDamageToPlayerColorSourceBonus(gameData,
                    gameQueryService.getDamageSourceColors(gameData, sourceColors));
            rawDamage += gameQueryService.getAdditionalDamageToOpponentsBonus(
                    gameData, sourceControllerId, source, sourcePermanent, playerId);
        }
        rawDamage = gameQueryService.applyDamageReplacementEffects(gameData, rawDamage);
        if (damagePreventionService.isColorDamagePreventedForTarget(gameData, playerId, sourceColors)) {
            gameLogService.append(gameData, GameLog.cardThen(source,
                    "'s damage to " + gameData.playerIdToName.get(playerId) + " is prevented."));
            return;
        }
        boolean sourceDamagePrevented = damagePreventionService.isSourceDamagePreventedForPlayer(
                gameData, playerId, damageSourceId);
        if (sourceDamagePrevented) {
            damagePreventionService.applySourceDamagePreventionForPlayer(
                    gameData, playerId, damageSourceId, rawDamage, sourceColors);
        }
        if (sourceDamagePrevented
                || damagePreventionService.isNoncombatDamageFromAttackerPreventedForPlayer(gameData, playerId, damageSourceId)
                || gameQueryService.isDamageFromMatchingSourcePreventedForPlayer(gameData, playerId, sourcePermanent)
                || isSourcePermanentPreventedFromDealingDamage(gameData, entry)) {
            gameLogService.append(gameData, GameLog.cardThen(source,
                    "'s damage to " + gameData.playerIdToName.get(playerId) + " is prevented."));
            return;
        }
        // Protection from color (e.g. Faith's Shield) prevents all damage from sources of that color.
        if (gameQueryService.isDamagePreventable(gameData)
                && gameQueryService.playerHasProtectionFromColor(gameData, playerId,
                        gameQueryService.getDamageSourceColor(gameData, source.getColor()))) {
            gameLogService.append(gameData, GameLog.cardThen(source,
                    "'s damage to " + gameData.playerIdToName.get(playerId) + " is prevented."));
            return;
        }
        // Protection from card name (Runed Halo) prevents all damage from sources with that name.
        // Gideon's Intervention likewise prevents damage from sources with the chosen name.
        if (gameQueryService.isDamagePreventable(gameData)
                && (gameQueryService.playerHasProtectionFromChosenName(gameData, playerId, cardName)
                        || gameQueryService.isDamageFromChosenNamePreventedForController(gameData, playerId, cardName))) {
            gameLogService.append(gameData, GameLog.cardThen(source,
                    "'s damage to " + gameData.playerIdToName.get(playerId) + " is prevented."));
            return;
        }
        // Apply source-specific redirect shields (e.g. Harm's Way) before general prevention
        rawDamage = damagePreventionService.applySourceRedirectShields(gameData, playerId, damageSourceId, rawDamage);
        processSourceRedirectDamage(gameData);
        rawDamage = damagePreventionService.applyPlayerSourceNextDamageRedirectShield(
                gameData, playerId, damageSourceId, rawDamage);
        processSourceRedirectDamage(gameData);
        // Reflect Damage: the chosen source's next damage is dealt to that source's controller instead.
        rawDamage = damagePreventionService.applyReflectDamageToSourceControllerShield(
                gameData, damageSourceId, rawDamage);
        processEyeForAnEyeReflections(gameData);
        // Opal-Eye: the chosen source's next damage is dealt to a fixed creature instead.
        rawDamage = damagePreventionService.applySourceNextDamageRedirectToPermanent(
                gameData, damageSourceId, null, rawDamage);
        processSourceRedirectDamage(gameData);
        // Saving Grace: redirect all damage this turn to the player onto the enchanted creature.
        rawDamage = damagePreventionService.applyTurnDamageRedirectToCreature(
                gameData, playerId, null, damageSourceId, rawDamage, false);
        processSourceRedirectDamage(gameData);
        // Martyrdom: redirect the next N damage to the player onto the creature carrying the ability.
        rawDamage = damagePreventionService.applyPlayerNextDamageRedirectShields(
                gameData, playerId, entry == null ? null : entry.getSourcePermanentId(), rawDamage);
        processSourceRedirectDamage(gameData);
        if (rawDamage <= 0) return;
        if (!damagePreventionService.applyColorDamagePreventionForPlayer(gameData, playerId, source.getColor())) {
            rawDamage = damagePreventionService.applyOpponentSourceDamageReduction(gameData, playerId, entry.getControllerId(), rawDamage);
            // Apply target+source-specific prevention shields (e.g. Healing Grace)
            if (damageSourceId != null) {
                rawDamage = damagePreventionService.applyTargetSourcePreventionShield(gameData, playerId, damageSourceId, rawDamage);
                // Eye for an Eye: reflect the next damage this source deals to the player back at the
                // source's controller. Does not reduce the damage dealt here; schedules a reflection.
                damagePreventionService.applyEyeForAnEyeReflection(gameData, playerId, damageSourceId, rawDamage);
                // Apply one-shot Circle-of-Protection shields (prevent the next damage event from the chosen source)
                rawDamage = damagePreventionService.applyPlayerNextSourceDamageShield(gameData, playerId, damageSourceId, rawDamage);
                damagePreventionService.applyEyeForAnEyeReflection(gameData, playerId, entry.getSourcePermanentId(), rawDamage);
                // Apply one-shot Sanctum Guardian / Honorable Passage shields
                rawDamage = damagePreventionService.applyChosenSourceNextDamageToAnyTargetShield(gameData, damageSourceId, rawDamage, playerId);
                processEyeForAnEyeReflections(gameData);
            }
            // Apply one-shot chosen-source prevention to permanents and spells.
            UUID sourceId = entry.getSourcePermanentId() != null
                    ? entry.getSourcePermanentId()
                    : source.getId();
            rawDamage = damagePreventionService.applyPlayerNextSourceDamageShield(gameData, playerId, sourceId, rawDamage);
            processEyeForAnEyeReflections(gameData);
            int effectiveDamage = damagePreventionService.applyPlayerPreventionShield(gameData, playerId, rawDamage);
            processPendingRedirectDamage(gameData);
            effectiveDamage = permanentRemovalService.redirectPlayerDamageToEnchantedCreature(
                    gameData, playerId, effectiveDamage, cardName, false, entry.getSourcePermanentId());

            // Battletide Alchemist: the controller prevents up to (Clerics they control) of this source's damage.
            int battletidePrevented = damagePreventionService.applyControllerPerClericDamagePrevention(gameData, playerId, effectiveDamage);
            if (battletidePrevented > 0) {
                effectiveDamage -= battletidePrevented;
                gameLogService.append(gameData, GameLog.textCardText(battletidePrevented + " of ", source,
                        "'s damage to " + gameData.playerIdToName.get(playerId) + " is prevented."));
            }

            // Urza's Armor and Sphere of Purity: the controller prevents a fixed amount of this source's damage.
            int fixedPrevented = damagePreventionService.applyControllerFixedPerSourceDamagePrevention(
                    gameData,
                    playerId,
                    effectiveDamage,
                    gameQueryService.isDamageSourceCreature(gameData, entry, sourcePermanent),
                    gameQueryService.isDamageSourceArtifact(gameData, entry, sourcePermanent),
                    gameQueryService.getDamageSourceColors(gameData, sourceColors),
                    false);
            if (fixedPrevented > 0) {
                effectiveDamage -= fixedPrevented;
                gameLogService.append(gameData, GameLog.textCardText(fixedPrevented + " of ", source,
                        "'s damage to " + gameData.playerIdToName.get(playerId) + " is prevented."));
            }

            int allButOnePrevented = damagePreventionService.applyAllButOneDamagePrevention(gameData, playerId, effectiveDamage);
            if (allButOnePrevented > 0) {
                effectiveDamage -= allButOnePrevented;
                gameLogService.append(gameData, GameLog.textCardText(allButOnePrevented + " of ", source,
                        "'s damage to " + gameData.playerIdToName.get(playerId) + " is prevented."));
            }

            // Purity: prevent all remaining noncombat damage to the controller and gain that much life
            int purityPrevented = damagePreventionService.applyControllerNoncombatDamagePrevention(gameData, playerId, effectiveDamage);
            if (purityPrevented > 0) {
                effectiveDamage -= purityPrevented;
                gameLogService.append(gameData, GameLog.cardThen(source,
                        "'s " + purityPrevented + " damage to " + gameData.playerIdToName.get(playerId) + " is prevented."));
                lifeSupport.applyGainLife(gameData, playerId, purityPrevented, "prevented damage");
            }

            // Hostility: prevent all remaining damage a spell you control would deal to an opponent and
            // create one token per 1 damage prevented (for the spell's controller).
            var hostility = damagePreventionService.findSpellDamageToOpponentPrevention(gameData, entry, playerId, effectiveDamage);
            if (hostility != null) {
                int hostilityPrevented = effectiveDamage;
                effectiveDamage = 0;
                gameLogService.append(gameData, GameLog.cardThen(source,
                        "'s " + hostilityPrevented + " damage to " + gameData.playerIdToName.get(playerId) + " is prevented."));
                permanentControlSupport.applyCreateToken(gameData, entry.getControllerId(),
                        hostility.token(), hostilityPrevented, entry.getCard().getSetCode());
            }

            // Glacial Chasm: prevent all remaining damage that would be dealt to its controller.
            int chasmPrevented = applyControllerAllDamagePrevention(gameData, playerId, effectiveDamage);
            if (chasmPrevented > 0) {
                effectiveDamage -= chasmPrevented;
                gameLogService.append(gameData, GameLog.cardThen(source,
                        "'s " + chasmPrevented + " damage to " + gameData.playerIdToName.get(playerId) + " is prevented."));
            }

            int lichReplaced = applyNefariousLichReplacement(gameData, playerId, effectiveDamage);
            if (lichReplaced > 0) {
                effectiveDamage -= lichReplaced;
                gameLogService.append(gameData, GameLog.cardThen(source,
                        "'s " + lichReplaced + " damage to " + gameData.playerIdToName.get(playerId)
                                + " is replaced by Nefarious Lich."));
            }

            int crumblingSanctuaryReplaced = applyCrumblingSanctuaryReplacement(gameData, playerId, effectiveDamage);
            if (crumblingSanctuaryReplaced > 0) {
                effectiveDamage -= crumblingSanctuaryReplaced;
                gameLogService.append(gameData, GameLog.cardThen(source,
                        "'s " + crumblingSanctuaryReplaced + " damage to " + gameData.playerIdToName.get(playerId)
                                + " is replaced by Crumbling Sanctuary."));
            }

            // Immortal Coil: prevent all remaining damage to the controller and exile a card from
            // their graveyard for each 1 damage prevented this way.
            int coilPrevented = applyImmortalCoilPrevention(gameData, playerId, effectiveDamage);
            if (coilPrevented > 0) {
                effectiveDamage -= coilPrevented;
                gameLogService.append(gameData, GameLog.cardThen(source,
                        "'s " + coilPrevented + " damage to " + gameData.playerIdToName.get(playerId) + " is prevented."));
            }

            effectiveDamage -= applyDelayingShieldCounterReplacement(gameData, playerId, effectiveDamage);

            // Soul Echo: each 1 damage removes an echo counter instead (replacement, not prevention).
            effectiveDamage -= applySoulEchoCounterRemoval(gameData, playerId, effectiveDamage);

            effectiveDamage -= damagePreventionService.applyDamageToControllerAndPutCounterOnSelf(
                    gameData, playerId, effectiveDamage);

            boolean sourceHasInfect = gameQueryService.sourceHasKeyword(gameData, entry, null, Keyword.INFECT);
            boolean treatAsInfect = sourceHasInfect || gameQueryService.shouldDamageBeDealtAsInfect(gameData, playerId);

            if (treatAsInfect) {
                if (effectiveDamage > 0) {
                    lifeSupport.applyPoisonCounters(gameData, playerId, effectiveDamage,
                            source != null ? source.getName() : entry.getCard().getName(),
                            entry.getControllerId());
                }
            } else if (effectiveDamage > 0 && !gameQueryService.canPlayerLifeChange(gameData, playerId)) {
                String playerName = gameData.playerIdToName.get(playerId);
                gameLogService.append(gameData, GameLog.text(playerName + "'s life total can't change."));
            } else {
                int currentLife = gameData.getLife(playerId);
                int newLife = currentLife - effectiveDamage;
                // Worship / Elderscale Wurm: damage can't reduce the player's life total past an active floor.
                // The full damage is still dealt (lifelink/damage triggers see the full amount); only the life
                // total reduction is capped.
                // 0 means no active floor — do not clamp (life may go negative).
                int lifeFloor = gameQueryService.damageLifeFloor(gameData, playerId, currentLife);
                if (lifeFloor > 0 && newLife < lifeFloor) {
                    newLife = lifeFloor;
                }
                gameData.playerLifeTotals.put(playerId, newLife);
                int lifeLost = currentLife - newLife;

                if (effectiveDamage > 0) {
                    String playerName = gameData.playerIdToName.get(playerId);
                    gameLogService.append(gameData, GameLog.textCardText(
                            playerName + " takes " + effectiveDamage + " damage from ", source, "."));
                    if (lifeLost > 0) {
                        triggerCollectionService.checkLifeLossTriggers(gameData, playerId, lifeLost);
                    }
                }
            }

            if (effectiveDamage > 0) {
                accumulateSourceDamageForReflection(gameData, source, entry.getControllerId(),
                        entry.getSourcePermanentId(), effectiveDamage, playerId);
                Permanent sourceCreature = entry.getSourcePermanentId() == null
                        ? null
                        : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
                triggerCollectionService.queueEnchantedCreatureDealsDamageTriggers(
                        gameData, sourceCreature, effectiveDamage);
                int artifactDamage = gameQueryService.isDamageSourceArtifact(gameData, entry, sourcePermanent)
                        ? effectiveDamage : 0;
                gameData.recordDamageToPlayer(playerId, effectiveDamage, artifactDamage);
                gameData.recordNoncombatDamageToPlayer(playerId, effectiveDamage);
                gameData.recordDamageDealtBySource(entry.getSourcePermanentId(), effectiveDamage);
                gameData.recordDamageRecipientBySource(entry.getSourcePermanentId(), playerId);
                entry.recordPlayerDealtDamage(playerId);
                gameData.recordNoncombatDamageSourceToPlayer(entry.getSourcePermanentId(), playerId);
                if (sourcePermanent != null && gameQueryService.isCreature(gameData, sourcePermanent)) {
                    gameData.recordCreatureDamageSourceToPlayer(sourcePermanent.getId(), playerId);
                }
                recordRedSpellDamage(gameData, entry, source, playerId);
                triggerCollectionService.checkDamageDealtToControllerTriggers(gameData, playerId, entry.getSourcePermanentId(), false);
                triggerCollectionService.checkEnchantedCreatureDealtDamageToControllerReflectTriggers(gameData, playerId, entry.getSourcePermanentId(), effectiveDamage);
                // The stack entry's controller is the damage source's controller (caster/activator);
                // used to gate the opponent-only ON_CONTROLLER_DEALT_DAMAGE_BY_OPPONENT slot.
                triggerCollectionService.checkControllerDealtDamageTriggers(gameData, playerId, entry.getControllerId(), effectiveDamage);
                // Night Dealings: "whenever a source you control deals damage to another player".
                triggerCollectionService.checkAllySourceDealtDamageToOpponentTriggers(
                        gameData, playerId, entry.getControllerId(), entry.getSourcePermanentId(), effectiveDamage);
                triggerCollectionService.checkAllySourceDealtNoncombatDamageToOpponentTriggers(
                        gameData, playerId, entry.getControllerId(), effectiveDamage);
                triggerCollectionService.checkOpponentDealtDamageTriggers(gameData, playerId, effectiveDamage);
                // Mangara's Equity: "whenever a creature of the chosen color deals damage to you"
                triggerCollectionService.checkCreatureDamageToYouOrYourPermanentTriggers(gameData, playerId, null,
                        entry.getSourcePermanentId() != null
                                ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                                : null,
                        effectiveDamage);
                // Source's own ON_DAMAGE_TO_PLAYER (e.g. Niv-Mizzet, Dracogenius ping → may draw).
                if (entry.getSourcePermanentId() != null) {
                    Permanent triggerSourcePermanent = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
                    if (triggerSourcePermanent != null) {
                        triggerCollectionService.checkSourceDealsDamageToPlayerTriggers(gameData, triggerSourcePermanent,
                                entry.getControllerId(), playerId, effectiveDamage);
                    }
                }
                triggerCollectionService.checkNoncombatDamageToOpponentTriggers(
                        gameData, playerId, sourceControllerId, effectiveDamage);
                triggerCollectionService.checkRedSpellOrPlaneswalkerDamageToOpponentTriggers(gameData, playerId, entry);
                checkSpellLifelink(gameData, entry, effectiveDamage);
            }
        }
        processEyeForAnEyeReflections(gameData);
    }

    private UUID findHarshJudgmentRedirectTarget(GameData gameData, StackEntry entry, UUID damagedPlayerId) {
        if (entry == null || damagedPlayerId == null
                || (entry.getEntryType() != StackEntryType.INSTANT_SPELL
                && entry.getEntryType() != StackEntryType.SORCERY_SPELL)
                || damagedPlayerId.equals(entry.getControllerId())) {
            return null;
        }

        Set<CardColor> sourceColors = sourceCardColors(entry.getEffectiveDamageSourceCard());
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(damagedPlayerId, List.of())) {
            if (permanent.getChosenColor() == null || !sourceColors.contains(permanent.getChosenColor())) {
                continue;
            }
            boolean hasRedirect = permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(RedirectChosenColorSpellDamageToControllerEffect.class::isInstance);
            if (hasRedirect) {
                return entry.getControllerId();
            }
        }
        return null;
    }

    /**
     * Remembers the controller of a red instant or sorcery spell that just dealt damage to a player,
     * so Suffocation can find "the last red instant or sorcery spell that dealt damage to you this turn".
     */
    private void recordRedSpellDamage(GameData gameData, StackEntry entry, Card source, UUID playerId) {
        if (source == null || !source.getColors().contains(CardColor.RED)) {
            return;
        }
        if (entry.getEntryType() != StackEntryType.INSTANT_SPELL
                && entry.getEntryType() != StackEntryType.SORCERY_SPELL) {
            return;
        }
        gameData.recordRedSpellDamageToPlayer(playerId, entry.getControllerId());
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
     * when damage redirect shields (e.g. Vengeful Archon) prevent damage. The shield's source
     * deals the prevented amount to the redirect target, which is a player for Vengeful Archon
     * and any target for Divine Deflection.
     */
    public void processPendingRedirectDamage(GameData gameData) {
        if (gameData.pendingRedirectDamage.isEmpty()) return;

        List<DamageRedirectShield> toProcess = new ArrayList<>(gameData.pendingRedirectDamage);
        gameData.pendingRedirectDamage.clear();

        for (DamageRedirectShield redirect : toProcess) {
            UUID targetId = redirect.redirectTargetId();
            int damage = redirect.remainingAmount();
            if (!gameData.playerIds.contains(targetId)) {
                dealRedirectDamageToPermanent(gameData, redirect, targetId, damage);
                continue;
            }
            String targetName = gameData.playerIdToName.get(targetId);
            String protectedName = protectedRecipientName(gameData, redirect);

            gameLogService.append(gameData, GameLog.cardThen(redirect.sourceCard(),
                    " prevents " + damage + " damage to " + protectedName + "."));
            gameLogService.append(gameData, GameLog.cardThen(redirect.sourceCard(),
                    " deals " + damage + " damage to " + targetName + "."));

            // Apply prevention shields on the redirect target (they may also have shields)
            int redirectEffective = damagePreventionService.applyPlayerPreventionShield(gameData, targetId, damage);
            // Recursively process any redirects triggered by the target's shields
            processPendingRedirectDamage(gameData);
            redirectEffective -= damagePreventionService.applyDamageToControllerAndPutCounterOnSelf(
                    gameData, targetId, redirectEffective);

            if (redirectEffective > 0) {
                if (gameQueryService.canPlayerLifeChange(gameData, targetId)) {
                    int currentLife = gameData.getLife(targetId);
                    gameData.playerLifeTotals.put(targetId, currentLife - redirectEffective);
                }
                Permanent sourcePermanent = redirect.sourcePermanentId() == null
                        ? null
                        : gameQueryService.findPermanentById(gameData, redirect.sourcePermanentId());
                boolean artifactSource = sourcePermanent != null
                        ? gameQueryService.isArtifact(gameData, sourcePermanent)
                        : redirect.sourceCard() != null && redirect.sourceCard().hasType(CardType.ARTIFACT);
                gameData.recordDamageToPlayer(targetId, redirectEffective, artifactSource ? redirectEffective : 0);
                gameData.recordDamageRecipientBySource(redirect.sourcePermanentId(), targetId);
                triggerCollectionService.checkOpponentDealtDamageTriggers(gameData, targetId, redirectEffective);
            }
        }
    }

    /**
     * Deals a redirect shield's prevented damage to a permanent target (Divine Deflection's "any
     * target" can be a creature or planeswalker). Routed through the normal creature damage path so
     * prevention, protection and damage triggers all apply. Nothing happens when the target has
     * left the battlefield.
     */
    private void dealRedirectDamageToPermanent(GameData gameData, DamageRedirectShield redirect,
                                               UUID targetId, int damage) {
        Permanent targetPermanent = gameQueryService.findPermanentById(gameData, targetId);
        if (targetPermanent == null) return;

        String protectedName = protectedRecipientName(gameData, redirect);
        gameLogService.append(gameData, GameLog.cardThen(redirect.sourceCard(),
                " prevents " + damage + " damage to " + protectedName + "."));

        StackEntry tempEntry = new StackEntry(
                StackEntryType.INSTANT_SPELL,
                redirect.sourceCard(),
                redirect.protectedPlayerId(),
                redirect.sourceCard().getName(),
                List.of(),
                targetId,
                redirect.sourcePermanentId());
        dealCreatureDamage(gameData, tempEntry, targetPermanent, damage);
        processPendingRedirectDamage(gameData);
    }

    private String protectedRecipientName(GameData gameData, DamageRedirectShield redirect) {
        if (redirect.protectedPermanentId() != null) {
            Permanent protectedPermanent = gameQueryService.findPermanentById(
                    gameData, redirect.protectedPermanentId());
            return protectedPermanent == null ? "the target creature" : protectedPermanent.getCard().getName();
        }
        return gameData.playerIdToName.get(redirect.protectedPlayerId());
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
                gameLogService.append(gameData, GameLog.text(damage + " damage is redirected to " + targetName + "."));

                int redirectEffective = damagePreventionService.applyPlayerPreventionShield(gameData, targetId, damage);
                processPendingRedirectDamage(gameData);
                redirectEffective -= damagePreventionService.applyDamageToControllerAndPutCounterOnSelf(
                        gameData, targetId, redirectEffective);

                if (redirectEffective > 0) {
                    if (gameQueryService.canPlayerLifeChange(gameData, targetId)) {
                        int currentLife = gameData.getLife(targetId);
                        gameData.playerLifeTotals.put(targetId, currentLife - redirectEffective);
                    }
                    Permanent sourcePermanent = redirect.damageSourceId() == null
                            ? null
                            : gameQueryService.findPermanentById(gameData, redirect.damageSourceId());
                    boolean artifactSource = sourcePermanent != null
                            && gameQueryService.isArtifact(gameData, sourcePermanent);
                    gameData.recordDamageToPlayer(targetId, redirectEffective, artifactSource ? redirectEffective : 0);
                    gameData.recordDamageRecipientBySource(redirect.damageSourceId(), targetId);
                    triggerCollectionService.checkOpponentDealtDamageTriggers(gameData, targetId, redirectEffective);
                }
            } else {
                Permanent targetPerm = gameQueryService.findPermanentById(gameData, targetId);
                if (targetPerm == null) continue;

                gameLogService.append(gameData, GameLog.textCardText(
                        damage + " damage is redirected to ", targetPerm.getCard(), "."));

                int effectiveDamage = damagePreventionService.applyCreaturePreventionShield(gameData, targetPerm, damage);
                if (effectiveDamage > 0) {
                    // A planeswalker destination loses that much loyalty (CR 120.3c) and a battle
                    // destination that many defense counters (CR 120.3h); a permanent that is also
                    // a creature additionally gets marked damage (CR 120.3e).
                    if (targetPerm.getCard().hasType(CardType.PLANESWALKER)) {
                        targetPerm.setCounterCount(CounterType.LOYALTY,
                                targetPerm.getCounterCount(CounterType.LOYALTY) - effectiveDamage);
                    }
                    if (targetPerm.getCard().hasType(CardType.BATTLE)) {
                        targetPerm.setCounterCount(CounterType.DEFENSE,
                                targetPerm.getCounterCount(CounterType.DEFENSE) - effectiveDamage);
                        battleDefeatSupport.checkAfterDefenseRemoved(gameData, targetPerm);
                    }
                    gameData.recordDamageRecipientBySource(redirect.damageSourceId(), targetPerm.getId());
                    boolean isCreature = gameQueryService.isCreature(gameData, targetPerm);
                    if (isCreature
                            || (!targetPerm.getCard().hasType(CardType.PLANESWALKER)
                            && !targetPerm.getCard().hasType(CardType.BATTLE))) {
                        // Record only — the state-based action check (CR 704.5g) performs any
                        // destruction once the current damage event finishes.
                        targetPerm.addMarkedDamage(redirect.damageSourceId(), effectiveDamage);
                        gameData.recordDamageToPermanent(targetPerm.getId(), effectiveDamage);
                    }
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

            // Divided damage is "any target" damage, so a permanent that is not a creature,
            // planeswalker or battle (CR 115.4) is an illegal target and isn't affected.
            if (!targetIsPlayer && !isAnyTargetDamageRecipient(gameData, targetPermanent)) {
                continue;
            }

            if (targetIsPlayer) {
                dealDamageToPlayer(gameData, tempEntry, targetId, rawDamage);
            } else {
                if (!(gameQueryService.isDamagePreventable(gameData) && gameQueryService.hasProtectionFromDamageSource(gameData, targetPermanent, sourceCard, tempEntry.getControllerId()))) {
                    dealCreatureDamage(gameData, tempEntry, targetPermanent, rawDamage);
                } else {
                    gameLogService.append(gameData, GameLog.cardTextCard(sourceCard,
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
    public void accumulateSourceDamageForReflection(GameData gameData, Card sourceCard, UUID sourceControllerId,
                                                    UUID sourcePermanentId, int damage) {
        accumulateSourceDamageForReflection(gameData, sourceCard, sourceControllerId, sourcePermanentId,
                damage, null, null);
    }

    public void accumulateSourceDamageForReflection(GameData gameData, Card sourceCard, UUID sourceControllerId,
                                                    UUID sourcePermanentId, int damage, UUID damagedPlayerId) {
        accumulateSourceDamageForReflection(gameData, sourceCard, sourceControllerId, sourcePermanentId,
                damage, damagedPlayerId, null);
    }

    public void accumulateSourceDamageForReflection(GameData gameData, Card sourceCard, UUID sourceControllerId,
                                                    UUID sourcePermanentId, int damage, UUID damagedPlayerId,
                                                    UUID damagedPermanentControllerId) {
        if (damage <= 0 || sourceCard == null || sourceControllerId == null) return;
        PendingSourceDamage batch = gameData.pendingSourceDamageForReflection.get(sourceCard.getId());
        if (batch == null) {
            gameData.pendingSourceDamageForReflection.put(sourceCard.getId(),
                    new PendingSourceDamage(sourceCard, sourceControllerId, sourcePermanentId, damage,
                            damagedPlayerId, damagedPermanentControllerId));
        } else {
            batch.add(damage, damagedPlayerId, damagedPermanentControllerId);
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
                    batch.getSourceCard(), batch.getControllerId(), batch.getSourcePermanentId(), batch.getAmount(),
                    batch.getDamageToPlayers());
            Set<UUID> damagedPlayers = new LinkedHashSet<>(batch.getDamageToPlayers().keySet());
            damagedPlayers.addAll(batch.getDamageToPermanentControllers());
            triggerCollectionService.checkOpponentSourceDamageToYouOrYourPermanentTriggers(
                    gameData, batch.getSourceCard(), batch.getControllerId(), batch.getSourcePermanentId(),
                    damagedPlayers);
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
    /**
     * Glacial Chasm: "Prevent all damage that would be dealt to you." Returns how much of the
     * damage aimed at {@code playerId} is prevented (all of it, when they control a permanent with
     * {@link PreventAllDamageToControllerEffect} and the damage is preventable). Effects flagged
     * {@code onlyDuringControllersTurn} (Personal Sanctuary) apply only while that player is the
     * active player.
     */
    public int applyControllerAllDamagePrevention(GameData gameData, UUID playerId, int damage) {
        if (!gameQueryService.isDamagePreventable(gameData)) return 0;
        if (damage <= 0) return 0;

        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return 0;

        boolean controllersTurn = playerId.equals(gameData.activePlayerId);
        boolean hasEffect = battlefield.stream().anyMatch(p ->
                p.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(e -> isControllerAllDamagePreventionActive(
                                gameData, playerId, p, e, controllersTurn)));
        return hasEffect ? damage : 0;
    }

    private boolean isControllerAllDamagePreventionActive(GameData gameData, UUID playerId,
                                                            Permanent source, CardEffect effect,
                                                            boolean controllersTurn) {
        if (effect instanceof PreventAllDamageToControllerEffect prevent) {
            return (!prevent.onlyDuringControllersTurn() || controllersTurn);
        }
        if (effect instanceof ConditionalEffect conditional
                && conditionEvaluationService.isMet(gameData, conditional.condition(),
                        ConditionContext.forStaticEffect(source, playerId))) {
            return isControllerAllDamagePreventionActive(
                    gameData, playerId, source, conditional.wrapped(), controllersTurn);
        }
        return false;
    }

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
     * Replaces damage to a Nefarious Lich controller with an exact graveyard exile. If the full
     * amount cannot be exiled, the replacement still removes the damage event and makes the player
     * lose the game.
     */
    public int applyNefariousLichReplacement(GameData gameData, UUID playerId, int damage) {
        if (damage <= 0) return 0;

        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return 0;

        boolean hasEffect = battlefield.stream().anyMatch(p ->
                p.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(NefariousLichDamageReplacementEffect.class::isInstance));
        if (!hasEffect) return 0;

        if (!graveyardService.exileExactlyCardsFromGraveyard(gameData, playerId, damage)) {
            if (gameOutcomeService.resolveLoss(gameData, playerId, LossReason.EFFECT) == LossOutcome.LOSES) {
                UUID winnerId = gameQueryService.getOpponentId(gameData, playerId);
                gameLogService.append(gameData, GameLog.text(gameData.playerIdToName.get(playerId)
                        + " can't exile enough cards from their graveyard and loses the game."));
                gameOutcomeService.declareWinner(gameData, winnerId);
            }
        }
        return damage;
    }

    /**
     * Replaces damage to any player while Crumbling Sanctuary is on the battlefield with exiling
     * cards from that player's library. The replacement removes the whole damage event, even when
     * the library contains fewer cards than the damage amount.
     */
    public int applyCrumblingSanctuaryReplacement(GameData gameData, UUID playerId, int damage) {
        if (damage <= 0) return 0;

        boolean hasEffect = gameData.playerBattlefields.values().stream()
                .flatMap(Collection::stream)
                .anyMatch(permanent -> permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(CrumblingSanctuaryDamageReplacementEffect.class::isInstance));
        if (!hasEffect) return 0;

        List<Card> library = gameData.playerDecks.get(playerId);
        int exiled = 0;
        if (library != null) {
            while (exiled < damage && !library.isEmpty()) {
                gameData.addToExile(playerId, library.removeFirst());
                exiled++;
            }
        }
        return damage;
    }

    /**
     * Soul Echo: while the targeted opponent has chosen it, "for each 1 damage that would be dealt to
     * you until your next upkeep, you remove an echo counter from this enchantment instead". Returns
     * how much of {@code damage} was replaced this way — one echo counter per 1 damage, capped by the
     * counters actually available across the player's armed Soul Echoes; any excess damage is dealt
     * normally. This is a replacement, not prevention, so it is not gated on
     * {@code isDamagePreventable}. Shared by the noncombat ({@link #dealDamageToPlayer}) and combat
     * ({@code CombatDamageService.applyPlayerDamage}) paths.
     */
    public int applySoulEchoCounterRemoval(GameData gameData, UUID playerId, int damage) {
        if (damage <= 0) return 0;

        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return 0;

        int replaced = 0;
        for (Permanent permanent : battlefield) {
            if (replaced >= damage) break;
            if (!permanent.isEchoDamageRedirectionActive()) continue;

            int available = permanent.getCounterCount(CounterType.ECHO);
            if (available <= 0) continue;

            int removed = Math.min(available, damage - replaced);
            permanent.setCounterCount(CounterType.ECHO, available - removed);
            replaced += removed;

            gameLogService.append(gameData, GameLog.textCardText(
                    gameData.playerIdToName.get(playerId) + " removes " + removed + " echo counter"
                            + (removed == 1 ? "" : "s") + " from ", permanent.getCard(),
                    " instead of taking " + removed + " damage."));
        }
        return replaced;
    }

    /**
     * Delaying Shield replaces damage to its controller with delay counters. This is a replacement,
     * not prevention, so it still applies when damage cannot be prevented and still replaces the
     * damage if a counter-placement restriction means no counters can actually be added.
     */
    public int applyDelayingShieldCounterReplacement(GameData gameData, UUID playerId, int damage) {
        if (damage <= 0) return 0;

        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return 0;

        for (Permanent permanent : battlefield) {
            boolean hasEffect = permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(e -> e instanceof DelayingShieldDamageReplacementEffect);
            if (!hasEffect) continue;

            if (!gameQueryService.cantHaveCounters(gameData, permanent)) {
                permanent.setCounterCount(CounterType.DELAY,
                        permanent.getCounterCount(CounterType.DELAY) + damage);
                gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(),
                        " gets " + damage + " delay counter" + (damage == 1 ? "" : "s") + " instead of damage."));
            }
            return damage;
        }
        return 0;
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

    /**
     * Object id used for per-source marked-damage tracking: the dealing permanent when known,
     * otherwise the spell/ability card instance (each cast is a distinct source).
     */
    private static UUID damageSourceKey(StackEntry entry, Permanent damageSource) {
        if (damageSource != null) {
            return damageSource.getId();
        }
        if (entry.getSourcePermanentId() != null) {
            return entry.getSourcePermanentId();
        }
        return entry.getCard().getId();
    }

}
