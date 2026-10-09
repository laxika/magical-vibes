package com.github.laxika.magicalvibes.service.effect.normalfx;
import com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry;
import com.github.laxika.magicalvibes.service.effect.MaroGoneNutsSupport;
import com.github.laxika.magicalvibes.model.effect.ReduceSpellDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GlobalDamageMultiplyingEffect;
import com.github.laxika.magicalvibes.model.PendingInteraction;
import com.github.laxika.magicalvibes.model.ChoiceContext;

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
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.model.CounterType;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.ObjectProvider;
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
    @org.springframework.beans.factory.annotation.Autowired @org.springframework.context.annotation.Lazy
    private InteractionHandlerRegistry interactionHandlerRegistry;

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
        Permanent source = damageSource;
        if (source == null && entry != null && entry.getSourcePermanentId() != null) {
            source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        }
        if (source == null && entry != null) {
            source = entry.getSourcePermanentSnapshot();
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
        // Prevention that applies to the creature also applies to damage from its abilities.
        // Combat damage and damage to players already consult this flag; keep creature damage
        // on the same path (for example, a Kry Shield-protected D'Avenant Archer).
        if (source != null && gameQueryService.isPreventedFromDealingDamage(gameData, source)) {
            gameLogService.append(gameData, GameLog.cardThen(source.getCard(), "'s damage is prevented."));
            return 0;
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
            damagePreventionService.applyAllByCreaturesPreventionLifeGain(gameData, rawDamage,
                    gameQueryService.findPermanentController(gameData, target.getId()));
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
        if (!cantBeRedirected && !targetDamageUnpreventable && damageSource == null && rawDamage > 0) {
            UUID redirectedPlayerId = damagePreventionService.getTargetSorceryDamageRedirectController(gameData, entry);
            if (redirectedPlayerId != null) {
                gameLogService.append(gameData, GameLog.cardThen(entry.getEffectiveDamageSourceCard(),
                        "'s damage to " + target.getCard().getName()
                                + " is dealt to its controller instead."));
                dealDamageToPlayer(gameData, entry, redirectedPlayerId, rawDamage);
                return 0;
            }
        }
        if (!targetDamageUnpreventable && damageSource == null) {
            UUID targetControllerId = gameQueryService.findPermanentController(gameData, target.getId());
            if (gameQueryService.isSpellDamageToControllerAndPermanentsPrevented(
                    gameData, entry, targetControllerId)) {
                gameLogService.append(gameData, GameLog.textCardText("Damage to ", target.getCard(), " is prevented."));
                return 0;
            }
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
        if (!targetDamageUnpreventable
                && gameQueryService.isDamageFromTargetingSpellOrAbilityPrevented(gameData, entry, target)) {
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
        UUID sourcePermanentIdForBonus = sourcePermanentForBonus == null
                ? entry == null ? null : entry.getSourcePermanentId()
                : sourcePermanentForBonus.getId();
        if (rawDamage > 0) {
            rawDamage += gameQueryService.getNoncreatureSourceDamageBonus(
                    gameData, entry, null, target);
            rawDamage += gameQueryService.getPlayersAndBattlesDamageBonus(
                    gameData, false, target);
            rawDamage += gameQueryService.getAdditionalDamageToOpponentsBonus(
                    gameData, bonusSourceControllerId, sourceCardForBonus, sourcePermanentForBonus, targetControllerId);
            rawDamage += gameQueryService.getControllerDamageToOpponentBonus(
                    gameData, bonusSourceControllerId, targetControllerId, false, true,
                    sourcePermanentIdForBonus);
            rawDamage += gameQueryService.getAdditionalSpellDamageToOpponentsBonus(
                    gameData, entry, targetControllerId);
            rawDamage += gameQueryService.getControllerNoncombatDamageBonus(
                    gameData, bonusSourceControllerId);
            rawDamage += gameQueryService.getPerpetualNoncombatDamageBonus(gameData, entry);
        }
        UUID sourceControllerId = bonusSourceControllerId;
        // Gisela, Blade of Goldnight: double the damage dealt to a permanent an opponent controls. The
        // combat counterpart lives in GameQueryService.applyCombatDamageMultiplier.
        rawDamage *= gameQueryService.getDamageToRecipientMultiplier(gameData, targetControllerId,
                recipientSourceControllerId, target.getId());
        rawDamage = gameQueryService.applyDamageReplacementEffects(gameData, rawDamage);
        UUID sourcePermId = damageSource != null ? damageSource.getId() : entry.getSourcePermanentId();
        if (!cantBeRedirected) {
            if (targetControllerId != null && sourcePermId != null) {
                rawDamage = damagePreventionService.applySourceRedirectShields(gameData, targetControllerId, sourcePermId, rawDamage);
                processSourceRedirectDamage(gameData, entry);
            }
            // Reflect Damage: the chosen source's next damage is dealt to that source's controller instead.
            UUID chosenSourceId = damageSourceKey(entry, damageSource);
            if (chosenSourceId != null) {
                rawDamage = damagePreventionService.applyReflectDamageToSourceControllerShield(gameData, chosenSourceId, rawDamage);
                processEyeForAnEyeReflections(gameData);
                if (rawDamage <= 0) return 0;
                // Opal-Eye: the chosen source's next damage is dealt to a fixed creature instead.
                rawDamage = damagePreventionService.applySourceNextDamageRedirectToPermanent(
                        gameData, chosenSourceId, target.getId(), rawDamage);
                processSourceRedirectDamage(gameData, entry);
                if (rawDamage <= 0) return 0;
            }
            // Saving Grace: redirect all damage this turn to a permanent you control onto the enchanted creature.
            if (targetControllerId != null) {
                rawDamage = damagePreventionService.applyTurnDamageRedirectToCreature(gameData, targetControllerId, target.getId(), rawDamage);
                processSourceRedirectDamage(gameData, entry);
            }
            // Palisade Giant: damage to other permanents its controller controls is dealt to it instead.
            if (targetControllerId != null) {
                rawDamage = damagePreventionService.applyStaticPermanentDamageRedirectToSelf(gameData, targetControllerId, target.getId(), rawDamage);
                processSourceRedirectDamage(gameData, entry);
            }
            rawDamage = damagePreventionService.applyCreatureControllerDamageRedirectUntilNextTurn(
                    gameData, targetControllerId, target, sourcePermId, rawDamage);
            processSourceRedirectDamage(gameData, entry);
            if (!gameData.resolvingDeclinedAllCreatureDamageRedirect
                    && rawDamage > 0 && !gameData.playersRedirectingAllCreatureDamage.isEmpty()) {
                List<UUID> redirectControllers = gameData.orderedPlayerIds.stream()
                        .filter(gameData.playersRedirectingAllCreatureDamage::contains).toList();
                if (!redirectControllers.isEmpty()) {
                    queueCreatureDamageRedirectChoice(gameData, entry, target.getId(), rawDamage,
                            redirectControllers);
                    return 0;
                }
            }
            if (!gameData.resolvingDeclinedAllCreatureDamageRedirect) {
                rawDamage = damagePreventionService.applyAllCreatureDamageRedirectToController(
                        gameData, target, sourcePermId, rawDamage);
                processSourceRedirectDamage(gameData, entry);
            }
            rawDamage = damagePreventionService.applyEnchantedCreatureDamageRedirectToController(
                    gameData, target, sourcePermId, rawDamage);
            processSourceRedirectDamage(gameData, entry);
            if (rawDamage <= 0) return 0;
            rawDamage = damagePreventionService.applySourcePermanentAndControllerNextDamageRedirectToPermanent(
                    gameData, target.getId(), sourcePermId, rawDamage);
            processSourceRedirectDamage(gameData, entry);
            if (rawDamage <= 0) return 0;
            // Apply creature-specific redirect shields (e.g. Oracle's Attendants): redirect all damage from
            // a chosen source to the protected creature onto another permanent.
            UUID redirectSourceId = sourcePermId != null ? sourcePermId
                    : entry.getEffectiveDamageSourceCard() == null ? null : entry.getEffectiveDamageSourceCard().getId();
            rawDamage = damagePreventionService.applyCreatureRedirectShields(
                    gameData, target.getId(), redirectSourceId, rawDamage);
            processSourceRedirectDamage(gameData, entry);
        }
        if (applyDralnuReplacement(gameData, target, rawDamage) > 0) {
            return 0;
        }
        if (!targetDamageUnpreventable && damageSource == null && rawDamage > 0) {
            int prevented = Math.min(rawDamage, gameQueryService.getSpellDamagePrevention(gameData, entry));
            if (prevented > 0) {
                rawDamage -= prevented;
                gameLogService.append(gameData, GameLog.textCardText(prevented + " of ",
                        target.getCard(), "'s damage is prevented."));
            }
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
        if (!targetDamageUnpreventable && sourcePermId == null && entry.getCard() != null) {
            UUID sourceCardId = entry.getEffectiveDamageSourceCard().getId();
            rawDamage = damagePreventionService.applyTargetSourcePreventionShield(
                    gameData, target.getId(), sourceCardId, rawDamage);
            rawDamage = damagePreventionService.applyChosenSourceNextDamageToAnyTargetShield(
                    gameData, sourceCardId, rawDamage, target.getId(), entry);
            processEyeForAnEyeReflections(gameData);
            rawDamage = damagePreventionService.applyControllerCreaturesNextSourceDamageShield(
                    gameData, targetControllerId, sourceCardId, rawDamage);
        }
        if (!targetDamageUnpreventable) {
            rawDamage = damagePreventionService.applyChannelHarmPreventionToPermanent(
                    gameData, target, sourceControllerId, rawDamage);
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
        sourceColors = gameQueryService.getDamageSourceColors(gameData, sourceColors);
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
        if (!targetDamageUnpreventable
                && gameQueryService.isDamageFromNamedPlanePreventedForControlledPermanent(
                gameData, entry, targetControllerId)) {
            gameLogService.append(gameData, GameLog.textCardText("Damage to ", target.getCard(), " is prevented."));
            return 0;
        }
        Permanent effectiveDamageSource = damageSource;
        if (effectiveDamageSource == null && entry != null && entry.getSourcePermanentId() != null) {
            effectiveDamageSource = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        }
        // Uncle Istvan: "Prevent all damage that would be dealt to this creature by creatures." Noncombat
        // path — combat damage is prevented in DamagePreventionService.applyCreaturePreventionShield.
        if (!targetDamageUnpreventable
                && gameQueryService.isCreatureSourceDamageToSelfPrevented(gameData, target, entry, damageSource)) {
            gameLogService.append(gameData, GameLog.textCardText("Damage to ", target.getCard(), " is prevented."));
            return 0;
        }
        if (!targetDamageUnpreventable
                && gameQueryService.isDamageFromDesertsToSelfPrevented(
                gameData, target, entry, damageSource, false)) {
            gameLogService.append(gameData, GameLog.textCardText("Damage to ", target.getCard(), " is prevented."));
            return 0;
        }
        if (!targetDamageUnpreventable
                && gameQueryService.isDamageFromDesertsToCamelOrBandedCreaturePrevented(
                gameData, target, entry, damageSource, false)) {
            gameLogService.append(gameData, GameLog.textCardText("Damage to ", target.getCard(), " is prevented."));
            return 0;
        }
        if (!targetDamageUnpreventable
                && gameQueryService.isDamageFromControlledSourceToControlledCreaturePrevented(
                gameData, target, sourceControllerId)) {
            gameLogService.append(gameData, GameLog.textCardText("Damage to ", target.getCard(), " is prevented."));
            return 0;
        }
        if (!targetDamageUnpreventable
                && gameQueryService.isDamageFromMatchingSourcePreventedForControlledCreature(
                gameData, target, effectiveDamageSource)) {
            gameLogService.append(gameData, GameLog.textCardText("Damage to ", target.getCard(), " is prevented."));
            return 0;
        }
        if (!targetDamageUnpreventable && gameQueryService.isPlaneswalker(gameData, target)) {
            rawDamage = damagePreventionService.applyComeuppancePrevention(
                    gameData, targetControllerId, rawDamage, sourceCardForBonus,
                    sourcePermanentForBonus, sourceControllerId, false);
            processPendingRedirectDamage(gameData);
        }
        if (!targetDamageUnpreventable
                && gameQueryService.isArtifactDamageToEnchantedCreaturePrevented(
                gameData, target, effectiveDamageSource,
                effectiveDamageSource == null && entry != null ? entry.getEffectiveDamageSourceCard() : null)) {
            gameLogService.append(gameData, GameLog.textCardText("Damage to ", target.getCard(), " is prevented."));
            return 0;
        }
        if (!targetDamageUnpreventable
                && gameQueryService.isArtifactDamageToSelfPrevented(
                gameData, target, effectiveDamageSource,
                effectiveDamageSource == null && entry != null ? entry.getEffectiveDamageSourceCard() : null)) {
            gameLogService.append(gameData, GameLog.textCardText("Damage to ", target.getCard(), " is prevented."));
            return 0;
        }
        int damage = damagePreventionService.applyCreaturePreventionShield(
                gameData, target, rawDamage, false, effectiveDamageSource);
        // Djeru, With Eyes Open: "If a source would deal damage to a planeswalker you control, prevent
        // N of that damage." Applied before recording/triggers so reflection and damage-counting see the
        // reduced amount; the loyalty branch below then removes the reduced amount.
        if (!targetDamageUnpreventable && gameQueryService.isPlaneswalker(gameData, target)) {
            damage -= damagePreventionService.applyPlaneswalkerFixedPerSourceDamagePrevention(gameData, targetControllerId, damage);
            damage -= damagePreventionService.applyAllButOneDamageToPlaneswalkerPrevention(
                    gameData, targetControllerId, damage, false);
        }
        damagePreventionService.applyDamageHealingReplacement(gameData, target, damage);

        UUID damageSourceControllerId = damageSource != null
                ? gameQueryService.findPermanentController(gameData, damageSource.getId())
                : entry.getControllerId();
        if (gameQueryService.isCreature(gameData, target)
                && gameQueryService.noncombatDamageToOpponentCreatureAsCounters(
                        gameData, damageSourceControllerId, targetControllerId)) {
            placeDamageMinusOneCounters(gameData, entry, target, damage, damageSourceControllerId,
                    damageSource != null ? damageSource.getCard() : entry.getCard());
            processPendingRedirectDamage(gameData);
            return 0;
        }

        if (damageSource != null) {
            graveyardService.recordCreatureDamagedByPermanent(gameData, damageSource.getId(), target, damage);
        } else if (entry.getSourcePermanentId() != null) {
            graveyardService.recordCreatureDamagedByPermanent(gameData, entry.getSourcePermanentId(), target, damage);
        } else {
            graveyardService.recordCreatureDamagedBySource(
                    gameData, damageSourceKey(entry, null), target, damage);
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
                    gameData, target, targetControllerId, excessDamage);
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
     * Deals damage to a creature bypassing all prevention effects (shields, protection, global prevention).
     * Used for effects where "the damage can't be prevented" (e.g. Combust).
     */
    public void dealCreatureDamageUnpreventable(GameData gameData, StackEntry entry, Permanent target, int rawDamage) {
        // Defense in depth: a creature can never deal negative damage. Guards against any upstream
        // computation (e.g. future power-based effects) that might produce a negative value.
        int damage = Math.max(0, rawDamage);
        if (!target.isDamageCantBePreventedOrRedirectedThisTurn() && damage > 0) {
            UUID redirectedPlayerId = damagePreventionService.getTargetSorceryDamageRedirectController(gameData, entry);
            if (redirectedPlayerId != null) {
                boolean previous = gameData.damageCantBePreventedThisTurn;
                gameData.damageCantBePreventedThisTurn = true;
                try {
                    gameLogService.append(gameData, GameLog.cardThen(entry.getEffectiveDamageSourceCard(),
                            "'s damage to " + target.getCard().getName()
                                    + " is dealt to its controller instead."));
                    dealDamageToPlayer(gameData, entry, redirectedPlayerId, damage);
                } finally {
                    gameData.damageCantBePreventedThisTurn = previous;
                }
                return;
            }
        }
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
            damage += gameQueryService.getControllerNoncombatDamageBonus(
                    gameData, sourceControllerId);
            damage += gameQueryService.getPerpetualNoncombatDamageBonus(gameData, entry);
        }

        if (!target.isDamageCantBePreventedOrRedirectedThisTurn()) {
            damage = damagePreventionService.applyEnchantedCreatureDamageRedirectToController(
                    gameData, target, entry.getSourcePermanentId(), damage);
            processSourceRedirectDamage(gameData, entry);
        }
        if (damage <= 0) return;

        boolean previous = gameData.damageCantBePreventedThisTurn;
        gameData.damageCantBePreventedThisTurn = true;
        try {
            damage = damagePreventionService.applyCreaturePreventionShield(
                    gameData, target, damage, false, sourcePermanent);
        } finally {
            gameData.damageCantBePreventedThisTurn = previous;
        }

        recordRedSourceNoncombatDamage(gameData, entry.getEffectiveDamageSourceCard(), sourcePermanent,
                sourceControllerId, damage);
        if (applyDralnuReplacement(gameData, target, damage) > 0) {
            return;
        }
        damagePreventionService.applyDamageHealingReplacement(gameData, target, damage);

        if (entry.getSourcePermanentId() != null) {
            graveyardService.recordCreatureDamagedByPermanent(gameData, entry.getSourcePermanentId(), target, damage);
        } else {
            graveyardService.recordCreatureDamagedBySource(
                    gameData, damageSourceKey(entry, null), target, damage);
        }

        if (damage > 0) {
            recordExcessDamageToCreatureIfAny(gameData, entry, target, sourcePermanent, damage);
            accumulateSourceDamageForReflection(gameData, entry.getEffectiveDamageSourceCard(),
                    entry.getControllerId(), entry.getSourcePermanentId(), damage,
                    null, gameQueryService.findPermanentController(gameData, target.getId()), target.getId(), entry);
            triggerCollectionService.checkDelayedWatchedCreatureDealtDamageByAttackingCreatureTriggers(
                    gameData, sourcePermanent, target, damage);
            triggerCollectionService.checkDealtDamageToCreatureTriggers(
                    gameData, target, damage, entry.getControllerId(), entry.getEffectiveDamageSourceCard(),
                    entry.getSourcePermanentId());
            triggerCollectionService.checkNoncombatDamageToSelfTriggers(
                    gameData, target, damage, entry.getControllerId(), entry.getEffectiveDamageSourceCard(),
                    entry.getSourcePermanentId());
            triggerCollectionService.checkAllySourceDealtNoncombatDamageToCreatureTriggers(
                    gameData, sourceControllerId, target, damage);

            // Fire ON_OPPONENT_CREATURE_DEALT_DAMAGE triggers (e.g. Kazarov)
            UUID damagedCreatureControllerId = gameQueryService.findPermanentController(gameData, target.getId());
            if (damagedCreatureControllerId != null) {
                triggerCollectionService.checkOpponentCreatureDealtDamageTriggers(gameData, damagedCreatureControllerId);
                triggerCollectionService.checkTemporaryGlobalOpponentCreatureDealtDamageTriggers(
                        gameData, target, damagedCreatureControllerId, damage);
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
        gameData.recordNoncombatDamageToPermanent(target.getId(), damage);
        recordDamageToPermanent(gameData, target.getId(), damage, entry, sourcePermanent);
        triggerCollectionService.checkAnyPermanentDealtDamageTriggers(gameData, target, damage);
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
                ? gameQueryService.findPermanentController(gameData, damageSource.getId())
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
     * current controller when it is still on the battlefield.
     */
    public void checkSpellLifelink(GameData gameData, StackEntry entry, int effectiveDamage) {
        if (effectiveDamage <= 0) return;
        if (!gameQueryService.shouldControllerSpellHaveLifelink(gameData, entry)) return;
        UUID controllerId = entry.getSourcePermanentId() == null ? null
                : gameQueryService.findPermanentController(gameData, entry.getSourcePermanentId());
        if (controllerId == null) controllerId = entry.getDamageSourceControllerId();
        lifeSupport.applyGainLife(gameData, controllerId != null ? controllerId : entry.getControllerId(), effectiveDamage,
                "lifelink", entry.getEffectiveDamageSourceCard(), entry.getEntryType());
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
        if (!gameQueryService.isDamagePreventable(gameData) || entry.getSourcePermanentId() == null) return false;
        Permanent source = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (source != null && (gameQueryService.isPreventedFromDealingDamage(gameData, source)
                || gameQueryService.isDamageFromPermanentSourcePrevented(gameData, source)
                || gameQueryService.isDamageByCreaturePrevented(gameData, source)
                || gameData.isPreventedFromDealingDamage(entry.getSourcePermanentId()))) return true;
        // Defang / Heart of Light: an aura can blank all damage dealt by the enchanted permanent,
        // including damage from its own activated and triggered abilities.
        return source != null
                && (gameQueryService.hasAuraWithEffect(gameData, source,
                        effect -> effect instanceof PreventAllDamageDealtByEnchantedCreatureEffect prevented
                                && !prevented.combatOnly())
                    || gameQueryService.hasAuraWithEffect(gameData, source, PreventAllDamageToAndByEnchantedCreatureEffect.class));
    }

    private boolean isGlobalCreaturePreventionForEntry(GameData gameData, StackEntry entry) {
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

    private void applyGlobalCreaturePreventionLifeGain(GameData gameData, StackEntry entry, int damage,
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
            if (gameQueryService.isPlaneswalker(gameData, targetPermanent)
                    && !(targetPermanent.isDamageCantBePreventedOrRedirectedThisTurn()
                    && gameQueryService.isCreature(gameData, targetPermanent))) {
                if (!cantBeRedirected) {
                    UUID sourcePermanentId = entry.getSourcePermanentId();
                    rawDamage = damagePreventionService.applyCreatureRedirectShields(
                            gameData, targetPermanent.getId(), sourcePermanentId, rawDamage);
                    processSourceRedirectDamage(gameData, entry);
                    if (rawDamage <= 0) {
                        return 0;
                    }
                }
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
                    return 0;
                }
                if (!targetPermanent.isDamageCantBePreventedOrRedirectedThisTurn() && rawDamage > 0) {
                    int prevented = Math.min(rawDamage, gameQueryService.getSpellDamagePrevention(gameData, entry));
                    if (prevented > 0) {
                        rawDamage -= prevented;
                        gameLogService.append(gameData, GameLog.textCardText(prevented + " of ",
                                targetPermanent.getCard(), "'s damage is prevented."));
                    }
                }
                // "Prevent all damage that would be dealt to ~" (e.g. Gideon of the Trials 0) also stops
                // loyalty loss. The creature-damage path applies this set in DamagePreventionService, but
                // the loyalty branch below bypasses it, so guard it here.
                if (gameQueryService.isDamagePreventable(gameData)
                        && (gameData.creaturesWithAllDamagePrevented.contains(targetPermanent.getId())
                        || gameQueryService.hasActiveStaticEffect(
                                gameData, targetPermanent, PreventAllDamageEffect.class))) {
                    gameLogService.append(gameData, GameLog.cardThen(source, "'s damage is prevented."));
                    return 0;
                }
                if (gameQueryService.isCreature(gameData, targetPermanent)
                        && damagePreventionService.replaceNextDamageToTargetWithDestruction(
                        gameData, targetPermanent, rawDamage)) {
                    return 0;
                }
                // CR 306.8: damage dealt to a planeswalker removes that many loyalty counters from it
                // (SBAs then move it to the graveyard once it has 0 loyalty). Mirrors the combat path.
                int loyaltyDamage = gameQueryService.applyDamageReplacementEffects(
                        gameData, entry, null, Math.max(0, rawDamage));
                // Djeru, With Eyes Open: prevent N of the damage dealt to a planeswalker you control.
                UUID pwControllerId = gameQueryService.findPermanentController(gameData, targetPermanent.getId());
                Permanent sourcePermanent = entry.getSourcePermanentId() == null
                        ? null
                        : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
                Set<CardColor> sourceColors = sourcePermanent == null
                        ? gameQueryService.getEffectiveCardColors(gameData, source)
                        : gameQueryService.getEffectiveColors(gameData, sourcePermanent);
                UUID damageSourceId = damageSourceKey(entry, sourcePermanent);
                loyaltyDamage = damagePreventionService.applyChosenSourceNextDamageToAnyTargetShield(
                        gameData, damageSourceId, loyaltyDamage, targetPermanent.getId(), entry);
                processEyeForAnEyeReflections(gameData);
                if (loyaltyDamage <= 0) {
                    return 0;
                }
                if (damagePreventionService.isColorDamagePreventedForTarget(gameData, targetPermanent.getId(), sourceColors)) {
                    gameLogService.append(gameData, GameLog.textCardText("Damage to ", targetPermanent.getCard(), " is prevented."));
                    return 0;
                }
                loyaltyDamage = damagePreventionService.applyPermanentDamagePreventionShield(
                        gameData, targetPermanent, loyaltyDamage);
                loyaltyDamage = damagePreventionService.applyControllerAndPermanentsNoncombatDamagePrevention(
                        gameData, targetPermanent, loyaltyDamage);
                loyaltyDamage = damagePreventionService.applyComeuppancePrevention(
                        gameData, pwControllerId, loyaltyDamage, source, sourcePermanent,
                        sourceControllerId, false);
                processPendingRedirectDamage(gameData);
                if (loyaltyDamage <= 0) {
                    return 0;
                }
                loyaltyDamage -= damagePreventionService.applyPlaneswalkerFixedPerSourceDamagePrevention(gameData, pwControllerId, loyaltyDamage);
                loyaltyDamage -= damagePreventionService.applyAllButOneDamageToPlaneswalkerPrevention(
                        gameData, pwControllerId, loyaltyDamage, false);
                int damageDealt = loyaltyDamage;
                int loyaltyCounterRemoval = gameQueryService.applyPlaneswalkerLoyaltyDamageReplacement(
                        gameData, targetPermanent, damageDealt);
                if (damageDealt > 0) {
                    recordRedSourceNoncombatDamage(gameData, source, sourcePermanent, sourceControllerId,
                            damageDealt);
                    gameData.recordNoncombatDamageToPermanent(targetPermanent.getId(), damageDealt);
                    recordDamageToPermanent(gameData, targetPermanent.getId(), damageDealt, entry, sourcePermanent);
                    int excessDamage = Math.max(0, damageDealt - (gameQueryService.isToughnessAsLoyaltyPermanent(
                            gameData, targetPermanent) ? gameQueryService.getEffectiveToughness(gameData, targetPermanent)
                            : targetPermanent.getCounterCount(CounterType.LOYALTY)));
                    if (excessDamage > 0) {
                        triggerCollectionService.checkOpponentPermanentDealtExcessDamageTriggers(
                                gameData, targetPermanent, pwControllerId, excessDamage);
                    }
                    if (entry.getEntryType() == StackEntryType.INSTANT_SPELL
                            || entry.getEntryType() == StackEntryType.SORCERY_SPELL) {
                        gameData.recordQualifyingDamageControllerToPermanent(
                                targetPermanent.getId(), entry.getControllerId());
                    }
                    triggerCollectionService.checkAnyPermanentDealtDamageTriggers(
                            gameData, targetPermanent, damageDealt);
                    triggerCollectionService.checkAllyDealtDamageToPlaneswalkerTriggers(
                            gameData, sourcePermanent, entry.getControllerId(), targetPermanent.getId(),
                            damageDealt, false, null);
                    accumulateSourceDamageForReflection(gameData, source, entry.getControllerId(),
                            entry.getSourcePermanentId(), damageDealt, null, pwControllerId,
                            targetPermanent.getId(), entry);
                    queueEnchantedCreatureDealsDamageTrigger(gameData, entry, sourcePermanent, damageDealt);
                    gameData.recordDamageDealtBySource(damageSourceKey(entry, sourcePermanent), damageDealt);
                    gameData.recordDamageSourceControlledBy(
                            entry.getSourcePermanentId() != null ? entry.getSourcePermanentId() : entry.getCard().getId(),
                            entry.getControllerId());
                    gameData.recordDamageRecipientBySource(entry.getSourcePermanentId(), targetPermanent.getId());
                    targetPermanent.setCounterCount(CounterType.LOYALTY,
                            targetPermanent.getCounterCount(CounterType.LOYALTY) - loyaltyCounterRemoval);
                    gameLogService.append(gameData, GameLog.cardTextCard(source,
                            " deals " + damageDealt + " damage to ", targetPermanent.getCard(),
                            " (" + targetPermanent.getCounterCount(CounterType.LOYALTY) + " loyalty remaining)."));
                    checkSpellLifelink(gameData, entry, damageDealt);
                }
                return Math.max(0, damageDealt);
            }
            // A battle deliberately has no arm of its own here: it falls through to
            // dealCreatureDamage, whose CR 120.3h branch removes the defense counters after the
            // shared pipeline has applied prevention shields, redirects, damage multipliers and
            // spell lifelink (CR 702.15b). The planeswalker arm above predates that pipeline and
            // still open-codes its own; the two must not diverge again.
            Map<UUID, Integer> damageBefore = cantRegenerate
                    ? new HashMap<>(gameData.damageDealtToPermanentsThisTurn)
                    : Map.of();
            int damageDealt = dealCreatureDamage(gameData, entry, targetPermanent, rawDamage,
                    cantBeRedirected);
            if (cantRegenerate) {
                gameData.damageDealtToPermanentsThisTurn.forEach((permanentId, totalDamage) -> {
                    if (totalDamage > damageBefore.getOrDefault(permanentId, 0)) {
                        Permanent damagedPermanent = gameQueryService.findPermanentById(gameData, permanentId);
                        if (damagedPermanent != null && gameQueryService.isCreature(gameData, damagedPermanent)) {
                            damagedPermanent.setCantRegenerateThisTurn(true);
                        }
                    }
                });
            }
            return damageDealt;
        }
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
        if (exileInsteadOfDie) {
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
        if (exileInsteadOfDie) {
            for (List<Permanent> battlefield : gameData.playerBattlefields.values()) {
                for (Permanent permanent : battlefield) {
                    int sourceDamage = gameData.damageDealtToPermanentsBySourceThisTurn
                            .getOrDefault(permanent.getId(), Map.of()).getOrDefault(damageSourceId, 0);
                    if (sourceDamage > damageBefore.getOrDefault(permanent.getId(), 0)
                            && gameQueryService.isCreature(gameData, permanent)) {
                        permanent.setExileInsteadOfDieThisTurn(true);
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

    private boolean beginSpellDamageModifierOrder(GameData gameData, StackEntry entry, UUID playerId, int rawDamage) {
        if (rawDamage <= 0) {
            return false;
        }
        List<ChoiceContext.SpellDamageModifier> modifiers = new ArrayList<>();
        gameData.forEachPermanent((controller, permanent) -> {
            if (permanent.isFaceDown() || gameQueryService.hasLostPrintedAbilities(gameData, permanent)) return;
            collectSpellDamageModifiers(gameData, permanent.getCard(), modifiers);
        });
        if (gameData.planechase != null) {
            for (var plane : gameData.planechase.faceUp) {
                collectSpellDamageModifiers(gameData, plane.getCard(), modifiers);
            }
        }
        if (gameQueryService.getSpellDamageReduction(gameData, entry) <= 0) {
            modifiers.removeIf(modifier -> !modifier.multiply());
        }
        int enchantedMultiplier = gameQueryService.getEnchantedPlayerDamageMultiplier(gameData, playerId);
        if (enchantedMultiplier > 1) {
            modifiers.add(new ChoiceContext.SpellDamageModifier(
                    "Multiply damage to enchanted player by " + enchantedMultiplier, enchantedMultiplier, true));
        }
        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        UUID sourceControllerId = sourcePermanent == null ? entry.getControllerId()
                : gameQueryService.findPermanentController(gameData, sourcePermanent.getId());
        int recipientMultiplier = gameQueryService.getDamageToRecipientMultiplier(
                gameData, playerId, sourceControllerId);
        if (recipientMultiplier > 1) {
            modifiers.add(new ChoiceContext.SpellDamageModifier(
                    "Multiply damage to recipient by " + recipientMultiplier, recipientMultiplier, true));
        }
        boolean sanctuary = gameData.playerBattlefields.values().stream().flatMap(List::stream)
                .anyMatch(permanent -> gameQueryService.hasActiveStaticEffect(
                        gameData, permanent, CrumblingSanctuaryDamageReplacementEffect.class));
        if (sanctuary) {
            modifiers.add(new ChoiceContext.SpellDamageModifier(
                    "Crumbling Sanctuary: exile library cards instead of damage", 0, false, true));
        }
        int preventionShield = gameData.playerDamagePreventionShields.getOrDefault(playerId, 0);
        if (preventionShield > 0 && gameQueryService.isDamagePreventable(gameData)) {
            modifiers.add(new ChoiceContext.SpellDamageModifier(
                    "Prevent the next " + preventionShield + " damage to you", preventionShield,
                    false, false, true));
        }
        int multiplier = modifiers.stream().filter(ChoiceContext.SpellDamageModifier::multiply)
                .mapToInt(ChoiceContext.SpellDamageModifier::amount)
                .reduce(1, (left, right) -> left * right);
        if (multiplier <= 1 || modifiers.stream().allMatch(
                ChoiceContext.SpellDamageModifier::multiply)) {
            return false;
        }
        beginSpellDamageModifierChoice(gameData,
                new ChoiceContext.SpellDamageModifierOrder(
                        entry, playerId, rawDamage / Math.max(1, multiplier / enchantedMultiplier / recipientMultiplier),
                        modifiers, gameData.unpreventableDamageInProgress));
        return true;
    }

    private void collectSpellDamageModifiers(GameData gameData, Card card,
            List<ChoiceContext.SpellDamageModifier> modifiers) {
        for (CardEffect effect : card.getEffects(EffectSlot.STATIC)) {
            int amount;
            boolean multiply;
            if (effect instanceof GlobalDamageMultiplyingEffect global) {
                amount = MaroGoneNutsSupport.apply(
                        gameData, effect, global.damageMultiplierFactor());
                if (amount <= 1) continue;
                multiply = true;
            } else if (effect instanceof ReduceSpellDamageEffect reduction) {
                amount = reduction.amount();
                if (amount <= 0) continue;
                multiply = false;
            } else {
                continue;
            }
            modifiers.add(new ChoiceContext.SpellDamageModifier(
                    card.getName() + " (" + (modifiers.size() + 1) + "): "
                            + (multiply ? "multiply damage by " : "subtract damage by ") + amount,
                    amount, multiply));
        }
    }

    private void beginSpellDamageModifierChoice(GameData gameData,
            ChoiceContext.SpellDamageModifierOrder order) {
        interactionHandlerRegistry.begin(gameData, new PendingInteraction.ColorChoice(
                order.recipientId(), null, null, order,
                order.remaining().stream().map(ChoiceContext.SpellDamageModifier::label).toList(),
                "Choose the damage replacement effect to apply next."));
    }

    /** Applies a chosen modifier once, then continues the same impending damage event. */
    public void resolveSpellDamageModifierOrder(GameData gameData,
            ChoiceContext.SpellDamageModifierOrder order, String label) {
        List<ChoiceContext.SpellDamageModifier> remaining =
                new ArrayList<>(order.remaining());
        var selected = remaining.stream().filter(modifier -> modifier.label().equals(label)).findFirst()
                .orElseThrow(() -> new IllegalArgumentException("Invalid damage replacement effect"));
        remaining.remove(selected);
        if (selected.replacesWithExile()) {
            applyCrumblingSanctuaryReplacement(gameData, order.recipientId(), order.damage());
            return;
        }
        int damage = applySpellDamageModifier(gameData, order.recipientId(), order.damage(), selected);
        boolean onlyMultiplication = remaining.stream().allMatch(
                ChoiceContext.SpellDamageModifier::multiply);
        boolean onlyReduction = remaining.stream().noneMatch(
                ChoiceContext.SpellDamageModifier::multiply);
        if (damage > 0 && remaining.stream().anyMatch(ChoiceContext.SpellDamageModifier::replacesWithExile)
                && remaining.size() > 1 || damage > 0 && !onlyMultiplication && !onlyReduction) {
            beginSpellDamageModifierChoice(gameData,
                    new ChoiceContext.SpellDamageModifierOrder(
                            order.entry(), order.recipientId(), damage, remaining, order.unpreventable()));
            return;
        }
        for (var modifier : remaining) {
            if (modifier.replacesWithExile()) {
                applyCrumblingSanctuaryReplacement(gameData, order.recipientId(), damage);
                return;
            }
            damage = applySpellDamageModifier(gameData, order.recipientId(), damage, modifier);
        }
        boolean previousUnpreventable = gameData.unpreventableDamageInProgress;
        gameData.unpreventableDamageInProgress = order.unpreventable();
        try {
            dealDamageToPlayerFromSource(gameData, order.entry(), order.recipientId(), damage, true);
        } finally {
            gameData.unpreventableDamageInProgress = previousUnpreventable;
        }
    }

    private int applySpellDamageModifier(GameData gameData, UUID playerId, int damage,
            ChoiceContext.SpellDamageModifier modifier) {
        if (modifier.playerPreventionShield()) {
            return damagePreventionService.applyPlayerPreventionShield(gameData, playerId, damage);
        }
        return modifier.multiply() ? damage * modifier.amount() : Math.max(0, damage - modifier.amount());
    }

    private void dealDamageToPlayerFromSource(GameData gameData, StackEntry entry, UUID playerId, int rawDamage) {
        dealDamageToPlayerFromSource(gameData, entry, playerId, rawDamage, false);
    }

    private void dealDamageToPlayerFromSource(GameData gameData, StackEntry entry, UUID playerId, int rawDamage,
                                              boolean spellDamageModifiersApplied) {
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
            playerId = redirectedTarget;
        }
        UUID targetSorceryControllerId = damagePreventionService.getTargetSorceryDamageRedirectController(gameData, entry);
        if (rawDamage > 0 && targetSorceryControllerId != null
                && !targetSorceryControllerId.equals(playerId)) {
            gameLogService.append(gameData, GameLog.cardThen(source,
                    "'s damage to " + gameData.playerIdToName.get(playerId)
                            + " is dealt to its controller instead."));
            playerId = targetSorceryControllerId;
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
        if (!spellDamageModifiersApplied && beginSpellDamageModifierOrder(gameData, entry, playerId, rawDamage)) {
            return;
        }
        if (gameQueryService.playerHasFlying(gameData, playerId)
                && gameQueryService.isDamageSourceCreature(gameData, entry, sourcePermanent)
                && !gameQueryService.sourceHasKeyword(gameData, entry, sourcePermanent, Keyword.FLYING)) {
            gameLogService.append(gameData, GameLog.cardThen(source,
                    "'s damage to " + gameData.playerIdToName.get(playerId) + " is prevented by flying."));
            return;
        }
        if (gameQueryService.isSpellDamageToControllerAndPermanentsPrevented(gameData, entry, playerId)) {
            gameLogService.append(gameData, GameLog.cardThen(source,
                    "'s damage to " + gameData.playerIdToName.get(playerId) + " is prevented."));
            return;
        }
        // Curse of Bloodletting and similar: double damage dealt to the enchanted player (replacement effect)
        if (!spellDamageModifiersApplied) {
            rawDamage *= gameQueryService.getEnchantedPlayerDamageMultiplier(gameData, playerId);
            rawDamage *= gameQueryService.getDamageToRecipientMultiplier(gameData, playerId, sourceControllerId);
        }
        if (rawDamage > 0) {
            rawDamage += gameQueryService.getNoncreatureSourceDamageBonus(
                    gameData, entry, playerId, null);
            rawDamage += gameQueryService.getPlayersAndBattlesDamageBonus(
                    gameData, true, null);
            rawDamage += gameQueryService.getControllerDamageToOpponentBonus(
                    gameData, sourceControllerId, playerId, false,
                    sourcePermanent == null ? entry.getSourcePermanentId() : sourcePermanent.getId());
            rawDamage += gameQueryService.getAdditionalSpellDamageToOpponentsBonus(
                    gameData, entry, playerId);
            rawDamage += gameQueryService.getControllerNoncombatDamageBonus(
                    gameData, sourceControllerId);
            rawDamage += gameQueryService.getPerpetualNoncombatDamageBonus(gameData, entry);
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
        if (!spellDamageModifiersApplied) {
            rawDamage = Math.max(0, rawDamage - gameQueryService.getSpellDamageReduction(gameData, entry));
        }
        Set<CardColor> sourceColors = sourcePermanent == null
                ? gameQueryService.getEffectiveCardColors(gameData, source)
                : gameQueryService.getEffectiveColors(gameData, sourcePermanent);
        Set<CardColor> damageSourceColors = gameQueryService.getDamageSourceColors(gameData, sourceColors);
        UUID damageSourceId = damageSourceKey(entry, sourcePermanent);
        // Tok-Tok, Volcano Born: a source of a matching colour deals that much damage plus N instead.
        if (rawDamage > 0) {
            rawDamage += gameQueryService.getDamageToPlayerColorSourceBonus(gameData,
                    damageSourceColors);
            rawDamage += gameQueryService.getAdditionalDamageToOpponentsBonus(
                    gameData, sourceControllerId, source, sourcePermanent, playerId);
        }
        rawDamage = gameQueryService.applyDamageReplacementEffects(gameData, entry, playerId, rawDamage);
        rawDamage = gameQueryService.applyOjerAxonilDamageReplacement(
                gameData, rawDamage, damageSourceColors, sourceControllerId, playerId);
        if (rawDamage > 0) {
            int prevented = Math.min(rawDamage, gameQueryService.getSpellDamagePrevention(gameData, entry));
            if (prevented > 0) {
                rawDamage -= prevented;
                gameLogService.append(gameData, GameLog.cardThen(source,
                        "'s " + prevented + " damage to " + gameData.playerIdToName.get(playerId) + " is prevented."));
            }
        }
        if (damagePreventionService.isColorDamagePreventedForTarget(
                gameData, playerId, damageSourceColors)) {
            gameLogService.append(gameData, GameLog.cardThen(source,
                    "'s damage to " + gameData.playerIdToName.get(playerId) + " is prevented."));
            return;
        }
        boolean sourceDamagePrevented = damagePreventionService.isSourceDamagePreventedForPlayer(
                gameData, playerId, damageSourceId);
        if (sourceDamagePrevented) {
            damagePreventionService.applySourceDamagePreventionForPlayer(
                    gameData, playerId, damageSourceId, rawDamage, damageSourceColors);
        }
        if (sourceDamagePrevented
                || damagePreventionService.isNoncombatDamageFromAttackerPreventedForPlayer(gameData, playerId, damageSourceId)
                || gameQueryService.isDamageFromMatchingSourcePreventedForPlayer(gameData, playerId, entry)
                || isGlobalCreaturePreventionForEntry(gameData, entry)
                || isSourcePermanentPreventedFromDealingDamage(gameData, entry)) {
            applyGlobalCreaturePreventionLifeGain(gameData, entry, rawDamage, playerId);
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
        if (gameQueryService.isDamagePreventable(gameData)
                && gameQueryService.playerHasProtectionFromOpponents(gameData, playerId, sourceControllerId)) {
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
        if (gameQueryService.isDamagePreventable(gameData)
                && gameQueryService.playerHasProtectionFromChosenCardType(
                gameData, playerId, source, sourcePermanent)) {
            gameLogService.append(gameData, GameLog.cardThen(source,
                    "'s damage to " + gameData.playerIdToName.get(playerId) + " is prevented."));
            return;
        }
        // Apply source-specific redirect shields (e.g. Harm's Way) before general prevention
        rawDamage = damagePreventionService.applySourceRedirectShields(gameData, playerId, damageSourceId, rawDamage);
        processSourceRedirectDamage(gameData, entry);
        rawDamage = damagePreventionService.applyPlayerSourceNextDamageRedirectShield(
                gameData, playerId, damageSourceId, rawDamage);
        processSourceRedirectDamage(gameData, entry);
        // Reflect Damage: the chosen source's next damage is dealt to that source's controller instead.
        rawDamage = damagePreventionService.applyReflectDamageToSourceControllerShield(
                gameData, damageSourceId, rawDamage);
        processEyeForAnEyeReflections(gameData);
        // Opal-Eye: the chosen source's next damage is dealt to a fixed creature instead.
        rawDamage = damagePreventionService.applySourceNextDamageRedirectToPermanent(
                gameData, damageSourceId, null, rawDamage);
        processSourceRedirectDamage(gameData, entry);
        // Saving Grace: redirect all damage this turn to the player onto the enchanted creature.
        rawDamage = damagePreventionService.applyTurnDamageRedirectToCreature(
                gameData, playerId, null, damageSourceId, rawDamage, false);
        processSourceRedirectDamage(gameData, entry);
        rawDamage = damagePreventionService.applySourcePermanentAndControllerNextDamageRedirectToPlayer(
                gameData, playerId, damageSourceId, rawDamage);
        processSourceRedirectDamage(gameData, entry);
        // Martyrdom: redirect the next N damage to the player onto the creature carrying the ability.
        rawDamage = damagePreventionService.applyPlayerNextDamageRedirectShields(
                gameData, playerId, entry == null ? null : entry.getSourcePermanentId(), rawDamage);
        processSourceRedirectDamage(gameData, entry);
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
                rawDamage = damagePreventionService.applyPlayerNextSourceDamageShield(
                        gameData, playerId, damageSourceId, rawDamage, false, source);
                damagePreventionService.applyEyeForAnEyeReflection(gameData, playerId, entry.getSourcePermanentId(), rawDamage);
                // Apply one-shot Sanctum Guardian / Honorable Passage shields
                rawDamage = damagePreventionService.applyChosenSourceNextDamageToAnyTargetShield(
                        gameData, damageSourceId, rawDamage, playerId, entry);
                processEyeForAnEyeReflections(gameData);
            }
            // Apply one-shot chosen-source prevention to permanents and spells.
            UUID sourceId = entry.getSourcePermanentId() != null
                    ? entry.getSourcePermanentId()
                    : source.getId();
            rawDamage = damagePreventionService.applyPlayerNextSourceDamageShield(gameData, playerId, sourceId, rawDamage);
            processEyeForAnEyeReflections(gameData);
            rawDamage = damagePreventionService.applyChannelHarmPrevention(
                    gameData, playerId, sourceControllerId, rawDamage);
            rawDamage = damagePreventionService.applyComeuppancePrevention(
                    gameData, playerId, rawDamage, source, sourcePermanent,
                    sourceControllerId, false);
            rawDamage = damagePreventionService.applyJudgmentOfAlexanderPrevention(
                    gameData, playerId, rawDamage, source, sourcePermanent,
                    sourceControllerId, false);
            int effectiveDamage = damagePreventionService.applyPlayerPreventionShield(gameData, playerId, rawDamage);
            processPendingRedirectDamage(gameData);
            effectiveDamage = permanentRemovalService.redirectPlayerDamageToEnchantedCreature(
                    gameData, playerId, effectiveDamage, cardName, false, entry.getSourcePermanentId(), source);

            if (damagePreventionService.queueClericPreventionChoice(gameData, entry, playerId,
                    effectiveDamage, false, damagePreventionService.clericPreventionSources(gameData))) {
                return;
            }
            finishPlayerDamageAfterClericChoice(gameData, entry, playerId, effectiveDamage);
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

    /** Resumes the remaining replacements and damage results after an optional Cleric choice. */
    public void finishPlayerDamageAfterClericChoice(GameData gameData, StackEntry entry, UUID playerId,
                                                    int effectiveDamage) {
        Card source = entry.getEffectiveDamageSourceCard();
        Permanent sourcePermanent = entry.getSourcePermanentId() == null ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        UUID sourceControllerId = sourcePermanent == null ? entry.getControllerId()
                : gameQueryService.findPermanentController(gameData, sourcePermanent.getId());
        if (sourceControllerId == null) sourceControllerId = entry.getControllerId();
        Set<CardColor> sourceColors = sourcePermanent == null
                ? gameQueryService.getEffectiveCardColors(gameData, source)
                : gameQueryService.getEffectiveColors(gameData, sourcePermanent);
        UUID damageSourceId = entry.getSourcePermanentId() != null
                ? entry.getSourcePermanentId() : source.getId();
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

        if (damagePreventionService.hasControllerOpponentDamageMillReplacement(
                gameData, sourceControllerId, playerId, effectiveDamage)) {
            int replacedDamage = effectiveDamage;
            effectiveDamage = 0;
            gameLogService.append(gameData, GameLog.cardThen(source,
                    "'s " + replacedDamage + " damage to " + gameData.playerIdToName.get(playerId)
                            + " is prevented and replaced with milling."));
            for (UUID opponentId : gameData.orderedPlayerIds) {
                if (!opponentId.equals(sourceControllerId)) {
                    graveyardService.resolveMillPlayer(gameData, opponentId, replacedDamage);
                }
            }
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

        int angelPrevented = applyAngelOfSufferingReplacement(
                gameData, playerId, effectiveDamage, effectiveDamage);
        if (angelPrevented > 0) {
            effectiveDamage -= angelPrevented;
            gameLogService.append(gameData, GameLog.cardThen(source,
                    "'s " + angelPrevented + " damage to " + gameData.playerIdToName.get(playerId)
                            + " is prevented by Angel of Suffering."));
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

        effectiveDamage -= applyDamageToControllerCounterReplacement(gameData, playerId, effectiveDamage);

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
        } else if (effectiveDamage > 0 && !gameQueryService.canPlayerLoseLife(gameData, playerId)) {
            String playerName = gameData.playerIdToName.get(playerId);
            gameLogService.append(gameData, GameLog.text(playerName + "'s life total can't change."));
        } else if (effectiveDamage > 0 && gameQueryService.damageDoesNotCauseLifeLoss(gameData, playerId)) {
            String playerName = gameData.playerIdToName.get(playerId);
            gameLogService.append(gameData, GameLog.textCardText(
                    playerName + " takes " + effectiveDamage + " damage from ", source, "."));
        } else {
            int currentLife = gameData.getLife(playerId);
            int lifeAfterDamage = currentLife - effectiveDamage
                    * gameQueryService.opponentLifeLossMultiplier(gameData, playerId);
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

        if (effectiveDamage > 0) {
            recordRedSourceNoncombatDamage(gameData, source, sourcePermanent, sourceControllerId,
                    effectiveDamage);
            accumulateSourceDamageForReflection(gameData, source, entry.getControllerId(),
                    entry.getSourcePermanentId(), effectiveDamage, playerId, null, null, entry);
            Permanent sourceCreature = entry.getSourcePermanentId() == null
                    ? null
                    : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
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
            entry.recordPlayerDealtDamage(playerId);
            gameData.recordNoncombatDamageSourceToPlayer(entry.getSourcePermanentId(), playerId);
            String sourcePermanentName = sourcePermanent != null
                    ? sourcePermanent.getCard().getName()
                    : entry.getSourcePermanentSnapshot() == null
                    ? null : entry.getSourcePermanentSnapshot().getCard().getName();
            gameData.recordPermanentDamageSourceNameToPlayer(sourcePermanentName, playerId);
            if (sourcePermanent != null && gameQueryService.isCreature(gameData, sourcePermanent)) {
                gameData.recordCreatureDamageSourceToPlayer(sourcePermanent.getId(), playerId);
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
                triggerCollectionService.checkAllyCreaturesDealDamageToOpponentTriggers(
                        gameData, sourceControllerId, playerId, List.of(triggeringDamageSource));
            }
            triggerCollectionService.checkAllySourceDealtNoncombatDamageToOpponentTriggers(
                    gameData, playerId, entry.getControllerId(), effectiveDamage);
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
     * Processes pending source-specific redirect damage entries (e.g. Harm's Way).
     * The prevented damage is dealt to the redirect target, which can be a player or permanent.
     */
    public void processSourceRedirectDamage(GameData gameData) {
        processSourceRedirectDamage(gameData, null);
    }

    private void processSourceRedirectDamage(GameData gameData, StackEntry entry) {
        if (gameData.pendingSourceRedirectDamage.isEmpty()) return;

        List<SourceDamageRedirectShield> toProcess = new ArrayList<>(gameData.pendingSourceRedirectDamage);
        gameData.pendingSourceRedirectDamage.clear();

        for (SourceDamageRedirectShield redirect : toProcess) {
            UUID sourceId = redirect.damageSourceId() != null ? redirect.damageSourceId()
                    : entry == null ? null : damageSourceKey(entry, null);
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
                    if (gameQueryService.canPlayerLoseLife(gameData, targetId)) {
                        int lifeLoss = redirectEffective
                                * gameQueryService.opponentLifeLossMultiplier(gameData, targetId);
                        gameData.playerLifeTotals.put(targetId,
                                gameQueryService.lifeAfterDamage(gameData, targetId, lifeLoss));
                    }
                    Permanent sourcePermanent = sourceId == null
                            ? null
                            : gameQueryService.findPermanentById(gameData, sourceId);
                    boolean artifactSource = sourcePermanent != null
                            && gameQueryService.isArtifact(gameData, sourcePermanent);
                    gameData.recordDamageToPlayer(targetId, redirectEffective, artifactSource ? redirectEffective : 0);
                    if (entry != null) {
                        entry.recordPlayerDealtDamage(targetId);
                    }
                    gameData.recordDamageDealtBySourceToPlayer(
                            sourceId, targetId, redirectEffective);
                    gameData.recordDamageDealtBySource(sourceId, redirectEffective);
                    gameData.recordDamageRecipientBySource(sourceId, targetId);
                    triggerCollectionService.checkEnchantedPlayerDealtDamageTriggers(
                            gameData, targetId, redirectEffective);
                    triggerCollectionService.checkOpponentDealtDamageTriggers(
                            gameData, targetId, sourceId, redirectEffective);
                }
            } else {
                Permanent targetPerm = gameQueryService.findPermanentById(gameData, targetId);
                if (targetPerm == null) continue;

                gameLogService.append(gameData, GameLog.textCardText(
                        damage + " damage is redirected to ", targetPerm.getCard(), "."));

                // The redirected damage can itself be redirected by the new recipient's own shield
                // (a second Carom); that chained part is dealt when the queue is processed again.
                damage = damagePreventionService.applyCreatureRedirectShields(gameData, targetPerm.getId(), sourceId, damage);
                if (damage <= 0) continue;

                int effectiveDamage = damagePreventionService.applyCreaturePreventionShield(gameData, targetPerm, damage);
                if (effectiveDamage > 0) {
                    if (entry != null && entry.isExilesCreaturesDamaged()
                            && gameQueryService.isCreature(gameData, targetPerm)) {
                        targetPerm.setExileInsteadOfDieThisTurn(true);
                    }
                    gameData.recordDamageDealtBySource(sourceId, effectiveDamage);
                    damagePreventionService.applyDamageHealingReplacement(gameData, targetPerm, effectiveDamage);
                    // A planeswalker destination loses that much loyalty (CR 120.3c) and a battle
                    // destination that many defense counters (CR 120.3h); a permanent that is also
                    // a creature additionally gets marked damage (CR 120.3e).
                    if (gameQueryService.isToughnessAsLoyaltyPermanent(gameData, targetPerm)) {
                        targetPerm.setToughnessAsLoyalty(
                                gameQueryService.getToughnessAsLoyalty(targetPerm) - effectiveDamage);
                    } else if (targetPerm.getCard().hasType(CardType.PLANESWALKER)) {
                        targetPerm.setCounterCount(CounterType.LOYALTY,
                                targetPerm.getCounterCount(CounterType.LOYALTY) - effectiveDamage);
                    }
                    if (targetPerm.getCard().hasType(CardType.BATTLE)) {
                        targetPerm.setCounterCount(CounterType.DEFENSE,
                                targetPerm.getCounterCount(CounterType.DEFENSE) - effectiveDamage);
                    }
                    triggerCollectionService.checkAnyPermanentDealtDamageTriggers(
                            gameData, targetPerm, effectiveDamage);
                    if (targetPerm.getCard().hasType(CardType.BATTLE)) {
                        battleDefeatSupport.checkAfterDefenseRemoved(gameData, targetPerm);
                    }
                    gameData.recordDamageRecipientBySource(sourceId, targetPerm.getId());
                    boolean isCreature = gameQueryService.isCreature(gameData, targetPerm);
                    boolean toughnessAsLoyalty = gameQueryService.isToughnessAsLoyaltyPermanent(gameData, targetPerm);
                    if ((isCreature && !toughnessAsLoyalty)
                            || (!toughnessAsLoyalty && !targetPerm.getCard().hasType(CardType.PLANESWALKER)
                            && !targetPerm.getCard().hasType(CardType.BATTLE))) {
                        // Record only — the state-based action check (CR 704.5g) performs any
                        // destruction once the current damage event finishes.
                        targetPerm.addMarkedDamage(sourceId, effectiveDamage);
                        gameData.recordNoncombatDamageToPermanent(targetPerm.getId(), effectiveDamage);
                        Permanent damageSource = sourceId == null ? null
                                : gameQueryService.findPermanentById(gameData, sourceId);
                        gameData.recordDamageToPermanentFromSource(targetPerm.getId(), effectiveDamage,
                                sourceId, damageSource == null ? null
                                        : gameQueryService.getEffectiveName(gameData, damageSource),
                                damageSource == null ? null
                                        : gameQueryService.findPermanentController(gameData, damageSource.getId()));
                    }
                }
            }
        }
        processSourceRedirectDamage(gameData, entry);
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
                        + ":" + sourceEntry.getResolvingEffectIndex()).getBytes(java.nio.charset.StandardCharsets.UTF_8))
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
        if (sourcePermanent == null) return null;

        List<CardEffect> effects = new ArrayList<>(sourceCard.getEffects(EffectSlot.ON_SELF_DEALS_DAMAGE));
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

        graveyardService.exileCardsFromGraveyard(gameData, playerId, damage);
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

        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return 0;

        for (Permanent permanent : battlefield) {
            if (!permanent.isEchoDamageRedirectionActive()) continue;

            int available = permanent.getCounterCount(CounterType.ECHO);
            int removed = Math.min(available, damage);
            permanent.setCounterCount(CounterType.ECHO, available - removed);

            gameLogService.append(gameData, GameLog.textCardText(
                    gameData.playerIdToName.get(playerId) + " removes " + removed + " echo counter"
                            + (removed == 1 ? "" : "s") + " from ", permanent.getCard(),
                    " instead of taking " + damage + " damage."));
            return damage;
        }
        return 0;
    }

    /**
     * Replaces damage to a controller with counters on a permanent they control. This is a
     * replacement, not prevention, so it still applies when damage cannot be prevented and still
     * replaces the damage if a counter-placement restriction means no counters can actually be
     * added.
     */
    public int applyDamageToControllerCounterReplacement(GameData gameData, UUID playerId, int damage) {
        if (damage <= 0) return 0;

        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return 0;

        for (Permanent permanent : battlefield) {
            for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                if (!(effect instanceof DamageToControllerCounterReplacementEffect replacement)) {
                    continue;
                }

                if (!gameQueryService.cantHaveCounters(gameData, permanent)) {
                    CounterType counterType = replacement.counterType();
                    permanent.setCounterCount(counterType,
                            permanent.getCounterCount(counterType) + damage);
                    String counterName = permanentCounterSupport.counterTypeName(counterType);
                    gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(),
                            " gets " + damage + " " + counterName + " counter"
                                    + (damage == 1 ? "" : "s") + " instead of damage."));
                }
                return damage;
            }
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
