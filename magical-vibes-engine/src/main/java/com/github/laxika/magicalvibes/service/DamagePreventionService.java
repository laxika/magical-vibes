package com.github.laxika.magicalvibes.service;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.service.effect.LayerSystemService;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.CombatDamagePreventionTokenShield;
import com.github.laxika.magicalvibes.model.ChannelHarmShield;
import com.github.laxika.magicalvibes.model.CombatDamagePreventionTokenShield;
import com.github.laxika.magicalvibes.model.ComeuppanceDamagePreventionShield;
import com.github.laxika.magicalvibes.model.CreatureControllerDamageRedirectShield;
import com.github.laxika.magicalvibes.model.CreatureDamageRedirectShield;
import com.github.laxika.magicalvibes.model.DamagePreventionLifeGainShield;
import com.github.laxika.magicalvibes.model.DamageRedirectShield;
import com.github.laxika.magicalvibes.model.EyeForAnEyeReflection;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GameLog;
import com.github.laxika.magicalvibes.model.JudgmentOfAlexanderDamagePreventionShield;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.PermanentChoiceContext;
import com.github.laxika.magicalvibes.model.PendingMayAbility;
import com.github.laxika.magicalvibes.model.PlayerNextDamageRedirectShield;
import com.github.laxika.magicalvibes.model.PlayerSourceNextDamageRedirectShield;
import com.github.laxika.magicalvibes.model.SourceNextDamageRedirectToPermanentShield;
import com.github.laxika.magicalvibes.model.PlayerSourceNextDamageShield;
import com.github.laxika.magicalvibes.model.SourceDamageRedirectShield;
import com.github.laxika.magicalvibes.model.SourceNextCombatDamageToOpponentRedirectShield;
import com.github.laxika.magicalvibes.model.SourceNextCombatDamageToControllerShield;
import com.github.laxika.magicalvibes.model.SourcePermanentAndControllerNextDamageRedirectShield;
import com.github.laxika.magicalvibes.model.SourceNextDamageToAnyTargetShield;
import com.github.laxika.magicalvibes.model.TargetSourceDamagePreventionShield;
import com.github.laxika.magicalvibes.model.TargetSorceryDamageRedirectShield;
import com.github.laxika.magicalvibes.model.TurnDamageRedirectToCreatureShield;
import com.github.laxika.magicalvibes.model.TurnSourceDamageRedirectToControllerShield;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllCombatDamageToAndByEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllCombatDamageToAttachedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllCombatDamageToAndBySelfEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllCombatDamageToSelfEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToAndByEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToAttachedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.service.effect.MaroGoneNutsSupport;
import com.github.laxika.magicalvibes.model.effect.DamageHealingEffect;
import com.github.laxika.magicalvibes.model.effect.ControlledCreaturesDamageReductionEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllNoncombatDamageToAttachedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageAndAddMinusCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageAndAddPlusCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PreventCombatDamageToAttackingCreaturesYouControlEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageAndRemovePlusOnePlusOneCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageFromOpponentSourcesEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToOtherCreaturesAndAddPlusCountersEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToSelfFromCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.PreventCombatDamageToSelfAndAddPlusOneCounterEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToControllerPerClericEffect;
import com.github.laxika.magicalvibes.model.effect.PlaneswalkerDamagePreventionEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PreventFixedDamagePerSourceToControllerEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeRecipient;
import com.github.laxika.magicalvibes.model.filter.PermanentAttachedToSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentControlledBySourceControllerPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;
import com.github.laxika.magicalvibes.model.effect.PreventXDamagePerSourceToControllerAndCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.PreventNoncombatDamageToControllerAndGainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.PreventHalfDamageToControllerAndTheirPermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerAndCreaturesDamagePreventionEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerAndPermanentsNoncombatDamagePreventionEffect;
import com.github.laxika.magicalvibes.model.effect.FilteredCreaturesDamagePreventionEffect;
import com.github.laxika.magicalvibes.model.effect.AllButOneDamagePreventionEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToSelfAndSourceControllerDrawsEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToSelfAndDealThatMuchDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PreventDamageAndSacrificeAttachedEquipmentEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllCombatDamageToSelfFromBlockersEffect;
import com.github.laxika.magicalvibes.model.effect.PreventAllDamageToSelfFromCreaturesItBlocksEffect;
import com.github.laxika.magicalvibes.model.effect.PreventCombatDamageToSelfAndExileFromLibraryEffect;
import com.github.laxika.magicalvibes.model.effect.PreventCombatDamageToPlayerAndDrawHalfEffect;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.ControllerOpponentDamageMillReplacementEffect;
import com.github.laxika.magicalvibes.model.effect.PreventSpellDamageToOpponentAndCreateTokensEffect;
import com.github.laxika.magicalvibes.model.effect.PreventXDamageFromEachSourceToAttachedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.SelfDamagePreventionEffect;
import com.github.laxika.magicalvibes.model.effect.DamagePreventionControlChangeEffect;
import com.github.laxika.magicalvibes.model.effect.ControlDuration;
import com.github.laxika.magicalvibes.model.effect.EffectDuration;
import com.github.laxika.magicalvibes.model.effect.GainControlOfTargetEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectPlayerDamageToEnchantedCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectPlayerDamageToSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectUnblockedCombatDamageToSelfEffect;
import com.github.laxika.magicalvibes.model.effect.RedirectAllDamageToEnchantedCreatureControllerEffect;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.AnyTargetPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentIsSourcePermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;
import com.github.laxika.magicalvibes.model.filter.PlayerRelation;
import com.github.laxika.magicalvibes.model.filter.PlayerRelationPredicate;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.StackEntryType;
import com.github.laxika.magicalvibes.model.replacement.ReplacementOption;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.battlefield.CreatureControlService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.DamagePreventionReplacementSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.LifeSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.PermanentControlSupport;
import com.github.laxika.magicalvibes.service.effect.normalfx.DestructionSupport;
import com.github.laxika.magicalvibes.service.input.PlayerInputService;
import com.github.laxika.magicalvibes.service.effect.staticfx.StaticEffectConditionResolver;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.trigger.TriggerCollectionService;
import org.springframework.beans.factory.ObjectProvider;
import com.github.laxika.magicalvibes.service.GameLogService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Objects;
import java.util.Set;
import java.util.UUID;
import java.util.function.Predicate;
import com.github.laxika.magicalvibes.model.CounterType;

@Slf4j
@Component
public class DamagePreventionService {

    @org.springframework.beans.factory.annotation.Autowired
    private com.github.laxika.magicalvibes.service.interaction.InteractionHandlerRegistry interactionHandlerRegistry;

    private final GameQueryService gameQueryService;
    private final ObjectProvider<LayerSystemService> layerSystemServiceProvider;
    private final PredicateEvaluationService predicateEvaluationService;
    private final LifeSupport lifeSupport;
    private final DrawService drawService;
    private final AmountEvaluationService amountEvaluationService;
    private final DamagePreventionReplacementSupport damagePreventionReplacementSupport;
    private final StaticEffectConditionResolver staticEffectConditionResolver;
    private final ObjectProvider<PermanentControlSupport> permanentControlSupportProvider;
    private final ObjectProvider<CreatureControlService> creatureControlServiceProvider;
    private final ObjectProvider<PlayerInputService> playerInputServiceProvider;
    private final ObjectProvider<DestructionSupport> destructionSupportProvider;
    private final GameLogService gameLogService;
    private final TriggerCollectionService triggerCollectionService;

    public DamagePreventionService(GameQueryService gameQueryService,
                                   ObjectProvider<LayerSystemService> layerSystemServiceProvider,
                                   PredicateEvaluationService predicateEvaluationService,
                                   LifeSupport lifeSupport, DrawService drawService,
                                   AmountEvaluationService amountEvaluationService,
                                   DamagePreventionReplacementSupport damagePreventionReplacementSupport,
                                   StaticEffectConditionResolver staticEffectConditionResolver,
                                   ObjectProvider<PermanentControlSupport> permanentControlSupportProvider,
                                   ObjectProvider<CreatureControlService> creatureControlServiceProvider,
                                   ObjectProvider<PlayerInputService> playerInputServiceProvider,
                                   ObjectProvider<DestructionSupport> destructionSupportProvider,
                                   GameLogService gameLogService,
                                   TriggerCollectionService triggerCollectionService) {
        this.gameQueryService = gameQueryService;
        this.layerSystemServiceProvider = layerSystemServiceProvider;
        this.predicateEvaluationService = predicateEvaluationService;
        this.lifeSupport = lifeSupport;
        this.drawService = drawService;
        this.amountEvaluationService = amountEvaluationService;
        this.damagePreventionReplacementSupport = damagePreventionReplacementSupport;
        this.staticEffectConditionResolver = staticEffectConditionResolver;
        this.permanentControlSupportProvider = permanentControlSupportProvider;
        this.creatureControlServiceProvider = creatureControlServiceProvider;
        this.playerInputServiceProvider = playerInputServiceProvider;
        this.destructionSupportProvider = destructionSupportProvider;
        this.gameLogService = gameLogService;
        this.triggerCollectionService = triggerCollectionService;
    }

    /** Applies a one-shot replacement that destroys a target creature instead of dealing damage. */
    public boolean replaceNextDamageToTargetWithDestruction(GameData gameData, Permanent permanent, int damage) {
        if (damage <= 0 || permanent == null || permanent.getDamageDestructionShield() <= 0
                || !gameQueryService.isCreature(gameData, permanent)
                || gameQueryService.findPermanentById(gameData, permanent.getId()) != permanent) {
            return false;
        }

        permanent.setDamageDestructionShield(permanent.getDamageDestructionShield() - 1);
        destructionSupportProvider.getObject().tryDestroyAndLog(
                gameData, permanent, "a damage replacement effect");
        return true;
    }

    /**
     * Swans of Bryn Argoll: "If a source would deal damage to this creature, prevent that damage.
     * The source's controller draws cards equal to the damage prevented this way." Returns {@code true}
     * when the damage is fully prevented (the caller must then deal no damage to {@code target}).
     * When {@code sourceControllerId} is non-null, that player draws one card per point of prevented
     * damage. Prevention (and therefore the draw) only applies while damage is currently preventable.
     */
    public boolean applySwansSourceControllerDraw(GameData gameData, Permanent target, int damage, UUID sourceControllerId) {
        return applySwansSourceControllerDraw(gameData, target, damage, sourceControllerId, false);
    }

    public boolean applySwansSourceControllerDraw(GameData gameData, Permanent target, int damage,
                                                   UUID sourceControllerId, boolean combatDamage) {
        if (!gameQueryService.isDamagePreventable(gameData, combatDamage)) return false;
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
     * Sokrates, Athenian Teacher: prevents combat damage to a player from the affected creature and
     * makes the creature's controller and that player each draw half the damage, rounded down.
     */
    public boolean applySokraticDialogue(GameData gameData, Permanent source, int damage,
                                         UUID sourceControllerId, UUID playerId) {
        if (damage <= 0) return false;
        if (!gameQueryService.hasActiveStaticEffectIncludingGranted(
                gameData, source, PreventCombatDamageToPlayerAndDrawHalfEffect.class)) return false;

        int cards = damage / 2;
        if (sourceControllerId != null) {
            for (int i = 0; i < cards; i++) {
                drawService.resolveDrawCard(gameData, sourceControllerId);
            }
        }
        if (playerId != null) {
            for (int i = 0; i < cards; i++) {
                drawService.resolveDrawCard(gameData, playerId);
            }
        }
        // The draws happen either way; the damage is only prevented when damage prevention is allowed.
        return gameQueryService.isDamagePreventable(gameData, true);
    }

    /**
     * Gloom Surgeon: "If combat damage would be dealt to this creature, prevent that damage and exile
     * that many cards from the top of your library." Returns {@code true} when the damage is fully
     * prevented (the caller must then deal no damage to {@code target}). Only call this from the combat
     * damage path — noncombat damage is unaffected. Exiling stops early on an empty library.
     */
    public boolean applyPreventCombatDamageToSelfAndExile(GameData gameData, Permanent target, int damage) {
        if (!gameQueryService.isDamagePreventable(gameData, true)) return false;
        if (damage <= 0) return false;
        if (!gameQueryService.hasActiveStaticEffect(gameData, target,
                PreventCombatDamageToSelfAndExileFromLibraryEffect.class)) return false;
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
        if (!gameQueryService.isDamagePreventable(gameData, true)) return false;
        boolean hasEffect = gameQueryService.hasActiveStaticEffect(
                gameData, target, PreventAllCombatDamageToSelfFromBlockersEffect.class);
        return hasEffect && source.isBlocking() && source.getBlockingTargetIds().contains(target.getId());
    }

    public boolean isCombatDamageFromCreatureBlockedByTargetPrevented(GameData gameData, Permanent target,
                                                                       Permanent source) {
        if (!gameQueryService.isDamagePreventable(gameData, true)) return false;
        boolean hasEffect = target.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(PreventAllDamageToSelfFromCreaturesItBlocksEffect.class::isInstance);
        return hasEffect && gameQueryService.isCreature(gameData, source)
                && target.isBlocking() && target.getBlockingTargetIds().contains(source.getId());
    }

    public int applyGlobalPreventionShield(GameData gameData, int damage) {
        return applyGlobalPreventionShield(gameData, damage, false);
    }

    public int applyGlobalPreventionShield(GameData gameData, int damage, boolean combatDamage) {
        if (!gameQueryService.isDamagePreventable(gameData, combatDamage)) return damage;
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
        return applyTargetSourcePreventionShield(gameData, targetId, sourceId, damage, false);
    }

    public int applyTargetSourcePreventionShield(GameData gameData, UUID targetId, UUID sourceId, int damage,
                                                  boolean combatDamage) {
        if (!gameQueryService.isDamagePreventable(gameData, combatDamage)) return damage;
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

    /** Heals all damage previously marked on a permanent when a positive damage event reaches it. */
    public void applyDamageHealingReplacement(GameData gameData, Permanent permanent, int damage) {
        if (permanent == null || damage <= 0) {
            return;
        }
        if (gameQueryService.hasActiveStaticEffect(gameData, permanent, DamageHealingEffect.class)) {
            permanent.healDamage();
        }
    }

    /** Replaces effect-based destruction by removing one shield counter. */
    public boolean replaceDestructionWithShieldCounter(Permanent permanent) {
        if (permanent == null || permanent.getCounterCount(CounterType.SHIELD) <= 0) {
            return false;
        }
        permanent.setCounterCount(CounterType.SHIELD,
                permanent.getCounterCount(CounterType.SHIELD) - 1);
        return true;
    }

    /** Returns whether a permanent replaces damage to itself with +1/+1 counters. */
    public boolean hasDamageToPlusOnePlusOneCounterReplacement(Permanent permanent) {
        return permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(e -> e instanceof PreventDamageAndAddPlusCountersEffect);
    }

    public int applyCreaturePreventionShield(GameData gameData, Permanent permanent, int damage, boolean isCombatDamage) {
        return applyCreaturePreventionShield(gameData, permanent, damage, isCombatDamage, null);
    }

    public int applyCreaturePreventionShieldWithoutSelfDamagePrevention(
            GameData gameData, Permanent permanent, int damage, boolean isCombatDamage) {
        return applyCreaturePreventionShield(gameData, permanent, damage, isCombatDamage, null, false);
    }

    public int applyCreaturePreventionShield(GameData gameData, Permanent permanent, int damage,
                                              boolean isCombatDamage, Permanent damageSource) {
        return applyCreaturePreventionShield(gameData, permanent, damage, isCombatDamage, damageSource, true);
    }

    private int applyCreaturePreventionShield(GameData gameData, Permanent permanent, int damage,
                                              boolean isCombatDamage, Permanent damageSource,
                                              boolean applySelfDamagePrevention) {
        CreatureDamageStep.Context context = new CreatureDamageStep.Context(
                permanent, isCombatDamage, damageSource, applySelfDamagePrevention);
        for (CreatureDamageStep step : creatureDamageSteps()) {
            if (damage <= 0) return 0;
            if (!step.applies().test(gameData, context, damage)) continue;
            boolean endsChain = step.endsChain().test(gameData, context, damage);
            damage = step.apply().apply(gameData, context, damage);
            if (endsChain) return damage;
        }
        return Math.max(0, damage);
    }

    /**
     * The replacement and prevention effects that can apply to damage dealt to a permanent, in the
     * order the fixed-order chain applies them. Counter and destruction replacements come first and
     * apply even to damage that can't be prevented; every step after "can't be prevented" is a
     * prevention effect.
     */
    public List<CreatureDamageStep> creatureDamageSteps() {
        List<CreatureDamageStep> steps = creatureDamageSteps;
        if (steps == null) {
            steps = buildCreatureDamageSteps();
            creatureDamageSteps = steps;
        }
        return steps;
    }

    private volatile List<CreatureDamageStep> creatureDamageSteps;

    private static final CreatureDamageStep.Applies NEVER = (gameData, context, damage) -> false;
    private static final CreatureDamageStep.Applies ALWAYS = (gameData, context, damage) -> true;

    private CreatureDamageStep step(String family, String label, ReplacementOption.Kind kind, boolean pure,
                                    CreatureDamageStep.Applies applies, CreatureDamageStep.Apply apply) {
        return new CreatureDamageStep(family, label, kind, pure, applies, apply, NEVER);
    }

    private CreatureDamageStep preventAll(String family, String label, CreatureDamageStep.Applies applies) {
        return step(family, label, ReplacementOption.Kind.PREVENTION, true, applies, (gameData, context, damage) -> 0);
    }

    /** Whether nothing stops this damage from being prevented (CR 615.12). */
    private boolean preventable(GameData gameData, CreatureDamageStep.Context context) {
        return !isDamageUnpreventable(gameData, context)
                && gameQueryService.isDamagePreventable(gameData, context.combatDamage());
    }

    private boolean isDamageUnpreventable(GameData gameData, CreatureDamageStep.Context context) {
        return context.permanent().isDamageCantBePreventedOrRedirectedThisTurn()
                || !gameQueryService.isDamagePreventable(gameData)
                || (context.damageSource() != null && gameQueryService.damageCantBePreventedFromSource(
                gameData, context.damageSource(), context.combatDamage()));
    }

    private UUID controllerOf(GameData gameData, Permanent permanent) {
        return gameQueryService.findPermanentController(gameData, permanent.getId());
    }

    private List<CreatureDamageStep> buildCreatureDamageSteps() {
        ReplacementOption.Kind replacement = ReplacementOption.Kind.REPLACEMENT;
        ReplacementOption.Kind prevention = ReplacementOption.Kind.PREVENTION;
        List<CreatureDamageStep> steps = new ArrayList<>();
        steps.add(preventAll("AllCombatDamagePrevented", "Prevent all combat damage",
                (gd, c, damage) -> c.combatDamage() && gd.preventAllCombatDamage && preventable(gd, c)));
        steps.add(step("DestructionShield", "Destroy it instead", replacement, false,
                (gd, c, damage) -> damage > 0 && c.permanent().getDamageDestructionShield() > 0
                        && gameQueryService.isCreature(gd, c.permanent())
                        && gameQueryService.findPermanentById(gd, c.permanent().getId()) == c.permanent(),
                (gd, c, damage) -> replaceNextDamageToTargetWithDestruction(gd, c.permanent(), damage) ? 0 : damage));
        steps.add(step("ControlledCreaturesReduction", "Reduce the damage to your creature", replacement, true,
                (gd, c, damage) -> damage > 0 && gameQueryService.isCreature(gd, c.permanent())
                        && applyControlledCreaturesDamageReduction(gd, c.permanent(), damage) < damage,
                (gd, c, damage) -> applyControlledCreaturesDamageReduction(gd, c.permanent(), damage)));
        steps.add(step("DamageToPlusOneCounters", "Put +1/+1 counters on it instead", replacement, false,
                (gd, c, damage) -> damage > 0 && ((!gameQueryService.hasLostAllAbilities(gd, c.permanent())
                        && c.permanent().getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(PreventDamageAndAddPlusCountersEffect.class::isInstance))
                        || gameQueryService.hasAuraWithEffect(gd, c.permanent(), PreventDamageAndAddPlusCountersEffect.class)),
                (gd, c, damage) -> {
                    Permanent permanent = c.permanent();
                    if (!gameQueryService.cantHavePlusOnePlusOneCounters(gd, permanent)) {
                        int counters = gameQueryService.doublePlusOnePlusOneCounters(gd, permanent, damage);
                        if (counters > 0) {
                            permanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE,
                                    permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + counters);
                            recordPlusOnePlusOneCounterPlacedOnControlledPermanent(gd, permanent, counters);
                        }
                    }
                    return 0;
                }));
        steps.add(step("DamageToMinusOneCounters", "Put -1/-1 counters on it instead", replacement, false,
                (gd, c, damage) -> damage > 0 && preventable(gd, c)
                        && !gameQueryService.hasLostAllAbilities(gd, c.permanent())
                        && c.permanent().getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(PreventDamageAndAddMinusCountersEffect.class::isInstance),
                (gd, c, damage) -> {
                    Permanent permanent = c.permanent();
                    if (!gameQueryService.cantHaveCounters(gd, permanent)
                            && !gameQueryService.cantHaveMinusOneMinusOneCounters(gd, permanent)) {
                        int counters = gameQueryService.reduceMinusOneMinusOneCounters(gd, permanent, damage);
                        if (counters > 0) {
                            permanent.setCounterCount(CounterType.MINUS_ONE_MINUS_ONE,
                                    permanent.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE) + counters);
                        }
                    }
                    return 0;
                }));
        steps.add(step("CombatDamageToPlusOneCounter", "Put a +1/+1 counter on it instead", replacement, false,
                (gd, c, damage) -> damage > 0 && c.combatDamage()
                        && hasActiveCombatDamageToSelfAndAddPlusOneCounterEffect(gd, c.permanent(), c.damageSource()),
                (gd, c, damage) -> applyCombatDamageToSelfAndAddPlusOneCounter(gd, c.permanent(), c.damageSource(), damage)));
        steps.add(step("PlusOneCounterShieldUnpreventable", "Put +1/+1 counters on it equal to the damage (can't be prevented)", replacement, false,
                (gd, c, damage) -> damage > 0 && !preventable(gd, c)
                        && c.permanent().isAllDamageToPlusOnePlusOneCounterPreventionShield(),
                (gd, c, damage) -> {
                    putShieldPlusOneCounters(gd, c.permanent(), damage);
                    return damage;
                }));
        steps.add(step("ShieldCounter", "Remove a shield counter to prevent the damage", prevention, false,
                (gd, c, damage) -> damage > 0 && c.permanent().getCounterCount(CounterType.SHIELD) > 0,
                (gd, c, damage) -> {
                    boolean unpreventable = isDamageUnpreventable(gd, c);
                    consumeShieldCounter(gd, c.permanent());
                    return unpreventable ? damage : 0;
                }));
        steps.add(preventAll("ProtectedUntilNextTurn", "Prevent all damage to it until your next turn",
                (gd, c, damage) -> gameQueryService.isDamagePreventable(gd, c.combatDamage())
                        && gd.isProtectedFromDamageUntilNextTurn(c.permanent().getId())));
        steps.add(preventAll("NoncombatControllerAndPermanents", "Prevent noncombat damage to your permanents",
                (gd, c, damage) -> !c.combatDamage()
                        && applyControllerAndPermanentsNoncombatDamagePrevention(gd, c.permanent(), damage) < damage));
        steps.add(preventAll("AllCreaturesPrevented", "Prevent all damage to creatures",
                (gd, c, damage) -> gameQueryService.isDamagePreventable(gd, c.combatDamage())
                        && gd.preventAllDamageToAllCreatures));
        steps.add(preventAll("CreaturePrevented", "Prevent all damage to it this turn",
                (gd, c, damage) -> gameQueryService.isDamagePreventable(gd, c.combatDamage())
                        && gd.creaturesWithAllDamagePrevented.contains(c.permanent().getId())));
        steps.add(preventAll("PredicatePrevented", "Prevent all damage to it this turn",
                (gd, c, damage) -> gameQueryService.isDamagePreventable(gd, c.combatDamage())
                        && gameQueryService.isAllDamagePreventedByPredicate(gd, c.permanent())));
        steps.add(preventAll("ControlledPredicatePrevented", "Prevent all damage to your matching permanents",
                (gd, c, damage) -> gameQueryService.isDamagePreventable(gd, c.combatDamage())
                        && gameQueryService.isDamagePreventedByControlledPredicate(gd, c.permanent(), c.combatDamage())));
        steps.add(preventAll("CombatDamagePreventedForCreature", "Prevent combat damage to it",
                (gd, c, damage) -> c.combatDamage() && gameQueryService.isDamagePreventable(gd, true)
                        && (gd.creaturesWithCombatDamagePrevented.contains(c.permanent().getId())
                        || gd.creaturesWithCombatDamagePreventedThisCombat.contains(c.permanent().getId()))));
        steps.add(preventAll("ControllerCreaturesPrevented", "Prevent all damage to your creatures",
                (gd, c, damage) -> gameQueryService.isDamagePreventable(gd, c.combatDamage())
                        && controllerOf(gd, c.permanent()) != null
                        && gd.playersWithAllCreatureDamagePrevented.contains(controllerOf(gd, c.permanent()))));
        steps.add(preventAll("ControllerAllDamagePrevented", "Prevent all damage to you and your creatures or permanents",
                (gd, c, damage) -> gameQueryService.isDamagePreventable(gd, c.combatDamage())
                        && controllerOf(gd, c.permanent()) != null
                        && (gd.playersWithAllPermanentDamagePrevented.contains(controllerOf(gd, c.permanent()))
                        || (gameQueryService.isCreature(gd, c.permanent())
                        && gd.playersWithAllDamagePrevented.contains(controllerOf(gd, c.permanent()))))));
        steps.add(preventAll("PlaneswalkerCombatPrevented", "Prevent combat damage to your planeswalkers",
                (gd, c, damage) -> c.combatDamage() && gameQueryService.isDamagePreventable(gd, true)
                        && gameQueryService.isPlaneswalker(gd, c.permanent())
                        && controllerOf(gd, c.permanent()) != null
                        && gd.playersWithAllCombatDamageToPlayerAndPlaneswalkersPrevented
                        .contains(controllerOf(gd, c.permanent()))));
        steps.add(step("RemoveCountersOrSacrifice", "Remove counters from it, or sacrifice it, instead", replacement, false,
                (gd, c, damage) -> damagePreventionReplacementSupport.removesCountersOrSacrificesForDamage(
                        gd, c.permanent(), damage),
                (gd, c, damage) -> damagePreventionReplacementSupport
                        .applyDamageToPermanentByRemovingCountersOrSacrificing(gd, c.permanent(), damage) ? 0 : damage));
        steps.add(new CreatureDamageStep("RemovePlusOneCounters", "Remove +1/+1 counters from it instead",
                replacement, false,
                (gd, c, damage) -> {
                    var effect = preventRemovePlusOneCountersEffect(gd, c.permanent());
                    boolean hasCounter = c.permanent().getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) > 0;
                    return damage > 0 && effect != null && (!effect.requiresCounter() || hasCounter)
                            && (!effect.preventOnlyIfCounterAvailable() || hasCounter);
                },
                this::removePlusOneCountersForDamage,
                (gd, c, damage) -> !gameQueryService.isDamagePreventable(gd, c.combatDamage())));
        steps.add(step("SelfDamageControlChangeUnpreventable",
                "Have an opponent gain control of it (the damage can't be prevented)", prevention, false,
                (gd, c, damage) -> damage > 0 && !preventable(gd, c) && c.applySelfDamagePrevention()
                        && hasControlChangeOnPrevention(gd, c.permanent())
                        && selfDamagePrevented(gd, c.permanent(), damage) > 0,
                (gd, c, damage) -> {
                    makeOpponentGainControl(gd, c.permanent());
                    return damage;
                }));
        steps.add(step("SelfDamagePrevention", "Prevent damage to it", prevention, false,
                (gd, c, damage) -> preventable(gd, c) && c.applySelfDamagePrevention()
                        && applySelfDamagePreventionAmount(gd, c.permanent(), damage) > 0,
                (gd, c, damage) -> applySelfDamagePreventionShield(gd, c.permanent(), damage)));
        steps.add(step("ControllerAndCreaturesPerSource", "Prevent some of the damage from this source", prevention, true,
                (gd, c, damage) -> preventable(gd, c) && gameQueryService.isCreature(gd, c.permanent())
                        && controllerAndCreaturesPerSourcePrevention(gd, controllerOf(gd, c.permanent()),
                        damage, c.combatDamage(), c.damageSource(), c.permanent()) > 0,
                (gd, c, damage) -> damage - controllerAndCreaturesPerSourcePrevention(gd,
                        controllerOf(gd, c.permanent()), damage, c.combatDamage(), c.damageSource(), c.permanent())));
        steps.add(step("AllButOneToHero", "Prevent all but 1 of the damage", prevention, true,
                (gd, c, damage) -> preventable(gd, c)
                        && applyAllButOneDamageToHeroPrevention(gd, c.permanent(), damage, c.combatDamage()) > 0,
                (gd, c, damage) -> damage - applyAllButOneDamageToHeroPrevention(gd, c.permanent(), damage, c.combatDamage())));
        steps.add(preventAll("PreventAllDamageToSelf", "Prevent all damage to it",
                (gd, c, damage) -> preventable(gd, c)
                        && gameQueryService.hasActiveStaticEffect(gd, c.permanent(), PreventAllDamageEffect.class)));
        steps.add(step("SacrificeEquipment", "Sacrifice an attached Equipment to prevent the damage", prevention, false,
                (gd, c, damage) -> preventable(gd, c) && gameQueryService.hasActiveStaticEffect(
                        gd, c.permanent(), PreventDamageAndSacrificeAttachedEquipmentEffect.class)
                        && hasAttachedEquipment(gd, c.permanent()),
                (gd, c, damage) -> preventDamageAndSacrificeAttachedEquipment(gd, c.permanent()) ? 0 : damage));
        steps.add(step("PreventAndDealThatMuch", "Prevent the damage and have it deal that much", prevention, false,
                (gd, c, damage) -> preventable(gd, c) && gameQueryService.hasActiveStaticEffect(
                        gd, c.permanent(), PreventDamageToSelfAndDealThatMuchDamageEffect.class),
                (gd, c, damage) -> {
                    queuePhyrexianVindicatorTrigger(gd, c.permanent(), damage);
                    return 0;
                }));
        steps.add(preventAll("PermanentSourceToCreature", "Prevent damage from this source",
                (gd, c, damage) -> preventable(gd, c) && c.damageSource() != null
                        && gameQueryService.isDamageFromPermanentSourceToCreaturePrevented(
                        gd, c.damageSource(), c.permanent(), c.combatDamage())));
        steps.add(preventAll("ArtifactToEnchantedCreature", "Prevent damage from artifacts",
                (gd, c, damage) -> preventable(gd, c) && c.damageSource() != null
                        && gameQueryService.isArtifactDamageToEnchantedCreaturePrevented(
                        gd, c.permanent(), c.damageSource(), null)));
        steps.add(preventAll("ArtifactToSelf", "Prevent damage from artifacts",
                (gd, c, damage) -> preventable(gd, c) && c.damageSource() != null
                        && gameQueryService.isArtifactDamageToSelfPrevented(gd, c.permanent(), c.damageSource(), null)));
        steps.add(preventAll("CreatureSourceToSelf", "Prevent damage from creatures",
                (gd, c, damage) -> preventable(gd, c) && gameQueryService.isCreatureSourceDamageToSelfPrevented(
                        gd, c.permanent(), null, c.damageSource(), c.combatDamage())));
        steps.add(preventAll("DesertsToSelf", "Prevent damage from Deserts",
                (gd, c, damage) -> preventable(gd, c) && gameQueryService.isDamageFromDesertsToSelfPrevented(
                        gd, c.permanent(), null, c.damageSource(), c.combatDamage())));
        steps.add(preventAll("DesertsToCamelOrBanded", "Prevent damage from Deserts",
                (gd, c, damage) -> preventable(gd, c) && gameQueryService.isDamageFromDesertsToCamelOrBandedCreaturePrevented(
                        gd, c.permanent(), null, c.damageSource(), c.combatDamage())));
        steps.add(preventAll("EnchantedToAndBy", "Prevent all damage to the enchanted creature",
                (gd, c, damage) -> preventable(gd, c) && gameQueryService.hasAuraWithEffect(
                        gd, c.permanent(), PreventAllDamageToAndByEnchantedCreatureEffect.class)));
        steps.add(preventAll("AttachedPrevention", "Prevent all damage to the equipped or enchanted creature",
                (gd, c, damage) -> preventable(gd, c) && gameQueryService.hasAuraWithEffect(
                        gd, c.permanent(), PreventAllDamageToAttachedCreatureEffect.class)
                        && grantedAttachedPreventionSurvives(gd, c.permanent())));
        steps.add(preventAll("EnchantedCombatToAndBy", "Prevent combat damage to the enchanted creature",
                (gd, c, damage) -> c.combatDamage() && preventable(gd, c) && gameQueryService.hasAuraWithEffect(
                        gd, c.permanent(), PreventAllCombatDamageToAndByEnchantedCreatureEffect.class)));
        steps.add(preventAll("AttachedCombat", "Prevent combat damage to the equipped creature",
                (gd, c, damage) -> c.combatDamage() && preventable(gd, c) && gameQueryService.hasAuraWithEffect(
                        gd, c.permanent(), PreventAllCombatDamageToAttachedCreatureEffect.class)));
        steps.add(preventAll("CombatToAndBySelf", "Prevent combat damage to it",
                (gd, c, damage) -> c.combatDamage() && preventable(gd, c) && gameQueryService.hasActiveStaticEffect(
                        gd, c.permanent(), PreventAllCombatDamageToAndBySelfEffect.class)));
        steps.add(preventAll("CombatToSelf", "Prevent combat damage to it",
                (gd, c, damage) -> c.combatDamage() && preventable(gd, c) && gameQueryService.hasActiveStaticEffect(
                        gd, c.permanent(), PreventAllCombatDamageToSelfEffect.class)));
        steps.add(preventAll("Hope", "Hope: prevent damage to the attacking creature",
                (gd, c, damage) -> preventable(gd, c) && c.permanent().isAttacking()
                        && gameQueryService.hasKeyword(gd, c.permanent(), Keyword.HOPE)));
        steps.add(preventAll("AttackingCombatPrevented", "Prevent combat damage to your attacking creatures",
                (gd, c, damage) -> c.combatDamage() && preventable(gd, c) && c.permanent().isAttacking()
                        && hasAttackingCreatureCombatDamagePreventionSource(gd, c.permanent())));
        steps.add(preventAll("CreatureDamagePreventionSource", "Prevent damage to your creatures",
                (gd, c, damage) -> preventable(gd, c)
                        && hasCreatureDamagePreventionSource(gd, c.permanent(), c.combatDamage())));
        steps.add(new CreatureDamageStep("ControlledCreatureDamageLimit", "Limit the damage to it", prevention, true,
                (gd, c, damage) -> preventable(gd, c)
                        && gameQueryService.getControlledCreatureDamageLimit(gd, c.permanent()) < damage,
                (gd, c, damage) -> gameQueryService.getControlledCreatureDamageLimit(gd, c.permanent()),
                ALWAYS));
        steps.add(preventAll("AllCombatToControlledCreature", "Prevent combat damage to your creatures",
                (gd, c, damage) -> c.combatDamage() && preventable(gd, c)
                        && gameQueryService.isAllCombatDamageToControlledCreaturePrevented(gd, c.permanent())));
        steps.add(preventAll("AttachedNoncombat", "Prevent noncombat damage to the enchanted creature",
                (gd, c, damage) -> !c.combatDamage() && preventable(gd, c) && gameQueryService.hasAuraWithEffect(
                        gd, c.permanent(), PreventAllNoncombatDamageToAttachedCreatureEffect.class)));
        steps.add(step("HalfDamage", "Prevent half the damage", prevention, true,
                (gd, c, damage) -> preventable(gd, c) && hasHalfDamagePrevention(gd, controllerOf(gd, c.permanent())),
                (gd, c, damage) -> applyHalfDamagePrevention(gd, controllerOf(gd, c.permanent()), damage)));
        steps.add(step("AttachedPerSourceReduction", "Prevent some of the damage to the equipped creature", prevention, true,
                (gd, c, damage) -> preventable(gd, c)
                        && applyAttachedPerSourceDamageReduction(gd, c.permanent(), damage) < damage,
                (gd, c, damage) -> applyAttachedPerSourceDamageReduction(gd, c.permanent(), damage)));
        steps.add(step("OtherCreatureToCounters", "Prevent the damage and put +1/+1 counters on it", prevention, false,
                (gd, c, damage) -> preventable(gd, c)
                        && hasOtherCreatureDamagePreventionSource(gd, c.permanent(), c.combatDamage()),
                (gd, c, damage) -> {
                    putPlusOneCountersFor(gd, c.permanent(), damage);
                    return 0;
                }));
        steps.add(step("PlusOneCounterShield", "Prevent the damage and put +1/+1 counters on it", prevention, false,
                (gd, c, damage) -> preventable(gd, c) && c.permanent().isAllDamageToPlusOnePlusOneCounterPreventionShield(),
                (gd, c, damage) -> {
                    putShieldPlusOneCounters(gd, c.permanent(), damage);
                    return 0;
                }));
        steps.add(step("Temper", "Prevent damage and put +1/+1 counters on it (Temper)", prevention, false,
                (gd, c, damage) -> preventable(gd, c) && c.permanent().getDamageToPlusOnePlusOneCounterPreventionShield() > 0,
                (gd, c, damage) -> {
                    Permanent permanent = c.permanent();
                    int temperShield = permanent.getDamageToPlusOnePlusOneCounterPreventionShield();
                    int temperPrevented = Math.min(temperShield, damage);
                    permanent.setDamageToPlusOnePlusOneCounterPreventionShield(temperShield - temperPrevented);
                    if (!gameQueryService.cantHaveCounters(gd, permanent)) {
                        int counters = gameQueryService.doublePlusOnePlusOneCounters(gd, permanent, temperPrevented);
                        if (counters > 0) {
                            permanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE,
                                    permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + counters);
                            recordPlusOnePlusOneCounterPlacedOnControlledPermanent(gd, permanent, counters);
                        }
                    }
                    return damage - temperPrevented;
                }));
        steps.add(step("SacredBoon", "Prevent damage and put +0/+1 counters on it later (Sacred Boon)", prevention, false,
                (gd, c, damage) -> preventable(gd, c) && c.permanent().getDamageToCounterPreventionShield() > 0,
                (gd, c, damage) -> {
                    Permanent permanent = c.permanent();
                    int boonPrevented = gd.consumeSacredBoonShields(permanent.getId(), damage);
                    permanent.setDamageToCounterPreventionShield(permanent.getDamageToCounterPreventionShield() - boonPrevented);
                    return damage - boonPrevented;
                }));
        steps.add(step("ControlledPermanentRedirectShield", "Redirect the damage (Divine Deflection)", ReplacementOption.Kind.REDIRECT, false,
                (gd, c, damage) -> preventable(gd, c) && controllerOf(gd, c.permanent()) != null
                        && hasControlledPermanentRedirectShield(gd, controllerOf(gd, c.permanent()), c.permanent().getId()),
                (gd, c, damage) -> applyRedirectShields(gd, controllerOf(gd, c.permanent()), c.permanent().getId(),
                        damage, true, c.combatDamage())));
        steps.add(step("GlobalPreventionShield", "Prevent damage with the shared shield", prevention, false,
                (gd, c, damage) -> preventable(gd, c) && gd.globalDamagePreventionShield > 0,
                (gd, c, damage) -> applyGlobalPreventionShield(gd, damage, c.combatDamage())));
        steps.add(step("PreventionLifeGainShield", "Prevent damage and gain life", prevention, false,
                (gd, c, damage) -> preventable(gd, c) && hasDamagePreventionLifeGainShield(gd, c.permanent().getId()),
                (gd, c, damage) -> applyDamagePreventionLifeGainShield(gd, c.permanent().getId(), damage, c.combatDamage())));
        steps.add(step("PermanentPreventionShield", "Prevent the next damage to it", prevention, false,
                (gd, c, damage) -> preventable(gd, c) && c.permanent().getDamagePreventionShield() > 0,
                (gd, c, damage) -> applyPermanentDamagePreventionShield(gd, c.permanent(), damage, c.combatDamage())));
        return List.copyOf(steps);
    }

    private PreventDamageAndRemovePlusOnePlusOneCountersEffect preventRemovePlusOneCountersEffect(
            GameData gameData, Permanent permanent) {
        return gameQueryService.getActiveStaticEffects(gameData, permanent).stream()
                .filter(PreventDamageAndRemovePlusOnePlusOneCountersEffect.class::isInstance)
                .map(PreventDamageAndRemovePlusOnePlusOneCountersEffect.class::cast)
                .findFirst().orElse(null);
    }

    /**
     * Protean Hydra / Unbreathing Horde / Rock Hydra / Ugin's Conjurant / Magma Pummeler: removes
     * +1/+1 counters for the damage. Counters are removed whether or not damage can be prevented;
     * the prevention itself only happens when it can.
     */
    private int removePlusOneCountersForDamage(GameData gameData, CreatureDamageStep.Context context, int damage) {
        Permanent permanent = context.permanent();
        var effect = preventRemovePlusOneCountersEffect(gameData, permanent);
        int countersToRemove = effect.removeOneOnly()
                ? Math.min(1, permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE))
                : Math.min(damage, permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE));
        if (countersToRemove > 0) {
            permanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE,
                    permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) - countersToRemove);
            if (effect.dealsPreventedDamage()) {
                queuePreventedDamageTrigger(gameData, permanent, countersToRemove, false);
            }
            if (effect.givesEachPlayerRadCounters()) {
                giveEachPlayerRadCounters(gameData, countersToRemove);
            }
        }
        int amount = effect.preventOnlyIfCounterAvailable() ? countersToRemove : damage;
        boolean preventable = gameQueryService.isDamagePreventable(gameData, context.combatDamage());
        CreateTokenEffect tokenTemplate = effect.tokenTemplate();
        if (tokenTemplate != null) {
            UUID controllerId = controllerOf(gameData, permanent);
            if (controllerId != null) {
                permanentControlSupportProvider.getObject().applyCreateToken(
                        gameData, controllerId, tokenTemplate.withAmount(amount), permanent.getCard().getSetCode());
            }
        }
        return preventable ? damage - amount : damage;
    }

    /** Puts the +1/+1 counters a shielded creature gets for damage dealt to it (Gatta and Luzzu). */
    private void putShieldPlusOneCounters(GameData gameData, Permanent permanent, int damage) {
        if (gameQueryService.cantHaveCounters(gameData, permanent)) {
            return;
        }
        int counters = gameQueryService.doublePlusOnePlusOneCounters(gameData, permanent, damage);
        if (counters > 0) {
            permanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE,
                    permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + counters);
            recordPlusOnePlusOneCounterPlacedOnControlledPermanent(gameData, permanent, counters);
        }
    }

    private void putPlusOneCountersFor(GameData gameData, Permanent permanent, int damage) {
        if (gameQueryService.cantHavePlusOnePlusOneCounters(gameData, permanent)) return;
        int counters = gameQueryService.doublePlusOnePlusOneCounters(gameData, permanent, damage);
        if (counters > 0) {
            permanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE,
                    permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + counters);
            recordPlusOnePlusOneCounterPlacedOnControlledPermanent(gameData, permanent, counters);
        }
    }

    private int applySelfDamagePreventionAmount(GameData gameData, Permanent permanent, int damage) {
        if (damage <= 0 || permanent.isDamageCantBePreventedOrRedirectedThisTurn()
                || !gameQueryService.isDamagePreventable(gameData)) {
            return 0;
        }
        return Math.min(damage, selfDamagePrevented(gameData, permanent, damage));
    }

    private boolean hasAttachedEquipment(GameData gameData, Permanent creature) {
        return gameData.anyPermanentMatches(permanent -> creature.getId().equals(permanent.getAttachedTo())
                && GameQueryService.permanentHasSubtype(permanent, CardSubtype.EQUIPMENT));
    }

    private boolean hasControlledPermanentRedirectShield(GameData gameData, UUID controllerId, UUID permanentId) {
        return gameData.damageRedirectShields.stream().anyMatch(shield -> shield.protectedPermanentId() != null
                ? shield.protectedPermanentId().equals(permanentId)
                : shield.protectedPlayerId().equals(controllerId) && shield.coversControlledPermanents());
    }

    /**
     * Outfitted Jouster: "When damage is prevented this way, sacrifice an Equipment attached to Outfitted
     * Jouster." The sacrifice is a reflexive trigger put on the stack after the prevention, and its
     * controller chooses which attached Equipment they control is sacrificed.
     */
    private boolean preventDamageAndSacrificeAttachedEquipment(GameData gameData, Permanent creature) {
        if (!hasAttachedEquipment(gameData, creature)) return false;
        UUID controllerId = gameQueryService.findPermanentController(gameData, creature.getId());
        if (controllerId == null) return false;

        var attachedEquipment = new PermanentAllOfPredicate(List.of(
                new PermanentHasSubtypePredicate(CardSubtype.EQUIPMENT),
                new PermanentAttachedToSourcePermanentPredicate(),
                new PermanentControlledBySourceControllerPredicate()));
        StackEntry trigger = new StackEntry(StackEntryType.TRIGGERED_ABILITY, creature.getCard(), controllerId,
                creature.getCard().getName() + "'s triggered ability",
                new ArrayList<>(List.of(new SacrificePermanentsEffect(1, attachedEquipment,
                        SacrificeRecipient.CONTROLLER))),
                null, creature.getId());
        trigger.setNonTargeting(true);
        gameData.enqueueTrigger(trigger);
        gameLogService.append(gameData, GameLog.cardThen(creature.getCard(), "'s ability triggers."));
        return true;
    }

    public boolean consumeShieldCounter(GameData gameData, Permanent permanent) {
        int shields = permanent.getCounterCount(CounterType.SHIELD);
        if (shields <= 0) {
            return false;
        }
        permanent.setCounterCount(CounterType.SHIELD, shields - 1);
        triggerCollectionService.checkLoyaltyCounterRemovalTriggers(gameData);
        gameLogService.append(gameData, GameLog.cardThen(permanent.getCard(), " loses a shield counter."));
        log.info("Game {} - {} loses a shield counter", gameData.id, permanent.getCard().getName());
        return true;
    }

    public int applySelfDamagePreventionShield(GameData gameData, Permanent permanent, int damage) {
        if (damage <= 0 || permanent.isDamageCantBePreventedOrRedirectedThisTurn()
                || !gameQueryService.isDamagePreventable(gameData)) {
            return damage;
        }
        int prevented = selfDamagePrevented(gameData, permanent, damage);
        if (prevented > 0 && hasControlChangeOnPrevention(gameData, permanent)) {
            makeOpponentGainControl(gameData, permanent);
        }
        return damage - Math.min(damage, prevented);
    }

    private int selfDamagePrevented(GameData gameData, Permanent permanent, int damage) {
        int prevented = gameQueryService.hasLostPrintedAbilities(gameData, permanent) ? 0
                : permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                .filter(SelfDamagePreventionEffect.class::isInstance)
                .map(SelfDamagePreventionEffect.class::cast)
                .mapToInt(effect -> effect.preventedDamage(damage))
                .sum();
        return prevented + gameQueryService.getGrantedEffects(gameData, permanent).stream()
                .filter(SelfDamagePreventionEffect.class::isInstance)
                .map(SelfDamagePreventionEffect.class::cast)
                .mapToInt(effect -> effect.preventedDamage(damage))
                .sum();
    }

    private boolean hasControlChangeOnPrevention(GameData gameData, Permanent permanent) {
        return permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(DamagePreventionControlChangeEffect.class::isInstance)
                || gameQueryService.getGrantedEffects(gameData, permanent).stream()
                .anyMatch(DamagePreventionControlChangeEffect.class::isInstance);
    }

    private void makeOpponentGainControl(GameData gameData, Permanent permanent) {
        UUID currentControllerId = gameQueryService.findPermanentController(gameData, permanent.getId());
        if (currentControllerId == null) return;

        List<UUID> opponents = gameData.orderedPlayerIds.stream()
                .filter(playerId -> !playerId.equals(currentControllerId))
                .toList();
        if (opponents.isEmpty()) return;

        if (opponents.size() == 1) {
            creatureControlServiceProvider.getObject().applyControlEffect(
                    gameData, opponents.getFirst(), permanent,
                    new GainControlOfTargetEffect(ControlDuration.PERMANENT),
                    EffectDuration.PERMANENT, null, permanent.getCard().getName());
            return;
        }

        gameData.interaction.setPermanentChoiceContext(
                new PermanentChoiceContext.ChooseOpponentGainsControlOfSource(
                        permanent.getId(), permanent.getCard().getName()));
        playerInputServiceProvider.getObject().beginAnyTargetChoice(
                gameData, currentControllerId, List.of(), opponents,
                permanent.getCard().getName() + " — choose an opponent.");
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
        if (damage <= 0 || damageSource == null || !gameQueryService.isCreature(gameData, damageSource)) {
            return damage;
        }

        if (isCombatDamage && hasActiveCombatDamageToSelfAndAddPlusOneCounterEffect(
                gameData, permanent, damageSource)) {
            return applyCombatDamageToSelfAndAddPlusOneCounter(gameData, permanent, damageSource, damage);
        }

        if (!gameQueryService.isDamagePreventable(gameData, isCombatDamage)) {
            return damage;
        }

        if (gameQueryService.isDamageFromPermanentSourceToCreaturePrevented(
                gameData, damageSource, permanent, isCombatDamage)) {
            return 0;
        }

        if (gameQueryService.isDamageFromDesertsToSelfPrevented(
                gameData, permanent, null, damageSource, isCombatDamage)) {
            return 0;
        }
        if (gameQueryService.isDamageFromDesertsToCamelOrBandedCreaturePrevented(
                gameData, permanent, null, damageSource, isCombatDamage)) {
            return 0;
        }

        if (gameQueryService.isDamageFromMatchingSourcePreventedForControlledCreature(
                gameData, permanent, damageSource)) {
            return 0;
        }

        UUID sourceControllerId = gameQueryService.findPermanentController(gameData, damageSource.getId());
        if (gameQueryService.isDamageFromControlledSourceToControlledCreaturePrevented(
                gameData, permanent, sourceControllerId, isCombatDamage)) {
            return 0;
        }

        UUID controllerId = gameQueryService.findPermanentController(gameData, permanent.getId());
        int prevented = 0;
        if (controllerId != null) {
            prevented = evaluatePerSourceControllerAndCreaturesDamagePrevention(
                    gameData, controllerId, damage, isCombatDamage, damageSource, permanent);
        }
        return damage - Math.min(damage, prevented);
    }

    /** Applies Ironscale Hydra's one-counter-per-creature combat damage replacement. */
    public int applyCombatDamageToSelfAndAddPlusOneCounter(GameData gameData, Permanent permanent,
                                                            Permanent damageSource, int damage) {
        if (damage <= 0 || !hasActiveCombatDamageToSelfAndAddPlusOneCounterEffect(
                gameData, permanent, damageSource)) {
            return damage;
        }

        if (!gameQueryService.cantHavePlusOnePlusOneCounters(gameData, permanent)) {
            int counters = gameQueryService.doublePlusOnePlusOneCounters(gameData, permanent, 1);
            if (counters > 0) {
                permanent.setCounterCount(CounterType.PLUS_ONE_PLUS_ONE,
                        permanent.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE) + counters);
                recordPlusOnePlusOneCounterPlacedOnControlledPermanent(gameData, permanent, counters);
            }
        }

        if (gameQueryService.isDamagePreventable(gameData, true)
                && !permanent.isDamageCantBePreventedOrRedirectedThisTurn()
                && !gameQueryService.damageCantBePreventedFromSource(gameData, damageSource, true)) {
            return 0;
        }
        return damage;
    }

    private boolean hasActiveCombatDamageToSelfAndAddPlusOneCounterEffect(
            GameData gameData, Permanent permanent, Permanent damageSource) {
        return permanent != null
                && damageSource != null
                && gameQueryService.isCreature(gameData, permanent)
                && gameQueryService.isCreature(gameData, damageSource)
                && gameQueryService.hasActiveStaticEffectIncludingGranted(
                gameData, permanent, PreventCombatDamageToSelfAndAddPlusOneCounterEffect.class);
    }

    public int applyPermanentDamagePreventionShield(GameData gameData, Permanent permanent, int damage) {
        return applyPermanentDamagePreventionShield(gameData, permanent, damage, false);
    }

    public int applyPermanentDamagePreventionShield(GameData gameData, Permanent permanent, int damage,
                                                     boolean combatDamage) {
        if (!gameQueryService.isDamagePreventable(gameData, combatDamage) || damage <= 0) return damage;

        int shield = permanent.getDamagePreventionShield();
        if (shield <= 0) return damage;

        int prevented = Math.min(shield, damage);
        permanent.setDamagePreventionShield(shield - prevented);
        return damage - prevented;
    }

    private void queuePhyrexianVindicatorTrigger(GameData gameData, Permanent permanent, int preventedDamage) {
        queuePreventedDamageTrigger(gameData, permanent, preventedDamage, true);
    }

    private void queuePreventedDamageTrigger(GameData gameData, Permanent permanent, int damageAmount,
                                             boolean excludeSource) {
        UUID controllerId = gameQueryService.findPermanentController(gameData, permanent.getId());
        if (controllerId == null || damageAmount <= 0) return;

        var targetFilter = excludeSource
                ? new AnyTargetPredicateTargetFilter(
                new PermanentNotPredicate(new PermanentIsSourcePermanentPredicate()),
                new PlayerRelationPredicate(PlayerRelation.ANY),
                "another target")
                : null;
        gameData.queueInteraction(new PermanentChoiceContext.SpellTargetTriggerAnyTarget(
                permanent.getCard(), controllerId,
                List.of(new DealDamageToAnyTargetEffect(new XValue())),
                false, targetFilter, damageAmount, permanent.getId(), new Permanent(permanent)));
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
    public int applyHalfDamagePrevention(GameData gameData, UUID recipientId, int damage) {
        if (recipientId == null || damage <= 0) return damage;
        List<Permanent> battlefield = gameData.playerBattlefields.get(recipientId);
        if (battlefield == null) return damage;
        for (Permanent p : battlefield) {
            if (gameQueryService.hasLostPrintedAbilities(gameData, p)) continue;
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
            GameData gameData, UUID controllerId, int damage, boolean combatDamage, Permanent damageSource,
            Permanent protectedCreature) {
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
                } else if (resolved instanceof FilteredCreaturesDamagePreventionEffect prevention
                        && protectedCreature != null
                        && (!combatDamage || damageSource != null)
                        && (prevention.filter() == null || predicateEvaluationService.matchesPermanentPredicate(
                        protectedCreature,
                        prevention.filter(),
                        FilterContext.of(gameData)
                                .withSourceCardId(source.getCard().getId())
                                .withSourceControllerId(controllerId)
                                .withSourcePermanentSnapshot(source)
                                .withSourcePermanentId(source.getId())))) {
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

    private int sharedCombatPreventionAmount(GameData gameData, UUID damageSourceId,
                                              UUID preventionSourceId, UUID recipientId,
                                              int defaultAmount, boolean combatDamage) {
        if (!combatDamage || damageSourceId == null) return defaultAmount;
        var sourceAllocations = gameData.combatSharedDamagePreventionAllocations.get(damageSourceId);
        if (sourceAllocations == null || !sourceAllocations.containsKey(preventionSourceId)) return defaultAmount;
        return sourceAllocations.get(preventionSourceId).getOrDefault(recipientId, 0);
    }

    private int evaluatePerSourceControllerAndCreaturesDamagePrevention(
            GameData gameData, UUID controllerId, int damage, boolean combatDamage, Permanent damageSource,
            Permanent protectedCreature) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return 0;

        int reduction = 0;
        for (Permanent source : battlefield) {
            for (CardEffect effect : source.getCard().getEffects(EffectSlot.STATIC)) {
                CardEffect resolved = staticEffectConditionResolver.resolve(
                        gameData, source, controllerId, effect);
                if (resolved instanceof ControllerAndCreaturesDamagePreventionEffect prevention) {
                    reduction += prevention.amount();
                } else if (resolved instanceof FilteredCreaturesDamagePreventionEffect prevention
                        && protectedCreature != null
                        && (prevention.filter() == null || predicateEvaluationService.matchesPermanentPredicate(
                        protectedCreature,
                        prevention.filter(),
                        FilterContext.of(gameData)
                                .withSourceCardId(source.getCard().getId())
                                .withSourceControllerId(controllerId)
                                .withSourcePermanentSnapshot(source)
                                .withSourcePermanentId(source.getId())))) {
                    reduction += prevention.amount();
                } else if (resolved instanceof PreventXDamagePerSourceToControllerAndCreaturesEffect prevention
                        && (!prevention.combatOnly() || combatDamage)
                        && (!prevention.creatureSourcesOnly() || gameQueryService.isCreature(gameData, damageSource))) {
                    int amount = amountEvaluationService.evaluate(gameData, prevention.amount(),
                            AmountContext.forStaticEffect(source, controllerId));
                    UUID recipientId = protectedCreature == null ? controllerId : protectedCreature.getId();
                    reduction += sharedCombatPreventionAmount(gameData, damageSource.getId(), source.getId(),
                            recipientId, amount, combatDamage);
                }
            }
        }
        return Math.min(damage, reduction);
    }

    /**
     * Prevents noncombat damage to a permanent controlled by a protected player. The protecting permanent
     * itself is not covered by its own "other permanents you control" wording (The Wanderer, WAR 37).
     */
    public int applyControllerAndPermanentsNoncombatDamagePrevention(
            GameData gameData, Permanent permanent, int damage) {
        if (!gameQueryService.isDamagePreventable(gameData) || damage <= 0) return damage;
        UUID controllerId = gameQueryService.findPermanentController(gameData, permanent.getId());
        return controllerId != null
                && hasControllerAndPermanentsNoncombatDamagePrevention(gameData, controllerId, permanent.getId())
                ? 0
                : damage;
    }

    public boolean hasControllerAndPermanentsNoncombatDamagePrevention(GameData gameData, UUID playerId) {
        return hasControllerAndPermanentsNoncombatDamagePrevention(gameData, playerId, null);
    }

    private boolean hasControllerAndPermanentsNoncombatDamagePrevention(GameData gameData, UUID playerId,
                                                                        UUID excludedPermanentId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        return battlefield != null && battlefield.stream()
                .filter(permanent -> !permanent.getId().equals(excludedPermanentId))
                .anyMatch(permanent -> gameQueryService.hasActiveStaticEffect(
                        gameData, permanent, ControllerAndPermanentsNoncombatDamagePreventionEffect.class));
    }

    /**
     * Vigor-style protection: returns true when the given creature's controller controls some other
     * permanent (i.e. not the creature itself — "another creature you control") carrying
     * {@link PreventDamageToOtherCreaturesAndAddPlusCountersEffect}. Such damage is fully prevented and
     * replaced with +1/+1 counters by the caller.
     */
    private boolean hasOtherCreatureDamagePreventionSource(GameData gameData, Permanent creature,
                                                            boolean isCombatDamage) {
        UUID controllerId = gameQueryService.findPermanentController(gameData, creature.getId());
        if (controllerId == null) return false;
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return false;
        return battlefield.stream()
                .anyMatch(source -> gameQueryService.getActiveStaticEffects(gameData, source).stream()
                        .filter(PreventDamageToOtherCreaturesAndAddPlusCountersEffect.class::isInstance)
                        .map(PreventDamageToOtherCreaturesAndAddPlusCountersEffect.class::cast)
                        .anyMatch(effect -> (!isCombatDamage || !effect.noncombatOnly())
                                && (effect.sourceOnly()
                                ? source.getId().equals(creature.getId())
                                : effect.includeSource() || !source.getId().equals(creature.getId()))));
    }

    /**
     * Dolmen Gate-style protection: returns true when the given attacking creature's controller controls
     * a permanent carrying {@link PreventCombatDamageToAttackingCreaturesYouControlEffect} whose optional
     * filter matches the creature. Combat damage dealt to such a creature is fully prevented by the caller.
     */
    private boolean hasAttackingCreatureCombatDamagePreventionSource(GameData gameData, Permanent creature) {
        UUID controllerId = gameQueryService.findPermanentController(gameData, creature.getId());
        if (controllerId == null) return false;
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        if (battlefield == null) return false;
        return battlefield.stream()
                .anyMatch(source -> gameQueryService.getActiveStaticEffects(gameData, source).stream()
                        .filter(PreventCombatDamageToAttackingCreaturesYouControlEffect.class::isInstance)
                        .map(PreventCombatDamageToAttackingCreaturesYouControlEffect.class::cast)
                        .anyMatch(effect -> effect.filter() == null
                                || predicateEvaluationService.matchesPermanentPredicate(
                                gameData,
                                creature,
                                effect.filter())));
    }

    /**
     * Mark of Asylum / Inner Sanctum / Bubble Matrix-style protection: returns true when some permanent
     * on the battlefield carries a {@link PreventDamageToCreaturesEffect} covering this damage to the
     * given creature. Mark of Asylum's variant ({@code noncombatOnly}) leaves combat damage untouched;
     * Inner Sanctum's covers both but only for creatures its controller controls, while Bubble Matrix's
     * ({@code allCreatures}) covers every creature regardless of controller. An optional permanent
     * filter further narrows the protected creatures. Damage matched here is fully prevented by the
     * caller.
     */
    private boolean hasCreatureDamagePreventionSource(GameData gameData, Permanent creature, boolean isCombatDamage) {
        UUID controllerId = gameQueryService.findPermanentController(gameData, creature.getId());
        if (controllerId == null) return false;
        return gameData.playerBattlefields.entrySet().stream()
                .anyMatch(entry -> entry.getValue().stream()
                        .anyMatch(source -> gameQueryService.getActiveStaticEffects(gameData, source).stream()
                                .anyMatch(e -> e instanceof PreventDamageToCreaturesEffect prevent
                                        && !(isCombatDamage && prevent.noncombatOnly())
                                        && (prevent.allCreatures() || controllerId.equals(entry.getKey()))
                                        && (!prevent.excludeSource()
                                        || !source.getId().equals(creature.getId()))
                                        && (prevent.filter() == null
                                        || predicateEvaluationService.matchesPermanentPredicate(
                                        creature,
                                        prevent.filter(),
                                        FilterContext.of(gameData)
                                                .withSourceCardId(source.getCard().getId())
                                                .withSourceControllerId(entry.getKey())
                                                .withSourcePermanentSnapshot(source)
                                                .withSourcePermanentId(source.getId()))))));
    }

    private void giveEachPlayerRadCounters(GameData gameData, int amount) {
        for (UUID playerId : gameData.orderedPlayerIds) {
            gameData.playerRadCounters.merge(playerId, amount, Integer::sum);
            String playerName = gameData.playerIdToName.getOrDefault(playerId, "Player");
            gameLogService.append(gameData,
                    GameLog.text(playerName + " gets " + amount + " rad counter" + (amount == 1 ? "." : "s.")));
        }
    }

    /**
     * Deep Wood: whether combat damage dealt to the given player by attacking creatures is prevented this
     * turn. Combat damage to a defending player always originates from attacking creatures, so this needs
     * only the player flag.
     */
    public boolean isCombatDamageFromAttackersPreventedForPlayer(GameData gameData, UUID playerId) {
        if (!gameQueryService.isDamagePreventable(gameData, true)) return false;
        return gameData.playersWithDamageFromAttackersPrevented.contains(playerId);
    }

    /**
     * Deep Wood: whether noncombat damage dealt to the given player is prevented this turn because its
     * source permanent is currently an attacking creature.
     */
    public boolean isNoncombatDamageFromAttackerPreventedForPlayer(GameData gameData, UUID playerId, UUID sourcePermanentId) {
        if (!gameQueryService.isDamagePreventable(gameData)
                || !gameData.playersWithDamageFromAttackersPrevented.contains(playerId)) return false;
        if (sourcePermanentId == null) return false;
        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        return source != null && source.isAttacking();
    }

    public int applyChannelHarmPrevention(GameData gameData, UUID protectedPlayerId,
                                          UUID sourceControllerId, int damage) {
        return applyChannelHarmPrevention(gameData, protectedPlayerId, sourceControllerId, damage, true);
    }

    public int applyChannelHarmPreventionToPermanent(GameData gameData, Permanent target,
                                                     UUID sourceControllerId, int damage) {
        UUID protectedPlayerId = gameQueryService.findPermanentController(gameData, target.getId());
        return applyChannelHarmPrevention(gameData, protectedPlayerId, sourceControllerId, damage, true);
    }

    private int applyChannelHarmPrevention(GameData gameData, UUID protectedPlayerId,
                                           UUID sourceControllerId, int damage, boolean damagePreventable) {
        if (damage <= 0 || !damagePreventable || !gameQueryService.isDamagePreventable(gameData)
                || protectedPlayerId == null || sourceControllerId == null
                || protectedPlayerId.equals(sourceControllerId)) {
            return damage;
        }

        for (ChannelHarmShield shield : gameData.channelHarmShields) {
            if (!protectedPlayerId.equals(shield.protectedPlayerId())) {
                continue;
            }
            gameData.pendingMayAbilities.add(new PendingMayAbility(
                    shield.sourceCard(), protectedPlayerId,
                    List.of(new com.github.laxika.magicalvibes.model.effect.ChannelHarmEffect(damage)),
                    "Have " + shield.sourceCard().getName() + " deal " + damage
                            + " damage to the target creature?",
                    shield.targetCreatureId()));
            return 0;
        }
        return damage;
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
        if (!gameQueryService.isDamagePreventable(gameData, combatDamage)) return damage;
        if (combatDamage && damageSource != null) {
            UUID sourceControllerId = gameQueryService.findPermanentController(gameData, damageSource.getId());
            damage = applyJudgmentOfAlexanderPrevention(gameData, playerId, damage,
                    damageSource.getCard(), damageSource, sourceControllerId, true);
            if (damage <= 0) return 0;
        }
        if (combatDamage) {
            CombatDamagePreventionTokenShield tokenShield =
                    gameData.combatDamagePreventionTokenShields.get(playerId);
            if (tokenShield != null) {
                notifyControllerDamagePrevented(gameData, playerId, damage);
                permanentControlSupportProvider.getObject().applyCreateToken(
                        gameData, playerId, tokenShield.token(), damage, tokenShield.sourceSetCode());
                return 0;
            }
        }
        if (combatDamage && gameData.preventAllCombatDamageToPlayers) {
            notifyControllerDamagePrevented(gameData, playerId, damage);
            return 0;
        }
        if (combatDamage
                && gameData.playersWithAllCombatDamageToPlayerAndPlaneswalkersPrevented.contains(playerId)) {
            notifyControllerDamagePrevented(gameData, playerId, damage);
            return 0;
        }
        if (gameData.playersWithAllDamagePrevented.contains(playerId)) {
            notifyControllerDamagePrevented(gameData, playerId, damage);
            return 0;
        }
        // Riot Control: prevent all damage that would be dealt to the caster this turn (their creatures are unaffected)
        if (gameData.playersWithAllPlayerDamagePrevented.contains(playerId)) {
            notifyControllerDamagePrevented(gameData, playerId, damage);
            return 0;
        }
        // Morningtide's Light: prevent all damage that would be dealt to the caster until their next turn.
        if (gameData.playersWithAllPlayerDamagePreventedUntilNextTurn.contains(playerId)) {
            notifyControllerDamagePrevented(gameData, playerId, damage);
            return 0;
        }
        // Gisela, Blade of Goldnight: prevent half the damage dealt to her controller, rounded up.
        damage = applyHalfDamagePrevention(gameData, playerId, damage);
        if (damage <= 0) return 0;
        damage -= applyControllerAndCreaturesFixedPerSourceDamagePrevention(
                gameData, playerId, damage, combatDamage, damageSource, null);
        if (damage <= 0) return 0;
        // Process redirect shields first (e.g. Vengeful Archon)
        damage = applyRedirectShields(gameData, playerId, null, damage, false, combatDamage);
        damage = applyGlobalPreventionShield(gameData, damage, combatDamage);
        damage = applyDamagePreventionLifeGainShield(gameData, playerId, damage, combatDamage);
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
                notifyControllerDamagePrevented(gameData, playerId, prevented);
            }
        }
        if (damage <= 0) return 0;
        damage = applyPlayerDamagePreventionShield(gameData, playerId, damage);
        if (damage <= 0) return 0;
        if (!combatDamage && hasControllerAndPermanentsNoncombatDamagePrevention(gameData, playerId)) {
            notifyControllerDamagePrevented(gameData, playerId, damage);
            return 0;
        }
        return damage;
    }

    public void notifyControllerDamagePrevented(GameData gameData, UUID playerId, int amount) {
        if (amount > 0) {
            triggerCollectionService.checkControllerDamagePreventedTriggers(gameData, playerId, amount);
        }
    }

    /** Applies target-specific shields that gain life for their resolving controller. */
    public int applyDamagePreventionLifeGainShield(GameData gameData, UUID targetId, int damage) {
        return applyDamagePreventionLifeGainShield(gameData, targetId, damage, false);
    }

    public int applyDamagePreventionLifeGainShield(GameData gameData, UUID targetId, int damage,
                                                    boolean combatDamage) {
        if (!gameQueryService.isDamagePreventable(gameData, combatDamage)
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

    /** Applies exactly one rider after the affected player selects overlapping shields. */
    public void applyAllByCreaturesPreventionLifeGain(GameData gameData, int preventedDamage,
                                                      UUID affectedPlayerId) {
        if (preventedDamage <= 0 || gameData.damageByCreaturesPreventionLifeGainPlayers.isEmpty()) return;
        List<UUID> controllers = gameData.damageByCreaturesPreventionLifeGainPlayers.stream().distinct().toList();
        if (controllers.size() == 1) {
            lifeSupport.applyGainLife(gameData, controllers.getFirst(), preventedDamage, "prevented damage");
            return;
        }
        var choice = new com.github.laxika.magicalvibes.model.PendingInteraction.ColorChoice(
                affectedPlayerId, null, null,
                new com.github.laxika.magicalvibes.model.ChoiceContext.CreaturePreventionLifeGainChoice(
                        preventedDamage, controllers),
                controllers.stream().map(UUID::toString).toList(),
                "Choose whose creature-damage prevention effect applies.");
        if (gameData.interaction.isAwaitingInput()) {
            gameData.queueInteraction(choice);
        } else {
            interactionHandlerRegistry.begin(gameData, choice);
        }
    }

    /**
     * Consumes damage redirect shields for the given player. Prevented damage is tracked
     * in {@link GameData#pendingRedirectDamage} for the caller to deal after damage processing.
     * Returns the remaining damage after redirect shield prevention.
     */
    private int applyRedirectShields(GameData gameData, UUID playerId, int damage) {
        return applyRedirectShields(gameData, playerId, null, damage, false, false);
    }

    /**
     * @param forControlledPermanent when {@code true} the damage is being dealt to a permanent the
     *                               player controls, so only shields that cover their permanents
     *                               (Divine Deflection) apply
     */
    private int applyRedirectShields(GameData gameData, UUID playerId, UUID permanentId, int damage,
                                     boolean forControlledPermanent) {
        return applyRedirectShields(gameData, playerId, permanentId, damage, forControlledPermanent, false);
    }

    private int applyRedirectShields(GameData gameData, UUID playerId, UUID permanentId, int damage,
                                     boolean forControlledPermanent, boolean combatDamage) {
        if (!gameQueryService.isDamagePreventable(gameData, combatDamage)) return damage;
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
        return isSourceDamagePreventedForPlayer(gameData, playerId, sourcePermanentId, false);
    }

    public boolean isSourceDamagePreventedForPlayer(GameData gameData, UUID playerId, UUID sourcePermanentId,
                                                     boolean combatDamage) {
        return isSourceDamagePreventedForPlayer(gameData, playerId, sourcePermanentId, combatDamage, null);
    }

    /**
     * Whole-turn chosen-source prevention. A shield that required the source to share a color with mana
     * spent on its activation rechecks the source's current colors (CR 609.7b); {@code null} colors skip
     * the recheck.
     */
    public boolean isSourceDamagePreventedForPlayer(GameData gameData, UUID playerId, UUID sourcePermanentId,
                                                     boolean combatDamage, Set<CardColor> sourceColors) {
        if (!gameQueryService.isDamagePreventable(gameData, combatDamage)) return false;
        if (sourcePermanentId == null) return false;
        Set<UUID> preventedSources = gameData.playerSourceDamagePreventionIds.get(playerId);
        if (preventedSources == null || !preventedSources.contains(sourcePermanentId)) return false;
        Map<UUID, Set<CardColor>> requiredBySource = gameData.playerSourceDamagePreventionRequiredColors.get(playerId);
        Set<CardColor> required = requiredBySource == null ? null : requiredBySource.get(sourcePermanentId);
        return sourceColors == null || required == null || required.isEmpty()
                || sourceColors.stream().anyMatch(required::contains);
    }

    /** Applies whole-turn chosen-source prevention to player damage and its optional color rider. */
    public int applySourceDamagePreventionForPlayer(GameData gameData, UUID playerId, UUID sourcePermanentId,
                                                    int damage, Set<CardColor> sourceColors) {
        return applySourceDamagePreventionForPlayer(gameData, playerId, sourcePermanentId, damage, sourceColors, false);
    }

    public int applySourceDamagePreventionForPlayer(GameData gameData, UUID playerId, UUID sourcePermanentId,
                                                    int damage, Set<CardColor> sourceColors, boolean combatDamage) {
        if (!isSourceDamagePreventedForPlayer(gameData, playerId, sourcePermanentId, combatDamage, sourceColors)) {
            return damage;
        }

        Set<UUID> lifeGainSources = gameData.playerSourceDamagePreventionLifeGainIds.get(playerId);
        if (damage > 0 && lifeGainSources != null && lifeGainSources.contains(sourcePermanentId)
                && sourceColors != null
                && (sourceColors.contains(CardColor.BLACK) || sourceColors.contains(CardColor.RED))) {
            queuePreventedDamageLifeGainTrigger(gameData, playerId, sourcePermanentId, damage);
        }
        return 0;
    }

    /**
     * Samite Ministration's "Whenever damage from a black or red source is prevented this way this
     * turn, you gain that much life": a triggered ability that goes on the stack after the damage
     * has been prevented. The ability is represented by the damage source's card; if that source can
     * no longer be found the life is gained immediately.
     */
    private void queuePreventedDamageLifeGainTrigger(GameData gameData, UUID playerId, UUID sourceId, int damage) {
        Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, sourceId);
        Card card = sourcePermanent != null ? sourcePermanent.getCard() : gameData.stack.stream()
                .map(StackEntry::getCard)
                .filter(candidate -> candidate != null && sourceId.equals(candidate.getId()))
                .findFirst().orElse(null);
        if (card == null) {
            lifeSupport.applyGainLife(gameData, playerId, damage, "prevented damage");
            return;
        }
        gameData.enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                card,
                playerId,
                card.getName() + "'s triggered ability",
                List.of(new GainLifeEffect(new Fixed(damage))),
                (UUID) null,
                (UUID) null));
    }

    /**
     * Applies one-shot chosen-source shields to player damage. A matching shield is consumed by the
     * next damage event; Dark Sphere shields leave half that event, rounded down, while ordinary
     * shields prevent the whole event. Returns the remaining damage.
     */
    public int applyPlayerNextSourceDamageShield(GameData gameData, UUID playerId, UUID sourcePermanentId, int damage) {
        return applyPlayerNextSourceDamageShield(gameData, playerId, sourcePermanentId, damage, false);
    }

    public int applyPlayerNextSourceDamageShield(GameData gameData, UUID playerId, UUID sourcePermanentId,
                                                  int damage, boolean combatDamage) {
        return applyPlayerNextSourceDamageShield(
                gameData, playerId, sourcePermanentId, damage, combatDamage, null);
    }

    public int applyPlayerNextSourceDamageShield(GameData gameData, UUID playerId, UUID sourcePermanentId,
                                                  int damage, boolean combatDamage, Card sourceCard) {
        return applyPlayerNextSourceDamageShield(
                gameData, playerId, sourcePermanentId, damage, combatDamage, sourceCard, false);
    }

    public int applyPlayerNextSourceDamageShield(GameData gameData, UUID playerId, UUID sourcePermanentId,
                                                  int damage, boolean combatDamage, Card sourceCard,
                                                  boolean sourceUnblocked) {
        if (!gameQueryService.isDamagePreventable(gameData, combatDamage)) return damage;
        if (damage <= 0 || playerId == null || sourcePermanentId == null
                || gameData.playerSourceNextDamageShields.isEmpty()) {
            return damage;
        }
        var it = gameData.playerSourceNextDamageShields.iterator();
        int remaining = damage;
        while (it.hasNext()) {
            var shield = it.next();
            if (shield.playerId().equals(playerId)
                    && shieldMatchesSource(gameData, shield, sourcePermanentId, sourceCard)) {
                if (shield.combatOnly() && !combatDamage) {
                    continue;
                }
                if (shield.unblockedOnly() && !sourceUnblocked) {
                    continue;
                }
                it.remove();
                int prevented = shield.preventAllButOne()
                        ? Math.max(0, remaining - 1)
                        : shield.preventHalfDamage() ? remaining / 2 : remaining;
                applyNextSourceShieldRiders(gameData, shield, prevented, sourceCard);
                remaining -= prevented;
                if (remaining == 0) {
                    return 0;
                }
            }
        }
        return remaining;
    }

    private boolean shieldMatchesSource(GameData gameData, PlayerSourceNextDamageShield shield,
                                        UUID sourceId, Card sourceCard) {
        if (shield.requiredSourceFilter() != null) {
            Permanent source = gameQueryService.findPermanentById(gameData, sourceId);
            if (!gameQueryService.matchesDamageSourcePredicate(gameData, null, sourceCard, source,
                    shield.requiredSourceFilter())) {
                return false;
            }
        }
        if (shield.requiredDamageColors() != null) {
            Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, sourceId);
            Set<CardColor> sourceColors = sourcePermanent != null
                    ? gameQueryService.getEffectiveColors(gameData, sourcePermanent)
                    : sourceCard != null
                            ? gameQueryService.getEffectiveCardColors(gameData, sourceCard)
                            : Set.of();
            Set<CardColor> damageColors = gameQueryService.getDamageSourceColors(gameData, sourceColors);
            if (damageColors.stream().noneMatch(shield.requiredDamageColors()::contains)) {
                return false;
            }
        }
        if (shield.sourceId().equals(sourceId)) {
            return true;
        }
        Card effectiveSourceCard = sourceCard;
        if (effectiveSourceCard == null) {
            Permanent sourcePermanent = gameQueryService.findPermanentById(gameData, sourceId);
            effectiveSourceCard = sourcePermanent == null ? null : sourcePermanent.getCard();
        }
        return effectiveSourceCard != null && shield.sourceId().equals(effectiveSourceCard.getId());
    }

    /**
     * Shadowbane's creature half: if a one-shot shield covering the creature's controller's
     * permanents matches this (creature controller, source), the entire next damage event to that
     * creature is prevented and the shield is consumed. Returns the remaining damage.
     */
    public int applyControllerCreaturesNextSourceDamageShield(GameData gameData, UUID creatureControllerId,
                                                              UUID sourcePermanentId, int damage) {
        return applyControllerCreaturesNextSourceDamageShield(
                gameData, creatureControllerId, sourcePermanentId, damage, false);
    }

    public int applyControllerCreaturesNextSourceDamageShield(GameData gameData, UUID creatureControllerId,
                                                              UUID sourcePermanentId, int damage,
                                                              boolean combatDamage) {
        if (!gameQueryService.isDamagePreventable(gameData, combatDamage)) return damage;
        if (damage <= 0 || creatureControllerId == null || sourcePermanentId == null
                || gameData.playerSourceNextDamageShields.isEmpty()
                && gameData.playerSourceShieldsUsedInOpenEvent.isEmpty()) {
            return damage;
        }
        for (var shield : List.copyOf(gameData.playerSourceShieldsUsedInOpenEvent)) {
            if (shield.coversControlledCreatures()
                    && shield.playerId().equals(creatureControllerId)
                    && shieldMatchesSource(gameData, shield, sourcePermanentId, null)) {
                applyNextSourceShieldRiders(gameData, shield, damage, null);
                return 0;
            }
        }
        var it = gameData.playerSourceNextDamageShields.iterator();
        while (it.hasNext()) {
            var shield = it.next();
            if (shield.coversControlledCreatures()
                    && shield.playerId().equals(creatureControllerId)
                    && shieldMatchesSource(gameData, shield, sourcePermanentId, null)) {
                it.remove();
                // One damage event can hit several creatures at once; the shield covers all of them.
                if (!gameData.pendingSimultaneousDamage.isEmpty() || gameData.openDamageEvent != null
                        || gameData.effectResolutionDepth > 0) {
                    gameData.playerSourceShieldsUsedInOpenEvent.add(shield);
                }
                applyNextSourceShieldRiders(gameData, shield, damage, null);
                return 0;
            }
        }
        return damage;
    }

    /** Applies whatever "… prevented this way" rider the consumed shield carries. */
    private void applyNextSourceShieldRiders(GameData gameData, PlayerSourceNextDamageShield shield,
                                              int damage, Card sourceCard) {
        gainLifeForNextSourceShield(gameData, shield, damage, sourceCard);
        exileFromLibraryForNextSourceShield(gameData, shield, damage);
        if (shield.drawCards() && shield.damageSourceControllerCard() != null) {
            queuePreventedDamageReflexiveTrigger(gameData, shield, damage);
            return;
        }
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
     * New Way Forward's "When damage is prevented this way, this spell deals that much damage to
     * that source's controller and you draw that many cards": a reflexive triggered ability
     * (CR 603.12) that goes on the stack after the damage has been prevented.
     */
    private void queuePreventedDamageReflexiveTrigger(GameData gameData, PlayerSourceNextDamageShield shield,
                                                      int damage) {
        UUID sourceControllerId = gameQueryService.findPermanentController(gameData, shield.sourceId());
        if (sourceControllerId == null) {
            sourceControllerId = shield.sourceControllerId();
        }
        if (sourceControllerId == null) {
            return;
        }
        Card card = shield.damageSourceControllerCard();
        gameData.enqueueTrigger(new StackEntry(
                StackEntryType.TRIGGERED_ABILITY,
                card,
                shield.playerId(),
                card.getName() + "'s triggered ability",
                List.of(new DealDamageToAnyTargetEffect(new Fixed(damage), false, false, -1, null, false, false, false),
                        new DrawCardEffect(new Fixed(damage))),
                sourceControllerId,
                (UUID) null));
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
    private void gainLifeForNextSourceShield(GameData gameData, PlayerSourceNextDamageShield shield,
                                              int damage, Card sourceCard) {
        if (!shield.gainLife()) return;
        if (shield.gainLifeOnlyFromBlackSource()) {
            Permanent source = gameQueryService.findPermanentById(gameData, shield.sourceId());
            boolean black = source != null
                    ? gameQueryService.getEffectiveColors(gameData, source).contains(CardColor.BLACK)
                    : sourceCard != null
                            ? gameQueryService.getEffectiveCardColors(gameData, sourceCard).contains(CardColor.BLACK)
                            : gameData.stack.stream()
                            .filter(entry -> entry.getTargetableId().equals(shield.sourceId()))
                            .anyMatch(entry -> gameQueryService.getEffectiveCardColors(
                                    gameData, entry.getCard()).contains(CardColor.BLACK));
            if (!black) {
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
        return applyChosenSourceNextDamageToAnyTargetShield(
                gameData, sourcePermanentId, damage, recipientId, false, null);
    }

    public int applyChosenSourceNextDamageToAnyTargetShield(GameData gameData, UUID sourcePermanentId, int damage,
                                                            UUID recipientId, StackEntry sourceEntry) {
        return applyChosenSourceNextDamageToAnyTargetShield(
                gameData, sourcePermanentId, damage, recipientId, false, sourceEntry);
    }

    public int applyChosenSourceNextDamageToAnyTargetShield(GameData gameData, UUID sourcePermanentId, int damage,
                                                            UUID recipientId, boolean combatDamage) {
        return applyChosenSourceNextDamageToAnyTargetShield(
                gameData, sourcePermanentId, damage, recipientId, combatDamage, null);
    }

    private int applyChosenSourceNextDamageToAnyTargetShield(GameData gameData, UUID sourcePermanentId, int damage,
                                                             UUID recipientId, boolean combatDamage,
                                                             StackEntry sourceEntry) {
        if (damage <= 0 || sourcePermanentId == null || gameData.sourceNextDamageToAnyTargetShields.isEmpty()) {
            return damage;
        }
        boolean preventable = gameQueryService.isDamagePreventable(gameData, combatDamage);
        var it = gameData.sourceNextDamageToAnyTargetShields.iterator();
        while (it.hasNext()) {
            var shield = it.next();
            if (!chosenSourceMatches(gameData, shield.sourceId(), sourcePermanentId)
                    || !chosenSourceKeepsRequiredProperties(gameData, shield, sourcePermanentId, sourceEntry)) {
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
                damage = damage * shield.damageMultiplier()
                        * MaroGoneNutsSupport.doublingFactor(gameData);
                continue;
            }
            if (!preventable) {
                return damage;
            }
            it.remove();
            int prevented = Math.min(damage, shield.remainingDamage());
            if (prevented < shield.remainingDamage() && shield.remainingDamage() != Integer.MAX_VALUE) {
                gameData.sourceNextDamageToAnyTargetShields.addLast(
                        shield.withRemainingDamage(shield.remainingDamage() - prevented));
            }
            if (shield.damageRedSourceController()) {
                Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
                UUID sourceControllerId = gameQueryService.findPermanentController(gameData, sourcePermanentId);
                boolean redSource = source != null
                        && gameQueryService.getEffectiveColors(gameData, source).contains(CardColor.RED);
                if (source == null) {
                    StackEntry matchingEntry = sourceEntry != null
                            && (sourcePermanentId.equals(sourceEntry.getSourcePermanentId())
                            || sourceEntry.getEffectiveDamageSourceCard() != null
                            && sourcePermanentId.equals(sourceEntry.getEffectiveDamageSourceCard().getId()))
                            ? sourceEntry
                            : gameData.stack.stream()
                            .filter(stackEntry -> stackEntry.getCard() != null
                                    && sourcePermanentId.equals(stackEntry.getTargetableId()))
                            .findFirst()
                            .orElse(null);
                    if (matchingEntry != null) {
                        sourceControllerId = matchingEntry.getControllerId();
                        redSource = gameQueryService.getEffectiveCardColors(
                                gameData, matchingEntry.getEffectiveDamageSourceCard()).contains(CardColor.RED);
                    }
                }
                if (sourceControllerId != null && redSource
                        && shield.passageCard() != null && shield.passageControllerId() != null) {
                    gameData.pendingEyeForAnEyeReflections.add(new EyeForAnEyeReflection(
                            sourceControllerId, prevented, shield.passageCard(), shield.passageControllerId()));
                }
            }
            if (shield.lifeGainPlayerId() != null) {
                lifeSupport.applyGainLife(gameData, shield.lifeGainPlayerId(), prevented, "prevented damage");
            }
            if (shield.token() != null && shield.tokenControllerId() != null) {
                permanentControlSupportProvider.getObject().applyCreateToken(
                        gameData, shield.tokenControllerId(), shield.token(), prevented,
                        shield.tokenSourceSetCode());
            }
            return damage - prevented;
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
        UUID sourceControllerId = reflectSourceControllerId(gameData, sourcePermanentId);
        Card sourceCard = reflectSourceCard(gameData, sourcePermanentId);
        if (sourceControllerId == null || sourceCard == null) return damage;
        // List.remove(Object) removes the first matching entry — a single shield is consumed per event.
        if (!gameData.reflectDamageToSourceControllerShields.remove(sourcePermanentId)) return damage;

        gameData.pendingEyeForAnEyeReflections.add(new EyeForAnEyeReflection(
                sourceControllerId, damage, sourceCard, sourceControllerId,
                gameQueryService.findPermanentById(gameData, sourcePermanentId) != null ? sourcePermanentId : null));
        return 0;
    }

    /** Controller of a Reflect Damage source: its permanent's controller, or the controller of a spell on the stack. */
    private UUID reflectSourceControllerId(GameData gameData, UUID sourceId) {
        if (gameQueryService.findPermanentById(gameData, sourceId) != null) {
            return gameQueryService.findPermanentController(gameData, sourceId);
        }
        StackEntry spell = reflectSourceSpell(gameData, sourceId);
        return spell == null ? null : spell.getControllerId();
    }

    private Card reflectSourceCard(GameData gameData, UUID sourceId) {
        Permanent permanent = gameQueryService.findPermanentById(gameData, sourceId);
        if (permanent != null) {
            return permanent.getCard();
        }
        StackEntry spell = reflectSourceSpell(gameData, sourceId);
        return spell == null ? null : spell.getCard();
    }

    private StackEntry reflectSourceSpell(GameData gameData, UUID sourceId) {
        return gameData.stack.stream()
                .filter(entry -> entry.getEntryType() == StackEntryType.INSTANT_SPELL
                        || entry.getEntryType() == StackEntryType.SORCERY_SPELL)
                .filter(entry -> entry.getCard() != null && sourceId.equals(entry.getCard().getId()))
                .findFirst().orElse(null);
    }

    /** Redirects a source's next combat damage to the stored controller of the effect that created the shield. */
    public int applySourceNextCombatDamageToControllerShield(GameData gameData, UUID sourcePermanentId, int damage) {
        if (damage <= 0 || sourcePermanentId == null
                || gameData.sourceNextCombatDamageToControllerShields.isEmpty()) {
            return damage;
        }

        SourceNextCombatDamageToControllerShield matchingShield = null;
        synchronized (gameData.sourceNextCombatDamageToControllerShields) {
            for (SourceNextCombatDamageToControllerShield shield
                    : gameData.sourceNextCombatDamageToControllerShields) {
                if (sourcePermanentId.equals(shield.sourcePermanentId())) {
                    matchingShield = shield;
                    break;
                }
            }
            if (matchingShield != null) {
                gameData.sourceNextCombatDamageToControllerShields.remove(matchingShield);
            }
        }
        if (matchingShield == null || !gameData.playerIds.contains(matchingShield.controllerId())) {
            return damage;
        }

        gameData.pendingSourceRedirectDamage.add(new SourceDamageRedirectShield(
                null, sourcePermanentId, damage, matchingShield.controllerId()));
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

    /** Returns the controller receiving damage redirected from a targeted sorcery, if any. */
    public UUID getTargetSorceryDamageRedirectController(GameData gameData, StackEntry entry) {
        if (entry == null || entry.getSourcePermanentId() != null
                || entry.getEntryType() != StackEntryType.SORCERY_SPELL
                || entry.getCard() == null
                || gameData.targetSorceryDamageRedirectShields.isEmpty()) {
            return null;
        }
        UUID sorceryCardId = entry.getCard().getId();
        synchronized (gameData.targetSorceryDamageRedirectShields) {
            return gameData.targetSorceryDamageRedirectShields.stream()
                    .filter(shield -> sorceryCardId.equals(shield.sorceryCardId())
                            && shield.graveyardEntryVersion() == gameData.graveyardEntryVersion(sorceryCardId))
                    .map(TargetSorceryDamageRedirectShield::controllerId)
                    .findFirst()
                    .orElse(null);
        }
    }

    /**
     * Applies one-shot Opal-Eye shields: if a shield matches this source, the entire next damage event
     * it would deal (to any recipient) is instead dealt to the shield's destination permanent. The
     * redirected event is queued in {@link GameData#pendingSourceRedirectDamage} for the caller's
     * {@code processSourceRedirectDamage}. This is a redirection (replacement) effect, not prevention,
     * so it applies even when damage can't be prevented. The shield is left in place while the
     * destination is no longer a creature on the battlefield, and damage dealt to the destination
     * itself is dealt normally, but still uses up the shield.
     *
     * @param damagedPermanentId the permanent being damaged, or {@code null} when the damage is to a player
     * @return the remaining damage after redirection (0 if redirected)
     */
    public int applySourceNextDamageRedirectToPermanent(GameData gameData, UUID sourcePermanentId,
                                                        UUID damagedPermanentId, int damage) {
        // No isDamagePreventable check — this is redirection (replacement), not prevention.
        if (damage <= 0 || sourcePermanentId == null) {
            return damage;
        }
        List<SourceNextDamageRedirectToPermanentShield> candidates = new ArrayList<>();
        candidates.addAll(gameData.sourceRedirectShieldsUsedInOpenEvent);
        candidates.addAll(gameData.sourceNextDamageRedirectToPermanentShields);
        for (SourceNextDamageRedirectToPermanentShield shield : candidates) {
            if (!shield.sourceId().equals(sourcePermanentId)) continue;
            UUID destinationId = shield.destinationPermanentId();
            Permanent destination = gameQueryService.findPermanentById(gameData, destinationId);
            if (destination == null || !gameQueryService.isCreature(gameData, destination)) continue;
            if (gameData.sourceNextDamageRedirectToPermanentShields.remove(shield)
                    && !gameData.pendingSimultaneousDamage.isEmpty()) {
                gameData.sourceRedirectShieldsUsedInOpenEvent.add(shield);
            }
            // Redirecting damage to the destination itself changes nothing (it is dealt normally),
            // but the one-shot replacement effect has still been used up (CR 614.5).
            if (destinationId.equals(damagedPermanentId)) return damage;
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
        UUID liveSourceControllerId = gameQueryService.findPermanentController(gameData, sourcePermanentId);
        var it = gameData.eyeForAnEyeShields.iterator();
        while (it.hasNext()) {
            var shield = it.next();
            if (shield.protectedPlayerId().equals(playerId) && shield.sourceId().equals(sourcePermanentId)) {
                it.remove();
                UUID sourceControllerId = liveSourceControllerId != null
                        ? liveSourceControllerId : shield.sourceControllerId();
                if (sourceControllerId != null) {
                    gameData.pendingEyeForAnEyeReflections.add(new EyeForAnEyeReflection(
                            sourceControllerId, damage, shield.eyeCard(), shield.eyeControllerId()));
                }
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
            if (!shield.protectedPlayerId().equals(protectedPlayerId) || !shield.damageSourceId().equals(sourcePermanentId)
                    || !isRedirectDestination(gameData, shield.redirectTargetId()))
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
        return applyCreatureRedirectShields(gameData, protectedPermanentId, sourcePermanentId, damage, combatDamage,
                false);
    }

    private static boolean isAnySourceNextEvent(CreatureDamageRedirectShield shield) {
        return shield.isNextEvent() && shield.damageSourceId() == null;
    }

    /**
     * Applies the creature redirect shields of one kind: the any-source next-event shields when
     * {@code anySourceNextEvent} is set, every other shield otherwise.
     */
    public int applyCreatureRedirectShields(GameData gameData, UUID protectedPermanentId, UUID sourcePermanentId,
                                            int damage, boolean combatDamage, boolean anySourceNextEvent) {
        // No isDamagePreventable check — this is redirection (replacement), not prevention.
        if (damage <= 0 || protectedPermanentId == null
                || gameData.creatureDamageRedirectShields.isEmpty()) return damage;

        int remaining = damage;
        List<CreatureDamageRedirectShield> toReAdd = new ArrayList<>();
        Iterator<CreatureDamageRedirectShield> it = gameData.creatureDamageRedirectShields.iterator();

        while (it.hasNext() && remaining > 0) {
            CreatureDamageRedirectShield shield = it.next();
            if (!shield.protectedPermanentId().equals(protectedPermanentId)) continue;
            // Redirecting to the protected creature itself is a no-op; the damage is dealt normally.
            if (protectedPermanentId.equals(shield.redirectTargetId())) continue;
            if (isAnySourceNextEvent(shield) != anySourceNextEvent) continue;
            if (shield.combatOnly() && !combatDamage) continue;
            // A null source matches any source (e.g. Zealous Inquisitor); otherwise it must match exactly.
            if (shield.damageSourceId() != null && !shield.damageSourceId().equals(sourcePermanentId)) continue;
            if (!gameData.playerIds.contains(shield.redirectTargetId())
                    && gameQueryService.findPermanentById(gameData, shield.redirectTargetId()) == null) {
                it.remove();
                continue;
            }
            if (!isRedirectDestination(gameData, shield.redirectTargetId())) continue;
            if (shield.isUnlimited() && !gameData.playerIds.contains(shield.redirectTargetId())
                    && !gameQueryService.isCreature(gameData,
                    gameQueryService.findPermanentById(gameData, shield.redirectTargetId()))) {
                continue;
            }

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
     * Applies Glarecaster's shared one-shot redirect shield to damage dealt to its source permanent.
     */
    public int applySourcePermanentAndControllerNextDamageRedirectToPermanent(
            GameData gameData, UUID protectedPermanentId, UUID sourcePermanentId, int damage) {
        if (damage <= 0 || protectedPermanentId == null
                || gameData.sourcePermanentAndControllerNextDamageRedirectShields.isEmpty()) {
            return damage;
        }

        Iterator<SourcePermanentAndControllerNextDamageRedirectShield> it =
                gameData.sourcePermanentAndControllerNextDamageRedirectShields.iterator();
        while (it.hasNext()) {
            SourcePermanentAndControllerNextDamageRedirectShield shield = it.next();
            if (!protectedPermanentId.equals(shield.protectedPermanentId())
                    || !isRedirectDestination(gameData, shield.redirectTargetId())) continue;
            it.remove();
            gameData.pendingSourceRedirectDamage.add(new SourceDamageRedirectShield(
                    protectedPermanentId, sourcePermanentId, damage, shield.redirectTargetId()));
            return 0;
        }
        return damage;
    }

    /**
     * Applies Glarecaster's shared one-shot redirect shield to damage dealt to its controller.
     */
    public int applySourcePermanentAndControllerNextDamageRedirectToPlayer(
            GameData gameData, UUID protectedPlayerId, UUID sourcePermanentId, int damage) {
        if (damage <= 0 || protectedPlayerId == null
                || gameData.sourcePermanentAndControllerNextDamageRedirectShields.isEmpty()) {
            return damage;
        }

        Iterator<SourcePermanentAndControllerNextDamageRedirectShield> it =
                gameData.sourcePermanentAndControllerNextDamageRedirectShields.iterator();
        while (it.hasNext()) {
            SourcePermanentAndControllerNextDamageRedirectShield shield = it.next();
            if (!protectedPlayerId.equals(shield.protectedPlayerId())
                    || !isRedirectDestination(gameData, shield.redirectTargetId())) continue;
            it.remove();
            gameData.pendingSourceRedirectDamage.add(new SourceDamageRedirectShield(
                    protectedPlayerId, sourcePermanentId, damage, shield.redirectTargetId()));
            return 0;
        }
        return damage;
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
     * The distinct destinations of Soltari Guerrillas shields that currently apply to combat damage
     * from {@code sourcePermanentId}, as UUID strings. When more than one applies, the damaged player
     * chooses which replacement effect applies (CR 616.1).
     */
    public List<String> applicableSourceNextCombatDamageToOpponentRedirects(GameData gameData,
                                                                             UUID sourcePermanentId) {
        if (sourcePermanentId == null) return List.of();
        return gameData.sourceNextCombatDamageToOpponentRedirectShields.stream()
                .filter(shield -> shield.sourcePermanentId().equals(sourcePermanentId)
                        && isCreatureOnBattlefield(gameData, shield.destinationPermanentId()))
                .map(shield -> shield.destinationPermanentId().toString())
                .distinct()
                .toList();
    }

    /**
     * Applies one Soltari Guerrillas shield, chosen by destination: consumes it and queues the
     * redirected damage. Any other shield from the same source stays, since that damage is no longer
     * dealt to an opponent.
     *
     * @return the remaining damage after redirection (0 if redirected)
     */
    public int applySourceNextCombatDamageToOpponentRedirectTo(GameData gameData, UUID damagedPlayerId,
                                                               UUID sourcePermanentId, UUID destinationId,
                                                               int damage) {
        if (damage <= 0) return damage;
        Iterator<SourceNextCombatDamageToOpponentRedirectShield> it =
                gameData.sourceNextCombatDamageToOpponentRedirectShields.iterator();
        while (it.hasNext()) {
            SourceNextCombatDamageToOpponentRedirectShield shield = it.next();
            if (!shield.sourcePermanentId().equals(sourcePermanentId)
                    || !shield.destinationPermanentId().equals(destinationId)) continue;
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
            if (!isRedirectDestination(gameData, destinationId)) continue;

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
            if (turnDamageRedirectShieldApplies(gameData, shield, protectedPlayerId, damagedPermanentId,
                    damageSourceId, combatDamage)) {
                gameData.pendingSourceRedirectDamage.add(new SourceDamageRedirectShield(
                        protectedPlayerId, null, damage, shield.redirectTargetCreatureId()));
                return 0;
            }
        }
        return damage;
    }

    /**
     * The positions in {@link GameData#turnDamageRedirectToCreatureShields} of the shields that would
     * redirect this damage. Each shield is its own replacement effect (CR 616.1), so two Gideon's
     * Sacrifices give the affected player two options.
     */
    public List<Integer> applicableTurnDamageRedirectToCreatureShields(GameData gameData, UUID protectedPlayerId,
                                                                        UUID damagedPermanentId, UUID damageSourceId,
                                                                        boolean combatDamage) {
        if (protectedPlayerId == null) return List.of();
        List<Integer> applicable = new ArrayList<>();
        List<TurnDamageRedirectToCreatureShield> shields = gameData.turnDamageRedirectToCreatureShields;
        for (int i = 0; i < shields.size(); i++) {
            if (turnDamageRedirectShieldApplies(gameData, shields.get(i), protectedPlayerId, damagedPermanentId,
                    damageSourceId, combatDamage)) {
                applicable.add(i);
            }
        }
        return applicable;
    }

    /** Redirects the damage through the shield at {@code shieldIndex}; returns the damage left (0). */
    public int applyTurnDamageRedirectToCreatureShield(GameData gameData, int shieldIndex,
                                                       UUID protectedPlayerId, int damage) {
        if (damage <= 0) return damage;
        TurnDamageRedirectToCreatureShield shield = gameData.turnDamageRedirectToCreatureShields.get(shieldIndex);
        gameData.pendingSourceRedirectDamage.add(new SourceDamageRedirectShield(
                protectedPlayerId, null, damage, shield.redirectTargetCreatureId()));
        return 0;
    }

    private boolean turnDamageRedirectShieldApplies(GameData gameData, TurnDamageRedirectToCreatureShield shield,
                                                    UUID protectedPlayerId, UUID damagedPermanentId,
                                                    UUID damageSourceId, boolean combatDamage) {
        if (!shield.protectedPlayerId().equals(protectedPlayerId)) return false;
        if (shield.combatOnly() && !combatDamage) return false;
        if (shield.damageSourceId() != null && !shield.damageSourceId().equals(damageSourceId)) return false;
        if (!shield.includeControlledPermanents() && damagedPermanentId != null) return false;
        if (shield.controlledCreaturesOnly() && damagedPermanentId != null) {
            Permanent damagedPermanent = gameQueryService.findPermanentById(gameData, damagedPermanentId);
            if (damagedPermanent == null || !gameQueryService.isCreature(gameData, damagedPermanent)) return false;
        }
        UUID targetId = shield.redirectTargetCreatureId();
        // Redirecting damage to the destination creature itself is a no-op; deal it normally.
        if (targetId.equals(damagedPermanentId)) return false;
        // Gideon's Sacrifice also permits a planeswalker destination, but only while the chosen
        // permanent is still a creature or planeswalker on the battlefield.
        Permanent target = gameQueryService.findPermanentById(gameData, targetId);
        return target != null && (gameQueryService.isCreature(gameData, target)
                || shield.allowsPlaneswalker() && gameQueryService.isPlaneswalker(gameData, target));
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

    public boolean isUnblockedCreatureSource(GameData gameData, UUID sourcePermanentId) {
        if (sourcePermanentId == null || gameData.currentStep == null
                || gameData.currentStep.isBeforeBlockersDeclared()) return false;
        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        return source != null
                && gameQueryService.isCreature(gameData, source)
                && source.isAttacking()
                && !source.isBlockedWithoutBlockers()
                && !gameQueryService.isBlockedByAnyCreature(gameData, source)
                && !isBlockedByIndex(gameData, source);
    }

    /** A blocker records the attacker it blocks by index on the active player's battlefield (banding). */
    private static boolean isBlockedByIndex(GameData gameData, Permanent attacker) {
        int attackerIndex = gameData.playerBattlefields.getOrDefault(gameData.activePlayerId, List.of())
                .indexOf(attacker);
        if (attackerIndex < 0) return false;
        for (var entry : gameData.playerBattlefields.entrySet()) {
            if (entry.getKey().equals(gameData.activePlayerId)) continue;
            for (Permanent blocker : entry.getValue()) {
                if (blocker.isBlocking() && blocker.getBlockingTargets().contains(attackerIndex)) return true;
            }
        }
        return false;
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

        var layerSystemService = layerSystemServiceProvider.getObject();
        var pass = layerSystemService.beginPass(gameData);
        try {
            for (Permanent permanent : List.copyOf(battlefield)) {
                if (permanent.getId().equals(damagedPermanentId)
                        || gameQueryService.hasLostAllAbilities(gameData, permanent)) continue;
                boolean absorbs = gameQueryService.getActiveStaticEffects(gameData, permanent).stream()
                        .anyMatch(effect -> effect instanceof RedirectPlayerDamageToSelfEffect e && e.includeOtherPermanents());
                if (!absorbs) continue;
                gameData.pendingSourceRedirectDamage.add(
                        new SourceDamageRedirectShield(protectedPlayerId, null, damage, permanent.getId()));
                return 0;
            }
            return damage;
        } finally {
            layerSystemService.endPass(pass);
        }
    }

    public boolean applyColorDamagePreventionForPlayer(GameData gameData, UUID playerId, CardColor sourceColor) {
        return applyColorDamagePreventionForPlayer(gameData, playerId, sourceColor, false);
    }

    public boolean applyColorDamagePreventionForPlayer(GameData gameData, UUID playerId, CardColor sourceColor,
                                                       boolean combatDamage) {
        if (!gameQueryService.isDamagePreventable(gameData, combatDamage)) return false;
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
        return isColorDamagePreventedForTarget(gameData, targetId, sourceColors, false);
    }

    public boolean isColorDamagePreventedForTarget(GameData gameData, UUID targetId, Set<CardColor> sourceColors,
                                                    boolean combatDamage) {
        if (!gameQueryService.isDamagePreventable(gameData, combatDamage)
                || targetId == null || sourceColors == null) return false;
        Set<CardColor> damageSourceColors = gameQueryService.getDamageSourceColors(gameData, sourceColors);
        if (damageSourceColors.isEmpty()) {
            return gameData.colorlessDamagePreventionUntilEndOfTurn.contains(targetId);
        }
        Set<CardColor> preventedColors = gameData.colorDamagePreventionUntilEndOfTurn.get(targetId);
        if (preventedColors != null && !preventedColors.isEmpty()
                && damageSourceColors.stream().anyMatch(preventedColors::contains)) {
            return true;
        }
        return isControlledCreatureColorDamagePrevented(gameData, targetId, damageSourceColors);
    }

    private boolean isControlledCreatureColorDamagePrevented(GameData gameData, UUID targetId,
                                                             Set<CardColor> damageSourceColors) {
        if (gameData.colorDamagePreventionForControlledCreaturesUntilEndOfTurn.isEmpty()) return false;
        Permanent creature = gameQueryService.findPermanentById(gameData, targetId);
        if (creature == null || !gameQueryService.isCreature(gameData, creature)) return false;
        UUID controllerId = gameQueryService.findPermanentController(gameData, targetId);
        if (controllerId == null) return false;
        Set<CardColor> preventedColors =
                gameData.colorDamagePreventionForControlledCreaturesUntilEndOfTurn.get(controllerId);
        return preventedColors != null && damageSourceColors.stream().anyMatch(preventedColors::contains);
    }

    /**
     * Applies static damage reduction from permanents with {@link PreventDamageFromOpponentSourcesEffect}
     * on the receiving player's battlefield (e.g. Guardian Seraph and Energy Field).
     * Only reduces damage from sources not controlled by the receiving player. Returns the damage
     * after reduction (min 0).
     */
    public int applyOpponentSourceDamageReduction(GameData gameData, UUID playerId, UUID sourceControllerId, int damage) {
        return applyOpponentSourceDamageReduction(gameData, playerId, sourceControllerId, damage, false);
    }

    public int applyOpponentSourceDamageReduction(GameData gameData, UUID playerId, UUID sourceControllerId,
                                                   int damage, boolean combatDamage) {
        return applyOpponentSourceDamageReduction(gameData, playerId, sourceControllerId, damage, combatDamage, false);
    }

    /**
     * As above for damage to a permanent {@code controllerId} controls: only effects that also cover
     * permanents apply (Valkmira, Protector's Shield).
     */
    public int applyOpponentSourceDamageReductionToPermanent(GameData gameData, UUID controllerId,
                                                              UUID sourceControllerId, int damage,
                                                              boolean combatDamage) {
        return applyOpponentSourceDamageReduction(gameData, controllerId, sourceControllerId, damage, combatDamage, true);
    }

    private int applyOpponentSourceDamageReduction(GameData gameData, UUID playerId, UUID sourceControllerId,
                                                    int damage, boolean combatDamage, boolean toPermanent) {
        if (!gameQueryService.isDamagePreventable(gameData, combatDamage)) return damage;
        if (damage <= 0) return damage;
        if (sourceControllerId == null || sourceControllerId.equals(playerId)) return damage;

        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return damage;

        long totalReduction = 0;
        for (Permanent permanent : battlefield) {
            if (gameQueryService.hasLostAllAbilities(gameData, permanent)) continue;
            for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof PreventDamageFromOpponentSourcesEffect e
                        && (!toPermanent || e.includesPermanents())) {
                    totalReduction += e.amount();
                    if (totalReduction >= damage) return 0;
                }
            }
        }

        if (totalReduction <= 0) return damage;
        return (int) (damage - totalReduction);
    }

    /**
     * Applies Comeuppance prevention to damage dealt to a player or one of their planeswalkers.
     * Creature-source damage is queued back to that creature; other damage is queued back to the
     * source's controller.
     */
    public int applyComeuppancePrevention(GameData gameData, UUID protectedPlayerId, int damage,
                                          Card sourceCard, Permanent sourcePermanent,
                                          UUID sourceControllerId, boolean combatDamage) {
        if (!gameQueryService.isDamagePreventable(gameData, combatDamage)
                || damage <= 0 || protectedPlayerId == null || sourceControllerId == null
                || sourceControllerId.equals(protectedPlayerId)
                || gameData.comeuppanceDamagePreventionShields.isEmpty()) {
            return damage;
        }

        boolean creatureSource = sourcePermanent != null
                ? gameQueryService.isCreature(gameData, sourcePermanent)
                : sourceCard != null && sourceCard.hasType(CardType.CREATURE);
        for (ComeuppanceDamagePreventionShield shield : gameData.comeuppanceDamagePreventionShields) {
            if (!protectedPlayerId.equals(shield.protectedPlayerId())) continue;

            if (creatureSource && sourcePermanent != null) {
                gameData.pendingRedirectDamage.add(new DamageRedirectShield(
                        protectedPlayerId, damage, null, shield.sourceCard(), sourcePermanent.getId()));
            } else if (!creatureSource && gameData.playerIds.contains(sourceControllerId)) {
                gameData.pendingRedirectDamage.add(new DamageRedirectShield(
                        protectedPlayerId, damage, null, shield.sourceCard(), sourceControllerId));
            }
            gameLogService.append(gameData, GameLog.cardThen(shield.sourceCard(),
                    " prevents " + damage + " damage to its controller or a planeswalker they control."));
            return 0;
        }
        return damage;
    }

    /** Applies Judgment of Alexander's creature-source prevention and queues its retaliation trigger. */
    public int applyJudgmentOfAlexanderPrevention(GameData gameData, UUID protectedPlayerId, int damage,
                                                   Card sourceCard, Permanent sourcePermanent,
                                                   UUID sourceControllerId, boolean combatDamage) {
        if (!gameQueryService.isDamagePreventable(gameData, combatDamage)
                || damage <= 0 || protectedPlayerId == null || sourceControllerId == null
                || sourceControllerId.equals(protectedPlayerId)
                || gameData.judgmentOfAlexanderDamagePreventionShields.isEmpty()) {
            return damage;
        }

        boolean creatureSource = sourcePermanent != null
                ? gameQueryService.isCreature(gameData, sourcePermanent)
                : sourceCard != null && sourceCard.hasType(CardType.CREATURE);
        for (JudgmentOfAlexanderDamagePreventionShield shield
                : gameData.judgmentOfAlexanderDamagePreventionShields) {
            if (!protectedPlayerId.equals(shield.protectedPlayerId())) continue;

            gameLogService.append(gameData, GameLog.cardThen(shield.sourceCard(),
                    " prevents " + damage + " damage to " + gameData.playerIdToName.get(protectedPlayerId) + "."));
            // Only damage from a creature is prevented with retaliation; other opponent sources are
            // prevented without triggering the commanders.
            if (creatureSource) {
                triggerCollectionService.checkCommanderDamagePreventedTriggers(
                        gameData, protectedPlayerId, sourcePermanent == null ? null : sourcePermanent.getId(),
                        shield.sourceCard());
            }
            return 0;
        }
        return damage;
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
                gameQueryService.hasActiveStaticEffectIncludingGranted(
                        gameData, p, PreventNoncombatDamageToControllerAndGainLifeEffect.class));
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
    /** Active optional-prevention sources for one player's impending damage. */
    public List<UUID> clericPreventionSources(GameData gameData) {
        List<UUID> result = new ArrayList<>();
        gameData.forEachPermanent((controller, permanent) -> {
            if (gameQueryService.hasActiveStaticEffectIncludingGranted(gameData, permanent,
                    PreventDamageToControllerPerClericEffect.class)
                    && clericPreventionAmount(gameData, controller) > 0) result.add(permanent.getId());
        });
        return result;
    }

    /** The number of Clerics currently controlled by the player making the choice. */
    public int clericPreventionAmount(GameData gameData, UUID controllerId) {
        return (int) gameData.playerBattlefields.getOrDefault(controllerId, List.of()).stream()
                .filter(permanent -> gameQueryService.isCreature(gameData, permanent)
                        && gameQueryService.hasEffectiveSubtype(gameData, permanent, CardSubtype.CLERIC)).count();
    }

    /** Queues one independent optional prevention while preserving the actual damage source. */
    public boolean queueClericPreventionChoice(GameData gameData, StackEntry entry, UUID playerId,
                                               int damage, boolean combat, List<UUID> alchemistIds) {
        if (damage <= 0 || !gameQueryService.isDamagePreventable(gameData, combat)) return false;
        Permanent damageSource = entry.getSourcePermanentId() == null ? null
                : gameQueryService.findPermanentById(gameData, entry.getSourcePermanentId());
        if (damageSource == null) damageSource = entry.getSourcePermanentSnapshot();
        UUID sourceControllerId = entry.getSourcePermanentId() == null ? null
                : gameQueryService.findPermanentController(gameData, entry.getSourcePermanentId());
        if (sourceControllerId == null) sourceControllerId = entry.getDamageSourceControllerId();
        if (sourceControllerId == null) sourceControllerId = entry.getControllerId();
        if (damageSource != null && gameQueryService.damageCantBePreventedFromSource(gameData, damageSource, combat)) return false;
        for (int index = 0; index < alchemistIds.size(); index++) {
            Permanent alchemist = gameQueryService.findPermanentById(gameData, alchemistIds.get(index));
            if (alchemist == null || !gameQueryService.hasActiveStaticEffectIncludingGranted(gameData,
                    alchemist, PreventDamageToControllerPerClericEffect.class)) continue;
            UUID controller = gameQueryService.findPermanentController(gameData, alchemist.getId());
            if (controller == null || clericPreventionAmount(gameData, controller) == 0) continue;
            gameData.pendingMayAbilities.addFirst(new com.github.laxika.magicalvibes.model.PendingMayAbility(
                    entry.getEffectiveDamageSourceCard(), controller, List.of(new PreventDamageToControllerPerClericEffect(
                    playerId, alchemist.getId(), alchemistIds.subList(index + 1, alchemistIds.size()),
                    combat, entry.getEntryType(), gameData.unpreventableDamageInProgress)),
                    "Prevent up to " + clericPreventionAmount(gameData, controller) + " damage to "
                            + gameData.playerIdToName.get(playerId) + "?", playerId, null,
                    entry.getSourcePermanentId(), null, 0, 0, null, null, null,
                    damageSource == null ? null : new Permanent(damageSource), sourceControllerId, null, damage));
            return true;
        }
        return false;
    }

    public int applyControllerPerClericDamagePrevention(GameData gameData, UUID playerId, int damage) {
        return applyControllerPerClericDamagePrevention(gameData, playerId, damage, false);
    }

    public int applyControllerPerClericDamagePrevention(GameData gameData, UUID playerId, int damage,
                                                         boolean combatDamage) {
        if (!gameQueryService.isDamagePreventable(gameData, combatDamage)) return 0;
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
        return applyControllerFixedPerSourceDamagePrevention(gameData, playerId, damage, sourceIsCreature,
                sourceIsArtifact, sourceColors, combatDamage, null);
    }

    /** Applies per-source prevention using a player's selected division when present. */
    public int applyControllerFixedPerSourceDamagePrevention(GameData gameData, UUID playerId, int damage,
                                                              boolean sourceIsCreature, boolean sourceIsArtifact,
                                                              Set<CardColor> sourceColors, boolean combatDamage,
                                                              UUID damageSourceId) {
        if (!gameQueryService.isDamagePreventable(gameData, combatDamage)) return 0;
        if (damage <= 0) return 0;

        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) return 0;

        int reduction = 0;
        for (Permanent permanent : battlefield) {
            if (gameQueryService.hasLostAllAbilities(gameData, permanent)) continue;
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
                    int amount = amountEvaluationService.evaluate(gameData, prevention.amount(),
                            AmountContext.forStaticEffect(permanent, playerId));
                    reduction += sharedCombatPreventionAmount(gameData, damageSourceId, permanent.getId(),
                            playerId, amount, combatDamage);
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
        return applyAllButOneDamagePrevention(gameData, recipientControllerId, damage, false);
    }

    public int applyAllButOneDamagePrevention(GameData gameData, UUID recipientControllerId, int damage,
                                               boolean combatDamage) {
        if (!gameQueryService.isDamagePreventable(gameData, combatDamage)) return 0;
        if (damage <= 1 || recipientControllerId == null) return 0;

        return hasAllButOneDamagePrevention(
                gameData, recipientControllerId, AllButOneDamagePreventionEffect::protectsController)
                ? damage - 1
                : 0;
    }

    public int applyAllButOneDamageToPlaneswalkerPrevention(GameData gameData, UUID recipientControllerId,
                                                             int damage, boolean combatDamage) {
        if (!gameQueryService.isDamagePreventable(gameData, combatDamage)) return 0;
        if (damage <= 1 || recipientControllerId == null) return 0;

        return hasAllButOneDamagePrevention(
                gameData, recipientControllerId, AllButOneDamagePreventionEffect::protectsPlaneswalkers)
                ? damage - 1
                : 0;
    }

    /** Hyperion, Supreme Hero-style prevention for a Hero permanent controlled by the recipient. */
    private int applyAllButOneDamageToHeroPrevention(GameData gameData, Permanent permanent, int damage,
                                                      boolean combatDamage) {
        if (!gameQueryService.isDamagePreventable(gameData, combatDamage)
                || permanent == null || damage <= 1
                || !gameQueryService.hasEffectiveSubtype(gameData, permanent, CardSubtype.HERO)) {
            return 0;
        }

        UUID controllerId = gameQueryService.findPermanentController(gameData, permanent.getId());
        return controllerId != null
                && hasAllButOneDamagePrevention(
                gameData, controllerId, AllButOneDamagePreventionEffect::protectsHeroes)
                ? damage - 1
                : 0;
    }

    private boolean hasAllButOneDamagePrevention(GameData gameData, UUID controllerId,
                                                  Predicate<AllButOneDamagePreventionEffect> applies) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
        boolean hasPrevention = battlefield != null && battlefield.stream()
                .filter(p -> !gameQueryService.hasLostAllAbilities(gameData, p))
                .flatMap(p -> p.getCard().getEffects(EffectSlot.STATIC).stream())
                .anyMatch(effect -> effect instanceof AllButOneDamagePreventionEffect prevention
                        && applies.test(prevention));
        if (hasPrevention) return true;

        return gameData.emblems.stream()
                .anyMatch(emblem -> controllerId.equals(emblem.controllerId())
                        && emblem.staticEffects().stream()
                        .anyMatch(effect -> effect instanceof AllButOneDamagePreventionEffect prevention
                                && applies.test(prevention)));
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
        return applyPlaneswalkerFixedPerSourceDamagePrevention(gameData, planeswalkerControllerId, damage, false);
    }

    public int applyPlaneswalkerFixedPerSourceDamagePrevention(GameData gameData, UUID planeswalkerControllerId,
                                                                int damage, boolean combatDamage) {
        if (!gameQueryService.isDamagePreventable(gameData, combatDamage)) return 0;
        if (damage <= 0 || planeswalkerControllerId == null) return 0;

        int reduction = 0;
        List<Permanent> battlefield = gameData.playerBattlefields.get(planeswalkerControllerId);
        if (battlefield != null) {
            for (Permanent permanent : battlefield) {
                if (gameQueryService.hasLostAllAbilities(gameData, permanent)) continue;
                for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof PlaneswalkerDamagePreventionEffect prevention
                            && !permanent.isStaticEffectSuppressed(effect.getClass())) {
                        reduction = saturatingAdd(reduction, prevention.amount());
                    }
                }
            }
        }
        if (gameData.planechase != null
                && Objects.equals(gameData.planechase.controllerId, planeswalkerControllerId)) {
            for (var planar : gameData.planechase.faceUp) {
                for (CardEffect effect : planar.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof PlaneswalkerDamagePreventionEffect prevention) {
                        reduction = saturatingAdd(reduction, prevention.amount());
                    }
                }
            }
        }
        return Math.min(damage, reduction);
    }

    private int saturatingAdd(int left, int right) {
        return (int) Math.min(Integer.MAX_VALUE, (long) left + right);
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
                .filter(p -> !gameQueryService.hasLostAllAbilities(gameData, p))
                .flatMap(p -> p.getCard().getEffects(EffectSlot.STATIC).stream())
                .filter(e -> e instanceof PreventSpellDamageToOpponentAndCreateTokensEffect)
                .map(e -> (PreventSpellDamageToOpponentAndCreateTokensEffect) e)
                .findFirst().orElse(null);
    }

    /** Returns whether a source controlled by the given player must replace damage to an opponent with milling. */
    public boolean hasControllerOpponentDamageMillReplacement(GameData gameData, UUID sourceControllerId,
                                                               UUID targetPlayerId, int damage) {
        return damage > 0 && !controllerOpponentDamageMillSources(
                gameData, sourceControllerId, targetPlayerId).isEmpty();
    }

    /**
     * The permanents controlled by {@code sourceControllerId} whose static effect mills the opponent
     * {@code targetPlayerId} instead of dealing damage to them. Each one is a separate prevention effect.
     */
    public List<Permanent> controllerOpponentDamageMillSources(GameData gameData, UUID sourceControllerId,
                                                               UUID targetPlayerId) {
        if (sourceControllerId == null
                || targetPlayerId == null
                || sourceControllerId.equals(targetPlayerId)) {
            return List.of();
        }

        List<Permanent> battlefield = gameData.playerBattlefields.get(sourceControllerId);
        if (battlefield == null) return List.of();

        return battlefield.stream()
                .filter(permanent -> !permanent.isFaceDown()
                        && !permanent.isLosesAllAbilitiesUntilEndOfTurn())
                .filter(permanent -> gameQueryService.hasActiveStaticEffect(
                        gameData, permanent, ControllerOpponentDamageMillReplacementEffect.class))
                .toList();
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
                if (p.isAttached() && p.getAttachedTo().equals(creature.getId())
                        && !gameQueryService.hasLostAllAbilities(gameData, p)) {
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

    private void recordPlusOnePlusOneCounterPlacedOnControlledPermanent(
            GameData gameData, Permanent permanent, int count) {
        UUID controllerId = gameQueryService.findPermanentController(gameData, permanent.getId());
        if (controllerId != null && count > 0 && gameQueryService.isCreature(gameData, permanent)) {
            gameData.playersWhoPutPlusOnePlusOneCountersOnCreaturesThisTurn.add(controllerId);
            gameData.playersWhoControlledPermanentsThatReceivedPlusOneCountersThisTurn.add(controllerId);
            gameData.plusOnePlusOneCountersPutOnControlledCreaturesThisTurn.merge(
                    controllerId, count, Integer::sum);
        }
    }

    /**
     * Caduceus grants the equipped creature "Prevent all damage that would be dealt to this
     * creature", so ability removal with a later timestamp than the Equipment ends the prevention.
     */
    private boolean grantedAttachedPreventionSurvives(GameData gameData, Permanent creature) {
        return gameData.anyPermanentMatches(p -> p.isAttached() && p.getAttachedTo().equals(creature.getId())
                && p.getCard().getEffects(EffectSlot.STATIC).stream().anyMatch(effect ->
                        effect instanceof PreventAllDamageToAttachedCreatureEffect
                                || effect instanceof ConditionalEffect conditional
                                && conditional.wrapped() instanceof PreventAllDamageToAttachedCreatureEffect)
                && gameQueryService.grantedAbilitySurvivesRemoval(gameData, creature, p.getTimestamp()));
    }

    /**
     * Consumes the player's "prevent the next N damage" shield ({@code playerDamagePreventionShields})
     * against {@code damage} and returns what remains.
     */
    public int applyPlayerDamagePreventionShield(GameData gameData, UUID playerId, int damage) {
        int shield = gameData.playerDamagePreventionShields.getOrDefault(playerId, 0);
        if (shield <= 0 || damage <= 0) return damage;
        int prevented = Math.min(shield, damage);
        int remaining = shield - prevented;
        if (remaining == 0) {
            gameData.playerDamagePreventionShields.remove(playerId);
        } else {
            gameData.playerDamagePreventionShields.put(playerId, remaining);
        }
        notifyControllerDamagePrevented(gameData, playerId, prevented);
        return damage - prevented;
    }

    /** Fixed per-source prevention covering a player and their creatures, for damage to the player. */
    public int applyControllerAndCreaturesFixedPerSourceDamagePrevention(GameData gameData, UUID playerId,
                                                                          int damage) {
        return applyControllerAndCreaturesFixedPerSourceDamagePrevention(
                gameData, playerId, damage, false, null, null);
    }

    /** Vengeful Archon-style shields protecting a player; the absorbed damage is dealt elsewhere later. */
    public int applyPlayerRedirectShields(GameData gameData, UUID playerId, int damage) {
        return applyRedirectShields(gameData, playerId, null, damage, false, false);
    }

    /** Whether a Vengeful Archon-style shield protects {@code playerId}. */
    public boolean hasPlayerRedirectShield(GameData gameData, UUID playerId) {
        return gameData.damageRedirectShields.stream()
                .anyMatch(shield -> shield.protectedPermanentId() == null
                        && playerId.equals(shield.protectedPlayerId()));
    }

    /** Whether a Candles' Glow-style shield protects {@code targetId}. */
    public boolean hasDamagePreventionLifeGainShield(GameData gameData, UUID targetId) {
        return gameData.damagePreventionLifeGainShields.stream()
                .anyMatch(shield -> targetId.equals(shield.targetId()));
    }

    /** Whether a Healing Grace-style shield covers damage from {@code sourceId} to {@code targetId}. */
    public boolean hasTargetSourcePreventionShield(GameData gameData, UUID targetId, UUID sourceId) {
        return targetId != null && sourceId != null && gameData.targetSourceDamagePreventionShields.stream()
                .anyMatch(shield -> shield.targetId().equals(targetId) && shield.sourceId().equals(sourceId));
    }

    /**
     * Whether {@link #applyPlayerNextSourceDamageShield(GameData, UUID, UUID, int, boolean, Card, boolean)}
     * would consume a shield for this damage.
     */
    public boolean hasPlayerNextSourceDamageShield(GameData gameData, UUID playerId, UUID sourcePermanentId,
                                                   boolean combatDamage, Card sourceCard, boolean sourceUnblocked) {
        if (playerId == null || sourcePermanentId == null) return false;
        return gameData.playerSourceNextDamageShields.stream()
                .anyMatch(shield -> shield.playerId().equals(playerId)
                        && shieldMatchesSource(gameData, shield, sourcePermanentId, sourceCard)
                        && (!shield.combatOnly() || combatDamage)
                        && (!shield.unblockedOnly() || sourceUnblocked));
    }

    /**
     * A chosen permanent spell is also the source of damage dealt by the permanent it becomes
     * (CR 609.7a), so a shield keyed to the spell's card id matches that permanent.
     */
    /**
     * CR 609.7b: a shield for "a red source of your choice" rechecks the source's properties when it
     * would deal damage; if they no longer match, the damage isn't prevented and the shield isn't used up.
     */
    private boolean chosenSourceKeepsRequiredProperties(GameData gameData, SourceNextDamageToAnyTargetShield shield,
                                                       UUID sourcePermanentId, StackEntry sourceEntry) {
        if (shield.requiredSourceFilter() == null) {
            return true;
        }
        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        Card sourceCard = sourceEntry == null ? null : sourceEntry.getEffectiveDamageSourceCard();
        return gameQueryService.matchesDamageSourcePredicate(
                gameData, sourceEntry, sourceCard, source, shield.requiredSourceFilter());
    }

    private boolean chosenSourceMatches(GameData gameData, UUID shieldSourceId, UUID sourcePermanentId) {
        if (shieldSourceId.equals(sourcePermanentId)) {
            return true;
        }
        Permanent source = gameQueryService.findPermanentById(gameData, sourcePermanentId);
        return source != null && shieldSourceId.equals(source.getCard().getId());
    }

    /**
     * Whether {@link #applyChosenSourceNextDamageToAnyTargetShield(GameData, UUID, int, UUID, StackEntry)}
     * would use a shield for this damage: one that multiplies it, or one that prevents it while damage
     * can be prevented.
     */
    public boolean hasChosenSourceNextDamageToAnyTargetShield(GameData gameData, UUID sourcePermanentId,
                                                              UUID recipientId, boolean combatDamage) {
        if (sourcePermanentId == null) return false;
        boolean preventable = gameQueryService.isDamagePreventable(gameData, combatDamage);
        return gameData.sourceNextDamageToAnyTargetShields.stream()
                .anyMatch(shield -> chosenSourceMatches(gameData, shield.sourceId(), sourcePermanentId)
                        && chosenSourceKeepsRequiredProperties(gameData, shield, sourcePermanentId, null)
                        && (!shield.combatOnly() || combatDamage)
                        && (!shield.playersOnly() || gameData.playerIdToName.containsKey(recipientId))
                        && (shield.combatPhase() == null || shield.combatPhase().equals(gameData.combatPhasesThisTurn))
                        && (shield.recipientId() == null || shield.recipientId().equals(recipientId))
                        && (shield.damageMultiplier() != 0 || preventable));
    }

    /** Whether a Channel Harm shield would take over damage from {@code sourceControllerId}'s source. */
    public boolean hasChannelHarmShield(GameData gameData, UUID protectedPlayerId, UUID sourceControllerId) {
        return gameQueryService.isDamagePreventable(gameData)
                && protectedPlayerId != null && sourceControllerId != null
                && !protectedPlayerId.equals(sourceControllerId)
                && gameData.channelHarmShields.stream()
                .anyMatch(shield -> protectedPlayerId.equals(shield.protectedPlayerId()));
    }

    /** Whether Comeuppance would prevent damage from {@code sourceControllerId}'s source. */
    public boolean hasComeuppanceShield(GameData gameData, UUID protectedPlayerId, UUID sourceControllerId,
                                        boolean combatDamage) {
        return gameQueryService.isDamagePreventable(gameData, combatDamage)
                && protectedPlayerId != null && sourceControllerId != null
                && !sourceControllerId.equals(protectedPlayerId)
                && gameData.comeuppanceDamagePreventionShields.stream()
                .anyMatch(shield -> protectedPlayerId.equals(shield.protectedPlayerId()));
    }

    /** Whether Judgment of Alexander would prevent damage from this source (any opponent source). */
    public boolean hasJudgmentOfAlexanderShield(GameData gameData, UUID protectedPlayerId, Card sourceCard,
                                                Permanent sourcePermanent, UUID sourceControllerId,
                                                boolean combatDamage) {
        if (!gameQueryService.isDamagePreventable(gameData, combatDamage)
                || protectedPlayerId == null || sourceControllerId == null
                || sourceControllerId.equals(protectedPlayerId)) {
            return false;
        }
        return gameData.judgmentOfAlexanderDamagePreventionShields.stream()
                .anyMatch(shield -> protectedPlayerId.equals(shield.protectedPlayerId()));
    }

    /** Whether a one-shot Circle of Protection-style color prevention covers {@code sourceColor}. */
    public boolean hasColorDamagePreventionForPlayer(GameData gameData, UUID playerId, CardColor sourceColor,
                                                     boolean combatDamage) {
        if (!gameQueryService.isDamagePreventable(gameData, combatDamage)) return false;
        CardColor damageColor = gameQueryService.getDamageSourceColor(gameData, sourceColor);
        if (damageColor == null) return false;
        Map<CardColor, Integer> colorMap = gameData.playerColorDamagePreventionCount.get(playerId);
        Integer count = colorMap == null ? null : colorMap.get(damageColor);
        return count != null && count > 0;
    }

    /** Whether an Eye for an Eye shield watches damage from {@code sourcePermanentId} to {@code playerId}. */
    public boolean hasEyeForAnEyeShield(GameData gameData, UUID playerId, UUID sourcePermanentId) {
        return playerId != null && sourcePermanentId != null && gameData.eyeForAnEyeShields.stream()
                .anyMatch(shield -> shield.protectedPlayerId().equals(playerId)
                        && shield.sourceId().equals(sourcePermanentId));
    }

    /** Whether a Harm's Way-style shield redirects damage from {@code sourcePermanentId} to this player. */
    public boolean hasSourceRedirectShield(GameData gameData, UUID protectedPlayerId, UUID sourcePermanentId) {
        return sourcePermanentId != null && gameData.sourceDamageRedirectShields.stream()
                .anyMatch(shield -> shield.protectedPlayerId().equals(protectedPlayerId)
                        && shield.damageSourceId().equals(sourcePermanentId)
                        && isRedirectDestination(gameData, shield.redirectTargetId()));
    }

    /** Whether General's Regalia redirects the next damage from {@code sourcePermanentId} to this player. */
    public boolean hasPlayerSourceNextDamageRedirectShield(GameData gameData, UUID protectedPlayerId,
                                                           UUID sourcePermanentId) {
        if (protectedPlayerId == null || sourcePermanentId == null) return false;
        return gameData.playerSourceNextDamageRedirectShields.stream()
                .anyMatch(shield -> shield.protectedPlayerId().equals(protectedPlayerId)
                        && shield.sourcePermanentId().equals(sourcePermanentId)
                        && isCreatureOnBattlefield(gameData, shield.redirectTargetPermanentId()));
    }

    /** Whether Reflect Damage turns the next damage from {@code sourcePermanentId} back on its controller. */
    public boolean hasReflectDamageToSourceControllerShield(GameData gameData, UUID sourcePermanentId) {
        return sourcePermanentId != null
                && gameData.reflectDamageToSourceControllerShields.contains(sourcePermanentId)
                && reflectSourceControllerId(gameData, sourcePermanentId) != null;
    }

    /** Whether Opal-Eye-style shields redirect the next damage from {@code sourcePermanentId}. */
    public boolean hasSourceNextDamageRedirectToPermanent(GameData gameData, UUID sourcePermanentId,
                                                          UUID damagedPermanentId) {
        return sourcePermanentId != null && java.util.stream.Stream.concat(
                        gameData.sourceNextDamageRedirectToPermanentShields.stream(),
                        gameData.sourceRedirectShieldsUsedInOpenEvent.stream())
                .anyMatch(shield -> shield.sourceId().equals(sourcePermanentId)
                        && isCreatureOnBattlefield(gameData, shield.destinationPermanentId()));
    }

    /** Whether Saving Grace-style shields redirect this damage to a player onto a creature. */
    public boolean hasTurnDamageRedirectToCreature(GameData gameData, UUID protectedPlayerId,
                                                   UUID damageSourceId, boolean combatDamage) {
        if (protectedPlayerId == null) return false;
        return gameData.turnDamageRedirectToCreatureShields.stream()
                .anyMatch(shield -> {
                    if (!shield.protectedPlayerId().equals(protectedPlayerId)) return false;
                    if (shield.combatOnly() && !combatDamage) return false;
                    if (shield.damageSourceId() != null && !shield.damageSourceId().equals(damageSourceId)) {
                        return false;
                    }
                    Permanent target = gameQueryService.findPermanentById(gameData, shield.redirectTargetCreatureId());
                    return target != null && (gameQueryService.isCreature(gameData, target)
                            || shield.allowsPlaneswalker() && gameQueryService.isPlaneswalker(gameData, target));
                });
    }

    /** Whether Glarecaster's shared shield redirects the next damage to this player. */
    public boolean hasSourcePermanentAndControllerNextDamageRedirectToPlayer(GameData gameData,
                                                                             UUID protectedPlayerId) {
        return protectedPlayerId != null && gameData.sourcePermanentAndControllerNextDamageRedirectShields.stream()
                .anyMatch(shield -> protectedPlayerId.equals(shield.protectedPlayerId())
                        && isRedirectDestination(gameData, shield.redirectTargetId()));
    }

    /** Whether Martyrdom-style shields redirect the next damage to this player. */
    public boolean hasPlayerNextDamageRedirectShield(GameData gameData, UUID protectedPlayerId) {
        return protectedPlayerId != null && gameData.playerNextDamageRedirectShields.stream()
                .anyMatch(shield -> shield.protectedPlayerId().equals(protectedPlayerId)
                        && isRedirectDestination(gameData, shield.redirectTargetPermanentId()));
    }

    /** Whether Aegis of Honor-style shields send this instant or sorcery damage back to its caster. */
    public boolean hasNextInstantOrSorceryDamageRedirectShield(GameData gameData, StackEntry entry,
                                                               UUID protectedPlayerId) {
        return entry != null && protectedPlayerId != null && entry.getSourcePermanentId() == null
                && (entry.getEntryType() == StackEntryType.INSTANT_SPELL
                || entry.getEntryType() == StackEntryType.SORCERY_SPELL)
                && gameData.playerNextInstantOrSorceryDamageRedirectShields.contains(protectedPlayerId);
    }

    /** Whether Gisela-style effects halve damage to {@code recipientId}. */
    public boolean hasHalfDamagePrevention(GameData gameData, UUID recipientId) {
        return gameData.playerBattlefields.getOrDefault(recipientId, List.of()).stream()
                .filter(permanent -> !gameQueryService.hasLostPrintedAbilities(gameData, permanent))
                .anyMatch(permanent -> permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(PreventHalfDamageToControllerAndTheirPermanentsEffect.class::isInstance));
    }

    /**
     * Whether damage can still be redirected to {@code destinationId}: a player in the game, or a
     * permanent on the battlefield that is still a battle, creature or planeswalker. Otherwise the
     * redirection effect does nothing (CR 614.9) and the damage stays with its original recipient.
     */
    public boolean isRedirectDestination(GameData gameData, UUID destinationId) {
        if (destinationId == null) return false;
        if (gameData.playerIds.contains(destinationId)) return true;
        Permanent permanent = gameQueryService.findPermanentById(gameData, destinationId);
        return permanent != null && (gameQueryService.isCreature(gameData, permanent)
                || gameQueryService.isPlaneswalker(gameData, permanent)
                || permanent.getCard().hasType(CardType.BATTLE));
    }

    private boolean isCreatureOnBattlefield(GameData gameData, UUID permanentId) {
        Permanent permanent = gameQueryService.findPermanentById(gameData, permanentId);
        return permanent != null && gameQueryService.isCreature(gameData, permanent);
    }

    /** Whether Saving Grace-style shields redirect this noncombat damage to a permanent of the protected player. */
    public boolean hasTurnDamageRedirectToCreatureForPermanent(GameData gameData, UUID protectedPlayerId,
                                                               UUID damagedPermanentId) {
        if (protectedPlayerId == null) return false;
        return gameData.turnDamageRedirectToCreatureShields.stream()
                .anyMatch(shield -> {
                    if (!shield.protectedPlayerId().equals(protectedPlayerId) || shield.combatOnly()
                            || shield.damageSourceId() != null || !shield.includeControlledPermanents()) {
                        return false;
                    }
                    if (shield.controlledCreaturesOnly()) {
                        Permanent damaged = gameQueryService.findPermanentById(gameData, damagedPermanentId);
                        if (damaged == null || !gameQueryService.isCreature(gameData, damaged)) return false;
                    }
                    if (shield.redirectTargetCreatureId().equals(damagedPermanentId)) return false;
                    Permanent target = gameQueryService.findPermanentById(gameData, shield.redirectTargetCreatureId());
                    return target != null && (gameQueryService.isCreature(gameData, target)
                            || shield.allowsPlaneswalker() && gameQueryService.isPlaneswalker(gameData, target));
                });
    }

    /** The permanents (Palisade Giant) that absorb damage dealt to another permanent of their controller. */
    public List<Permanent> staticPermanentDamageRedirectSources(GameData gameData, UUID protectedPlayerId,
                                                               UUID damagedPermanentId) {
        if (protectedPlayerId == null || damagedPermanentId == null) return List.of();
        return gameData.playerBattlefields.getOrDefault(protectedPlayerId, List.of()).stream()
                .filter(permanent -> !permanent.getId().equals(damagedPermanentId)
                        && !gameQueryService.hasLostAllAbilities(gameData, permanent)
                        && gameQueryService.getActiveStaticEffects(gameData, permanent).stream()
                        .anyMatch(effect -> effect instanceof RedirectPlayerDamageToSelfEffect redirect
                                && redirect.includeOtherPermanents()))
                .toList();
    }

    /**
     * The permanents that would be dealt damage {@code playerId} would be dealt by this source
     * instead, each its own redirection effect for the player to order (CR 616.1): an enchanted
     * creature whose Aura takes its controller's damage, a permanent whose static ability takes
     * that player's damage ("dealt to this creature instead", Palisade Giant, Veteran Bodyguard),
     * and for combat damage from an unblocked creature the untapped guards (Weathered Bodyguards)
     * and the creature Kjeldoran Royal Guard's ability chose. A destination that can no longer be
     * dealt the damage is left out (CR 614.9).
     */
    public List<Permanent> playerDamageAbsorbers(GameData gameData, UUID playerId, StackEntry sourceEntry,
                                                 Card sourceCard, UUID sourcePermanentId, boolean combat) {
        if (playerId == null) return List.of();
        Permanent sourcePermanent = sourcePermanentId == null
                ? null : gameQueryService.findPermanentById(gameData, sourcePermanentId);
        boolean unblockedSource = combat && isUnblockedCreatureSource(gameData, sourcePermanentId);
        Set<Permanent> absorbers = new LinkedHashSet<>();
        absorbers.addAll(gameQueryService.findEnchantedCreaturesByAuraEffect(
                gameData, playerId, RedirectPlayerDamageToEnchantedCreatureEffect.class));
        if (unblockedSource && gameData.combatDamageRedirectTarget != null
                && playerId.equals(gameData.combatDamageRedirectPlayer)) {
            Permanent chosen = gameQueryService.findPermanentById(gameData, gameData.combatDamageRedirectTarget);
            if (chosen != null) absorbers.add(chosen);
        }
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(playerId, List.of())) {
            if (gameQueryService.hasLostAllAbilities(gameData, permanent)) continue;
            for (CardEffect effect : gameQueryService.getActiveStaticEffects(gameData, permanent)) {
                if (effect instanceof RedirectUnblockedCombatDamageToSelfEffect
                        && unblockedSource && !permanent.isTapped()) {
                    absorbers.add(permanent);
                } else if (effect instanceof RedirectPlayerDamageToSelfEffect redirect
                        && !(redirect.requiresUntapped() && permanent.isTapped())
                        && (!redirect.onlyFromUnblockedCreatures() || !permanent.isTapped()
                        && gameQueryService.isCreature(gameData, permanent)
                        && isUnblockedCreatureSource(gameData, sourcePermanentId))
                        && gameQueryService.matchesDamageSourcePredicate(
                        gameData, sourceEntry, sourceCard, sourcePermanent, redirect.sourcePredicate())) {
                    absorbers.add(permanent);
                }
            }
        }
        absorbers.removeIf(absorber -> !isRedirectDestination(gameData, absorber.getId()));
        return List.copyOf(absorbers);
    }

    /** Deals damage to {@code absorber} instead of the damaged permanent (Palisade Giant). */
    public int applyStaticPermanentDamageRedirectToSelf(GameData gameData, UUID protectedPlayerId, int damage,
                                                        Permanent absorber) {
        if (damage <= 0) return damage;
        gameData.pendingSourceRedirectDamage.add(
                new SourceDamageRedirectShield(protectedPlayerId, null, damage, absorber.getId()));
        return 0;
    }

    /** Whether Ascent of the Worthy redirects damage dealt to {@code damagedCreature}. */
    public boolean hasCreatureControllerDamageRedirectUntilNextTurn(GameData gameData, UUID protectedPlayerId,
                                                                    Permanent damagedCreature) {
        if (protectedPlayerId == null || damagedCreature == null
                || !gameQueryService.isCreature(gameData, damagedCreature)) return false;
        return gameData.creatureControllerDamageRedirectShields.stream()
                .anyMatch(shield -> protectedPlayerId.equals(shield.protectedPlayerId())
                        && !damagedCreature.getId().equals(shield.redirectTargetCreatureId())
                        && isCreatureOnBattlefield(gameData, shield.redirectTargetCreatureId()));
    }

    /** Whether Treacherous Link sends damage dealt to {@code target} to its controller. */
    public boolean hasEnchantedCreatureDamageRedirectToController(GameData gameData, Permanent target) {
        return target != null && gameQueryService.isCreature(gameData, target)
                && gameQueryService.hasAuraWithEffect(gameData, target, RedirectAllDamageToEnchantedCreatureControllerEffect.class)
                && gameQueryService.findPermanentController(gameData, target.getId()) != null;
    }

    /** Whether Glarecaster's shared shield redirects the next damage dealt to {@code permanentId}. */
    public boolean hasSourcePermanentAndControllerNextDamageRedirectToPermanent(GameData gameData, UUID permanentId) {
        return permanentId != null && gameData.sourcePermanentAndControllerNextDamageRedirectShields.stream()
                .anyMatch(shield -> permanentId.equals(shield.protectedPermanentId())
                        && isRedirectDestination(gameData, shield.redirectTargetId()));
    }

    /** Whether Oracle's Attendants-style shields redirect damage from {@code sourceId} to {@code permanentId}. */
    public boolean hasCreatureRedirectShield(GameData gameData, UUID permanentId, UUID sourceId, boolean combatDamage) {
        return hasCreatureRedirectShield(gameData, permanentId, sourceId, combatDamage, false);
    }

    /**
     * Whether a creature redirect shield covers the damage; {@code anySourceNextEvent} selects the
     * next-event shields that match any source (Mirrorwood Treefolk), which redirect the whole damage
     * event rather than one source's part of it, or, when false, every other shield.
     */
    public boolean hasCreatureRedirectShield(GameData gameData, UUID permanentId, UUID sourceId, boolean combatDamage,
                                             boolean anySourceNextEvent) {
        if (permanentId == null) return false;
        return gameData.creatureDamageRedirectShields.stream()
                .anyMatch(shield -> shield.protectedPermanentId().equals(permanentId)
                        && !permanentId.equals(shield.redirectTargetId())
                        && isAnySourceNextEvent(shield) == anySourceNextEvent
                        && (!shield.combatOnly() || combatDamage)
                        && (shield.damageSourceId() == null || shield.damageSourceId().equals(sourceId))
                        && isRedirectDestination(gameData, shield.redirectTargetId())
                        && (gameData.playerIds.contains(shield.redirectTargetId())
                        ? true
                        : (!shield.isUnlimited() || gameQueryService.isCreature(gameData,
                        gameQueryService.findPermanentById(gameData, shield.redirectTargetId())))));
    }

    /** Whether Shadowbane's shield covers the next damage from {@code sourceId} to this controller's creature. */
    public boolean hasControllerCreaturesNextSourceDamageShield(GameData gameData, UUID creatureControllerId,
                                                                UUID sourceId, boolean combatDamage) {
        if (!gameQueryService.isDamagePreventable(gameData, combatDamage)
                || creatureControllerId == null || sourceId == null) return false;
        return java.util.stream.Stream.concat(gameData.playerSourceNextDamageShields.stream(),
                        gameData.playerSourceShieldsUsedInOpenEvent.stream())
                .anyMatch(shield -> shield.coversControlledCreatures()
                        && shield.playerId().equals(creatureControllerId)
                        && shieldMatchesSource(gameData, shield, sourceId, null));
    }

    /** Whether Swans of Bryn Argoll would prevent damage dealt to {@code target}. */
    public boolean hasSwansSourceControllerDraw(GameData gameData, Permanent target, boolean combatDamage) {
        return gameQueryService.isDamagePreventable(gameData, combatDamage)
                && target.getCard().getEffects(EffectSlot.STATIC).stream()
                .anyMatch(PreventDamageToSelfAndSourceControllerDrawsEffect.class::isInstance);
    }

    /** Vengeful Archon-style shields protecting a player from combat or noncombat damage. */
    public int applyPlayerRedirectShields(GameData gameData, UUID playerId, int damage, boolean combatDamage) {
        return applyRedirectShields(gameData, playerId, null, damage, false, combatDamage);
    }

    /** Fixed per-source prevention covering a player and their creatures, for damage from {@code damageSource}. */
    public int applyControllerAndCreaturesFixedPerSourceDamagePrevention(GameData gameData, UUID playerId,
                                                                          int damage, boolean combatDamage,
                                                                          Permanent damageSource) {
        return applyControllerAndCreaturesFixedPerSourceDamagePrevention(
                gameData, playerId, damage, combatDamage, damageSource, null);
    }

    /** Consumes the player's "prevent the next N combat damage" shield and returns what remains. */
    public int applyPlayerCombatDamagePreventionShield(GameData gameData, UUID playerId, int damage) {
        int combatShield = gameData.playerCombatDamagePreventionShields.getOrDefault(playerId, 0);
        if (combatShield <= 0 || damage <= 0) return damage;
        int prevented = Math.min(combatShield, damage);
        int remaining = combatShield - prevented;
        if (remaining == 0) {
            gameData.playerCombatDamagePreventionShields.remove(playerId);
        } else {
            gameData.playerCombatDamagePreventionShields.put(playerId, remaining);
        }
        notifyControllerDamagePrevented(gameData, playerId, prevented);
        return damage - prevented;
    }

    /** Prevents combat damage to a player and creates that many tokens from their token shield. */
    public int applyCombatDamagePreventionTokenShield(GameData gameData, UUID playerId, int damage) {
        CombatDamagePreventionTokenShield tokenShield = gameData.combatDamagePreventionTokenShields.get(playerId);
        if (tokenShield == null || damage <= 0) return damage;
        notifyControllerDamagePrevented(gameData, playerId, damage);
        permanentControlSupportProvider.getObject().applyCreateToken(
                gameData, playerId, tokenShield.token(), damage, tokenShield.sourceSetCode());
        return 0;
    }

    /** Whether the next combat damage from {@code sourceId} is dealt to a stored controller instead. */
    public boolean hasSourceNextCombatDamageToControllerShield(GameData gameData, UUID sourceId) {
        return sourceId != null && gameData.sourceNextCombatDamageToControllerShields.stream()
                .anyMatch(shield -> sourceId.equals(shield.sourcePermanentId())
                        && gameData.playerIds.contains(shield.controllerId()));
    }

    /** Whether Mirror Strike sends combat damage from {@code sourceId} to this player back to its controller. */
    public boolean hasTurnSourceDamageRedirectToController(GameData gameData, UUID protectedPlayerId, UUID sourceId) {
        if (protectedPlayerId == null || sourceId == null) return false;
        UUID controllerId = gameQueryService.findPermanentController(gameData, sourceId);
        return controllerId != null && gameData.playerIds.contains(controllerId)
                && !controllerId.equals(protectedPlayerId)
                && gameData.turnSourceDamageRedirectToControllerShields.stream()
                .anyMatch(shield -> shield.protectedPlayerId().equals(protectedPlayerId)
                        && shield.sourcePermanentId().equals(sourceId));
    }

    /** Whether Soltari Guerrillas sends the next combat damage from {@code sourceId} to a creature. */
    public boolean hasSourceNextCombatDamageToOpponentRedirect(GameData gameData, UUID sourceId) {
        return sourceId != null && gameData.sourceNextCombatDamageToOpponentRedirectShields.stream()
                .anyMatch(shield -> shield.sourcePermanentId().equals(sourceId)
                        && isCreatureOnBattlefield(gameData, shield.destinationPermanentId()));
    }

    /** Whether Saving Grace-style shields redirect damage to a permanent of the protected player. */
    public boolean hasTurnDamageRedirectToCreatureForPermanent(GameData gameData, UUID protectedPlayerId,
                                                               UUID damagedPermanentId, boolean combatDamage) {
        if (protectedPlayerId == null) return false;
        return gameData.turnDamageRedirectToCreatureShields.stream()
                .anyMatch(shield -> {
                    if (!shield.protectedPlayerId().equals(protectedPlayerId)
                            || (shield.combatOnly() && !combatDamage)
                            || shield.damageSourceId() != null || !shield.includeControlledPermanents()) {
                        return false;
                    }
                    if (shield.controlledCreaturesOnly()) {
                        Permanent damaged = gameQueryService.findPermanentById(gameData, damagedPermanentId);
                        if (damaged == null || !gameQueryService.isCreature(gameData, damaged)) return false;
                    }
                    if (shield.redirectTargetCreatureId().equals(damagedPermanentId)) return false;
                    Permanent target = gameQueryService.findPermanentById(gameData, shield.redirectTargetCreatureId());
                    return target != null && (gameQueryService.isCreature(gameData, target)
                            || shield.allowsPlaneswalker() && gameQueryService.isPlaneswalker(gameData, target));
                });
    }

    /** Whether Gloom Surgeon would prevent combat damage dealt to {@code target}. */
    public boolean hasPreventCombatDamageToSelfAndExile(GameData gameData, Permanent target) {
        return gameQueryService.isDamagePreventable(gameData, true)
                && gameQueryService.hasActiveStaticEffect(gameData, target,
                PreventCombatDamageToSelfAndExileFromLibraryEffect.class);
    }

    /** Whether Sokrates' dialogue replaces combat damage {@code source} would deal to a player. */
    public boolean hasSokraticDialogue(GameData gameData, Permanent source) {
        return source != null && gameQueryService.hasActiveStaticEffectIncludingGranted(
                gameData, source, PreventCombatDamageToPlayerAndDrawHalfEffect.class);
    }

    /**
     * Per-source prevention for a creature its controller protects, counting a player's division of
     * shared prevention among simultaneous combat recipients.
     */
    public int controllerAndCreaturesPerSourcePrevention(GameData gameData, UUID controllerId, int damage,
                                                         boolean combatDamage, Permanent damageSource,
                                                         Permanent protectedCreature) {
        if (combatDamage && damageSource != null) {
            return evaluatePerSourceControllerAndCreaturesDamagePrevention(
                    gameData, controllerId, damage, true, damageSource, protectedCreature);
        }
        return applyControllerAndCreaturesFixedPerSourceDamagePrevention(
                gameData, controllerId, damage, combatDamage, damageSource, protectedCreature);
    }
}
