package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.model.AskedLifeLossDeparture;
import com.github.laxika.magicalvibes.model.replacement.DamagePortion;
import com.github.laxika.magicalvibes.model.DepartureBatch;
import com.github.laxika.magicalvibes.model.DepartureIntent;
import com.github.laxika.magicalvibes.service.CreatureDamageStep;
import com.github.laxika.magicalvibes.service.battlefield.DepartureService;
import com.github.laxika.magicalvibes.service.effect.MaroGoneNutsSupport;
import com.github.laxika.magicalvibes.service.effect.TextChangeTransformer;
import com.github.laxika.magicalvibes.model.SaturatingMath;
import com.github.laxika.magicalvibes.model.effect.ReduceSpellDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GlobalDamageMultiplyingEffect;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ChoiceContext;
import com.github.laxika.magicalvibes.model.replacement.DamageEvent;
import com.github.laxika.magicalvibes.service.replacement.DamageJournalService;
import com.github.laxika.magicalvibes.service.effect.replacement.damage.RedirectedDamageRules;
import com.github.laxika.magicalvibes.service.replacement.ReplacementOrderService;

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
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.GraveyardTargetOperationState;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.DamageRedirectShield;
import com.github.laxika.magicalvibes.model.SourceDamageRedirectShield;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.PendingLifelinkGain;
import com.github.laxika.magicalvibes.model.ResolutionDamageEvent;
import com.github.laxika.magicalvibes.model.replacement.PendingSimultaneousDamage;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageDealtByEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToAndByEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToControllerAndExileFromGraveyardEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToControllerAndMillEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.DamageToControllerCounterReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CrumblingSanctuaryDamageReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.DralnuDamageReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.NefariousLichDamageReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectChosenColorSpellDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectAllCreatureDamageToControllerEffect;
import com.github.laxika.magicalvibes.model.PendingSourceDamage;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.TextReplacement;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.event.VisualCueType;
import com.github.laxika.magicalvibes.service.event.VisualCueService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
import org.springframework.stereotype.Component;

import java.util.*;
import java.util.function.Predicate;
import java.util.function.Function;
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
    @org.springframework.beans.factory.annotation.Autowired @org.springframework.context.annotation.Lazy
    private InteractionHandlerRegistry interactionHandlerRegistry;
    @org.springframework.beans.factory.annotation.Autowired @org.springframework.context.annotation.Lazy
    private com.github.laxika.magicalvibes.service.state.StateTriggerService stateTriggerService;

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
    private final ObjectProvider<DestructionSupport> destructionSupportProvider;
    private final TapUntapSupport tapUntapSupport;
    private final ReplacementOrderService replacementOrderService;
    private final DamageJournalService damageJournalService;
    private final ObjectProvider<com.github.laxika.magicalvibes.service.combat.CombatDamageService> combatDamageService;
    private final ObjectProvider<com.github.laxika.magicalvibes.service.input.PlayerInputService> playerInputService;
    private final VisualCueService visualCueService;
    private final ObjectProvider<DepartureService> departureService;

    /**
     * Whether a creature source's damage is dealt by another object, such as the Equipment whose granted
     * ability is resolving (Razor Boomerang). The creature's own damage triggers (an Aura's "enchanted
     * creature deals damage") don't apply to that damage.
     */
    private static boolean damageComesFromOtherObject(StackEntry entry, Permanent sourcePermanent) {
        return sourcePermanent != null && sourcePermanent.getCard().hasType(CardType.CREATURE)
                && entry.getDamageSourceCard() != null
                && entry.getDamageSourceCard() != sourcePermanent.getCard();
    }

    /** Colours of a non-permanent damage source (spell/ability card), for colour-based prevention. */
    private static Set<CardColor> sourceCardColors(Card card) {
        if (card == null) return Set.of();
        Set<CardColor> colors = new HashSet<>(card.getColors());
        if (card.getColor() != null) colors.add(card.getColor());
        return colors;
    }

    private void recordRedSourceNoncombatDamage(GameData gameData, Card sourceCard,
                                                Permanent sourcePermanent, UUID controllerId, int amount) {
        Set<CardColor> sourceColors = sourcePermanent == null
                ? sourceCardColors(sourceCard)
                : gameQueryService.getEffectiveColors(gameData, sourcePermanent);
        gameData.recordRedSourceNoncombatDamage(controllerId,
                gameQueryService.getDamageSourceColors(gameData, sourceColors), amount);
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
        boolean targetProtected = target.isDamageCantBePreventedOrRedirectedThisTurn();
        boolean unpreventable = targetProtected
                || gameData.damageCantBePreventedThisTurn || gameData.unpreventableDamageInProgress
                || damageSource != null && gameQueryService.damageCantBePreventedFromSource(gameData, damageSource);
        DamageEvent event = DamageEvent.toPermanent(entry,
                gameQueryService.findPermanentController(gameData, target.getId()), target.getId(), damageSource,
                Math.max(0, rawDamage), unpreventable, cantBeRedirected || targetProtected);
        return processDamageEvent(gameData, event);
    }

    /**
     * Deals {@code event}'s damage to its permanent once every replacement and prevention effect
     * has been applied: loyalty or defense removal, marked damage or counters, the damage records
     * and the triggers damage causes (CR 120.3). Returns the damage dealt.
     */
    private int finishPermanentDamage(GameData gameData, DamageEvent event) {
        Permanent target = gameQueryService.findPermanentById(gameData, event.recipientPermanentId());
        if (target == null) {
            return 0;
        }
        StackEntry entry = event.entry();
        Permanent damageSource = event.explicitSource();
        int damage = event.amount();
        UUID targetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
        Permanent sourcePermanentForBonus = damageSource != null
                ? damageSource
                : (entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId()));
        UUID sourceControllerId = gameQueryService.findDamageSourceController(
                gameData, entry, sourcePermanentForBonus);
        Permanent effectiveDamageSource = sourcePermanentForBonus;
        UUID sourcePermId = damageSource != null ? damageSource.getId() : entry.getSourcePermanentId();
        damagePreventionService.applyDamageHealingReplacement(gameData, target, damage);

        UUID damageSourceControllerId = damageSource != null
                ? gameQueryService.findDamageSourceController(gameData, entry, damageSource)
                : entry.getControllerId();
        if (gameQueryService.isCreature(gameData, target)
                && gameQueryService.noncombatDamageToOpponentCreatureAsCounters(
                        gameData, damageSourceControllerId, targetControllerId)) {
            placeDamageMinusOneCounters(gameData, entry, target, damage, damageSourceControllerId,
                    damageSource != null ? damageSource.getCard() : entry.getCard());
            processPendingRedirectDamage(gameData);
            return 0;
        }

        if (damage > 0) {
            visualCueService.emit(gameData, VisualCueType.DAMAGE_TO_PERMANENT,
                    damageCueSource(gameData, entry, damageSource), target.getId(), null, damage);
        }

        if (damageSource != null) {
            graveyardService.recordCreatureDamagedByPermanent(gameData, damageSource.getId(), target, damage);
        } else if (entry.getSourcePermanentId() != null) {
            graveyardService.recordCreatureDamagedByPermanent(gameData, entry.getSourcePermanentId(), target, damage);
        } else {
            graveyardService.recordCreatureDamagedBySource(
                    gameData, damageSourceKey(entry, null), target, damage);
            graveyardService.recordCreatureDamagedByController(gameData, entry.getControllerId(), target);
        }

        // Fire ON_DEALT_DAMAGE triggers (e.g. Nested Ghoul, Phyrexian Obliterator)
        if (damage > 0) {
            recordRedSourceNoncombatDamage(gameData,
                    damageSource != null ? damageSource.getCard() : entry.getEffectiveDamageSourceCard(),
                    sourcePermanentForBonus, sourceControllerId, damage);
            gameData.recordNoncombatDamageToPermanent(target.getId(), damage);
            recordDamageToPermanent(gameData, target.getId(), damage, entry, effectiveDamageSource);
            recordExcessDamageToCreatureIfAny(gameData, entry, target, damageSource, damage);
            if (damageSource == null) {
                recordSorcerySpellDamage(gameData, entry, damage);
            }
            triggerCollectionService.checkAnyPermanentDealtDamageTriggers(gameData, target, damage);
            if (entry.getEntryType() == StackEntryType.INSTANT_SPELL
                    || entry.getEntryType() == StackEntryType.SORCERY_SPELL) {
                gameData.recordQualifyingDamageControllerToPermanent(target.getId(), sourceControllerId);
            }
            gameData.recordDamageDealtBySource(
                    damageSourceKey(entry, damageSource), damage);
            UUID sourceId = damageSource != null ? damageSource.getId() : entry.getSourcePermanentId();
            gameData.recordDamageSourceControlledBy(
                    sourceId != null ? sourceId : entry.getCard().getId(), sourceControllerId);
            gameData.recordDamageRecipientBySource(sourcePermId, target.getId());

            accumulateSourceDamageForReflection(gameData,
                    damageSource != null ? damageSource.getCard() : entry.getEffectiveDamageSourceCard(),
                    sourceControllerId,
                    damageSource != null ? damageSource.getId() : entry.getSourcePermanentId(), damage,
                    null, targetControllerId, target.getId(), entry);
            triggerCollectionService.checkDelayedWatchedCreatureDealtDamageByAttackingCreatureTriggers(
                    gameData, effectiveDamageSource, target, damage);
            triggerCollectionService.checkDealtDamageToCreatureTriggers(
                    gameData, target, damage, sourceControllerId,
                    damageSource != null ? damageSource.getCard() : entry.getEffectiveDamageSourceCard(),
                    damageSource != null ? damageSource.getId() : entry.getSourcePermanentId());
            triggerCollectionService.checkNoncombatDamageToSelfTriggers(
                    gameData, target, damage, sourceControllerId,
                    damageSource != null ? damageSource.getCard() : entry.getEffectiveDamageSourceCard(),
                    damageSource != null ? damageSource.getId() : entry.getSourcePermanentId());
            triggerCollectionService.checkAllySourceDealtNoncombatDamageToCreatureTriggers(
                    gameData, sourceControllerId, target, damage);

            UUID damagedCreatureControllerId = gameQueryService.findPermanentController(gameData, target.getId());
            Permanent reflectionSource = damageSource != null
                    ? damageSource
                    : (sourcePermId != null ? gameQueryService.findPermanentById(gameData, sourcePermId) : null);
            // A source that left the battlefield before this damage still dealt it (CR 608.2h last-known
            // information), so reflection watchers see the activation-time snapshot.
            if (reflectionSource == null) {
                reflectionSource = entry.getSourcePermanentSnapshot();
            }

            if (gameQueryService.isPlaneswalker(gameData, target)) {
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
                    triggerCollectionService.checkTemporaryGlobalOpponentCreatureDealtDamageTriggers(
                            gameData, target, damagedCreatureControllerId, damage);
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
        boolean planeswalker = gameQueryService.isPlaneswalker(gameData, target);
        boolean toughnessAsLoyalty = gameQueryService.isToughnessAsLoyaltyPermanent(gameData, target);
        int excessDamage = planeswalker
                ? Math.max(0, damage - (toughnessAsLoyalty
                ? gameQueryService.getEffectiveToughness(gameData, target)
                : target.getCounterCount(CounterType.LOYALTY)))
                : computeExcessDamageToCreature(gameData, target, damage,
                        target.getMarkedDamage(), sourceHasDeathtouch);
        if (excessDamage > 0) {
            triggerCollectionService.checkOpponentPermanentDealtExcessDamageTriggers(
                    gameData, target, targetControllerId, excessDamage, !event.combat());
            triggerCollectionService.checkOpponentCreatureDealtExcessNoncombatDamageTriggers(
                    gameData, entry, target, targetControllerId, excessDamage);
        }

        // CR 120.3c — damage dealt to a planeswalker removes that many loyalty counters
        // (the SBA check reaps it at 0 loyalty). A permanent that is also a creature
        // additionally gets the damage marked below (CR 120.3e).
        if (planeswalker) {
            if (damage > 0) {
                if (toughnessAsLoyalty) {
                    int toughness = gameQueryService.getToughnessAsLoyalty(target);
                    target.setToughnessAsLoyalty(toughness - damage);
                } else {
                    int loyaltyCounterRemoval = gameQueryService.applyPlaneswalkerLoyaltyDamageReplacement(
                            gameData, target, damage);
                    target.setCounterCount(CounterType.LOYALTY,
                            target.getCounterCount(CounterType.LOYALTY) - loyaltyCounterRemoval);
                }
                queueEnchantedCreatureDealsDamageTrigger(gameData, entry, damageSource, damage);
                gameLogService.append(gameData, GameLog.cardTextCard(sourceCard,
                        " deals " + damage + " damage to ", target.getCard(),
                        toughnessAsLoyalty
                                ? " (" + gameQueryService.getEffectiveToughness(gameData, target)
                                + " toughness remaining)."
                                : " (" + target.getCounterCount(CounterType.LOYALTY)
                                + " loyalty remaining)."));
            }
            if (!gameQueryService.isCreature(gameData, target)) {
                if (damage > 0) {
                    checkSpellLifelink(gameData, entry, damage);
                }
                processPendingRedirectDamage(gameData);
                return damage;
            }
            if (toughnessAsLoyalty) {
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
        boolean dealsCounterDamage = gameQueryService.sourceDealsCounterDamageToCreatures(gameData, entry, damageSource);

        if (dealsCounterDamage) {
            placeDamageMinusOneCounters(gameData, entry, target, damage, damageSourceControllerId, sourceCard);
            if (damage > 0) {
                entry.recordPermanentDealtDamage(target.getId());
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
        if (damage > 0) {
            entry.recordPermanentDealtDamage(target.getId());
        }
        if (sourceHasDeathtouch) {
            target.setDamagedByDeathtouch(true);
        }

        gameLogService.append(gameData, GameLog.cardTextCard(sourceCard,
                " deals " + damage + " damage to ", target.getCard(), "."));
        log.info("Game {} - {} deals {} damage to {}", gameData.id, sourceName, damage, target.getCard().getName());

        if (damage > 0) {
            if (damageSource != null) {
                checkPermanentLifelink(gameData, entry, damageSource, damage);
            } else {
                checkSpellLifelink(gameData, entry, damage);
            }
            queueEnchantedCreatureDealsDamageTrigger(gameData, entry, damageSource, damage);
        }
        processPendingRedirectDamage(gameData);
        return damage;
    }

    /** Places counters for counter damage or a replacement that puts counters instead of dealing damage. */
    private void placeDamageMinusOneCounters(GameData gameData, StackEntry entry, Permanent target,
                                            int damage, UUID controllerId, Card sourceCard) {
        if (damage <= 0 || gameQueryService.cantHaveCounters(gameData, target)
                || gameQueryService.cantHaveMinusOneMinusOneCounters(gameData, target)) return;
        int counters = gameQueryService.reduceMinusOneMinusOneCounters(gameData, target, damage);
        if (counters <= 0) return;
        target.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE,
                target.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) + counters);
        permanentCounterSupport.notifyCountersPlaced(gameData, entry, target, counters, CounterType.MINUS_ONE_MINUS_ONE);
        gameLogService.append(gameData, GameLog.cardTextCard(sourceCard,
                " puts " + counters + " -1/-1 counters on ", target.getCard(), "."));
        log.info("Game {} - {} puts {} -1/-1 counters on {}", gameData.id,
                sourceCard.getName(), counters, target.getCard().getName());
        permanentCounterSupport.fireMinusOneMinusOneCounterPutOnCreatureTriggers(gameData, target, counters, controllerId);
    }

    /**
     * Deals damage that can't be prevented to a creature (e.g. Combust): its replacement effects
     * still apply in the order its controller chooses, its prevention effects don't. Returns the
     * damage dealt.
     */
    public int dealCreatureDamageUnpreventable(GameData gameData, StackEntry entry, Permanent target, int rawDamage) {
        return processDamageEvent(gameData, DamageEvent.toPermanent(entry,
                gameQueryService.findPermanentController(gameData, target.getId()), target.getId(), null,
                Math.max(0, rawDamage), true, target.isDamageCantBePreventedOrRedirectedThisTurn()));
    }

    private void recordDamageToPermanent(GameData gameData, UUID targetId, int amount,
                                         StackEntry entry, Permanent damageSource) {
        UUID sourceId = damageSource == null
                ? entry == null ? null : entry.getSourcePermanentId()
                : damageSource.getId();
        if (sourceId == null && entry != null && entry.getCard() != null) {
            sourceId = entry.getCard().getId();
        }
        Card sourceCard = damageSource != null
                ? damageSource.getCard()
                : entry == null ? null : entry.getEffectiveDamageSourceCard();
        String sourceName = damageSource != null
                ? gameQueryService.getEffectiveName(gameData, damageSource)
                : sourceCard == null ? null : sourceCard.getName();
        UUID sourceControllerId = damageSource != null
                ? gameQueryService.findDamageSourceController(gameData, entry, damageSource)
                : entry != null && entry.getSourcePermanentId() != null
                        ? gameQueryService.findPermanentController(gameData, entry.getSourcePermanentId())
                        : entry == null ? null : entry.getControllerId();
        if (sourceControllerId == null && entry != null) sourceControllerId = entry.getControllerId();
        gameData.recordDamageToPermanentFromSource(targetId, amount, sourceId, sourceName, sourceControllerId);
        boolean qualifyingSpell = entry != null
                && entry.getEntryType() != com.github.laxika.magicalvibes.model.StackEntryType.ACTIVATED_ABILITY
                && entry.getEntryType() != com.github.laxika.magicalvibes.model.StackEntryType.TRIGGERED_ABILITY;
        boolean qualifyingCreature = damageSource != null
                && (gameQueryService.hasEffectiveSubtype(gameData, damageSource,
                com.github.laxika.magicalvibes.model.CardSubtype.GIANT)
                || gameQueryService.hasEffectiveSubtype(gameData, damageSource,
                com.github.laxika.magicalvibes.model.CardSubtype.WIZARD));
        if (amount > 0 && (qualifyingSpell || qualifyingCreature)) {
            gameData.recordQualifyingDamageControllerToPermanent(targetId, sourceControllerId);
        }
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        if (target != null && gameQueryService.isCreature(gameData, target)) {
            gameData.recordDamageDealtToCreatureBySource(sourceId, targetId);
            if (amount > 0 && entry != null && entry.isExilesCreaturesDamaged()) {
                target.setExileInsteadOfDieThisTurn(true);
            }
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
     * Applies lifelink for the spell or permanent that dealt the damage, using the permanent's
     * current controller when it is still on the battlefield. While a spell or ability resolves,
     * the gain is added to that source's pending gain instead, so the damage one source deals in one
     * damage event is a single life-gain event; {@link #beginDamageEvent} and
     * {@link #endDamageEvent} apply it when that event ends.
     */
    public void checkSpellLifelink(GameData gameData, StackEntry entry, int effectiveDamage) {
        if (effectiveDamage <= 0) return;
        if (!gameQueryService.shouldControllerSpellHaveLifelink(gameData, entry)) return;
        UUID controllerId = entry.getSourcePermanentId() == null ? null
                : gameQueryService.findPermanentController(gameData, entry.getSourcePermanentId());
        if (controllerId == null) controllerId = entry.getDamageSourceControllerId();
        UUID gainingPlayerId = controllerId != null ? controllerId : entry.getControllerId();
        Card sourceCard = entry.getEffectiveDamageSourceCard();
        UUID sourceId = entry.getSourcePermanentId() != null ? entry.getSourcePermanentId()
                : sourceCard != null ? sourceCard.getId() : null;
        queueLifelinkGain(gameData, entry, gainingPlayerId, sourceId, sourceCard, effectiveDamage);
    }

    /**
     * Applies lifelink for a permanent that dealt damage as an explicit damage source while the
     * spell or ability {@code entry} resolves (CR 702.15b), such as a creature the spell chose to
     * deal damage. The permanent's own keywords decide, and its controller gains the life.
     */
    private void checkPermanentLifelink(GameData gameData, StackEntry entry, Permanent damageSource, int damage) {
        if (damage <= 0 || !gameQueryService.sourceHasKeyword(gameData, entry, damageSource, Keyword.LIFELINK)) {
            return;
        }
        UUID controllerId = gameQueryService.findPermanentController(gameData, damageSource.getId());
        if (controllerId == null) controllerId = entry.lastKnownPermanentController(damageSource.getId());
        if (controllerId == null) return;
        queueLifelinkGain(gameData, entry, controllerId, damageSource.getId(), damageSource.getCard(), damage);
    }

    private void queueLifelinkGain(GameData gameData, StackEntry entry, UUID gainingPlayerId, UUID sourceId,
                                   Card sourceCard, int amount) {
        UUID sourceControllerId = lifeSupport.resolveSpellOrAbilityControllerId(gameData);
        if (gameData.effectResolutionDepth == 0 || sourceId == null) {
            lifeSupport.applyGainLife(gameData, gainingPlayerId, amount, "lifelink",
                    sourceCard, entry.getEntryType(), sourceControllerId);
            return;
        }
        List<PendingLifelinkGain> pending = gameData.pendingLifelinkGains;
        for (int i = 0; i < pending.size(); i++) {
            if (pending.get(i).isSameEvent(sourceId, gainingPlayerId)) {
                pending.set(i, pending.get(i).plus(amount));
                return;
            }
        }
        pending.add(new PendingLifelinkGain(sourceId, gainingPlayerId, amount, sourceCard,
                entry.getEntryType(), sourceControllerId));
    }

    /**
     * Opens the damage event of the effect at {@code index} of {@code entry}, which is about to
     * resolve. When that is a new event, the open one ends first ({@link #endOpenDamageEvent}).
     * Returns {@code false} when ending it raised a prompt; the resolution then waits and comes
     * back to this effect.
     */
    public boolean beginDamageEvent(GameData gameData, StackEntry entry, int index) {
        ResolutionDamageEvent event = new ResolutionDamageEvent(entry.getTargetableId(), entry.damageEventAt(index));
        if (event.equals(gameData.openDamageEvent)) return true;
        if (!endOpenDamageEvent(gameData)) return false;
        gameData.playerSourceShieldsUsedInOpenEvent.clear();
        gameData.openDamageEvent = event;
        return true;
    }

    /**
     * Ends the open damage event: deals the damage its simultaneous steps held, combined per
     * recipient and source, then gains its lifelink. Returns {@code false} while a prompt the
     * combined damage raised waits for its player.
     */
    public boolean endOpenDamageEvent(GameData gameData) {
        if (!dealSimultaneousDamage(gameData)) return false;
        flushLifelinkGains(gameData);
        return true;
    }

    /** Ends the last damage event when the outermost resolution finishes, gaining its lifelink. */
    public void endDamageEvent(GameData gameData) {
        flushLifelinkGains(gameData);
        gameData.sourceRedirectShieldsUsedInOpenEvent.clear();
        gameData.playerSourceShieldsUsedInOpenEvent.clear();
        gameData.openDamageEvent = null;
    }

    /**
     * Deals the damage the open event's steps held. Damage one source would deal one recipient is
     * one event, so a replacement or prevention effect applies to the total once. A prompt is
     * answered like one an effect raises: this runs again and replays what was already dealt.
     */
    private boolean dealSimultaneousDamage(GameData gameData) {
        ResolutionDamageEvent open = gameData.openDamageEvent;
        if (open == null) return true;
        List<PendingSimultaneousDamage> held = gameData.pendingSimultaneousDamage.stream()
                .filter(pending -> pending.event().equals(open))
                .toList();
        if (held.isEmpty()) return true;
        damageJournalService.beginSimultaneousDamage(gameData, open);
        try {
            for (PendingSimultaneousDamage combined : combineSimultaneousDamage(held)) {
                StackEntry entry = combined.damage().entry();
                boolean previousExiles = entry.isExilesCreaturesDamaged();
                entry.setExilesCreaturesDamaged(combined.exilesDamagedCreatures());
                try {
                    processDamageEvent(gameData, combined.damage());
                } finally {
                    entry.setExilesCreaturesDamaged(previousExiles);
                }
            }
        } finally {
            damageJournalService.endEffect(gameData);
        }
        if (gameData.damageJournal != null) return false;
        gameData.pendingSimultaneousDamage.removeAll(held);
        gameData.sourceRedirectShieldsUsedInOpenEvent.clear();
        gameData.playerSourceShieldsUsedInOpenEvent.clear();
        return true;
    }

    private List<PendingSimultaneousDamage> combineSimultaneousDamage(List<PendingSimultaneousDamage> held) {
        List<PendingSimultaneousDamage> combined = new ArrayList<>();
        for (PendingSimultaneousDamage pending : held) {
            int match = -1;
            for (int i = 0; i < combined.size() && match < 0; i++) {
                if (combined.get(i).exilesDamagedCreatures() == pending.exilesDamagedCreatures()
                        && isSameRecipientAndSource(combined.get(i).damage(), pending.damage())) {
                    match = i;
                }
            }
            if (match < 0) {
                combined.add(pending);
            } else {
                PendingSimultaneousDamage first = combined.get(match);
                combined.set(match, new PendingSimultaneousDamage(first.event(), first.stepIndex(),
                        first.damage().withAmount(first.damage().amount() + pending.damage().amount()),
                        first.exilesDamagedCreatures()));
            }
        }
        return combined;
    }

    private boolean isSameRecipientAndSource(DamageEvent first, DamageEvent second) {
        UUID firstSource = first.explicitSource() == null ? null : first.explicitSource().getId();
        UUID secondSource = second.explicitSource() == null ? null : second.explicitSource().getId();
        return first.entry() == second.entry()
                && Objects.equals(firstSource, secondSource)
                && Objects.equals(first.recipientPermanentId(), second.recipientPermanentId())
                && (first.toPermanent() || first.recipientPlayerId().equals(second.recipientPlayerId()))
                && first.unpreventable() == second.unpreventable()
                && first.cantBeRedirected() == second.cantBeRedirected();
    }

    private void flushLifelinkGains(GameData gameData) {
        if (gameData.pendingLifelinkGains.isEmpty()) return;
        List<PendingLifelinkGain> gains = new ArrayList<>(gameData.pendingLifelinkGains);
        gameData.pendingLifelinkGains.clear();
        for (PendingLifelinkGain gain : gains) {
            lifeSupport.applyGainLife(gameData, gain.playerId(), gain.amount(), "lifelink",
                    gain.sourceCard(), gain.sourceEntryType(), gain.sourceControllerId());
        }
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

    public void gainLifeForTargetSpellDamage(GameData gameData, StackEntry entry, int damage) {
        if (damage <= 0) return;
        var shield = gameQueryService.getTargetSpellDamagePreventionShield(gameData, entry);
        if (shield != null && shield.lifeGainPlayerId() != null) {
            lifeSupport.applyGainLife(gameData, shield.lifeGainPlayerId(), damage, "prevented spell damage");
        }
    }

    public int resolveCreatureTargetDamage(GameData gameData, StackEntry entry, int damage) {
        Permanent target = gameQueryService.findPermanentById(gameData, entry.getTargetId());
        if (target == null) return 0;
        if (isDamagePreventedForCreature(gameData, entry, target)) return 0;
        return dealCreatureDamage(gameData, entry, target, damage);
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
        int lethalDamageThreshold = gameQueryService.getLethalDamageThreshold(gameData, target);
        int lethalNeeded = Math.max(0, lethalDamageThreshold - markedDamageBefore);
        return Math.max(0, damageDealt - lethalNeeded);
    }

    /**
     * Excess damage dealt to a permanent targeted as a creature. A creature planeswalker has both excess
     * over its toughness and excess over its loyalty; the greater of the two is the excess dealt.
     */
    public int computeExcessDamageToTargetedPermanent(GameData gameData, Permanent target, int damageDealt,
                                                      int markedDamageBefore, int loyaltyBefore,
                                                      boolean sourceHasDeathtouch) {
        int excess = gameQueryService.isCreature(gameData, target)
                ? computeExcessDamageToCreature(gameData, target, damageDealt, markedDamageBefore,
                sourceHasDeathtouch)
                : 0;
        if (gameQueryService.isPlaneswalker(gameData, target)) {
            boolean toughnessAsLoyalty = gameQueryService.isToughnessAsLoyaltyPermanent(gameData, target);
            int loyaltyExcess = Math.max(0, damageDealt - (toughnessAsLoyalty
                    ? gameQueryService.getEffectiveToughness(gameData, target)
                    : loyaltyBefore));
            excess = Math.max(excess, loyaltyExcess);
        }
        return excess;
    }

    private void recordExcessDamageToCreatureIfAny(GameData gameData, StackEntry entry,
                                                    Permanent target, Permanent damageSource, int damage) {
        if (damage <= 0 || !gameQueryService.isCreature(gameData, target)) {
            return;
        }
        boolean sourceHasDeathtouch = gameQueryService.sourceHasKeyword(
                gameData, entry, damageSource, Keyword.DEATHTOUCH);
        if (computeExcessDamageToCreature(gameData, target, damage,
                target.getMarkedDamage(), sourceHasDeathtouch) > 0) {
            gameData.recordExcessDamageToPermanent(target.getId());
        }
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
        return isSourcePermanentPreventedFromDealingDamage(gameData, entry, true);
    }

    /**
     * @param includeGlobalCreaturePrevention {@code false} leaves out "prevent all damage dealt by creatures"
     *        effects, which carry their own rider (Chant of Vitu-Ghazi's life gain) and are applied by their own
     *        rule rather than as a plain source prevention
     */
    public boolean isSourcePermanentPreventedFromDealingDamage(GameData gameData, StackEntry entry,
                                                               boolean includeGlobalCreaturePrevention) {
        if (!gameQueryService.isDamagePreventable(gameData) || entry.getSourcePermanentId() == null) return false;
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source == null) {
            // Damage from a source that has since left uses its last known information (CR 608.2h).
            source = entry.getSourcePermanentSnapshot();
        }
        if (source != null && entry.getDamageSourceCard() != null
                && entry.getDamageSourceCard() != source.getCard()) {
            // The ability belongs to this permanent but another object deals the damage (an Equipment
            // that granted the ability), so restrictions on the permanent's own damage don't apply.
            return false;
        }
        if (source != null && (gameQueryService.isPreventedFromDealingDamage(gameData, source)
                || gameQueryService.isDamageFromPermanentSourcePrevented(gameData, source)
                || (includeGlobalCreaturePrevention && gameQueryService.isDamageByCreaturePrevented(gameData, source))
                || gameData.isPreventedFromDealingDamage(entry.getSourcePermanentId()))) return true;
        // Defang / Heart of Light: an aura can blank all damage dealt by the enchanted permanent,
        // including damage from its own activated and triggered abilities.
        return source != null
                && (gameQueryService.hasAuraWithEffect(gameData, source,
                        effect -> effect instanceof PreventAllDamageDealtByEnchantedCreatureEffect prevented
                                && !prevented.combatOnly())
                    || gameQueryService.hasAuraWithEffect(gameData, source, PreventAllDamageToAndByEnchantedCreatureEffect.class));
    }

    public boolean isGlobalCreaturePreventionForEntry(GameData gameData, StackEntry entry) {
        if (entry.getSourcePermanentId() == null) return false;
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source != null) return gameQueryService.isDamageByCreaturePrevented(gameData, source);
        Permanent snapshot = entry.getSourcePermanentSnapshot();
        Card sourceCard = entry.getEffectiveDamageSourceCard();
        return gameQueryService.isDamagePreventable(gameData)
                && sourceCard != null && sourceCard.hasType(CardType.CREATURE)
                && (snapshot == null || !gameQueryService.damageCantBePreventedFromSource(gameData, snapshot))
                && (gameData.preventAllDamageByCreatures
                || gameData.playersWithDamageFromOpponentCreaturesPrevented.stream()
                        .anyMatch(playerId -> !playerId.equals(entry.getControllerId())));
    }

    public void applyGlobalCreaturePreventionLifeGain(GameData gameData, StackEntry entry, int damage,
                                                      UUID affectedPlayerId) {
        if (isGlobalCreaturePreventionForEntry(gameData, entry)) {
            damagePreventionService.applyAllByCreaturesPreventionLifeGain(gameData, damage, affectedPlayerId);
        }
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

    public int resolveAnyTargetDamage(GameData gameData, StackEntry entry, UUID targetId, int rawDamage, boolean cantRegenerate) {
        return resolveAnyTargetDamage(gameData, entry, targetId, rawDamage, cantRegenerate, false);
    }

    public int resolveAnyTargetDamage(GameData gameData, StackEntry entry, UUID targetId, int rawDamage,
                                       boolean cantRegenerate, boolean cantBeRedirected) {
        Card source = entry.getEffectiveDamageSourceCard();
        boolean targetIsPlayer = gameData.playerIds.contains(targetId);
        Permanent targetPermanent = targetIsPlayer ? null : gameQueryService.findPermanentById(gameData, targetId);

        if (!targetIsPlayer && targetPermanent == null) return 0;

        // Planeswalkerificate is both a creature and a planeswalker, but its damage resource is
        // toughness rather than loyalty counters. Use the creature pipeline so prevention and
        // damage triggers remain consistent while the special resource is reduced by the helper.
        if (!targetIsPlayer && gameQueryService.isToughnessAsLoyaltyPermanent(gameData, targetPermanent)) {
            return dealCreatureDamage(gameData, entry, targetPermanent, rawDamage, cantBeRedirected);
        }

        if (targetIsPlayer) {
            // Damage redirected from the player to a creature (Saving Grace) is still dealt by this
            // source, so a creature dealt damage this way can't be regenerated either.
            Map<UUID, Integer> damageBefore = cantRegenerate
                    ? new HashMap<>(gameData.damageDealtToPermanentsThisTurn)
                    : Map.of();
            UUID redirectedPlayerId = cantBeRedirected ? null
                    : damagePreventionService.applyNextInstantOrSorceryDamageRedirectShield(
                            gameData, entry, targetId, rawDamage);
            if (redirectedPlayerId != null && !redirectedPlayerId.equals(targetId)) {
                dealDamageToPlayer(gameData, entry, redirectedPlayerId, rawDamage);
                return 0;
            }
            if (isDamageSourcePreventedWithLog(gameData, entry)) return 0;
            // dealDamageToPlayer handles per-permanent prevention (permanentsPreventedFromDealingDamage)
            dealDamageToPlayer(gameData, entry, targetId, rawDamage);
            if (cantRegenerate) {
                markCreaturesDamagedSinceAsUnregenerable(gameData, damageBefore);
            }
            return 0;
        } else {
            if (!targetPermanent.isDamageCantBePreventedOrRedirectedThisTurn()
                    && isDamageSourcePreventedWithLog(gameData, entry)) return 0;
            if (!targetPermanent.isDamageCantBePreventedOrRedirectedThisTurn()
                    && gameQueryService.isDamagePreventable(gameData)
                    && (isGlobalCreaturePreventionForEntry(gameData, entry)
                        || isSourcePermanentPreventedFromDealingDamage(gameData, entry)
                        || gameQueryService.hasProtectionFromDamageSource(gameData, targetPermanent, source,
                            entry.getControllerId()))) {
                applyGlobalCreaturePreventionLifeGain(gameData, entry, rawDamage,
                        gameQueryService.findPermanentController(gameData, targetPermanent.getId()));
                gameLogService.append(gameData, GameLog.cardThen(source, "'s damage is prevented."));
                return 0;
            }
            // Planeswalkers and battles share the permanent pipeline: its result stage removes
            // loyalty (CR 120.3c) or defense counters (CR 120.3h) once every replacement and
            // prevention effect has been applied in the order the controller chose.
            Map<UUID, Integer> damageBefore = cantRegenerate
                    ? new HashMap<>(gameData.damageDealtToPermanentsThisTurn)
                    : Map.of();
            int damageDealt = dealCreatureDamage(gameData, entry, targetPermanent, rawDamage,
                    cantBeRedirected);
            if (cantRegenerate) {
                markCreaturesDamagedSinceAsUnregenerable(gameData, damageBefore);
            }
            return damageDealt;
        }
    }

    private void markCreaturesDamagedSinceAsUnregenerable(GameData gameData, Map<UUID, Integer> damageBefore) {
        gameData.damageDealtToPermanentsThisTurn.forEach((permanentId, totalDamage) -> {
            if (totalDamage > damageBefore.getOrDefault(permanentId, 0)) {
                Permanent damagedPermanent = gameQueryService.findPermanentById(gameData, permanentId);
                if (damagedPermanent != null && gameQueryService.isCreature(gameData, damagedPermanent)) {
                    damagedPermanent.setCantRegenerateThisTurn(true);
                }
            }
        });
    }

    public int computeExcessDamageToAnyTarget(int damageDealt, boolean creature, int lethalDamageThresholdBefore,
                                              int markedDamageBefore, boolean sourceHasDeathtouch,
                                              boolean planeswalker, int loyaltyBefore,
                                              boolean battle, int defenseBefore) {
        if (damageDealt <= 0) return 0;
        int lethalNeeded = Integer.MAX_VALUE;
        if (creature) {
            lethalNeeded = Math.min(lethalNeeded, sourceHasDeathtouch
                    ? 1 : Math.max(0, lethalDamageThresholdBefore - markedDamageBefore));
        }
        if (planeswalker) lethalNeeded = Math.min(lethalNeeded, Math.max(0, loyaltyBefore));
        if (battle) lethalNeeded = Math.min(lethalNeeded, Math.max(0, defenseBefore));
        return lethalNeeded == Integer.MAX_VALUE ? 0 : Math.max(0, damageDealt - lethalNeeded);
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
        damageAllCreaturesOnBattlefield(gameData, entry, damage, filter, exileInsteadOfDie,
                cantRegenerate, false);
    }

    public void damageAllCreaturesOnBattlefield(GameData gameData, StackEntry entry, int damage,
                                                Predicate<Permanent> filter, boolean exileInsteadOfDie,
                                                boolean cantRegenerate, boolean tapDamagedCreatures) {
        gameData.forEachBattlefield((playerId, battlefield) ->
                damageFilteredCreatures(gameData, entry, p -> damage, battlefield, filter,
                        exileInsteadOfDie, cantRegenerate, tapDamagedCreatures)
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
        damageAllCreaturesOnBattlefield(gameData, entry, damage, filter, exileInsteadOfDie,
                cantRegenerate, false);
    }

    public void damageAllCreaturesOnBattlefield(GameData gameData, StackEntry entry, ToIntFunction<Permanent> damage,
                                                Predicate<Permanent> filter, boolean exileInsteadOfDie,
                                                boolean cantRegenerate, boolean tapDamagedCreatures) {
        gameData.forEachBattlefield((playerId, battlefield) ->
                damageFilteredCreatures(gameData, entry, damage, battlefield, filter,
                        exileInsteadOfDie, cantRegenerate, tapDamagedCreatures)
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
        damageFilteredCreatures(gameData, entry, damage, permanents, filter, exileInsteadOfDie,
                cantRegenerate, false);
    }

    public void damageFilteredCreatures(GameData gameData, StackEntry entry, ToIntFunction<Permanent> damage,
                                        Collection<Permanent> permanents, Predicate<Permanent> filter,
                                        boolean exileInsteadOfDie, boolean cantRegenerate,
                                        boolean tapDamagedCreatures) {
        UUID damageSourceId = damageSourceKey(entry, null);
        Map<UUID, Integer> damageBefore = new HashMap<>();
        if (exileInsteadOfDie || cantRegenerate) {
            gameData.damageDealtToPermanentsBySourceThisTurn.forEach((permanentId, sources) ->
                    damageBefore.put(permanentId, sources.getOrDefault(damageSourceId, 0)));
        }
        for (Permanent p : permanents) {
            if (!filter.test(p)) continue;
            if (gameQueryService.isDamagePreventable(gameData) && gameQueryService.hasProtectionFromDamageSource(gameData, p, entry.getEffectiveDamageSourceCard(), entry.getControllerId())) continue;
            int damageDealt = dealCreatureDamage(gameData, entry, p, damage.applyAsInt(p));
            if (exileInsteadOfDie && damageDealt > 0 && gameQueryService.isCreature(gameData, p)) {
                p.setExileInsteadOfDieThisTurn(true);
            }
            if (cantRegenerate && damageDealt > 0) {
                p.setCantRegenerateThisTurn(true);
            }
            if (tapDamagedCreatures && damageDealt > 0) {
                tapUntapSupport.tapPermanent(gameData, p);
            }
        }
        if (exileInsteadOfDie || cantRegenerate) {
            for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
                for (Permanent permanent : battlefield) {
                    int sourceDamage = gameData.damageDealtToPermanentsBySourceThisTurn
                            .getOrDefault(permanent.getId(), Map.of()).getOrDefault(damageSourceId, 0);
                    if (sourceDamage > damageBefore.getOrDefault(permanent.getId(), 0)
                            && gameQueryService.isCreature(gameData, permanent)) {
                        if (exileInsteadOfDie) {
                            permanent.setExileInsteadOfDieThisTurn(true);
                        }
                        if (cantRegenerate) {
                            permanent.setCantRegenerateThisTurn(true);
                        }
                    }
                }
            }
        }
    }

    public void dealDamageToPlayer(GameData gameData, StackEntry entry, UUID playerId, int rawDamage) {
        Permanent sourcePermanent = entry.getSourcePermanentId() == null
                ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (gameQueryService.damageCantBePreventedFromSource(gameData, sourcePermanent)) {
            boolean previous = gameData.damageCantBePreventedThisTurn;
            gameData.damageCantBePreventedThisTurn = true;
            try {
                dealDamageToPlayerFromSource(gameData, entry, playerId, rawDamage);
            } finally {
                gameData.damageCantBePreventedThisTurn = previous;
            }
            return;
        }
        dealDamageToPlayerFromSource(gameData, entry, playerId, rawDamage);
    }

    /** Queues the next player's independent choice for the same impending damage. */
    public void queueCreatureDamageRedirectChoice(GameData gameData, StackEntry entry,
                                                  UUID targetId, int amount, List<UUID> controllers) {
        Permanent snapshot = entry.getSourcePermanentId() == null ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (snapshot == null) snapshot = entry.getSourcePermanentSnapshot();
        UUID sourceControllerId = entry.getSourcePermanentId() == null ? null
                : gameQueryService.findPermanentController(gameData, entry.getSourcePermanentId());
        if (sourceControllerId == null) sourceControllerId = entry.getDamageSourceControllerId();
        if (sourceControllerId == null) sourceControllerId = entry.getControllerId();
        gameData.pendingMayAbilities.addFirst(new PendingMayAbility(
                entry.getEffectiveDamageSourceCard(), controllers.getFirst(),
                List.of(new RedirectAllCreatureDamageToControllerEffect(
                        controllers.subList(1, controllers.size()), false, -1, false, false, entry.getEntryType())),
                "Have " + amount + " damage dealt to you instead?", targetId, null,
                entry.getSourcePermanentId(), null, 0, 0, null, null, null,
                snapshot == null ? null : new Permanent(snapshot), sourceControllerId, null, amount));
    }

    /**
     * Damage to a player becomes a {@link DamageEvent} that every applicable replacement and
     * prevention effect modifies in the order the affected player chooses (CR 616.1), the damage
     * multipliers and bonuses included.
     */
    private void dealDamageToPlayerFromSource(GameData gameData, StackEntry entry, UUID playerId, int rawDamage) {
        if (rawDamage <= 0) {
            processEyeForAnEyeReflections(gameData);
            return;
        }
        DamageEvent event = DamageEvent.toPlayer(entry, playerId, rawDamage,
                gameData.damageCantBePreventedThisTurn || gameData.unpreventableDamageInProgress);
        processDamageEvent(gameData, event);
    }

    /**
     * Applies the replacement and prevention effects to {@code event} and deals what is left,
     * returning the damage dealt to a damaged permanent (0 for a player). Damage the resolving
     * effect starts is journaled, so a prompt in the middle of the effect is resolved by resolving
     * the effect again (see {@link DamageJournalService}); other damage that becomes due while a
     * replacement decision is outstanding waits in {@code GameData.pendingReplacementEvents}.
     */
    private int processDamageEvent(GameData gameData, DamageEvent event) {
        if (gameData.collectingDamageEvent != null && damageJournalService.isEffectDamage(gameData)) {
            // A step of a simultaneous damage event: held until the event ends (dealSimultaneousDamage).
            gameData.pendingSimultaneousDamage.add(new PendingSimultaneousDamage(
                    gameData.collectingDamageEvent, gameData.collectingDamageStep, event,
                    event.entry().isExilesCreaturesDamaged()));
            return event.toPermanent() ? event.amount() : 0;
        }
        return switch (damageJournalService.next(gameData)) {
            case DamageJournalService.Step.Replay replay -> replay.dealt();
            case DamageJournalService.Step.Hold hold -> 0;
            case DamageJournalService.Step.FinishReady ready -> {
                int dealt = finishDamage(gameData, ready.event());
                damageJournalService.done(gameData, ready.slot(), dealt);
                processEyeForAnEyeReflections(gameData);
                yield dealt;
            }
            case DamageJournalService.Step.Track track -> continueDamage(gameData, event.withJournalSlot(track.slot()),
                    false, current -> replacementOrderService.run(gameData, current));
            case DamageJournalService.Step.Untracked untracked -> {
                if (isReplacementDecisionOutstanding(gameData)) {
                    gameData.pendingReplacementEvents.add(event);
                    yield 0;
                }
                yield continueDamage(gameData, event, false, current -> replacementOrderService.run(gameData, current));
            }
        };
    }

    /** Applies the effect its player chose from {@code order}, then continues that damage. */
    public void answerReplacementOrder(GameData gameData, ChoiceContext.ReplacementEffectOrder order, String label) {
        DamageEvent event = (DamageEvent) order.event();
        continueDamage(gameData, event, true,
                current -> replacementOrderService.<DamageEvent>answer(gameData, order, label));
        if (event.combat()) {
            combatDamageService.getObject().continueCombatDamageAfterDecision(gameData);
        }
    }

    /** Applies the replacement and prevention effects to one recipient's combat damage of a step. */
    public void runCombatDamageEvent(GameData gameData, DamageEvent event) {
        continueDamage(gameData, event, false, current -> replacementOrderService.run(gameData, current));
    }

    /**
     * Continues the combat damage event a "you may" choice paused. {@code remaining} is what is left
     * of the damage from {@code sourceId} the choice was about, or negative when the choice changed
     * nothing.
     */
    public void resumeSuspendedCombatDamage(GameData gameData, UUID sourceId, int remaining) {
        if (!(gameData.suspendedReplacementEvent instanceof DamageEvent suspended)) {
            return;
        }
        gameData.suspendedReplacementEvent = null;
        DamageEvent event = suspended;
        for (int i = 0; remaining >= 0 && i < suspended.portions().size(); i++) {
            if (suspended.portions().get(i).sourceId().equals(sourceId)) {
                event = suspended.withPortionAmount(i, remaining);
            }
        }
        continueDamage(gameData, event, true, current -> replacementOrderService.run(gameData, current));
    }

    /**
     * Asks, before {@code step} is applied to {@code damage}, every question about the departure it would perform
     * instead of the damage ("destroy it instead", "sacrifice it"): the destruction is the modified event, so its own
     * replacements apply to it before anything happens (CR 616.1f). When a question is opened, the damage event must
     * be suspended; {@link #resumeDamageAfterDepartureQuestions} applies the step and continues the event once every
     * answer is in.
     *
     * @return {@code true} when a question is open
     */
    public boolean askBeforeDamageDeparture(GameData gameData, CreatureDamageStep step,
                                            CreatureDamageStep.Context context, int damage) {
        DepartureIntent departure = step.departs().departure(gameData, context, damage);
        if (departure == null
                || !departureService.getObject().askBeforeEvent(gameData, DepartureBatch.of(List.of(departure)))) {
            return false;
        }
        gameData.suspendedDamageDepartureStep = step.family();
        return true;
    }

    /**
     * Continues the damage event suspended by {@link #askBeforeDamageDeparture} once its departure's questions are
     * answered: asks the next question the answers leave, or applies the step (whose departure consumes the answered
     * batch) and runs the rest of the event's replacements. Combat damage then goes on with the damage step.
     *
     * @return whether a damage event was waiting for the departure's answers (it may still be)
     */
    public boolean resumeDamageAfterDepartureQuestions(GameData gameData) {
        AskedLifeLossDeparture lifeLoss = gameData.askedLifeLossDeparture;
        if (lifeLoss != null && lifeLoss.suspended()) {
            return resumeDamageAfterLifeLossDepartureQuestions(gameData, lifeLoss);
        }
        String family = gameData.suspendedDamageDepartureStep;
        if (family == null) {
            return false;
        }
        if (!(gameData.suspendedReplacementEvent instanceof DamageEvent event)) {
            gameData.suspendedDamageDepartureStep = null;
            return false;
        }
        if (departureService.getObject().continueAskingBeforeEvent(gameData)) {
            return true;
        }
        gameData.suspendedDamageDepartureStep = null;
        gameData.suspendedReplacementEvent = null;
        CreatureDamageStep step = damagePreventionService.creatureDamageSteps().stream()
                .filter(candidate -> candidate.family().equals(family))
                .findFirst()
                .orElseThrow();
        Permanent target = gameQueryService.findPermanentById(gameData, event.recipientPermanentId());
        DamageEvent replaced = target == null ? event : event.withAmount(step.apply().apply(gameData,
                new CreatureDamageStep.Context(target, event.combat(), event.explicitSource(), true), event.amount()));
        continueDamageAfterDepartureQuestions(gameData, replaced);
        return true;
    }

    /**
     * Asks, before {@code event}'s damage is dealt to a player, every question about the sacrifice a life loss
     * replacement would perform instead of the life loss that damage causes (Lich's Duel Mastery: CR 120.3a, the
     * damage causes the loss of life; CR 616.1f, the sacrifice's own replacements are chosen before the modified event
     * happens). When a question is opened the event must be suspended; {@link #resumeDamageAfterDepartureQuestions}
     * deals it once every answer is in, and the life loss's sacrifice performs the answered batch.
     *
     * @return {@code true} when a question is open
     */
    private boolean askBeforeLifeLossDeparture(GameData gameData, DamageEvent event) {
        if (event.toPermanent() || event.redirected() || gameData.askedLifeLossDeparture != null) {
            return false;
        }
        UUID playerId = event.recipientPlayerId();
        DepartureIntent sacrifice = playerId == null ? null : lifeSupport.lifeLossSacrifice(gameData, playerId);
        if (sacrifice == null || lifeLossOf(gameData, event) <= 0
                || !departureService.getObject().askBeforeEvent(gameData, DepartureBatch.of(List.of(sacrifice)))) {
            return false;
        }
        gameData.askedLifeLossDeparture = new AskedLifeLossDeparture(playerId, sacrifice, true);
        return true;
    }

    /**
     * The damage of {@code event} that would make its player lose life if it were dealt now, mirroring
     * {@link #finishPlayerDamage} and the combat damage step: damage dealt as infect gives poison counters instead,
     * and nothing is lost while the player's life total can't change or damage causes no loss of life.
     */
    private int lifeLossOf(GameData gameData, DamageEvent event) {
        UUID playerId = event.recipientPlayerId();
        if (gameQueryService.damageDoesNotCauseLifeLoss(gameData, playerId)
                || gameQueryService.shouldDamageBeDealtAsInfect(gameData, playerId)) {
            return 0;
        }
        if (event.combat()) {
            if (!gameQueryService.canPlayerLifeChange(gameData, playerId)) {
                return 0;
            }
            return event.portions().stream()
                    .filter(portion -> !portion.counterDamage())
                    .mapToInt(DamagePortion::amount)
                    .sum();
        }
        if (!gameQueryService.canPlayerLoseLife(gameData, playerId)
                || gameQueryService.sourceHasKeyword(gameData, event.entry(), null, Keyword.INFECT)) {
            return 0;
        }
        return event.amount();
    }

    private boolean resumeDamageAfterLifeLossDepartureQuestions(GameData gameData, AskedLifeLossDeparture lifeLoss) {
        if (!(gameData.suspendedReplacementEvent instanceof DamageEvent event)) {
            gameData.askedLifeLossDeparture = null;
            DepartureService.discardAnsweredEventBatch(gameData, lifeLoss.intent());
            return false;
        }
        if (departureService.getObject().continueAskingBeforeEvent(gameData)) {
            return true;
        }
        gameData.askedLifeLossDeparture = lifeLoss.resumed();
        gameData.suspendedReplacementEvent = null;
        continueDamageAfterDepartureQuestions(gameData, event);
        return true;
    }

    /**
     * Ends what {@link #askBeforeLifeLossDeparture} asked once {@code playerId}'s damage has caused its life loss (or
     * didn't): the answered sacrifice is performed by then, and a batch it left unperformed is dropped.
     */
    public void finishLifeLossDeparture(GameData gameData, UUID playerId) {
        AskedLifeLossDeparture lifeLoss = gameData.askedLifeLossDeparture;
        if (lifeLoss == null || lifeLoss.suspended() || !lifeLoss.playerId().equals(playerId)) {
            return;
        }
        gameData.askedLifeLossDeparture = null;
        DepartureService.discardAnsweredEventBatch(gameData, lifeLoss.intent());
    }

    /** Runs the rest of a damage event that waited for a departure's answers, then the damage that waited for it. */
    private void continueDamageAfterDepartureQuestions(GameData gameData, DamageEvent event) {
        continueDamage(gameData, event, true, current -> replacementOrderService.run(gameData, current));
        if (event.combat()) {
            combatDamageService.getObject().continueCombatDamageAfterDecision(gameData);
        } else {
            drainPendingPlayerDamage(gameData);
        }
    }

    /**
     * Continues the damage a Cleric's "you may prevent" paused, now that {@code remainingDamage} is
     * left of it.
     */
    public void resumeSuspendedPlayerDamage(GameData gameData, StackEntry fallbackEntry, UUID playerId,
                                            int remainingDamage, boolean unpreventable) {
        DamageEvent event = gameData.suspendedReplacementEvent instanceof DamageEvent suspended
                ? suspended.withAmount(remainingDamage)
                : DamageEvent.toPlayer(fallbackEntry, playerId, remainingDamage, unpreventable);
        gameData.suspendedReplacementEvent = null;
        continueDamage(gameData, event, true, current -> replacementOrderService.run(gameData, current));
        drainPendingPlayerDamage(gameData);
    }

    /** Deals the damage that waited while an earlier replacement decision was outstanding. */
    public void drainPendingPlayerDamage(GameData gameData) {
        while (!gameData.pendingReplacementEvents.isEmpty() && !isReplacementDecisionOutstanding(gameData)) {
            if (gameData.pendingReplacementEvents.removeFirst() instanceof DamageEvent event) {
                continueDamage(gameData, event, false, current -> replacementOrderService.run(gameData, current));
            }
        }
    }

    /** Whether a replacement-ordering prompt or a paused "you may" is waiting on a player. */
    public boolean isReplacementDecisionOutstanding(GameData gameData) {
        if (gameData.suspendedReplacementEvent != null) {
            return true;
        }
        PendingInteraction.ColorChoice active = gameData.interaction.activeInteraction(PendingInteraction.ColorChoice.class);
        return active != null && active.context() instanceof ChoiceContext.ReplacementEffectOrder
                || gameData.pendingInteractions.stream().anyMatch(pending ->
                pending instanceof PendingInteraction.ColorChoice choice
                        && choice.context() instanceof ChoiceContext.ReplacementEffectOrder);
    }

    /**
     * Runs one step of {@code event}'s replacement ordering and handles what came of it. When the
     * damage continues after its player answered ({@code answered}) and the resolving effect
     * journals it, the finished event waits for the effect to be resolved again instead of being
     * dealt here.
     */
    private int continueDamage(GameData gameData, DamageEvent event, boolean answered,
                               Function<DamageEvent, ReplacementOrderService.Outcome<DamageEvent>> step) {
        boolean previous = gameData.unpreventableDamageInProgress;
        gameData.unpreventableDamageInProgress = previous || event.unpreventable();
        damageJournalService.enter(gameData);
        int dealt = 0;
        ReplacementOrderService.Outcome<DamageEvent> outcome;
        try {
            outcome = step.apply(event);
            if (outcome instanceof ReplacementOrderService.Outcome.Completed<DamageEvent> completed
                    && askBeforeLifeLossDeparture(gameData, completed.event())) {
                outcome = new ReplacementOrderService.Outcome.Suspended<>(completed.event());
            }
            if (outcome instanceof ReplacementOrderService.Outcome.Completed<DamageEvent> completed
                    && !(answered && damageJournalService.ready(gameData, completed.event()))) {
                dealt = finishDamage(gameData, completed.event());
                if (event.journalSlot() >= 0) {
                    damageJournalService.done(gameData, event.journalSlot(), dealt);
                }
            }
        } finally {
            damageJournalService.exit(gameData);
            gameData.unpreventableDamageInProgress = previous;
        }
        if (outcome instanceof ReplacementOrderService.Outcome.Suspended<DamageEvent> suspended) {
            gameData.suspendedReplacementEvent = suspended.event();
        }
        if (!(outcome instanceof ReplacementOrderService.Outcome.Completed<DamageEvent>) && event.journalSlot() >= 0) {
            damageJournalService.awaiting(gameData, event.journalSlot());
        }
        processEyeForAnEyeReflections(gameData);
        return dealt;
    }

    /**
     * Deals a finished event; returns the damage dealt to a permanent, or 0 for a player. Combat
     * damage goes back to its damage step, which deals all of the step's damage at once.
     */
    private int finishDamage(GameData gameData, DamageEvent event) {
        if (event.combat()) {
            combatDamageService.getObject().recordCombatDamageEvent(gameData, event);
            return 0;
        }
        if (event.toPermanent()) {
            return finishPermanentDamage(gameData, event);
        }
        finishPlayerDamage(gameData, event);
        return 0;
    }

    public UUID findHarshJudgmentRedirectTarget(GameData gameData, StackEntry entry, UUID damagedPlayerId) {
        if (entry == null || damagedPlayerId == null
                || (entry.getEntryType() != StackEntryType.INSTANT_SPELL
                && entry.getEntryType() != StackEntryType.SORCERY_SPELL
                && !entry.isSpellDamageContinuation())
                || damagedPlayerId.equals(entry.getControllerId())) {
            return null;
        }

        Card damageSource = entry.getEffectiveDamageSourceCard();
        Set<CardColor> sourceColors = damageSource == null ? Set.of()
                : gameData.spellColorOverrides.getOrDefault(damageSource.getId(), sourceCardColors(damageSource));
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
     * Stands in for a resolving spell that deals damage, so "whenever one or more Pirates you control deal
     * damage" can match a Kindred spell. Its subtypes include text changes made to the spell. Returns
     * {@code null} when the source is not a spell.
     */
    private Permanent spellDamageDealerSnapshot(GameData gameData, StackEntry entry, Card source) {
        if (entry.getSourcePermanentId() != null || source == null
                || entry.getEntryType() == StackEntryType.TRIGGERED_ABILITY
                || entry.getEntryType() == StackEntryType.ACTIVATED_ABILITY) {
            return null;
        }
        Permanent snapshot = new Permanent(source);
        if (!source.getKeywords().contains(Keyword.CHANGELING)) {
            List<CardSubtype> subtypes = new ArrayList<>(source.getSubtypes());
            for (TextReplacement replacement : gameData.spellTextReplacements.getOrDefault(source.getId(), List.of())) {
                CardSubtype from = TextChangeTransformer.creatureTypeForWord(replacement.fromWord());
                CardSubtype to = TextChangeTransformer.creatureTypeForWord(replacement.toWord());
                if (from != null && to != null && subtypes.remove(from)) {
                    subtypes.add(to);
                }
            }
            snapshot.setLastKnownSubtypes(Set.copyOf(subtypes));
        }
        return snapshot;
    }

    /**
     * Deals {@code event}'s damage once every replacement and prevention effect has been applied to
     * it: poison or life loss, the damage records, and the triggers damage causes (CR 120.3).
     */
    public void finishPlayerDamage(GameData gameData, DamageEvent event) {
        StackEntry entry = event.entry();
        UUID playerId = event.recipientPlayerId();
        int effectiveDamage = event.amount();
        Card source = entry.getEffectiveDamageSourceCard();
        Permanent sourcePermanent = entry.getSourcePermanentId() == null ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        UUID sourceControllerId = sourcePermanent == null ? entry.getControllerId()
                : gameQueryService.findPermanentController(gameData, sourcePermanent.getId());
        if (sourceControllerId == null) sourceControllerId = entry.getControllerId();
        UUID damageSourceId = entry.getSourcePermanentId() != null
                ? entry.getSourcePermanentId() : source.getId();
        boolean sourceHasInfect = gameQueryService.sourceHasKeyword(gameData, entry, null, Keyword.INFECT);
        boolean treatAsInfect = sourceHasInfect || gameQueryService.shouldDamageBeDealtAsInfect(gameData, playerId);
        if (effectiveDamage > 0) {
            visualCueService.emit(gameData, VisualCueType.DAMAGE_TO_PLAYER,
                    damageCueSource(gameData, entry, null), null, playerId, effectiveDamage);
        }

        if (treatAsInfect) {
            if (effectiveDamage > 0) {
                lifeSupport.applyPoisonCounters(gameData, playerId, effectiveDamage,
                        source != null ? source.getName() : entry.getCard().getName(),
                        entry.getControllerId());
            }
        } else if (effectiveDamage > 0 && !gameQueryService.canPlayerLoseLife(gameData, playerId)) {
            String playerName = gameData.playerIdToName.get(playerId);
            gameLogService.append(gameData, GameLog.text(playerName + "'s life total can't change."));
        } else if (effectiveDamage > 0 && gameQueryService.damageDoesNotCauseLifeLoss(gameData, playerId)) {
            String playerName = gameData.playerIdToName.get(playerId);
            gameLogService.append(gameData, GameLog.textCardText(
                    playerName + " takes " + effectiveDamage + " damage from ", source, "."));
        } else if (effectiveDamage > 0 && lifeSupport.replaceLifeLossWithShield(gameData, playerId)) {
            String playerName = gameData.playerIdToName.get(playerId);
            gameLogService.append(gameData, GameLog.textCardText(
                    playerName + " takes " + effectiveDamage + " damage from ", source, "."));
        } else {
            int currentLife = gameData.getLife(playerId);
            int lifeAfterDamage = SaturatingMath.subtract(currentLife, SaturatingMath.multiply(effectiveDamage,
                    gameQueryService.opponentLifeLossMultiplier(gameData, playerId)));
            // Worship / Elderscale Wurm: damage can't reduce the player's life total past an active floor.
            // The full damage is still dealt (lifelink/damage triggers see the full amount); only the life
            // total reduction is capped.
            // 0 means no active floor — do not clamp (life may go negative).
            int lifeFloor = gameQueryService.damageLifeFloor(gameData, playerId, currentLife);
            if (lifeFloor > 0 && lifeAfterDamage < lifeFloor) {
                lifeAfterDamage = lifeFloor;
            }
            int newLife = lifeAfterDamage;
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
        finishLifeLossDeparture(gameData, playerId);

        if (effectiveDamage > 0) {
            recordRedSourceNoncombatDamage(gameData, source, sourcePermanent, sourceControllerId,
                    effectiveDamage);
            accumulateSourceDamageForReflection(gameData, source, entry.getControllerId(),
                    entry.getSourcePermanentId(), effectiveDamage, playerId, null, null, entry);
            Permanent sourceCreature = entry.getSourcePermanentId() == null
                    ? null
                    : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
            if (damageComesFromOtherObject(entry, sourceCreature)) {
                sourceCreature = null;
            }
            triggerCollectionService.queueEnchantedCreatureDealsDamageTriggers(
                    gameData, sourceCreature, effectiveDamage);
            int artifactDamage = gameQueryService.isDamageSourceArtifact(gameData, entry, sourcePermanent)
                    ? effectiveDamage : 0;
            gameData.recordDamageToPlayer(playerId, effectiveDamage, artifactDamage);
            gameData.recordNoncombatDamageToPlayer(playerId, effectiveDamage);
            recordSorcerySpellDamage(gameData, entry, effectiveDamage);
            gameData.recordDamageDealtBySource(damageSourceId, effectiveDamage);
            gameData.recordDamageSourceControlledBy(damageSourceId, sourceControllerId);
            gameData.recordDamageDealtBySourceToPlayer(
                    entry.getSourcePermanentId(), playerId, effectiveDamage);
            if (entry.getSourcePermanentId() == null) {
                gameData.damageDealtToPlayersBySourceThisTurn
                        .computeIfAbsent(damageSourceId, ignored -> new java.util.concurrent.ConcurrentHashMap<>())
                        .merge(playerId, effectiveDamage, Integer::sum);
            }
            entry.recordPlayerDealtDamage(playerId);
            gameData.recordNoncombatDamageSourceToPlayer(entry.getSourcePermanentId(), playerId);
            String sourcePermanentName = sourcePermanent != null
                    ? sourcePermanent.getCard().getName()
                    : entry.getSourcePermanentSnapshot() == null
                    ? null : entry.getSourcePermanentSnapshot().getCard().getName();
            gameData.recordPermanentDamageSourceNameToPlayer(sourcePermanentName, playerId);
            if (sourcePermanent != null && gameQueryService.isCreature(gameData, sourcePermanent)) {
                gameData.recordCreatureDamageSourceToPlayer(sourcePermanent.getId(), playerId);
            } else if (sourcePermanent == null && entry.getSourcePermanentId() != null
                    && entry.getSourcePermanentSnapshot() != null
                    && entry.getSourcePermanentSnapshot().getCard().hasType(CardType.CREATURE)) {
                // The creature left the battlefield as a cost (Mogg Fanatic); its last known
                // information still says it was a creature that dealt the damage.
                gameData.recordCreatureDamageSourceToPlayer(entry.getSourcePermanentId(), playerId);
            }
            recordRedSpellDamage(gameData, entry, source, playerId);
            triggerCollectionService.checkEnchantedPlayerDealtDamageTriggers(
                    gameData, playerId, effectiveDamage);
            triggerCollectionService.checkDamageDealtToControllerTriggers(gameData, playerId, entry.getSourcePermanentId(), false);
            triggerCollectionService.checkEnchantedCreatureDealtDamageToControllerReflectTriggers(gameData, playerId, entry.getSourcePermanentId(), effectiveDamage);
            // The stack entry's controller is the damage source's controller (caster/activator);
            // used to gate the opponent-only ON_CONTROLLER_DEALT_DAMAGE_BY_OPPONENT slot.
            triggerCollectionService.checkControllerDealtDamageTriggers(gameData, playerId, entry.getControllerId(), effectiveDamage);
            // Night Dealings: "whenever a source you control deals damage to another player".
            triggerCollectionService.checkAllySourceDealtDamageToOpponentTriggers(
                    gameData, playerId, entry.getControllerId(), entry.getSourcePermanentId(), effectiveDamage);
            Permanent triggeringDamageSource = sourcePermanent != null
                    ? sourcePermanent : entry.getSourcePermanentSnapshot();
            if (triggeringDamageSource != null && gameQueryService.isCreature(gameData, triggeringDamageSource)) {
                triggerCollectionService.checkAllyCreaturesDealDamageToPlayerTriggers(
                        gameData, sourceControllerId, playerId, List.of(triggeringDamageSource));
            }
            Permanent opponentDamageDealer = triggeringDamageSource != null
                    ? triggeringDamageSource : spellDamageDealerSnapshot(gameData, entry, source);
            if (opponentDamageDealer != null) {
                triggerCollectionService.checkAllyCreaturesDealDamageToOpponentTriggers(
                        gameData, sourceControllerId, playerId, List.of(opponentDamageDealer));
            }
            triggerCollectionService.checkAllySourceDealtNoncombatDamageToOpponentTriggers(
                    gameData, playerId, sourceControllerId, effectiveDamage);
            triggerCollectionService.checkOpponentDealtDamageTriggers(
                    gameData, playerId, entry.getSourcePermanentId(), effectiveDamage);
            // Mangara's Equity: "whenever a creature of the chosen color deals damage to you"
            triggerCollectionService.checkCreatureDamageToYouOrYourPermanentTriggers(gameData, playerId, null,
                    entry.getSourcePermanentId() != null
                            ? gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId())
                            : null,
                    effectiveDamage);
            // Source's own ON_DAMAGE_TO_PLAYER (e.g. Niv-Mizzet, Dracogenius ping → may draw).
            if (entry.getSourcePermanentId() != null) {
                Permanent triggerSourcePermanent = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
                if (triggerSourcePermanent != null && !damageComesFromOtherObject(entry, triggerSourcePermanent)) {
                    // "you may draw" belongs to the source's controller at damage time, which can differ
                    // from the ability's controller if the source changed control while the ping was pending.
                    UUID sourceControllerAtDamage = gameQueryService.findPermanentController(
                            gameData, triggerSourcePermanent.getId());
                    triggerCollectionService.checkSourceDealsDamageToPlayerTriggers(gameData, triggerSourcePermanent,
                            sourceControllerAtDamage != null ? sourceControllerAtDamage : entry.getControllerId(),
                            playerId, effectiveDamage);
                }
            }
            triggerCollectionService.checkNoncombatDamageToOpponentTriggers(
                    gameData, playerId, sourceControllerId, effectiveDamage);
            triggerCollectionService.checkRedSpellOrPlaneswalkerDamageToOpponentTriggers(gameData, playerId, entry);
            checkSpellLifelink(gameData, entry, effectiveDamage);
        }
        processEyeForAnEyeReflections(gameData);
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
                && entry.getEntryType() != StackEntryType.SORCERY_SPELL
                && !entry.isSpellDamageContinuation()) {
            return;
        }
        gameData.recordRedSpellDamageToPlayer(playerId, entry.getControllerId());
    }

    private void recordSorcerySpellDamage(GameData gameData, StackEntry entry, int amount) {
        if (entry == null || entry.isCopy() || entry.getEntryType() != StackEntryType.SORCERY_SPELL
                || entry.getCard() == null) {
            return;
        }
        gameData.recordSorcerySpellDamage(entry.getCard().getId(), amount);
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
                    List.of(), (UUID) null, reflection.sourcePermanentId());
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
            if (targetId == null) continue;
            StackEntry damageEntry = new StackEntry(
                    redirect.sourcePermanentId() == null ? StackEntryType.INSTANT_SPELL
                            : StackEntryType.ACTIVATED_ABILITY,
                    redirect.sourceCard(), redirect.protectedPlayerId(),
                    redirect.sourceCard().getName(), List.of(), targetId, redirect.sourcePermanentId());
            resolveAnyTargetDamage(gameData, damageEntry, targetId, redirect.remainingAmount(), false);
            processPendingRedirectDamage(gameData);
        }
        flushSourceDamageReflections(gameData);
    }

    /**
     * Processes pending source-specific redirect damage entries (e.g. Harm's Way) that no damage
     * event carries: the redirected damage is dealt by its source as damage of its own.
     */
    public void processSourceRedirectDamage(GameData gameData) {
        dealRedirectedDamage(gameData, null);
    }

    /**
     * Deals the damage redirect effects just took over for {@code event}: combat damage through the
     * combat step's own bookkeeping, other damage as damage from the event's stack entry.
     */
    public void processRedirects(GameData gameData, DamageEvent event) {
        if (event.combat()) {
            combatDamageService.getObject().processCombatRedirects(gameData, event);
        } else {
            dealRedirectedDamage(gameData, event);
        }
    }

    /**
     * Deals the damage waiting in {@code pendingSourceRedirectDamage} to its new recipients. The
     * redirected damage is the same damage modified (CR 614.9), so it goes through the new
     * recipient's replacement and prevention effects, ordered by that player (CR 616.1), while the
     * effects that already modified {@code origin} don't apply again (CR 614.5). Without an
     * {@code origin}, the damage is dealt by the redirect's chosen source.
     */
    private void dealRedirectedDamage(GameData gameData, DamageEvent origin) {
        while (!gameData.pendingSourceRedirectDamage.isEmpty()) {
            List<SourceDamageRedirectShield> toProcess = new ArrayList<>(gameData.pendingSourceRedirectDamage);
            gameData.pendingSourceRedirectDamage.clear();
            for (SourceDamageRedirectShield redirect : toProcess) {
                DamageEvent redirected = redirectedEvent(gameData, origin, redirect);
                if (redirected == null || redirected.amount() <= 0) continue;
                if (isRedirectedToProtectedCreature(gameData, redirected)) continue;
                if (isReplacementDecisionOutstanding(gameData)) {
                    gameData.pendingReplacementEvents.add(redirected);
                } else {
                    continueDamage(gameData, redirected, false, current -> replacementOrderService.run(gameData, current));
                }
            }
        }
    }

    /**
     * Protection (CR 702.16e) applies to damage a redirect deals to a creature just as it does to
     * damage dealt directly: damage from a source the creature has protection from is prevented.
     */
    private boolean isRedirectedToProtectedCreature(GameData gameData, DamageEvent redirected) {
        if (!redirected.toPermanent() || !gameQueryService.isDamagePreventable(gameData)) return false;
        Permanent target = gameQueryService.findPermanentById(gameData, redirected.recipientPermanentId());
        if (target == null) return false;
        StackEntry entry = redirected.entry();
        if (!gameQueryService.hasProtectionFromDamageSource(gameData, target,
                entry.getEffectiveDamageSourceCard(), entry.getControllerId())) {
            return false;
        }
        gameLogService.append(gameData, GameLog.cardThen(entry.getEffectiveDamageSourceCard(),
                "'s damage is prevented."));
        return true;
    }

    private DamageEvent redirectedEvent(GameData gameData, DamageEvent origin, SourceDamageRedirectShield redirect) {
        UUID targetId = redirect.redirectTargetId();
        int damage = redirect.remainingAmount();
        UUID controllerId;
        UUID permanentId;
        if (gameData.playerIds.contains(targetId)) {
            controllerId = targetId;
            permanentId = null;
            gameLogService.append(gameData, GameLog.text(
                    damage + " damage is redirected to " + gameData.playerIdToName.get(targetId) + "."));
        } else {
            Permanent targetPerm = gameQueryService.findPermanentById(gameData, targetId);
            if (targetPerm == null) return null;
            controllerId = gameQueryService.findPermanentController(gameData, targetId);
            permanentId = targetId;
            gameLogService.append(gameData, GameLog.textCardText(
                    damage + " damage is redirected to ", targetPerm.getCard(), "."));
        }
        if (origin != null) {
            return origin.redirectedTo(controllerId, permanentId, damage, RedirectedDamageRules::staysSpent);
        }
        Permanent source = redirect.damageSourceId() == null ? null
                : gameQueryService.findPermanentById(gameData, redirect.damageSourceId());
        if (source == null) return null;
        StackEntry entry = new StackEntry(StackEntryType.ACTIVATED_ABILITY, source.getCard(),
                gameQueryService.findPermanentController(gameData, source.getId()), source.getCard().getName(),
                List.of(), targetId, source.getId());
        boolean unpreventable = gameData.damageCantBePreventedThisTurn || gameData.unpreventableDamageInProgress;
        return permanentId == null
                ? DamageEvent.toPlayer(entry, targetId, damage, unpreventable)
                : DamageEvent.toPermanent(entry, controllerId, permanentId, source, damage, unpreventable, false);
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
            int rawDamage = assignment.getValue();

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
        accumulateSourceDamageForReflection(gameData, sourceCard, sourceControllerId, sourcePermanentId,
                damage, damagedPlayerId, damagedPermanentControllerId, null);
    }

    public void accumulateSourceDamageForReflection(GameData gameData, Card sourceCard, UUID sourceControllerId,
                                                    UUID sourcePermanentId, int damage, UUID damagedPlayerId,
                                                    UUID damagedPermanentControllerId, UUID damagedPermanentId) {
        accumulateSourceDamageForReflection(gameData, sourceCard, sourceControllerId, sourcePermanentId, damage,
                damagedPlayerId, damagedPermanentControllerId, damagedPermanentId, null);
    }

    private void accumulateSourceDamageForReflection(GameData gameData, Card sourceCard, UUID sourceControllerId,
                                                     UUID sourcePermanentId, int damage, UUID damagedPlayerId,
                                                     UUID damagedPermanentControllerId, UUID damagedPermanentId,
                                                     StackEntry sourceEntry) {
        if (damage <= 0 || sourceCard == null || sourceControllerId == null) return;
        UUID singleCreatureSpellTargetId = singleCreatureSpellTargetId(gameData, sourceEntry);
        boolean spellDamage = sourceEntry != null && (sourceEntry.getEntryType() == StackEntryType.INSTANT_SPELL
                || sourceEntry.getEntryType() == StackEntryType.SORCERY_SPELL);
        UUID damageEventKey = spellDamage
                ? UUID.nameUUIDFromBytes((sourceEntry.getTargetableId() + ":" + sourceCard.getId()
                        + ":" + sourceEntry.getResolvingDamageEvent()).getBytes(java.nio.charset.StandardCharsets.UTF_8))
                : sourceCard.getId();
        PendingSourceDamage batch = gameData.pendingSourceDamageForReflection.get(damageEventKey);
        if (batch == null) {
            batch = new PendingSourceDamage(sourceCard, sourceControllerId, sourcePermanentId, damage,
                            damagedPlayerId, damagedPermanentControllerId,
                            damagedPermanentId,
                            snapshotSelfDealsDamageEffects(gameData, sourceCard, sourcePermanentId),
                            singleCreatureSpellTargetId);
            gameData.pendingSourceDamageForReflection.put(damageEventKey, batch);
        } else {
            batch.rememberSingleCreatureSpellTarget(singleCreatureSpellTargetId);
            batch.add(damage, damagedPlayerId, damagedPermanentControllerId, damagedPermanentId);
        }
        if (damagedPermanentId != null) {
            Permanent recipient = gameQueryService.findPermanentById(gameData, damagedPermanentId);
            if (recipient != null) {
                Permanent snapshot = new Permanent(recipient);
                Card characteristics = recipient.getCard().createRuntimeCopy();
                Set<CardType> types = gameQueryService.getEffectiveCardTypes(gameData, recipient);
                if (!types.isEmpty()) {
                    characteristics.setType(types.iterator().next());
                    characteristics.setAdditionalTypes(types);
                }
                snapshot.setCard(characteristics);
                batch.rememberDamagedPermanent(snapshot);
            }
        }
        if (sourceEntry != null && (sourceEntry.getEntryType() == StackEntryType.INSTANT_SPELL
                || sourceEntry.getEntryType() == StackEntryType.SORCERY_SPELL)) {
            if (damagedPlayerId != null && !damagedPlayerId.equals(sourceControllerId)) {
                batch.recordInstantOrSorceryDamageRecipient(damagedPlayerId);
            } else if (damagedPermanentId != null) {
                Permanent damagedPermanent = gameQueryService.findPermanentById(gameData, damagedPermanentId);
                if (damagedPermanent != null && damagedPermanent.getCard().hasType(CardType.BATTLE)) {
                    batch.recordInstantOrSorceryDamageRecipient(damagedPermanentId);
                }
            }
        }
    }

    private UUID singleCreatureSpellTargetId(GameData gameData, StackEntry sourceEntry) {
        if (sourceEntry == null
                || (sourceEntry.getEntryType() != StackEntryType.INSTANT_SPELL
                && sourceEntry.getEntryType() != StackEntryType.SORCERY_SPELL)) {
            return null;
        }

        List<UUID> targetIds = new ArrayList<>(sourceEntry.getDeclaredTargetIds());
        if (sourceEntry.getTargetId() != null) {
            targetIds.add(sourceEntry.getTargetId());
        }
        // One creature chosen for several instances of "target" is still a single targeted object.
        targetIds = targetIds.stream().distinct().toList();
        if (targetIds.size() != 1) return null;

        Permanent target = gameQueryService.findPermanentById(gameData, targetIds.getFirst());
        return target != null && gameQueryService.isCreature(gameData, target) ? target.getId() : null;
    }

    /**
     * Queues the {@code ON_ANY_SOURCE_DEALS_DAMAGE} reflection triggers (Justice) for every source
     * that dealt non-combat damage during the just-finished resolution, then clears the accumulator.
     * Combat damage batches separately in {@code CombatDamageService}.
     */
    public void flushSourceDamageReflections(GameData gameData) {
        if (gameData.pendingSourceDamageForReflection.isEmpty()) return;
        Map<UUID, PendingSourceDamage> batches = new LinkedHashMap<>(gameData.pendingSourceDamageForReflection);
        gameData.pendingSourceDamageForReflection.clear();
        for (Map.Entry<UUID, PendingSourceDamage> event : batches.entrySet()) {
            PendingSourceDamage batch = event.getValue();
            if (!event.getKey().equals(batch.getSourceCard().getId())) {
                triggerCollectionService.checkAllyInstantOrSorcerySpellDealsDamageTriggers(gameData, batch);
            }
            triggerCollectionService.queueSourceDealsDamageReflections(gameData,
                    batch.getSourceCard(), batch.getControllerId(), batch.getSourcePermanentId(), batch.getAmount(),
                    batch.getDamageToPlayers(), batch.getSelfDealsDamageEffects(),
                    batch.getSingleCreatureSpellTargetId(), batch.getDamageToPermanents(), false,
                    batch.getDamagedPermanentSnapshots());
            List<TriggerCollectionService.SourceDamageRecipient> damageRecipients = batch.getDamageRecipients()
                    .stream()
                    .map(recipient -> new TriggerCollectionService.SourceDamageRecipient(
                            recipient.playerId(), recipient.permanentId()))
                    .toList();
            triggerCollectionService.checkOpponentSourceDamageToYouOrYourPermanentTriggers(
                    gameData, batch.getSourceCard(), batch.getControllerId(), batch.getSourcePermanentId(),
                    damageRecipients);
            for (int i = 0; i < batch.getInstantOrSorceryDamageRecipientCount(); i++) {
                triggerCollectionService.checkInstantOrSorceryDamageToOpponentOrBattleTriggers(
                        gameData, batch.getSourceCard(), batch.getControllerId(), true);
            }
        }
    }

    private List<CardEffect> snapshotSelfDealsDamageEffects(GameData gameData, Card sourceCard,
                                                             UUID sourcePermanentId) {
        if (sourcePermanentId == null) return null;
        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        // A source that already left the battlefield when it dealt the damage has no "whenever this
        // creature deals damage" abilities to trigger, so the empty snapshot suppresses the card fallback.
        if (sourcePermanent == null) return List.of();

        List<CardEffect> effects = new ArrayList<>();
        if (!sourcePermanent.isFaceDown() && !gameQueryService.hasLostPrintedAbilities(gameData, sourcePermanent)) {
            effects.addAll(sourceCard.getEffects(EffectSlot.ON_SELF_DEALS_DAMAGE));
        }
        effects.addAll(sourcePermanent.getTemporaryTriggeredEffects(EffectSlot.ON_SELF_DEALS_DAMAGE));
        effects.addAll(sourcePermanent.getPersistentTriggeredEffects(EffectSlot.ON_SELF_DEALS_DAMAGE));
        effects.addAll(triggerCollectionService.grantedTriggeredEffects(
                gameData, sourcePermanent, EffectSlot.ON_SELF_DEALS_DAMAGE));
        return effects;
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
        return applyControllerAllDamagePrevention(gameData, playerId, damage, false);
    }

    public int applyControllerAllDamagePrevention(GameData gameData, UUID playerId, int damage,
                                                   boolean combatDamage) {
        if (!gameQueryService.isDamagePreventable(gameData, combatDamage)) return 0;
        if (damage <= 0) return 0;

        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return 0;

        boolean controllersTurn = playerId.equals(gameData.activePlayerId);
        boolean hasEffect = battlefield.stream().anyMatch(p ->
                !gameQueryService.hasLostPrintedAbilities(gameData, p)
                        && p.getCard().getEffects(EffectSlot.STATIC).stream()
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
        return applyImmortalCoilPrevention(gameData, playerId, damage, false);
    }

    public int applyImmortalCoilPrevention(GameData gameData, UUID playerId, int damage,
                                           boolean combatDamage) {
        if (!gameQueryService.isDamagePreventable(gameData, combatDamage)) return 0;
        if (damage <= 0) return 0;

        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return 0;

        boolean hasEffect = battlefield.stream().anyMatch(p ->
                p.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(e -> e instanceof PreventAllDamageToControllerAndExileFromGraveyardEffect));
        if (!hasEffect) return 0;

        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        // The controller chooses which cards are exiled. The choice is skipped when it cannot matter:
        // every card goes, there is already a choice waiting, or all the cards share one name.
        if (graveyard != null && graveyard.size() > damage
                && !gameData.interaction.isAwaitingInput()
                && graveyard.stream().map(Card::getName).distinct().count() > 1) {
            gameData.graveyardTargetOperation.resolutionTimeExileOwnGraveyardResume = true;
            interactionHandlerRegistry.begin(gameData, new PendingInteraction.MultiGraveyardChoice(
                    playerId, new ArrayList<>(graveyard), damage,
                    "Choose " + damage + " card" + (damage != 1 ? "s" : "")
                            + " from your graveyard to exile for the prevented damage.", damage));
            return damage;
        }
        graveyardService.exileCardsFromGraveyard(gameData, playerId, damage);
        // CR 603.8: a state trigger (Immortal Coil's "no cards in your graveyard") triggers as soon as the
        // state is true, even in the middle of a spell's resolution.
        stateTriggerService.checkStateTriggers(gameData);
        return damage;
    }

    /**
     * Angel of Suffering: mills twice the damage that would be dealt to its controller and
     * prevents the preventable portion. If the damage cannot be prevented, it still mills twice
     * that many cards.
     */
    public int applyAngelOfSufferingReplacement(GameData gameData, UUID playerId, int damage,
                                                int preventableDamage) {
        if (damage <= 0) return 0;

        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return 0;

        long sources = battlefield.stream().filter(p ->
                gameQueryService.hasActiveStaticEffect(gameData, p,
                        PreventAllDamageToControllerAndMillEffect.class)).count();
        if (sources == 0) return 0;

        int prevented = gameQueryService.isDamagePreventable(gameData)
                ? Math.min(damage, Math.max(0, preventableDamage)) : 0;
        graveyardService.resolveMillPlayer(gameData, playerId, damage * 2);
        int remainingDamage = damage - prevented;
        if (remainingDamage > 0) {
            for (int i = 1; i < sources; i++) {
                graveyardService.resolveMillPlayer(gameData, playerId, remainingDamage * 2);
            }
        }
        return prevented;
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

        List<Card> graveyard = gameData.playerGraveyards.get(playerId);
        List<Card> exilableCards = graveyard == null ? List.of() : graveyard.stream()
                .filter(card -> !card.isToken() && !gameData.dynamicTokenCardIds.contains(card.getId()))
                .toList();
        if (exilableCards.size() > damage && !gameData.interaction.isAwaitingInput()
                && gameData.graveyardTargetOperation.replacementGraveyardExile == null) {
            gameData.graveyardTargetOperation.replacementGraveyardExile =
                    new GraveyardTargetOperationState.ReplacementGraveyardExileContext(playerId, damage);
            playerInputService.getObject().beginMultiGraveyardChoice(gameData, playerId, exilableCards,
                    damage, damage, "Choose " + damage + " card" + (damage == 1 ? "" : "s")
                            + " from your graveyard to exile instead of being dealt damage.");
            return damage;
        }

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
     * Replaces damage to Dralnu with a sacrifice of that many permanents controlled by its
     * controller. If the controller has fewer permanents, all of them are sacrificed.
     */
    public int applyDralnuReplacement(GameData gameData, Permanent target, int damage) {
        if (damage <= 0 || !hasDralnuDamageReplacement(gameData, target)) return 0;

        UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());
        if (controllerId != null) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
            int count = battlefield == null ? 0 : Math.min(damage, battlefield.size());
            destructionSupportProvider.getObject().sacrificePlayerMatchingPermanents(
                    gameData, controllerId, count, new com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate());
        }
        return damage;
    }

    public boolean hasDralnuDamageReplacement(GameData gameData, Permanent target) {
        return target != null && gameQueryService.getActiveStaticEffects(gameData, target).stream()
                .anyMatch(DralnuDamageReplacementEffect.class::isInstance);
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
     * how much of {@code damage} was replaced this way — one echo counter per 1 damage, up to the
     * counters actually available on one armed Soul Echo. Running out of counters does not leave a
     * remainder to be dealt, because the replacement applies to the entire damage event. This is a
     * replacement, not prevention, so it is not gated on
     * {@code isDamagePreventable}. Shared by the noncombat ({@link #dealDamageToPlayer}) and combat
     * ({@code CombatDamageService.applyPlayerDamage}) paths.
     */
    public int applySoulEchoCounterRemoval(GameData gameData, UUID playerId, int damage) {
        if (damage <= 0) return 0;
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(playerId, List.of())) {
            if (permanent.isEchoDamageRedirectionActive()) {
                return applySoulEchoCounterRemoval(gameData, playerId, permanent, damage);
            }
        }
        return 0;
    }

    /** Applies {@code soulEcho}'s replacement to {@code damage} dealt to {@code playerId}. */
    public int applySoulEchoCounterRemoval(GameData gameData, UUID playerId, Permanent soulEcho, int damage) {
        if (damage <= 0 || !soulEcho.isEchoDamageRedirectionActive()) return 0;

        int available = soulEcho.getCounterCount(CounterType.ECHO);
        int removed = Math.min(available, damage);
        soulEcho.setCounterCount(CounterType.ECHO, available - removed);

        gameLogService.append(gameData, GameLog.textCardText(
                gameData.playerIdToName.get(playerId) + " removes " + removed + " echo counter"
                        + (removed == 1 ? "" : "s") + " from ", soulEcho.getCard(),
                " instead of taking " + damage + " damage."));
        return damage;
    }

    /**
     * Replaces damage to a controller with counters on a permanent they control. This is a
     * replacement, not prevention, so it still applies when damage cannot be prevented and still
     * replaces the damage if a counter-placement restriction means no counters can actually be
     * added.
     */
    public int applyDamageToControllerCounterReplacement(GameData gameData, UUID playerId, int damage) {
        if (damage <= 0) return 0;
        List<Permanent> sources = damageToControllerCounterReplacementSources(gameData, playerId);
        return sources.isEmpty() ? 0
                : applyDamageToControllerCounterReplacement(gameData, playerId, sources.getFirst(), damage);
    }

    /** The permanents {@code playerId} controls that turn damage to them into counters on themselves. */
    public List<Permanent> damageToControllerCounterReplacementSources(GameData gameData, UUID playerId) {
        return gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                .filter(permanent -> permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(DamageToControllerCounterReplacementEffect.class::isInstance))
                .toList();
    }

    /** Replaces {@code damage} to {@code playerId} with counters on {@code source}. */
    public int applyDamageToControllerCounterReplacement(GameData gameData, UUID playerId, Permanent source,
                                                         int damage) {
        if (damage <= 0) return 0;
        DamageToControllerCounterReplacementEffect replacement = source.getCard().getEffects(EffectSlot.STATIC)
                .stream()
                .filter(DamageToControllerCounterReplacementEffect.class::isInstance)
                .map(DamageToControllerCounterReplacementEffect.class::cast)
                .findFirst()
                .orElse(null);
        if (replacement == null) return 0;

        if (!gameQueryService.cantHaveCounters(gameData, source)) {
            CounterType counterType = replacement.counterType();
            int placed = permanentCounterSupport.placeCounterOnPermanentForPlayer(
                    gameData, null, source, counterType, damage, playerId);
            String counterName = permanentCounterSupport.counterTypeName(counterType);
            gameLogService.append(gameData, GameLog.cardThen(source.getCard(),
                    " gets " + placed + " " + counterName + " counter"
                            + (placed == 1 ? "" : "s") + " instead of damage."));
        }
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

    /**
     * Object id used for per-source marked-damage tracking: the dealing permanent when known,
     * otherwise the spell/ability card instance (each cast is a distinct source).
     */
    /**
     * The source of a noncombat damage cue as the players see it: the permanent dealing the damage
     * when it is one (a face-down one stays anonymous), otherwise the resolving spell, or the card
     * an ability deals damage as.
     */
    private VisualCueService.Source damageCueSource(GameData gameData, StackEntry entry, Permanent damageSource) {
        if (damageSource != null) {
            return VisualCueService.Source.of(damageSource);
        }
        Card card = entry.getEffectiveDamageSourceCard();
        if (card == entry.getCard() && entry.getSourcePermanentId() == null) {
            return VisualCueService.Source.of(entry);
        }
        Permanent permanent = gameQueryService.findPermanentByCardId(gameData, card.getId());
        if (permanent == null && entry.getSourcePermanentSnapshot() != null
                && card.getId().equals(entry.getSourcePermanentSnapshot().getCard().getId())) {
            permanent = entry.getSourcePermanentSnapshot();
        }
        return permanent != null ? VisualCueService.Source.of(permanent) : VisualCueService.Source.of(card);
    }

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
