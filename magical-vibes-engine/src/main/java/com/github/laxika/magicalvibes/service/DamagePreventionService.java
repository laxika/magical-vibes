package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CreatureControllerDamageRedirectShield;
import com.github.laxika.magicalvibes.model.CreatureDamageRedirectShield;
import com.github.laxika.magicalvibes.model.DamagePreventionLifeGainShield;
import com.github.laxika.magicalvibes.model.DamageRedirectShield;
import com.github.laxika.magicalvibes.model.EyeForAnEyeReflection;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PlayerNextDamageRedirectShield;
import com.github.laxika.magicalvibes.model.PlayerSourceNextDamageRedirectShield;
import com.github.laxika.magicalvibes.model.PlayerSourceNextDamageShield;
import com.github.laxika.magicalvibes.model.SourceDamageRedirectShield;
import com.github.laxika.magicalvibes.model.SourceNextCombatDamageToOpponentRedirectShield;
import com.github.laxika.magicalvibes.model.TargetSourceDamagePreventionShield;
import com.github.laxika.magicalvibes.model.TurnDamageRedirectToCreatureShield;
import com.github.laxika.magicalvibes.model.TurnSourceDamageRedirectToControllerShield;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllCombatDamageToAndByEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllCombatDamageToAttachedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllCombatDamageToAndBySelfEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllCombatDamageToSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToAndByEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DelayedPlusOnePlusOneCounterRegrowthEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ControlledCreaturesDamageReductionEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllNoncombatDamageToAttachedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageAndAddMinusCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PreventCombatDamageToAttackingCreaturesYouControlEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageAndRemovePlusOnePlusOneCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromOpponentSourcesEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToOtherCreaturesAndAddPlusCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToSelfFromCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToControllerPerClericEffect;
import com.github.laxika.magicalvibes.model.effect.PlaneswalkerDamagePreventionEffect;
import com.github.laxika.magicalvibes.model.effect.PreventFixedDamagePerSourceToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.PreventXDamagePerSourceToControllerAndCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.PreventNoncombatDamageToControllerAndGainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PreventHalfDamageToControllerAndTheirPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerAndCreaturesDamagePreventionEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerAndPermanentsNoncombatDamagePreventionEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllButOneDamageToControllerAndPlaneswalkersEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToSelfAndSourceControllerDrawsEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToSelfAndDealThatMuchDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllCombatDamageToSelfFromBlockersEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToSelfFromCreaturesItBlocksEffect;
import com.github.laxika.magicalvibes.model.effect.PreventCombatDamageToSelfAndExileFromLibraryEffect;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.PreventSpellDamageToOpponentAndCreateTokensEffect;
import com.github.laxika.magicalvibes.model.effect.PreventXDamageFromEachSourceToAttachedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SelfDamagePreventionEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectPlayerDamageToSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectAllDamageToEnchantedCreatureControllerEffect;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.DamagePreventionReplacementSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentControlSupport;
import com.github.laxika.magicalvibes.service.effect.staticfx.StaticEffectConditionResolver;
import org.springframework.beans.factory.ObjectProvider;
import com.github.laxika.magicalvibes.service.GameLogService;
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
    private final DrawService drawService;
    private final AmountEvaluationService amountEvaluationService;
    private final DamagePreventionReplacementSupport damagePreventionReplacementSupport;
    private final StaticEffectConditionResolver staticEffectConditionResolver;
    private final ObjectProvider<PermanentControlSupport> permanentControlSupportProvider;
    private final GameLogService gameLogService;

    public DamagePreventionService(GameQueryService gameQueryService, LifeSupport lifeSupport, DrawService drawService,
                                   AmountEvaluationService amountEvaluationService,
                                   DamagePreventionReplacementSupport damagePreventionReplacementSupport,
                                   StaticEffectConditionResolver staticEffectConditionResolver,
                                   ObjectProvider<PermanentControlSupport> permanentControlSupportProvider,
                                   GameLogService gameLogService) {
        this.gameQueryService = gameQueryService;
        this.lifeSupport = lifeSupport;
        this.drawService = drawService;
        this.amountEvaluationService = amountEvaluationService;
        this.damagePreventionReplacementSupport = damagePreventionReplacementSupport;
        this.staticEffectConditionResolver = staticEffectConditionResolver;
        this.permanentControlSupportProvider = permanentControlSupportProvider;
        this.gameLogService = gameLogService;
    }

    public int applyDamageToControllerAndPutCounterOnSelf(GameData gameData, UUID playerId, int damage) {
        return damagePreventionReplacementSupport.preventDamageToControllerAndPutCounterOnSelf(gameData, playerId, damage);
    }

    /**
     * Swans of Bryn Argoll: "If a source would deal damage to this creature, prevent that damage.
     * The source's controller draws cards equal to the damage prevented this way." Returns {@code true}
     * when the damage is fully prevented (the caller must then deal no damage to {@code target}).
     * When {@code sourceControllerId} is non-null, that player draws one card per point of prevented
     * damage. Prevention (and therefore the draw) only applies while damage is currently preventable.
     */
    public boolean applySwansSourceControllerDraw(GameData gameData, Permanent target, int damage, UUID sourceControllerId) {
        if (!gameQueryService.isDamagePreventable(gameData)) return false;
        if (damage <= 0) return false;
        boolean hasEffect = target.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(e -> e instanceof PreventDamageToSelfAndSourceControllerDrawsEffect);
        if (!hasEffect) return false;
        if (sourceControllerId != null) {
            for (int i = 0; i < damage; i++) {
                drawService.resolveDrawCard(gameData, sourceControllerId);
            }
        }
        return true;
    }

    /**
     * Gloom Surgeon: "If combat damage would be dealt to this creature, prevent that damage and exile
     * that many cards from the top of your library." Returns {@code true} when the damage is fully
     * prevented (the caller must then deal no damage to {@code target}). Only call this from the combat
     * damage path — noncombat damage is unaffected. Exiling stops early on an empty library.
     */
    public boolean applyPreventCombatDamageToSelfAndExile(GameData gameData, Permanent target, int damage) {
        if (!gameQueryService.isDamagePreventable(gameData)) return false;
        if (damage <= 0) return false;
        boolean hasEffect = target.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(e -> e instanceof PreventCombatDamageToSelfAndExileFromLibraryEffect);
        if (!hasEffect) return false;
        UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());
        List<Card> deck = controllerId == null ? null : gameData.playerDecks.get(controllerId);
        if (deck != null) {
            int exiled = 0;
            while (exiled < damage && !deck.isEmpty()) {
                gameData.addToExile(controllerId, deck.removeFirst());
                exiled++;
            }
            log.info("Game {} - {} exiles {} card(s) from library top for combat damage prevented to {}",
                    gameData.id, gameData.playerIdToName.get(controllerId), exiled, target.getCard().getName());
        }
        return true;
    }

    /**
     * Armored Transport: "Prevent all combat damage that would be dealt to this creature by
     * creatures blocking it." Only combat damage whose source is currently blocking {@code target}
     * is prevented; damage the creature takes while blocking, and all noncombat damage, is untouched.
     */
    public boolean isCombatDamageFromBlockerPrevented(GameData gameData, Permanent target, Permanent source) {
        if (!gameQueryService.isDamagePreventable(gameData)) return false;
        boolean hasEffect = target.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(PreventAllCombatDamageToSelfFromBlockersEffect.class::isInstance);
        return hasEffect && source.isBlocking() && source.getBlockingTargetIds().contains(target.getId());
    }

    public boolean isCombatDamageFromCreatureBlockedByTargetPrevented(GameData gameData, Permanent target,
                                                                       Permanent source) {
        if (!gameQueryService.isDamagePreventable(gameData)) return false;
        boolean hasEffect = target.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(PreventAllDamageToSelfFromCreaturesItBlocksEffect.class::isInstance);
        return hasEffect && gameQueryService.isCreature(gameData, source)
                && target.isBlocking() && target.getBlockingTargetIds().contains(source.getId());
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

            if (shield.allDamage()) {
                return 0;
            }

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
        return applyCreaturePreventionShield(gameData, permanent, damage, isCombatDamage, null);
    }

    public int applyCreaturePreventionShield(GameData gameData, Permanent permanent, int damage,
                                              boolean isCombatDamage, Permanent damageSource) {
        if (damage > 0 && gameQueryService.isCreature(gameData, permanent)) {
            damage = applyControlledCreaturesDamageReduction(gameData, permanent, damage);
            if (damage <= 0) return 0;
        }
        if (permanent.isDamageCantBePreventedOrRedirectedThisTurn()) return damage;
        // Kiora, the Crashing Wave: prevent all damage dealt to the targeted permanent until its
        // controller's next turn begins.
        if (gameQueryService.isDamagePreventable(gameData)
                && gameData.isProtectedFromDamageUntilNextTurn(permanent.getId())) return 0;
        if (!isCombatDamage) {
            damage = applyControllerAndPermanentsNoncombatDamagePrevention(gameData, permanent, damage);
            if (damage <= 0) return 0;
        }
        // Blinding Fog: prevent all damage to all creatures
        if (gameQueryService.isDamagePreventable(gameData) && gameData.preventAllDamageToAllCreatures) return 0;
        // Wellgabber Apothecary: prevent all damage to specific target creatures this turn
        if (gameQueryService.isDamagePreventable(gameData) && gameData.creaturesWithAllDamagePrevented.contains(permanent.getId())) return 0;
        // Ethersworn Shieldmage: prevent all damage to permanents matching an active predicate this turn (e.g. artifact creatures)
        if (gameQueryService.isDamagePreventable(gameData) && gameQueryService.isAllDamagePreventedByPredicate(gameData, permanent)) return 0;
        if (isCombatDamage && gameQueryService.isDamagePreventable(gameData)
                && gameQueryService.isDamagePreventedByControlledPredicate(gameData, permanent)) return 0;
        // Foxfire: prevent all combat damage that would be dealt to specific target creatures this turn
        if (isCombatDamage && gameQueryService.isDamagePreventable(gameData) && gameData.creaturesWithCombatDamagePrevented.contains(permanent.getId())) return 0;
        // Safe Passage: prevent all damage to creatures controlled by a player with full prevention
        if (gameQueryService.isDamagePreventable(gameData)) {
            UUID controllerId = gameQueryService.findPermanentController(gameData, permanent.getId());
            // Divine Light: prevent all damage to creatures controlled by the protected player.
            if (controllerId != null && gameData.playersWithAllCreatureDamagePrevented.contains(controllerId)) return 0;
            if (controllerId != null && gameData.playersWithAllDamagePrevented.contains(controllerId)) return 0;
        }
        // Protean Hydra / Unbreathing Horde / Rock Hydra / Ugin's Conjurant: counter-based damage replacement.
        // Counters are removed regardless of whether damage is preventable. When removeOneOnly=true
        // (Unbreathing Horde), exactly one counter is removed per damage event. When
        // preventOnlyIfCounterAvailable=true (Rock Hydra), only the damage represented by removed
        // counters is prevented. Otherwise, all damage is prevented. Ugin's Conjurant applies only
        // while it has a +1/+1 counter.
        var preventRemoveEffect = permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                .filter(e -> e instanceof PreventDamageAndRemovePlusOnePlusOneCountersEffect)
                .map(e -> (PreventDamageAndRemovePlusOnePlusOneCountersEffect) e)
                .findFirst().orElse(null);
        if (damage > 0 && preventRemoveEffect != null
                && (!preventRemoveEffect.requiresCounter()
                || permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) > 0)) {
            int countersToRemove = preventRemoveEffect.removeOneOnly()
                    ? Math.min(1, permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                    : Math.min(damage, permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE));
            if (countersToRemove > 0 && !gameQueryService.cantHaveCounters(gameData, permanent)) {
                permanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) - countersToRemove);
                registerDelayedRegrowth(gameData, permanent, countersToRemove);
            }
            int preventedDamage = preventRemoveEffect.preventOnlyIfCounterAvailable()
                    ? countersToRemove
                    : damage;
            // Prevention only applies if damage is preventable
            if (gameQueryService.isDamagePreventable(gameData)) {
                CreateTokenEffect tokenTemplate = preventRemoveEffect.tokenTemplate();
                if (tokenTemplate != null) {
                    UUID controllerId = gameQueryService.findPermanentController(gameData, permanent.getId());
                    if (controllerId != null) {
                            permanentControlSupportProvider.getObject().applyCreateToken(
                                gameData, controllerId, tokenTemplate.withAmount(preventedDamage), permanent.getCard().getSetCode());
                    }
                }
                return damage - preventedDamage;
            }
            return damage;
        }
        if (gameQueryService.isDamagePreventable(gameData)) {
            int incomingDamage = damage;
            int selfDamagePrevented = permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                    .filter(SelfDamagePreventionEffect.class::isInstance)
                    .map(SelfDamagePreventionEffect.class::cast)
                    .mapToInt(effect -> effect.preventedDamage(incomingDamage))
                    .sum();
            if (selfDamagePrevented > 0) {
                damage -= Math.min(damage, selfDamagePrevented);
                if (damage <= 0) return 0;
            }
            if (gameQueryService.isCreature(gameData, permanent)) {
                damage -= applyControllerAndCreaturesFixedPerSourceDamagePrevention(
                        gameData, gameQueryService.findPermanentController(gameData, permanent.getId()), damage,
                        isCombatDamage, damageSource);
                if (damage <= 0) return 0;
            }
            if (gameQueryService.hasActiveStaticEffect(
                    gameData, permanent, PreventAllDamageEffect.class)) return 0;
            if (permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(PreventDamageToSelfAndDealThatMuchDamageEffect.class::isInstance)) {
                queuePhyrexianVindicatorTrigger(gameData, permanent, damage);
                return 0;
            }
            if (damageSource != null
                    && gameQueryService.isDamageFromPermanentSourceToCreaturePrevented(gameData, damageSource, permanent)) {
                return 0;
            }
            if (gameQueryService.hasAuraWithEffect(gameData, permanent, PreventAllDamageToAndByEnchantedCreatureEffect.class)) return 0;
            if (isCombatDamage && gameQueryService.hasAuraWithEffect(gameData, permanent, PreventAllCombatDamageToAndByEnchantedCreatureEffect.class)) return 0;
            // General's Kabuto: "Prevent all combat damage that would be dealt to equipped creature."
            if (isCombatDamage && gameQueryService.hasAuraWithEffect(gameData, permanent, PreventAllCombatDamageToAttachedCreatureEffect.class)) return 0;
            // Fog Bank: "Prevent all combat damage that would be dealt to and dealt by this creature."
            if (isCombatDamage && permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(PreventAllCombatDamageToAndBySelfEffect.class::isInstance)) return 0;
            // Seraph of the Sword: "Prevent all combat damage that would be dealt to this creature."
            if (isCombatDamage && permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(PreventAllCombatDamageToSelfEffect.class::isInstance)) return 0;
            // Dolmen Gate: "Prevent all combat damage that would be dealt to attacking creatures you control."
            if (isCombatDamage && permanent.isAttacking() && hasAttackingCreatureCombatDamagePreventionSource(gameData, permanent)) return 0;
            // Mark of Asylum / Inner Sanctum: "Prevent all [noncombat] damage that would be dealt to creatures you control."
            if (hasCreatureDamagePreventionSource(gameData, permanent, isCombatDamage)) return 0;
            // Emmara Tandris: "Prevent all damage that would be dealt to creature tokens you control."
            int controlledCreatureDamageLimit = gameQueryService.getControlledCreatureDamageLimit(gameData, permanent);
            if (controlledCreatureDamageLimit < damage) return controlledCreatureDamageLimit;
            if (isCombatDamage
                    && gameQueryService.isAllCombatDamageToControlledCreaturePrevented(gameData, permanent)) return 0;
            // Uncle Istvan: "Prevent all damage that would be dealt to this creature by creatures." Combat
            // damage is always dealt by a creature (CR 510.1c), so all combat damage to such a permanent is
            // prevented. Noncombat creature-sourced damage is handled in DamageSupport.dealCreatureDamage,
            // where the source is known.
            if (isCombatDamage && permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                    .filter(PreventDamageToSelfFromCreaturesEffect.class::isInstance)
                    .map(PreventDamageToSelfFromCreaturesEffect.class::cast)
                    .anyMatch(effect -> effect.sourcePredicate() == null)) return 0;
            if (!isCombatDamage && gameQueryService.hasAuraWithEffect(gameData, permanent, PreventAllNoncombatDamageToAttachedCreatureEffect.class)) return 0;
            // Gisela, Blade of Goldnight: prevent half the damage dealt to a permanent her controller
            // controls, rounded up.
            damage = applyHalfDamagePrevention(gameData,
                    gameQueryService.findPermanentController(gameData, permanent.getId()), damage);
            if (damage <= 0) return 0;
            // Shield of the Realm: "If a source would deal damage to equipped creature, prevent N of that damage."
            damage = applyAttachedPerSourceDamageReduction(gameData, permanent, damage);
            if (damage <= 0) return 0;
            if (damage > 0 && permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(e -> e instanceof PreventDamageAndAddMinusCountersEffect)) {
                if (!gameQueryService.cantHavePlusOnePlusOneCounters(gameData, permanent)) {
                    int counters = gameQueryService.reduceMinusOneMinusOneCounters(gameData, permanent, damage);
                    if (counters > 0) {
                        permanent.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE, permanent.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) + counters);
                    }
                }
                return 0;
            }
            // Vigor: "If damage would be dealt to another creature you control, prevent that damage.
            // Put a +1/+1 counter on that creature for each 1 damage prevented this way." The effect
            // lives on a different permanent (Vigor) controlled by this creature's controller.
            if (damage > 0 && hasOtherCreatureDamagePreventionSource(gameData, permanent)) {
                if (!gameQueryService.cantHavePlusOnePlusOneCounters(gameData, permanent)) {
                    int counters = gameQueryService.doublePlusOnePlusOneCounters(gameData, permanent, damage);
                    if (counters > 0) {
                        permanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE, permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + counters);
                        recordPlusOnePlusOneCounterPlacedOnControlledPermanent(gameData, permanent);
                    }
                }
                return 0;
            }
            // Temper: prevent the next X damage to the target creature and put a +1/+1 counter on it
            // for each 1 damage prevented this way.
            int temperShield = permanent.getDamageToPlusOnePlusOneCounterPreventionShield();
            if (temperShield > 0 && damage > 0) {
                int temperPrevented = Math.min(temperShield, damage);
                permanent.setDamageToPlusOnePlusOneCounterPreventionShield(temperShield - temperPrevented);
                if (!gameQueryService.cantHaveCounters(gameData, permanent)) {
                    int counters = gameQueryService.doublePlusOnePlusOneCounters(gameData, permanent, temperPrevented);
                    if (counters > 0) {
                        permanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE,
                                permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + counters);
                        recordPlusOnePlusOneCounterPlacedOnControlledPermanent(gameData, permanent);
                    }
                }
                damage -= temperPrevented;
                if (damage <= 0) return 0;
            }
            // Sacred Boon: "Prevent the next N damage... At the beginning of the next end step, put a
            // +0/+1 counter on that creature for each 1 damage prevented this way." Prevented damage is
            // accumulated into a delayed +0/+1 counter trigger drained at the next end step.
            int boonShield = permanent.getDamageToCounterPreventionShield();
            if (boonShield > 0 && damage > 0) {
                int boonPrevented = Math.min(boonShield, damage);
                permanent.setDamageToCounterPreventionShield(boonShield - boonPrevented);
                gameData.addDelayedPlusZeroPlusOneCounters(permanent.getId(), boonPrevented);
                damage -= boonPrevented;
                if (damage <= 0) return 0;
            }
            // Divine Deflection: a redirect shield covering the controller's permanents as well as
            // the controller. Prevented damage is queued for the shield's source to deal on.
            UUID permanentControllerId = gameQueryService.findPermanentController(gameData, permanent.getId());
            if (permanentControllerId != null) {
                damage = applyRedirectShields(gameData, permanentControllerId, permanent.getId(), damage, true);
                if (damage <= 0) return 0;
            }
            damage = applyGlobalPreventionShield(gameData, damage);
            damage = applyDamagePreventionLifeGainShield(gameData, permanent.getId(), damage);
            if (damage <= 0) return 0;
            return applyPermanentDamagePreventionShield(gameData, permanent, damage);
        }
        return damage;
    }

    /**
     * Applies replacement effects that reduce damage to creatures controlled by a player. These
     * effects are replacements rather than prevention, so they still apply when damage cannot be
     * prevented.
     */
    private int applyControlledCreaturesDamageReduction(GameData gameData, Permanent creature, int damage) {
        UUID controllerId = gameQueryService.findPermanentController(gameData, creature.getId());
        if (controllerId == null || damage <= 0) return damage;

        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return damage;

        long totalReduction = 0;
        for (Permanent source : battlefield) {
            for (CardEffect effect : source.getCard().getEffects(EffectSlot.STATIC)) {
                CardEffect resolved = staticEffectConditionResolver.resolve(
                        gameData, source, controllerId, effect);
                if (resolved instanceof ControlledCreaturesDamageReductionEffect reduction) {
                    totalReduction += reduction.amount();
                    if (totalReduction >= damage) return 0;
                }
            }
        }
        return (int) (damage - totalReduction);
    }

    public int applyPerSourceCreatureDamagePreventionShield(GameData gameData, Permanent permanent,
                                                             Permanent damageSource, int damage,
                                                             boolean isCombatDamage) {
        if (damage <= 0 || damageSource == null || !gameQueryService.isDamagePreventable(gameData)
                || !gameQueryService.isCreature(gameData, damageSource)) {
            return damage;
        }

        if (gameQueryService.isDamageFromPermanentSourceToCreaturePrevented(gameData, damageSource, permanent)) {
            return 0;
        }

        UUID controllerId = gameQueryService.findPermanentController(gameData, permanent.getId());
        int prevented = 0;
        if (controllerId != null) {
            prevented = evaluatePerSourceControllerAndCreaturesDamagePrevention(
                    gameData, controllerId, damage, isCombatDamage, damageSource);
        }
        return damage - Math.min(damage, prevented);
    }

    public int applyPermanentDamagePreventionShield(GameData gameData, Permanent permanent, int damage) {
        if (!gameQueryService.isDamagePreventable(gameData) || damage <= 0) return damage;

        int shield = permanent.getDamagePreventionShield();
        if (shield <= 0) return damage;

        int prevented = Math.min(shield, damage);
        permanent.setDamagePreventionShield(shield - prevented);
        return damage - prevented;
    }

    private void queuePhyrexianVindicatorTrigger(GameData gameData, Permanent permanent, int preventedDamage) {
        UUID controllerId = gameQueryService.findPermanentController(gameData, permanent.getId());
        if (controllerId == null || preventedDamage <= 0) return;

        var targetFilter = new AnyTargetPredicateTargetFilter(
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "another target");
        gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                permanent.getCard(), controllerId,
                List.of(new DealDamageToAnyTargetEffect(new XValue())),
                false, targetFilter, preventedDamage, permanent.getId(), new Permanent(permanent)));
        gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(), "'s ability triggers."));
    }

    /**
     * Gisela, Blade of Goldnight: "If a source would deal damage to you or a permanent you control,
     * prevent half that damage, rounded up." Preventing half rounded up leaves half rounded down, so
     * each {@link PreventHalfDamageToControllerAndTheirPermanentsEffect} the recipient controls halves
     * the remaining damage. Callers must already have checked {@code isDamagePreventable}.
     *
     * @param recipientId the player being dealt damage, or the controller of the permanent being dealt
     *                    damage; {@code null} leaves the damage untouched
     * @return the damage remaining after prevention
     */
    private int applyHalfDamagePrevention(GameData gameData, UUID recipientId, int damage) {
        if (recipientId == null || damage <= 0) return damage;
        List<Permanent> battlefield = gameData.playerBattlefields.get(recipientId);
        if (battlefield == null) return damage;
        for (Permanent p : battlefield) {
            for (CardEffect effect : p.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof PreventHalfDamageToControllerAndTheirPermanentsEffect) {
                    damage /= 2;
                    if (damage == 0) return 0;
                }
            }
        }
        return damage;
    }

    /**
     * Applies fixed per-source prevention to a player's damage and to damage to that player's
     * creatures. Conditional static effects are resolved against their carrying permanent so
     * level counters and similar live conditions are evaluated at damage time.
     */
    private int applyControllerAndCreaturesFixedPerSourceDamagePrevention(
            GameData gameData, UUID controllerId, int damage, boolean combatDamage, Permanent damageSource) {
        if (controllerId == null || damage <= 0) return 0;
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return 0;

        int reduction = 0;
        boolean sourceIsCreature = damageSource != null && gameQueryService.isCreature(gameData, damageSource);
        for (Permanent source : battlefield) {
            for (CardEffect effect : source.getCard().getEffects(EffectSlot.STATIC)) {
                CardEffect resolved = staticEffectConditionResolver.resolve(
                        gameData, source, controllerId, effect);
                if (resolved instanceof ControllerAndCreaturesDamagePreventionEffect prevention) {
                    reduction += prevention.amount();
                } else if (resolved instanceof PreventXDamagePerSourceToControllerAndCreaturesEffect prevention
                        && (!prevention.combatOnly() || combatDamage)
                        && (!prevention.creatureSourcesOnly() || sourceIsCreature)) {
                    reduction += amountEvaluationService.evaluate(gameData, prevention.amount(),
                            AmountContext.forStaticEffect(source, controllerId));
                }
            }
        }
        return Math.min(damage, reduction);
    }

    private int evaluatePerSourceControllerAndCreaturesDamagePrevention(
            GameData gameData, UUID controllerId, int damage, boolean combatDamage, Permanent damageSource) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return 0;

        int reduction = 0;
        for (Permanent source : battlefield) {
            for (CardEffect effect : source.getCard().getEffects(EffectSlot.STATIC)) {
                CardEffect resolved = staticEffectConditionResolver.resolve(
                        gameData, source, controllerId, effect);
                if (resolved instanceof PreventXDamagePerSourceToControllerAndCreaturesEffect prevention
                        && (!prevention.combatOnly() || combatDamage)
                        && (!prevention.creatureSourcesOnly() || gameQueryService.isCreature(gameData, damageSource))) {
                    reduction += amountEvaluationService.evaluate(gameData, prevention.amount(),
                            AmountContext.forStaticEffect(source, controllerId));
                }
            }
        }
        return Math.min(damage, reduction);
    }

    /** Prevents noncombat damage to a permanent controlled by a protected player. */
    public int applyControllerAndPermanentsNoncombatDamagePrevention(
            GameData gameData, Permanent permanent, int damage) {
        if (!gameQueryService.isDamagePreventable(gameData) || damage <= 0) return damage;
        UUID controllerId = gameQueryService.findPermanentController(gameData, permanent.getId());
        return controllerId != null
                && hasControllerAndPermanentsNoncombatDamagePrevention(gameData, controllerId)
                ? 0
                : damage;
    }

    private boolean hasControllerAndPermanentsNoncombatDamagePrevention(GameData gameData, UUID playerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        return battlefield != null && battlefield.stream()
                .anyMatch(permanent -> gameQueryService.hasActiveStaticEffect(
                        gameData, permanent, ControllerAndPermanentsNoncombatDamagePreventionEffect.class));
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
     * Mark of Asylum / Inner Sanctum / Bubble Matrix-style protection: returns true when some permanent
     * on the battlefield carries a {@link PreventDamageToCreaturesEffect} covering this damage to the
     * given creature. Mark of Asylum's variant ({@code noncombatOnly}) leaves combat damage untouched;
     * Inner Sanctum's covers both but only for creatures its controller controls, while Bubble Matrix's
     * ({@code allCreatures}) covers every creature regardless of controller. Damage matched here is
     * fully prevented by the caller.
     */
    private boolean hasCreatureDamagePreventionSource(GameData gameData, Permanent creature, boolean isCombatDamage) {
        UUID controllerId = gameQueryService.findPermanentController(gameData, creature.getId());
        if (controllerId == null) return false;
        return gameData.playerBattlefields.entrySet().stream()
                .anyMatch(entry -> entry.getValue().stream()
                        .anyMatch(source -> source.getCard().getEffects(EffectSlot.STATIC).stream()
                                .anyMatch(e -> e instanceof PreventDamageToCreaturesEffect prevent
                                        && !(isCombatDamage && prevent.noncombatOnly())
                                        && (prevent.allCreatures() || controllerId.equals(entry.getKey()))
                                        && (!prevent.excludeSource()
                                        || !source.getId().equals(creature.getId())))));
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
        return applyPlayerPreventionShield(gameData, playerId, damage, false, null);
    }

    public int applyCombatPlayerPreventionShield(GameData gameData, UUID playerId, int damage) {
        return applyPlayerPreventionShield(gameData, playerId, damage, true, null);
    }

    public int applyCombatPlayerPreventionShield(GameData gameData, UUID playerId, int damage,
                                                   Permanent damageSource) {
        return applyPlayerPreventionShield(gameData, playerId, damage, true, damageSource);
    }

    private int applyPlayerPreventionShield(GameData gameData, UUID playerId, int damage,
                                            boolean combatDamage, Permanent damageSource) {
        if (!gameQueryService.isDamagePreventable(gameData)) return damage;
        if (combatDamage && gameData.preventAllCombatDamageToPlayers) return 0;
        if (gameData.playersWithAllDamagePrevented.contains(playerId)) return 0;
        // Riot Control: prevent all damage that would be dealt to the caster this turn (their creatures are unaffected)
        if (gameData.playersWithAllPlayerDamagePrevented.contains(playerId)) return 0;
        // Morningtide's Light: prevent all damage that would be dealt to the caster until their next turn.
        if (gameData.playersWithAllPlayerDamagePreventedUntilNextTurn.contains(playerId)) return 0;
        // Gisela, Blade of Goldnight: prevent half the damage dealt to her controller, rounded up.
        damage = applyHalfDamagePrevention(gameData, playerId, damage);
        if (damage <= 0) return 0;
        damage -= applyControllerAndCreaturesFixedPerSourceDamagePrevention(
                gameData, playerId, damage, combatDamage, damageSource);
        if (damage <= 0) return 0;
        // Process redirect shields first (e.g. Vengeful Archon)
        damage = applyRedirectShields(gameData, playerId, damage);
        damage = applyGlobalPreventionShield(gameData, damage);
        damage = applyDamagePreventionLifeGainShield(gameData, playerId, damage);
        if (damage <= 0) return 0;
        if (combatDamage) {
            int combatShield = gameData.playerCombatDamagePreventionShields.getOrDefault(playerId, 0);
            if (combatShield > 0) {
                int prevented = Math.min(combatShield, damage);
                int remaining = combatShield - prevented;
                if (remaining == 0) {
                    gameData.playerCombatDamagePreventionShields.remove(playerId);
                } else {
                    gameData.playerCombatDamagePreventionShields.put(playerId, remaining);
                }
                damage -= prevented;
            }
        }
        if (damage <= 0) return 0;
        int shield = gameData.playerDamagePreventionShields.getOrDefault(playerId, 0);
        if (shield > 0 && damage > 0) {
            int prevented = Math.min(shield, damage);
            int remaining = shield - prevented;
            if (remaining == 0) {
                gameData.playerDamagePreventionShields.remove(playerId);
            } else {
                gameData.playerDamagePreventionShields.put(playerId, remaining);
            }
            damage -= prevented;
        }
        if (damage <= 0) return 0;
        if (!combatDamage && hasControllerAndPermanentsNoncombatDamagePrevention(gameData, playerId)) return 0;
        return damage;
    }

    /** Applies target-specific shields that gain life for their resolving controller. */
    public int applyDamagePreventionLifeGainShield(GameData gameData, UUID targetId, int damage) {
        if (!gameQueryService.isDamagePreventable(gameData)
                || targetId == null || damage <= 0 || gameData.damagePreventionLifeGainShields.isEmpty()) {
            return damage;
        }

        int remaining = damage;
        List<DamagePreventionLifeGainShield> toReAdd = new ArrayList<>();
        Iterator<DamagePreventionLifeGainShield> it = gameData.damagePreventionLifeGainShields.iterator();
        while (it.hasNext() && remaining > 0) {
            DamagePreventionLifeGainShield shield = it.next();
            if (!targetId.equals(shield.targetId())) continue;

            int prevented = Math.min(shield.remainingAmount(), remaining);
            remaining -= prevented;
            it.remove();
            if (prevented < shield.remainingAmount()) {
                toReAdd.add(shield.withReducedAmount(prevented));
            }
            if (prevented > 0 && shield.lifeGainPlayerId() != null) {
                lifeSupport.applyGainLife(gameData, shield.lifeGainPlayerId(), prevented, "prevented damage");
            }
        }
        gameData.damagePreventionLifeGainShields.addAll(toReAdd);
        return remaining;
    }

    /**
     * Consumes damage redirect shields for the given player. Prevented damage is tracked
     * in {@link GameData#pendingRedirectDamage} for the caller to deal after damage processing.
     * Returns the remaining damage after redirect shield prevention.
     */
    private int applyRedirectShields(GameData gameData, UUID playerId, int damage) {
        return applyRedirectShields(gameData, playerId, null, damage, false);
    }

    /**
     * @param forControlledPermanent when {@code true} the damage is being dealt to a permanent the
     *                               player controls, so only shields that cover their permanents
     *                               (Divine Deflection) apply
     */
    private int applyRedirectShields(GameData gameData, UUID playerId, UUID permanentId, int damage,
                                     boolean forControlledPermanent) {
        if (damage <= 0 || gameData.damageRedirectShields.isEmpty()) return damage;

        int remaining = damage;
        List<DamageRedirectShield> toReAdd = new ArrayList<>();
        Iterator<DamageRedirectShield> it = gameData.damageRedirectShields.iterator();

        while (it.hasNext() && remaining > 0) {
            DamageRedirectShield shield = it.next();
            if (shield.protectedPermanentId() != null) {
                if (!shield.protectedPermanentId().equals(permanentId)) continue;
            } else {
                if (!shield.protectedPlayerId().equals(playerId)) continue;
                if (forControlledPermanent && !shield.coversControlledPermanents()) continue;
            }

            int prevented = Math.min(shield.remainingAmount(), remaining);
            remaining -= prevented;
            it.remove();

            // If shield is not fully consumed, save reduced version to re-add after iteration
            if (prevented < shield.remainingAmount()) {
                toReAdd.add(shield.withReducedAmount(prevented));
            }

            if (prevented > 0) {
                gameData.pendingRedirectDamage.add(new DamageRedirectShield(
                        shield.protectedPlayerId(), prevented, shield.sourcePermanentId(), shield.sourceCard(),
                        shield.redirectTargetId(), shield.coversControlledPermanents(), shield.protectedPermanentId()));
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

    /** Applies whole-turn chosen-source prevention to player damage and its optional color rider. */
    public int applySourceDamagePreventionForPlayer(GameData gameData, UUID playerId, UUID sourcePermanentId,
                                                    int damage, Set<CardColor> sourceColors) {
        if (!isSourceDamagePreventedForPlayer(gameData, playerId, sourcePermanentId)) return damage;

        Set<UUID> lifeGainSources = gameData.playerSourceDamagePreventionLifeGainIds.get(playerId);
        if (damage > 0 && lifeGainSources != null && lifeGainSources.contains(sourcePermanentId)
                && sourceColors != null
                && (sourceColors.contains(CardColor.BLACK) || sourceColors.contains(CardColor.RED))) {
            lifeSupport.applyGainLife(gameData, playerId, damage, "prevented damage");
        }
        return 0;
    }

    /**
     * Applies one-shot chosen-source shields to player damage. A matching shield is consumed by the
     * next damage event; Dark Sphere shields leave half that event, rounded down, while ordinary
     * shields prevent the whole event. Returns the remaining damage.
     */
    public int applyPlayerNextSourceDamageShield(GameData gameData, UUID playerId, UUID sourcePermanentId, int damage) {
        if (!gameQueryService.isDamagePreventable(gameData)) return damage;
        if (damage <= 0 || playerId == null || sourcePermanentId == null
                || gameData.playerSourceNextDamageShields.isEmpty()) {
            return damage;
        }
        var it = gameData.playerSourceNextDamageShields.iterator();
        int remaining = damage;
        while (it.hasNext()) {
            var shield = it.next();
            if (shield.playerId().equals(playerId) && shield.sourceId().equals(sourcePermanentId)) {
                it.remove();
                int prevented = shield.preventHalfDamage() ? remaining / 2 : remaining;
                applyNextSourceShieldRiders(gameData, shield, prevented);
                remaining -= prevented;
                if (remaining == 0) {
                    return 0;
                }
            }
        }
        return remaining;
    }

    /**
     * Shadowbane's creature half: if a one-shot shield covering the creature's controller's
     * permanents matches this (creature controller, source), the entire next damage event to that
     * creature is prevented and the shield is consumed. Returns the remaining damage.
     */
    public int applyControllerCreaturesNextSourceDamageShield(GameData gameData, UUID creatureControllerId,
                                                              UUID sourcePermanentId, int damage) {
        if (!gameQueryService.isDamagePreventable(gameData)) return damage;
        if (damage <= 0 || creatureControllerId == null || sourcePermanentId == null
                || gameData.playerSourceNextDamageShields.isEmpty()) {
            return damage;
        }
        var it = gameData.playerSourceNextDamageShields.iterator();
        while (it.hasNext()) {
            var shield = it.next();
            if (shield.coversControlledCreatures()
                    && shield.playerId().equals(creatureControllerId)
                    && shield.sourceId().equals(sourcePermanentId)) {
                it.remove();
                applyNextSourceShieldRiders(gameData, shield, damage);
                return 0;
            }
        }
        return damage;
    }

    /** Applies whatever "… prevented this way" rider the consumed shield carries. */
    private void applyNextSourceShieldRiders(GameData gameData, PlayerSourceNextDamageShield shield, int damage) {
        gainLifeForNextSourceShield(gameData, shield, damage);
        exileFromLibraryForNextSourceShield(gameData, shield, damage);
        if (shield.drawCards()) {
            for (int i = 0; i < damage; i++) {
                drawService.resolveDrawCard(gameData, shield.playerId());
            }
        }
        if (shield.damageSourceControllerCard() != null) {
            UUID sourceControllerId = gameQueryService.findPermanentController(gameData, shield.sourceId());
            if (sourceControllerId == null) {
                sourceControllerId = shield.sourceControllerId();
            }
            if (sourceControllerId != null) {
                gameData.pendingEyeForAnEyeReflections.add(new EyeForAnEyeReflection(
                        sourceControllerId, damage, shield.damageSourceControllerCard(), shield.playerId()));
            }
        }
    }

    /**
     * Bone Mask rider: exile cards from the top of the protected player's library equal to the
     * damage just prevented. Stops early if the library runs out.
     */
    private void exileFromLibraryForNextSourceShield(GameData gameData, PlayerSourceNextDamageShield shield, int damage) {
        if (!shield.exileFromLibrary()) return;
        List<Card> deck = gameData.playerDecks.get(shield.playerId());
        if (deck == null) return;
        int exiled = 0;
        while (exiled < damage && !deck.isEmpty()) {
            gameData.addToExile(shield.playerId(), deck.removeFirst());
            exiled++;
        }
        log.info("Game {} - {} exiles {} card(s) from library top for prevented damage",
                gameData.id, gameData.playerIdToName.get(shield.playerId()), exiled);
    }

    /**
     * Reverse Damage / Shadowbane rider: gain life equal to the damage just prevented. Shadowbane
     * only gains when the chosen source is black, which is read from the source permanent's
     * effective colours at prevention time.
     */
    private void gainLifeForNextSourceShield(GameData gameData, PlayerSourceNextDamageShield shield, int damage) {
        if (!shield.gainLife()) return;
        if (shield.gainLifeOnlyFromBlackSource()) {
            Permanent source = gameQueryService.findPermanentById(gameData, shield.sourceId());
            if (source == null || !gameQueryService.getEffectiveColors(gameData, source).contains(CardColor.BLACK)) {
                return;
            }
        }
        lifeSupport.applyGainLife(gameData, shield.playerId(), damage, "prevented damage");
    }

    /**
     * Applies one-shot Sanctum Guardian / Honorable Passage shields: if a shield matches this source,
     * the entire next damage event it would deal to any target (player, planeswalker, or creature) is
     * prevented and the shield is consumed. When the shield carries Honorable Passage's rider and the
     * source is red, schedules that much damage at the source's controller via
     * {@link GameData#pendingEyeForAnEyeReflections}. A shield that names a recipient (Kithkin Armor)
     * only fires when {@code recipientId} matches it. A shield with a non-zero
     * {@code damageMultiplier} (Desperate Gambit's won flip) multiplies the event instead of
     * preventing it — a replacement, so it applies even while damage can't be prevented. Returns the
     * remaining damage.
     */
    public int applyChosenSourceNextDamageToAnyTargetShield(GameData gameData, UUID sourcePermanentId, int damage,
                                                            UUID recipientId) {
        return applyChosenSourceNextDamageToAnyTargetShield(gameData, sourcePermanentId, damage, recipientId, false);
    }

    public int applyChosenSourceNextDamageToAnyTargetShield(GameData gameData, UUID sourcePermanentId, int damage,
                                                            UUID recipientId, boolean combatDamage) {
        if (damage <= 0 || sourcePermanentId == null || gameData.sourceNextDamageToAnyTargetShields.isEmpty()) {
            return damage;
        }
        boolean preventable = gameQueryService.isDamagePreventable(gameData);
        var it = gameData.sourceNextDamageToAnyTargetShields.iterator();
        while (it.hasNext()) {
            var shield = it.next();
            if (!shield.sourceId().equals(sourcePermanentId)) {
                continue;
            }
            if (shield.combatOnly() && !combatDamage) {
                continue;
            }
            if (shield.playersOnly() && !gameData.playerIdToName.containsKey(recipientId)) {
                continue;
            }
            if (shield.combatPhase() != null && !shield.combatPhase().equals(gameData.combatPhasesThisTurn)) {
                if (gameData.combatPhasesThisTurn > shield.combatPhase()) {
                    it.remove();
                }
                continue;
            }
            if (shield.recipientId() != null && !shield.recipientId().equals(recipientId)) {
                continue;
            }
            if (shield.damageMultiplier() != 0) {
                it.remove();
                return damage * shield.damageMultiplier();
            }
            if (!preventable) {
                return damage;
            }
            it.remove();
            if (shield.damageRedSourceController()) {
                Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
                UUID sourceControllerId = gameQueryService.findPermanentController(gameData, sourcePermanentId);
                if (source != null && sourceControllerId != null
                        && gameQueryService.getEffectiveColors(gameData, source).contains(CardColor.RED)
                        && shield.passageCard() != null && shield.passageControllerId() != null) {
                    gameData.pendingEyeForAnEyeReflections.add(new EyeForAnEyeReflection(
                            sourceControllerId, damage, shield.passageCard(), shield.passageControllerId()));
                }
            }
            if (shield.lifeGainPlayerId() != null) {
                lifeSupport.applyGainLife(gameData, shield.lifeGainPlayerId(), damage, "prevented damage");
            }
            if (shield.token() != null && shield.tokenControllerId() != null) {
                permanentControlSupportProvider.getObject().applyCreateToken(
                        gameData, shield.tokenControllerId(), shield.token(), damage,
                        shield.tokenSourceSetCode());
            }
            return 0;
        }
        return damage;
    }

    /**
     * Applies one-shot Reflect Damage shields: if a shield matches this source, the entire next damage
     * event it would deal (to any recipient) is instead dealt to that source's controller. The
     * reflected event is scheduled in {@link GameData#pendingEyeForAnEyeReflections} and dealt by the
     * source itself. This is a redirection (replacement) effect, not prevention, so it applies even
     * when damage can't be prevented. Returns the remaining damage (0 when redirected).
     */
    public int applyReflectDamageToSourceControllerShield(GameData gameData, UUID sourcePermanentId, int damage) {
        if (damage <= 0 || sourcePermanentId == null
                || gameData.reflectDamageToSourceControllerShields.isEmpty()) {
            return damage;
        }
        // List.remove(Object) removes the first matching entry — a single shield is consumed per event.
        if (!gameData.reflectDamageToSourceControllerShields.remove(sourcePermanentId)) return damage;

        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        UUID sourceControllerId = gameQueryService.findPermanentController(gameData, sourcePermanentId);
        if (source == null || sourceControllerId == null) return damage;

        gameData.pendingEyeForAnEyeReflections.add(new EyeForAnEyeReflection(
                sourceControllerId, damage, source.getCard(), sourceControllerId));
        return 0;
    }

    /**
     * Applies Aegis of Honor's one-shot replacement effect to direct damage from an instant or
     * sorcery spell. Returns the replacement recipient, or {@code null} when no shield matches.
     */
    public UUID applyNextInstantOrSorceryDamageRedirectShield(GameData gameData, StackEntry entry,
                                                               UUID protectedPlayerId, int damage) {
        if (damage <= 0 || entry == null || protectedPlayerId == null
                || entry.getSourcePermanentId() != null
                || (entry.getEntryType() != StackEntryType.INSTANT_SPELL
                && entry.getEntryType() != StackEntryType.SORCERY_SPELL)
                || gameData.playerNextInstantOrSorceryDamageRedirectShields.isEmpty()) {
            return null;
        }
        if (!gameData.playerNextInstantOrSorceryDamageRedirectShields.remove(protectedPlayerId)) {
            return null;
        }
        return entry.getControllerId();
    }

    /**
     * Applies one-shot Opal-Eye shields: if a shield matches this source, the entire next damage event
     * it would deal (to any recipient) is instead dealt to the shield's destination permanent. The
     * redirected event is queued in {@link GameData#pendingSourceRedirectDamage} for the caller's
     * {@code processSourceRedirectDamage}. This is a redirection (replacement) effect, not prevention,
     * so it applies even when damage can't be prevented. The shield is left in place while the
     * destination is no longer a creature on the battlefield, and damage dealt to the destination
     * itself is left alone — redirecting to itself is a no-op that would skip its own damage triggers.
     *
     * @param damagedPermanentId the permanent being damaged, or {@code null} when the damage is to a player
     * @return the remaining damage after redirection (0 if redirected)
     */
    public int applySourceNextDamageRedirectToPermanent(GameData gameData, UUID sourcePermanentId,
                                                        UUID damagedPermanentId, int damage) {
        // No isDamagePreventable check — this is redirection (replacement), not prevention.
        if (damage <= 0 || sourcePermanentId == null
                || gameData.sourceNextDamageRedirectToPermanentShields.isEmpty()) {
            return damage;
        }
        var it = gameData.sourceNextDamageRedirectToPermanentShields.iterator();
        while (it.hasNext()) {
            var shield = it.next();
            if (!shield.sourceId().equals(sourcePermanentId)) continue;
            UUID destinationId = shield.destinationPermanentId();
            // Redirecting damage to the destination itself is a no-op; deal it normally.
            if (destinationId.equals(damagedPermanentId)) continue;
            Permanent destination = gameQueryService.findPermanentById(gameData, destinationId);
            if (destination == null || !gameQueryService.isCreature(gameData, destination)) continue;
            it.remove();
            gameData.pendingSourceRedirectDamage.add(
                    new SourceDamageRedirectShield(null, sourcePermanentId, damage, destinationId));
            return 0;
        }
        return damage;
    }

    /**
     * Applies one-shot Eye for an Eye reflection shields: if a shield matches this (player, source),
     * the shield is consumed and an equal reflected damage event is scheduled back at the source's
     * controller in {@link GameData#pendingEyeForAnEyeReflections}. This is a reflection (replacement)
     * effect that does NOT reduce the damage dealt to the protected player, so it applies even when
     * damage can't be prevented; the caller keeps dealing the original damage unchanged.
     */
    public void applyEyeForAnEyeReflection(GameData gameData, UUID playerId, UUID sourcePermanentId, int damage) {
        if (damage <= 0 || playerId == null || sourcePermanentId == null
                || gameData.eyeForAnEyeShields.isEmpty()) {
            return;
        }
        var it = gameData.eyeForAnEyeShields.iterator();
        while (it.hasNext()) {
            var shield = it.next();
            if (shield.protectedPlayerId().equals(playerId) && shield.sourceId().equals(sourcePermanentId)) {
                it.remove();
                UUID sourceControllerId = gameQueryService.findPermanentController(gameData, sourcePermanentId);
                if (sourceControllerId != null) {
                    gameData.pendingEyeForAnEyeReflections.add(new EyeForAnEyeReflection(
                            sourceControllerId, damage, shield.eyeCard(), shield.eyeControllerId()));
                }
                return;
            }
        }
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
     * specific creature by a chosen or any source. This is a redirection (replacement) effect:
     * unlimited shields redirect all matching damage, next-event shields redirect one matching damage
     * event in full, and amount-limited shields redirect only their remaining amount. Reuses
     * {@link GameData#pendingSourceRedirectDamage}
     * so callers deal the redirected damage via their existing {@code processSourceRedirectDamage}.
     *
     * @param protectedPermanentId the creature receiving damage
     * @param sourcePermanentId    the permanent dealing the damage, or {@code null} for a spell or
     *                             ability source
     * @param damage               the raw damage amount
     * @return the remaining damage after redirection (0 if a shield matched)
     */
    public int applyCreatureRedirectShields(GameData gameData, UUID protectedPermanentId, UUID sourcePermanentId, int damage) {
        return applyCreatureRedirectShields(gameData, protectedPermanentId, sourcePermanentId, damage, false);
    }

    /**
     * Checks creature-specific damage redirect shields, optionally restricting them to combat damage.
     */
    public int applyCreatureRedirectShields(GameData gameData, UUID protectedPermanentId, UUID sourcePermanentId,
                                            int damage, boolean combatDamage) {
        // No isDamagePreventable check — this is redirection (replacement), not prevention.
        if (damage <= 0 || protectedPermanentId == null
                || gameData.creatureDamageRedirectShields.isEmpty()) return damage;

        int remaining = damage;
        List<CreatureDamageRedirectShield> toReAdd = new ArrayList<>();
        Iterator<CreatureDamageRedirectShield> it = gameData.creatureDamageRedirectShields.iterator();

        while (it.hasNext() && remaining > 0) {
            CreatureDamageRedirectShield shield = it.next();
            if (!shield.protectedPermanentId().equals(protectedPermanentId)) continue;
            if (shield.combatOnly() && !combatDamage) continue;
            // A null source matches any source (e.g. Zealous Inquisitor); otherwise it must match exactly.
            if (shield.damageSourceId() != null && !shield.damageSourceId().equals(sourcePermanentId)) continue;

            if (shield.isNextEvent()) {
                // Next-event (Jade Monolith, Mirrorwood Treefolk): redirect all of this one damage event,
                // then consume the shield.
                it.remove();
                gameData.pendingSourceRedirectDamage.add(new SourceDamageRedirectShield(
                        protectedPermanentId, sourcePermanentId, remaining, shield.redirectTargetId()));
                remaining = 0;
            } else if (shield.isUnlimited()) {
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

    /**
     * Ascent of the Worthy: redirects damage that would be dealt to a creature controlled by the
     * protected player onto the chosen creature until that player's next turn.
     */
    public int applyCreatureControllerDamageRedirectUntilNextTurn(GameData gameData,
                                                                   UUID protectedPlayerId,
                                                                   Permanent damagedCreature,
                                                                   UUID sourcePermanentId,
                                                                   int damage) {
        if (damage <= 0 || protectedPlayerId == null || damagedCreature == null
                || !gameQueryService.isCreature(gameData, damagedCreature)
                || gameData.creatureControllerDamageRedirectShields.isEmpty()) {
            return damage;
        }

        for (CreatureControllerDamageRedirectShield shield :
                gameData.creatureControllerDamageRedirectShields) {
            if (!protectedPlayerId.equals(shield.protectedPlayerId())
                    || damagedCreature.getId().equals(shield.redirectTargetCreatureId())) {
                continue;
            }
            Permanent redirectTarget = gameQueryService.findPermanentById(
                    gameData, shield.redirectTargetCreatureId());
            if (redirectTarget == null || !gameQueryService.isCreature(gameData, redirectTarget)) {
                continue;
            }
            gameData.pendingSourceRedirectDamage.add(new SourceDamageRedirectShield(
                    damagedCreature.getId(), sourcePermanentId, damage, redirectTarget.getId()));
            return 0;
        }
        return damage;
    }

    /**
     * Redirects damage that would be dealt to any creature to one active effect controller.
     * The redirected damage is queued so it is dealt through the existing player redirect path.
     */
    public int applyAllCreatureDamageRedirectToController(GameData gameData, Permanent target,
                                                          UUID sourcePermanentId, int damage) {
        if (damage <= 0 || target == null || !gameQueryService.isCreature(gameData, target)
                || gameData.playersRedirectingAllCreatureDamage.isEmpty()) return damage;

        UUID controllerId = gameData.playersRedirectingAllCreatureDamage.stream()
                .filter(gameData.playerIds::contains)
                .findFirst()
                .orElse(null);
        if (controllerId == null) return damage;

        gameData.pendingSourceRedirectDamage.add(
                new SourceDamageRedirectShield(null, sourcePermanentId, damage, controllerId));
        return 0;
    }

    /**
     * Treacherous Link: damage that would be dealt to the enchanted creature is dealt to its
     * current controller instead. The Aura is checked dynamically so the effect follows control
     * changes and stops when the Aura is unattached or ignored.
     */
    public int applyEnchantedCreatureDamageRedirectToController(GameData gameData, Permanent target,
                                                                UUID sourcePermanentId, int damage) {
        if (damage <= 0 || target == null || !gameQueryService.isCreature(gameData, target)
                || !gameQueryService.hasAuraWithEffect(
                gameData, target, RedirectAllDamageToEnchantedCreatureControllerEffect.class)) {
            return damage;
        }

        UUID controllerId = gameQueryService.findPermanentController(gameData, target.getId());
        if (controllerId == null) return damage;

        gameData.pendingSourceRedirectDamage.add(
                new SourceDamageRedirectShield(controllerId, sourcePermanentId, damage, controllerId));
        return 0;
    }

    /**
     * Soltari Guerrillas: the next time the given source would deal combat damage to an opponent
     * this turn, that damage is dealt to the shield's destination creature instead. Keyed on the
     * damage <em>source</em> rather than on the damaged object, one-shot, and matched only for
     * combat damage dealt to a player. This is a redirection (replacement) effect, so it applies
     * even when damage can't be prevented. The redirect only happens while the destination is still
     * a creature on the battlefield; otherwise the damage is dealt normally and the shield is left
     * in place. Redirected damage is queued in {@link GameData#pendingSourceRedirectDamage} for the
     * caller's {@code processSourceRedirectDamage}.
     *
     * @param damagedPlayerId  the opponent that would have taken the combat damage
     * @param sourcePermanentId the attacking permanent dealing the damage
     * @param damage           the raw damage amount
     * @return the remaining damage after redirection (0 if redirected)
     */
    public int applySourceNextCombatDamageToOpponentRedirect(GameData gameData, UUID damagedPlayerId,
                                                             UUID sourcePermanentId, int damage) {
        if (damage <= 0 || sourcePermanentId == null || damagedPlayerId == null
                || gameData.sourceNextCombatDamageToOpponentRedirectShields.isEmpty()) {
            return damage;
        }

        Iterator<SourceNextCombatDamageToOpponentRedirectShield> it =
                gameData.sourceNextCombatDamageToOpponentRedirectShields.iterator();
        while (it.hasNext()) {
            SourceNextCombatDamageToOpponentRedirectShield shield = it.next();
            if (!shield.sourcePermanentId().equals(sourcePermanentId)) continue;

            UUID destinationId = shield.destinationPermanentId();
            Permanent destination = gameQueryService.findPermanentById(gameData, destinationId);
            if (destination == null || !gameQueryService.isCreature(gameData, destination)) continue;

            it.remove();
            gameData.pendingSourceRedirectDamage.add(new SourceDamageRedirectShield(
                    damagedPlayerId, sourcePermanentId, damage, destinationId));
            return 0;
        }
        return damage;
    }

    /**
     * Martyrdom: redirect the next N damage this turn dealt to a protected player onto a fixed
     * permanent. Any source, amount-limited, consumed as it absorbs damage. This is a redirection
     * (replacement) effect, so it applies even when damage can't be prevented. The redirect only
     * happens while the destination is still a permanent on the battlefield; otherwise the damage is
     * dealt normally and the shield is left in place. Redirected damage is queued in
     * {@link GameData#pendingSourceRedirectDamage} for the caller's {@code processSourceRedirectDamage}.
     *
     * @param protectedPlayerId the player receiving damage
     * @param damage            the raw damage amount
     * @return the remaining damage after redirection
     */
    public int applyPlayerNextDamageRedirectShields(GameData gameData, UUID protectedPlayerId, int damage) {
        return applyPlayerNextDamageRedirectShields(gameData, protectedPlayerId, null, damage);
    }

    public int applyPlayerNextDamageRedirectShields(GameData gameData, UUID protectedPlayerId,
                                                    UUID sourcePermanentId, int damage) {
        // No isDamagePreventable check — this is redirection (replacement), not prevention.
        if (damage <= 0 || protectedPlayerId == null || gameData.playerNextDamageRedirectShields.isEmpty()) return damage;

        int remaining = damage;
        List<PlayerNextDamageRedirectShield> toReAdd = new ArrayList<>();
        Iterator<PlayerNextDamageRedirectShield> it = gameData.playerNextDamageRedirectShields.iterator();

        while (it.hasNext() && remaining > 0) {
            PlayerNextDamageRedirectShield shield = it.next();
            if (!shield.protectedPlayerId().equals(protectedPlayerId)) continue;
            UUID destinationId = shield.redirectTargetPermanentId();
            if (!gameData.playerIds.contains(destinationId)
                    && gameQueryService.findPermanentById(gameData, destinationId) == null) continue;

            int redirected = Math.min(shield.remainingAmount(), remaining);
            remaining -= redirected;
            it.remove();
            if (redirected < shield.remainingAmount()) {
                toReAdd.add(shield.withReducedAmount(redirected));
            }
            if (redirected > 0) {
                gameData.pendingSourceRedirectDamage.add(new SourceDamageRedirectShield(
                        protectedPlayerId, sourcePermanentId, redirected, destinationId));
            }
        }

        gameData.playerNextDamageRedirectShields.addAll(toReAdd);
        return remaining;
    }

    /**
     * General's Regalia: redirect the next damage event from a chosen source that would be dealt to
     * the controller onto a fixed creature. This protects only the player, not their permanents.
     */
    public int applyPlayerSourceNextDamageRedirectShield(GameData gameData, UUID protectedPlayerId,
                                                         UUID sourcePermanentId, int damage) {
        if (damage <= 0 || protectedPlayerId == null || sourcePermanentId == null
                || gameData.playerSourceNextDamageRedirectShields.isEmpty()) return damage;

        Iterator<PlayerSourceNextDamageRedirectShield> it =
                gameData.playerSourceNextDamageRedirectShields.iterator();
        while (it.hasNext()) {
            PlayerSourceNextDamageRedirectShield shield = it.next();
            if (!shield.protectedPlayerId().equals(protectedPlayerId)
                    || !shield.sourcePermanentId().equals(sourcePermanentId)) continue;

            Permanent destination = gameQueryService.findPermanentById(gameData,
                    shield.redirectTargetPermanentId());
            if (destination == null || !gameQueryService.isCreature(gameData, destination)) continue;

            it.remove();
            gameData.pendingSourceRedirectDamage.add(new SourceDamageRedirectShield(
                    protectedPlayerId, sourcePermanentId, damage, destination.getId()));
            return 0;
        }
        return damage;
    }

    /**
     * Saving Grace: redirect all damage this turn dealt to a protected player or a permanent they
     * control onto a fixed creature. Any source, unlimited amount, persists for the turn. This is a
     * redirection (replacement) effect, so it applies even when damage can't be prevented. The redirect
     * only happens while the destination is still a creature on the battlefield (ruling: if it isn't a
     * creature on the battlefield when the damage would be dealt, the damage isn't redirected). Damage
     * that would be dealt to the destination creature itself is left alone — redirecting to itself is a
     * no-op that would otherwise skip its own damage triggers. Redirected damage is queued in
     * {@link GameData#pendingSourceRedirectDamage} for the caller's {@code processSourceRedirectDamage}.
     *
     * @param protectedPlayerId  the player receiving damage, or the controller of the damaged permanent
     * @param damagedPermanentId the permanent being damaged, or {@code null} when the damage is to the player
     * @param damage             the raw damage amount
     * @return the remaining damage after redirection (0 if redirected)
     */
    public int applyTurnDamageRedirectToCreature(GameData gameData, UUID protectedPlayerId,
                                                 UUID damagedPermanentId, int damage) {
        return applyTurnDamageRedirectToCreature(gameData, protectedPlayerId, damagedPermanentId,
                null, damage, false);
    }

    public int applyTurnDamageRedirectToCreature(GameData gameData, UUID protectedPlayerId,
                                                 UUID damagedPermanentId, int damage, boolean combatDamage) {
        return applyTurnDamageRedirectToCreature(gameData, protectedPlayerId, damagedPermanentId,
                null, damage, combatDamage);
    }

    public int applyTurnDamageRedirectToCreature(GameData gameData, UUID protectedPlayerId,
                                                 UUID damagedPermanentId, UUID damageSourceId,
                                                 int damage, boolean combatDamage) {
        // No isDamagePreventable check — this is redirection (replacement), not prevention.
        if (damage <= 0 || protectedPlayerId == null || gameData.turnDamageRedirectToCreatureShields.isEmpty()) return damage;

        for (TurnDamageRedirectToCreatureShield shield : gameData.turnDamageRedirectToCreatureShields) {
            if (!shield.protectedPlayerId().equals(protectedPlayerId)) continue;
            if (shield.combatOnly() && !combatDamage) continue;
            if (shield.damageSourceId() != null && !shield.damageSourceId().equals(damageSourceId)) continue;
            if (!shield.includeControlledPermanents() && damagedPermanentId != null) continue;
            UUID targetId = shield.redirectTargetCreatureId();
            // Redirecting damage to the destination creature itself is a no-op; deal it normally.
            if (targetId.equals(damagedPermanentId)) continue;
            // Gideon's Sacrifice also permits a planeswalker destination, but only while the chosen
            // permanent is still a creature or planeswalker on the battlefield.
            Permanent target = gameQueryService.findPermanentById(gameData, targetId);
            if (target == null || (!gameQueryService.isCreature(gameData, target)
                    && (!shield.allowsPlaneswalker() || !gameQueryService.isPlaneswalker(gameData, target)))) {
                continue;
            }
            gameData.pendingSourceRedirectDamage.add(new SourceDamageRedirectShield(protectedPlayerId, null, damage, targetId));
            return 0;
        }
        return damage;
    }

    /** Redirects combat damage from a chosen source to a protected player onto that source's controller. */
    public int applyTurnSourceDamageRedirectToController(GameData gameData, UUID protectedPlayerId,
                                                         UUID sourcePermanentId, int damage) {
        if (damage <= 0 || protectedPlayerId == null || sourcePermanentId == null
                || gameData.turnSourceDamageRedirectToControllerShields.isEmpty()) {
            return damage;
        }

        for (TurnSourceDamageRedirectToControllerShield shield
                : gameData.turnSourceDamageRedirectToControllerShields) {
            if (!shield.protectedPlayerId().equals(protectedPlayerId)
                    || !shield.sourcePermanentId().equals(sourcePermanentId)) {
                continue;
            }
            UUID controllerId = gameQueryService.findPermanentController(gameData, sourcePermanentId);
            if (controllerId == null || !gameData.playerIds.contains(controllerId)
                    || controllerId.equals(protectedPlayerId)) {
                continue;
            }
            gameData.pendingSourceRedirectDamage.add(
                    new SourceDamageRedirectShield(protectedPlayerId, sourcePermanentId, damage, controllerId));
            return 0;
        }
        return damage;
    }

    /** Returns the active target for a player-only combat-damage redirect, if its creature remains. */
    public UUID findCombatDamageRedirectTarget(GameData gameData, UUID protectedPlayerId) {
        if (protectedPlayerId == null || gameData.turnDamageRedirectToCreatureShields.isEmpty()) return null;

        for (TurnDamageRedirectToCreatureShield shield : gameData.turnDamageRedirectToCreatureShields) {
            if (!shield.combatOnly() || !shield.protectedPlayerId().equals(protectedPlayerId)
                    || shield.includeControlledPermanents()) {
                continue;
            }
            Permanent target = gameQueryService.findPermanentById(gameData, shield.redirectTargetCreatureId());
            if (target != null && gameQueryService.isCreature(gameData, target)) {
                return target.getId();
            }
        }
        return null;
    }

    public UUID findStaticUnblockedCreatureDamageRedirectTarget(GameData gameData, UUID protectedPlayerId) {
        if (protectedPlayerId == null) return null;
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(protectedPlayerId, List.of())) {
            if (permanent.isTapped() || !gameQueryService.isCreature(gameData, permanent)) continue;
            boolean redirects = permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(effect -> effect instanceof RedirectPlayerDamageToSelfEffect redirect
                            && redirect.onlyFromUnblockedCreatures());
            if (redirects) return permanent.getId();
        }
        return null;
    }

    public boolean isUnblockedCreatureSource(GameData gameData, UUID sourcePermanentId) {
        if (sourcePermanentId == null || gameData.currentStep == null
                || gameData.currentStep.isBeforeBlockersDeclared()) return false;
        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        return source != null
                && gameQueryService.isCreature(gameData, source)
                && source.isAttacking()
                && !source.isBlockedWithoutBlockers()
                && !gameQueryService.isBlockedByAnyCreature(gameData, source);
    }

    public UUID findCombatDamageRedirectTargetFromSource(GameData gameData, UUID protectedPlayerId,
                                                         UUID damageSourceId) {
        if (protectedPlayerId == null || damageSourceId == null
                || gameData.turnDamageRedirectToCreatureShields.isEmpty()) return null;

        for (TurnDamageRedirectToCreatureShield shield : gameData.turnDamageRedirectToCreatureShields) {
            if (!shield.protectedPlayerId().equals(protectedPlayerId)
                    || shield.includeControlledPermanents()
                    || !damageSourceId.equals(shield.damageSourceId())) {
                continue;
            }
            Permanent target = gameQueryService.findPermanentById(gameData, shield.redirectTargetCreatureId());
            if (target != null && gameQueryService.isCreature(gameData, target)) {
                return target.getId();
            }
        }
        return null;
    }

    /**
     * Palisade Giant: a permanent with a static {@link RedirectPlayerDamageToSelfEffect} whose
     * {@code includeOtherPermanents} flag is set also absorbs damage that would be dealt to the
     * other permanents its controller controls. The player half of that effect lives in
     * {@code PermanentRemovalService.redirectPlayerDamageToEnchantedCreature}; this is the
     * permanent half, called from the creature-damage entry points. This is a redirection
     * (replacement) effect, so it applies even when damage can't be prevented. Damage dealt to the
     * absorbing permanent itself is left alone. Redirected damage is queued in
     * {@link GameData#pendingSourceRedirectDamage} for the caller's
     * {@code processSourceRedirectDamage}.
     *
     * @param protectedPlayerId  the controller of the damaged permanent
     * @param damagedPermanentId the permanent being damaged
     * @param damage             the raw damage amount
     * @return the remaining damage after redirection (0 if redirected)
     */
    public int applyStaticPermanentDamageRedirectToSelf(GameData gameData, UUID protectedPlayerId,
                                                        UUID damagedPermanentId, int damage) {
        if (damage <= 0 || protectedPlayerId == null || damagedPermanentId == null) return damage;
        List<Permanent> battlefield = gameData.playerBattlefields.get(protectedPlayerId);
        if (battlefield == null) return damage;

        for (Permanent permanent : List.copyOf(battlefield)) {
            // "other permanents you control" — the absorbing permanent takes its own damage normally.
            if (permanent.getId().equals(damagedPermanentId)) continue;
            boolean absorbs = permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                    .anyMatch(effect -> effect instanceof RedirectPlayerDamageToSelfEffect e && e.includeOtherPermanents());
            if (!absorbs) continue;
            gameData.pendingSourceRedirectDamage.add(
                    new SourceDamageRedirectShield(protectedPlayerId, null, damage, permanent.getId()));
            return 0;
        }
        return damage;
    }

    public boolean applyColorDamagePreventionForPlayer(GameData gameData, UUID playerId, CardColor sourceColor) {
        if (!gameQueryService.isDamagePreventable(gameData)) return false;
        // Ghostly Flame: a source it covers is colourless for damage, so a Circle of Protection
        // for its printed colour no longer applies.
        sourceColor = gameQueryService.getDamageSourceColor(gameData, sourceColor);
        if (sourceColor == null) return false;
        Map<CardColor, Integer> colorMap = gameData.playerColorDamagePreventionCount.get(playerId);
        if (colorMap == null) return false;
        Integer count = colorMap.get(sourceColor);
        if (count == null || count <= 0) return false;
        colorMap.put(sourceColor, count - 1);
        return true;
    }

    public boolean isColorDamagePreventedForTarget(GameData gameData, UUID targetId, Set<CardColor> sourceColors) {
        if (!gameQueryService.isDamagePreventable(gameData) || targetId == null || sourceColors == null) return false;
        Set<CardColor> preventedColors = gameData.colorDamagePreventionUntilEndOfTurn.get(targetId);
        if (preventedColors == null || preventedColors.isEmpty()) return false;
        return sourceColors.stream()
                .map(color -> gameQueryService.getDamageSourceColor(gameData, color))
                .anyMatch(preventedColors::contains);
    }

    /**
     * Applies static damage reduction from permanents with {@link PreventDamageFromOpponentSourcesEffect}
     * on the receiving player's battlefield (e.g. Guardian Seraph and Energy Field).
     * Only reduces damage from sources not controlled by the receiving player. Returns the damage
     * after reduction (min 0).
     */
    public int applyOpponentSourceDamageReduction(GameData gameData, UUID playerId, UUID sourceControllerId, int damage) {
        if (!gameQueryService.isDamagePreventable(gameData)) return damage;
        if (damage <= 0) return damage;
        if (sourceControllerId == null || sourceControllerId.equals(playerId)) return damage;

        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return damage;

        long totalReduction = 0;
        for (Permanent permanent : battlefield) {
            for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof PreventDamageFromOpponentSourcesEffect e) {
                    totalReduction += e.amount();
                    if (totalReduction >= damage) return 0;
                }
            }
        }

        if (totalReduction <= 0) return damage;
        return (int) (damage - totalReduction);
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
     * Effects restricted to creature sources (Orbs of Warding) only contribute when
     * {@code sourceIsCreature}; artifact-source effects (Sphere of Purity) only contribute when
     * {@code sourceIsArtifact}. Returns the amount prevented (the caller subtracts it); 0 when
     * damage can't be prevented or no such permanent is present.
     */
    public int applyControllerFixedPerSourceDamagePrevention(
            GameData gameData,
            UUID playerId,
            int damage,
            boolean sourceIsCreature,
            boolean sourceIsArtifact
    ) {
        return applyControllerFixedPerSourceDamagePrevention(
                gameData, playerId, damage, sourceIsCreature, sourceIsArtifact, null, false);
    }

    public int applyControllerFixedPerSourceDamagePrevention(GameData gameData, UUID playerId, int damage, boolean sourceIsCreature) {
        return applyControllerFixedPerSourceDamagePrevention(
                gameData, playerId, damage, sourceIsCreature, false, null, false);
    }

    /**
     * Urza's Armor-style prevention with an optional source-color restriction. A null source-color
     * set means that only unrestricted effects can match; callers should pass the source's effective
     * damage colors when color-restricted effects need to be evaluated.
     */
    public int applyControllerFixedPerSourceDamagePrevention(GameData gameData, UUID playerId, int damage,
                                                              boolean sourceIsCreature, Set<CardColor> sourceColors) {
        return applyControllerFixedPerSourceDamagePrevention(
                gameData, playerId, damage, sourceIsCreature, false, sourceColors, false);
    }

    public int applyControllerFixedPerSourceDamagePrevention(GameData gameData, UUID playerId, int damage,
                                                              boolean sourceIsCreature, boolean sourceIsArtifact,
                                                              Set<CardColor> sourceColors) {
        return applyControllerFixedPerSourceDamagePrevention(
                gameData, playerId, damage, sourceIsCreature, sourceIsArtifact, sourceColors, false);
    }

    public int applyControllerFixedPerSourceDamagePrevention(GameData gameData, UUID playerId, int damage,
                                                              boolean sourceIsCreature, boolean sourceIsArtifact,
                                                              Set<CardColor> sourceColors, boolean combatDamage) {
        if (!gameQueryService.isDamagePreventable(gameData)) return 0;
        if (damage <= 0) return 0;

        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return 0;

        int reduction = 0;
        for (Permanent permanent : battlefield) {
            for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof PreventFixedDamagePerSourceToControllerEffect prevention) {
                    if (prevention.combatOnly() && !combatDamage) continue;
                    if (prevention.requiresUntappedSource() && permanent.isTapped()) continue;
                    if (prevention.creatureSourcesOnly() && !sourceIsCreature) continue;
                    if (prevention.artifactSourcesOnly() && !sourceIsArtifact) continue;
                    if (prevention.sourceColors() != null
                            && (sourceColors == null
                            || prevention.sourceColors().stream().noneMatch(sourceColors::contains))) continue;
                    reduction += prevention.amount();
                } else if (effect instanceof PreventXDamagePerSourceToControllerAndCreaturesEffect prevention
                        && (!prevention.combatOnly() || combatDamage)
                        && (!prevention.creatureSourcesOnly() || sourceIsCreature)) {
                    reduction += amountEvaluationService.evaluate(gameData, prevention.amount(),
                            AmountContext.forStaticEffect(permanent, playerId));
                }
            }
        }
        return Math.min(damage, reduction);
    }

    /**
     * Ajani Steadfast-style prevention: prevent all but 1 damage to the controller or one of their
     * planeswalkers. The effect is recipient-scoped and applies once per damage event.
     */
    public int applyAllButOneDamagePrevention(GameData gameData, UUID recipientControllerId, int damage) {
        if (!gameQueryService.isDamagePreventable(gameData)) return 0;
        if (damage <= 1 || recipientControllerId == null) return 0;

        List<Permanent> battlefield = gameData.playerBattlefields.get(recipientControllerId);
        boolean hasPrevention = battlefield != null && battlefield.stream()
                .flatMap(p -> p.getCard().getEffects(EffectSlot.STATIC).stream())
                .anyMatch(PreventAllButOneDamageToControllerAndPlaneswalkersEffect.class::isInstance);
        if (!hasPrevention) {
            hasPrevention = gameData.emblems.stream()
                    .anyMatch(emblem -> recipientControllerId.equals(emblem.controllerId())
                            && emblem.staticEffects().stream()
                            .anyMatch(PreventAllButOneDamageToControllerAndPlaneswalkersEffect.class::isInstance));
        }
        return hasPrevention ? damage - 1 : 0;
    }

    /**
     * Djeru, With Eyes Open-style prevention: "If a source would deal damage to a planeswalker you
     * control, prevent N of that damage." Keyed on the controller of the damaged planeswalker: sums
     * the {@code amount} of every {@link PlaneswalkerDamagePreventionEffect} that player controls and
     * prevents up to that from each source that would deal damage to a planeswalker they control
     * (combat and noncombat). Returns the amount prevented (the caller subtracts it); 0 when damage
     * can't be prevented or no such permanent is present.
     */
    public int applyPlaneswalkerFixedPerSourceDamagePrevention(GameData gameData, UUID planeswalkerControllerId, int damage) {
        if (!gameQueryService.isDamagePreventable(gameData)) return 0;
        if (damage <= 0 || planeswalkerControllerId == null) return 0;

        List<Permanent> battlefield = gameData.playerBattlefields.get(planeswalkerControllerId);
        if (battlefield == null) return 0;

        int reduction = battlefield.stream()
                .flatMap(p -> p.getCard().getEffects(EffectSlot.STATIC).stream())
                .filter(e -> e instanceof PlaneswalkerDamagePreventionEffect)
                .mapToInt(e -> ((PlaneswalkerDamagePreventionEffect) e).amount())
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
                            totalReduction += amountEvaluationService.evaluate(gameData, e.amount(),
                                    AmountContext.forStaticEffect(p, playerId));
                        }
                    }
                }
            }
        }

        if (totalReduction <= 0) return damage;
        return Math.max(0, damage - totalReduction);
    }

    private void recordPlusOnePlusOneCounterPlacedOnControlledPermanent(GameData gameData, Permanent permanent) {
        UUID controllerId = gameQueryService.findPermanentController(gameData, permanent.getId());
        if (controllerId != null) {
            gameData.playersWhoControlledPermanentsThatReceivedPlusOneCountersThisTurn.add(controllerId);
        }
    }
}
