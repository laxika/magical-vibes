package com.github.laxika.magicalvibes.service.cast;

import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.BestowCast;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.DiscardCardCastingCost;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ExileCardsFromHandCastingCost;
import com.github.laxika.magicalvibes.model.ExileTopCardsFromGraveyardCastingCost;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.LifeCastingCost;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.Emblem;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.GraveyardCast;
import com.github.laxika.magicalvibes.model.CounterType;
import com.github.laxika.magicalvibes.model.RemoveCountersFromControlledCreaturesCastingCost;
import com.github.laxika.magicalvibes.model.Zone;
import com.github.laxika.magicalvibes.model.SacrificePermanentsCost;
import com.github.laxika.magicalvibes.model.ReturnPermanentsCost;
import com.github.laxika.magicalvibes.model.RevealCardsFromHandCastingCost;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TapUntappedPermanentsCost;
import com.github.laxika.magicalvibes.model.effect.ActivatedAbilityCostIncreasingEffect;
import com.github.laxika.magicalvibes.model.effect.ActivatedAbilityCostReducingEffect;
import com.github.laxika.magicalvibes.model.effect.AdditionalSacrificePerManaSymbolTaxEffect;
import com.github.laxika.magicalvibes.model.effect.AlternativeCostForSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.CyclingCostReducingEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardActivatedAbilityCostReducingEffect;
import com.github.laxika.magicalvibes.model.effect.GlobalAttackCostEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseCostOfSpellsTargetingThisSpellEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseOpponentCostForTargetingControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseOpponentLifeCostForTargetingControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostIfTargetingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardCardTargetCostReductionEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOpponentCostForTargetingControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostIfTargetingEnchantedPlayerEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostIfTargetingStackEntryEffect;
import com.github.laxika.magicalvibes.model.effect.PerTargetCastCostReductionEffect;
import com.github.laxika.magicalvibes.model.effect.RequirePaymentToAttackEffect;
import com.github.laxika.magicalvibes.model.effect.RequirePhyrexianPaymentToAttackEffect;
import com.github.laxika.magicalvibes.model.effect.SharedColorDiscardAlternativeCostEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.cost.AdditionalSpellCostService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
import com.github.laxika.magicalvibes.service.target.TargetLegalityService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;
import java.util.UUID;

/**
 * Single source of truth for what a spell costs to cast: static cost increases/reductions
 * (dispatched to {@link CostModificationHandlerBean}s via the {@link CostModificationHandlerRegistry}),
 * targeting taxes, target-based reductions, alternative costs, and attack payment requirements.
 *
 * <p>Both the view side (playable-card previews in {@code GameActionAvailabilityService}) and the
 * validation/payment side ({@code SpellCastingService}) must go through this service so the UI
 * never advertises a different cost than the engine charges.
 */
@Component
@RequiredArgsConstructor
public class CastingCostService {

    private final CostModificationHandlerRegistry costModificationHandlerRegistry;
    private final CostModificationSupport support;
    private final GameQueryService gameQueryService;
    private final PredicateEvaluationService predicateEvaluationService;
    private final ConditionEvaluationService conditionEvaluationService;
    private final AdditionalSpellCostService additionalSpellCostService;
    private final AmountEvaluationService amountEvaluationService;
    private final TargetLegalityService targetLegalityService;

    /**
     * All cost-modifying static effects currently on the battlefield, in emblems, or among active
     * floating continuous effects that could affect spells cast by one player, pre-collected in a
     * single pass so per-card evaluation doesn't re-scan all permanents.
     */
    public record CostModifierSnapshot(List<CollectedCostModifier> modifiers) {
    }

    record CollectedCostModifier(CostModificationHandlerBean handler, CardEffect effect, CostModificationSource source) {
    }

    public CostModifierSnapshot buildCostModifierSnapshot(GameData gameData, UUID playerId) {
        List<CollectedCostModifier> modifiers = new ArrayList<>();
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(pid);
            if (battlefield == null) continue;
            for (Permanent perm : battlefield) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    CostModificationHandlerBean handler = costModificationHandlerRegistry.getBattlefieldHandler(effect);
                    if (handler != null) {
                        modifiers.add(new CollectedCostModifier(handler, effect, new CostModificationSource(perm, pid)));
                    }
                }
            }
        }
        for (Emblem emblem : List.copyOf(gameData.emblems)) {
            for (CardEffect effect : emblem.staticEffects()) {
                CostModificationHandlerBean handler = costModificationHandlerRegistry.getBattlefieldHandler(effect);
                if (handler != null) {
                    modifiers.add(new CollectedCostModifier(handler, effect,
                            new CostModificationSource(null, emblem.controllerId())));
                }
            }
        }
        synchronized (gameData.floatingEffects) {
            for (var floating : gameData.floatingEffects) {
                CostModificationHandlerBean handler =
                        costModificationHandlerRegistry.getBattlefieldHandler(floating.effect());
                if (handler != null) {
                    modifiers.add(new CollectedCostModifier(handler, floating.effect(),
                            new CostModificationSource(null, floating.controllerId())));
                }
            }
        }
        return new CostModifierSnapshot(modifiers);
    }

    /**
     * Net generic-mana adjustment to the given card's cast cost for this player: positive means
     * the spell costs more, negative means it costs less. Covers static increases (taxes) and
     * reductions from the battlefield plus reductions on the spell itself; does NOT include
     * targeting-dependent modifiers ({@link #getTargetingSubtypeTax},
     * {@link #computeTargetBasedCostReduction}).
     */
    public int getCastCostModifier(GameData gameData, UUID playerId, Card card) {
        return getCastCostModifier(gameData, playerId, card, buildCostModifierSnapshot(gameData, playerId), false);
    }

    public int getCastCostModifier(GameData gameData, UUID playerId, Card card, int xValue) {
        return getCastCostModifier(gameData, playerId, card, buildCostModifierSnapshot(gameData, playerId), false,
                xValue);
    }

    public int getCastCostModifier(GameData gameData, UUID playerId, Card card, CostModifierSnapshot snapshot) {
        return getCastCostModifier(gameData, playerId, card, snapshot, false);
    }

    public int getCastCostModifier(GameData gameData, UUID playerId, Card card,
                                   CostModifierSnapshot snapshot, int xValue) {
        return getCastCostModifier(gameData, playerId, card, snapshot, false, xValue);
    }

    public int getCastCostModifier(GameData gameData, UUID playerId, Card card, boolean flashbackCost) {
        return getCastCostModifier(gameData, playerId, card, buildCostModifierSnapshot(gameData, playerId), flashbackCost);
    }

    public int getCastCostModifier(GameData gameData, UUID playerId, Card card,
                                   boolean flashbackCost, int xValue) {
        return getCastCostModifier(gameData, playerId, card, buildCostModifierSnapshot(gameData, playerId),
                flashbackCost, xValue);
    }

    public int getCastCostModifier(GameData gameData, UUID playerId, Card card,
                                   CostModifierSnapshot snapshot, boolean flashbackCost) {
        return getCastCostModifier(gameData, playerId, card, snapshot, flashbackCost, 0);
    }

    public int getCastCostModifier(GameData gameData, UUID playerId, Card card,
                                   CostModifierSnapshot snapshot, boolean flashbackCost, int xValue) {
        CostModificationContext context = new CostModificationContext(gameData, playerId, card,
                flashbackCost, xValue);
        int delta = 0;
        List<CollectedCostModifier> afterOtherModifiers = new ArrayList<>();
        var exilePlayCostModifier = gameData.exilePlayCostModifiers.get(card.getId());
        if (exilePlayCostModifier != null
                && playerId.equals(exilePlayCostModifier.permittedPlayerId())
                && !playerId.equals(exilePlayCostModifier.sourceControllerId())) {
            delta += exilePlayCostModifier.amount();
        }
        for (CardEffect effect : card.getEffects(EffectSlot.STATIC)) {
            CostModificationHandlerBean handler = costModificationHandlerRegistry.getSpellSelfHandler(effect);
            if (handler != null) {
                if (handler.appliesAfterOtherCostModifiers()) {
                    afterOtherModifiers.add(new CollectedCostModifier(
                            handler, effect, CostModificationSource.SPELL_ITSELF));
                } else {
                    delta += handler.modifyCost(context, effect, CostModificationSource.SPELL_ITSELF);
                }
            }
        }
        for (CollectedCostModifier modifier : snapshot.modifiers()) {
            if (modifier.handler().appliesAfterOtherCostModifiers()) {
                afterOtherModifiers.add(modifier);
            } else {
                delta += modifier.handler().modifyCost(context, modifier.effect(), modifier.source());
            }
        }
        for (CollectedCostModifier modifier : afterOtherModifiers) {
            delta += modifier.handler().modifyCostAfterOtherModifiers(
                    context, modifier.effect(), modifier.source(), delta);
        }
        return delta;
    }

    public ManaCost applyColoredManaCostReductions(GameData gameData, UUID playerId, Card card,
                                                   ManaCost cost) {
        return applyColoredManaCostReductions(gameData, playerId, card, cost,
                buildCostModifierSnapshot(gameData, playerId), false);
    }

    public ManaCost applyColoredManaCostReductions(GameData gameData, UUID playerId, Card card,
                                                   ManaCost cost, boolean flashbackCost) {
        return applyColoredManaCostReductions(gameData, playerId, card, cost,
                buildCostModifierSnapshot(gameData, playerId), flashbackCost);
    }

    public ManaCost applyColoredManaCostReductions(GameData gameData, UUID playerId, Card card,
                                                   ManaCost cost, CostModifierSnapshot snapshot) {
        return applyColoredManaCostReductions(gameData, playerId, card, cost, snapshot, false);
    }

    public ManaCost applyColoredManaCostReductions(GameData gameData, UUID playerId, Card card,
                                                   ManaCost cost, CostModifierSnapshot snapshot,
                                                   boolean flashbackCost) {
        CostModificationContext context = new CostModificationContext(gameData, playerId, card, flashbackCost);
        ManaCost effectiveCost = cost;
        for (CardEffect effect : card.getEffects(EffectSlot.STATIC)) {
            CostModificationHandlerBean handler = costModificationHandlerRegistry.getSpellSelfHandler(effect);
            if (handler != null) {
                ManaCost reduction = handler.coloredManaCostReduction(
                        context, effect, CostModificationSource.SPELL_ITSELF);
                if (reduction != null) {
                    effectiveCost = handler.coloredReductionCanReduceGeneric(effect)
                            ? effectiveCost.reducedBy(reduction)
                            : effectiveCost.reducedByColoredOnly(reduction);
                }
            }
        }
        for (CollectedCostModifier modifier : snapshot.modifiers()) {
            ManaCost reduction = modifier.handler().coloredManaCostReduction(
                    context, modifier.effect(), modifier.source());
            if (reduction != null) {
                effectiveCost = modifier.handler().coloredReductionCanReduceGeneric(modifier.effect())
                        ? effectiveCost.reducedBy(reduction)
                        : effectiveCost.reducedByColoredOnly(reduction);
            }
        }
        return effectiveCost;
    }

    /**
     * Applies spell-self and battlefield reductions that remove colored mana symbols from a spell's
     * cost. Generic-only modifiers remain represented by {@link #getCastCostModifier}.
     */
    public ManaCost applyCastCostReductions(GameData gameData, UUID playerId, Card card,
                                            ManaCost cost) {
        return applyCastCostReductions(gameData, playerId, card, cost,
                buildCostModifierSnapshot(gameData, playerId), false);
    }

    public ManaCost applyCastCostReductions(GameData gameData, UUID playerId, Card card,
                                            ManaCost cost, CostModifierSnapshot snapshot,
                                            boolean flashbackCost) {
        if (cost == null) {
            return null;
        }
        CostModificationContext context = new CostModificationContext(gameData, playerId, card, flashbackCost);
        ManaCost reduced = cost;
        for (CardEffect effect : card.getEffects(EffectSlot.STATIC)) {
            CostModificationHandlerBean handler = costModificationHandlerRegistry.getSpellSelfHandler(effect);
            if (handler != null) {
                ManaCost reduction = handler.coloredManaCostReduction(context, effect, CostModificationSource.SPELL_ITSELF);
                if (reduction != null) {
                    reduced = reduced.reducedBy(reduction);
                }
            }
        }
        for (CollectedCostModifier modifier : snapshot.modifiers()) {
            ManaCost reduction = modifier.handler().coloredManaCostReduction(context, modifier.effect(), modifier.source());
            if (reduction != null) {
                reduced = reduced.reducedBy(reduction);
            }
        }
        return reduced;
    }

    /**
     * Net generic-mana adjustment to an optional buyback cost paid while casting {@code card}.
     * Positive means more expensive, negative means cheaper.
     */
    public int getBuybackCostModifier(GameData gameData, UUID playerId, Card card) {
        return getBuybackCostModifier(gameData, playerId, card, buildCostModifierSnapshot(gameData, playerId));
    }

    public int getBuybackCostModifier(GameData gameData, UUID playerId, Card card,
                                      CostModifierSnapshot snapshot) {
        CostModificationContext context = new CostModificationContext(gameData, playerId, card);
        int delta = 0;
        for (CollectedCostModifier modifier : snapshot.modifiers()) {
            delta += modifier.handler().modifyBuybackCost(context, modifier.effect(), modifier.source());
        }
        return delta;
    }

    /**
     * Computes the additional cost imposed by static effects that tax spells or abilities
     * targeting permanents with a specific subtype (e.g. Kopala, Warden of Waves).
     * The tax applies once per source permanent with the effect, regardless of how many
     * matching permanents are targeted.
     */
    public int getTargetingSubtypeTax(GameData gameData, UUID casterId, UUID targetId, List<UUID> targetIds) {
        return getTargetingSubtypeTax(gameData, casterId, targetId, targetIds, true);
    }

    public int getTargetingSubtypeTax(GameData gameData, UUID casterId, UUID targetId, List<UUID> targetIds,
                                      boolean activatedAbility) {
        Set<UUID> allTargetIds = new HashSet<>();
        if (targetId != null) allTargetIds.add(targetId);
        if (targetIds != null) allTargetIds.addAll(targetIds);
        if (allTargetIds.isEmpty()) return 0;

        int tax = 0;
        for (UUID controllerId : gameData.orderedPlayerIds) {
            if (controllerId.equals(casterId)) continue;
            List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof IncreaseOpponentCostForTargetingControlledPermanentEffect taxEffect) {
                        if (activatedAbility && !taxEffect.taxesActivatedAbilities()) continue;
                        for (UUID tid : allTargetIds) {
                            Permanent targetPerm = gameQueryService.findPermanentById(gameData, tid);
                            if (targetPerm != null) {
                                UUID targetController = gameQueryService.findPermanentController(gameData, tid);
                                if (controllerId.equals(targetController)
                                        && predicateEvaluationService.matchesPermanentPredicate(
                                                targetPerm, taxEffect.predicate(),
                                                FilterContext.of(gameData).withSourcePermanentSnapshot(perm))) {
                                    tax += taxEffect.amount();
                                    break;
                                }
                            }
                        }
                    }
                }
            }
        }
        return tax;
    }

    /**
     * Computes the additional life cost imposed by static effects that tax opponent spells
     * targeting matching permanents controlled by the effect's controller.
     * The tax applies once per source permanent, regardless of how many matching permanents
     * are targeted.
     */
    public int getTargetingLifeTax(GameData gameData, UUID casterId, UUID targetId, List<UUID> targetIds) {
        Set<UUID> allTargetIds = new HashSet<>();
        if (targetId != null) allTargetIds.add(targetId);
        if (targetIds != null) allTargetIds.addAll(targetIds);
        if (allTargetIds.isEmpty()) return 0;

        int tax = 0;
        for (UUID controllerId : gameData.orderedPlayerIds) {
            if (controllerId.equals(casterId)) continue;
            List<Permanent> bf = gameData.playerBattlefields.get(controllerId);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (!(effect instanceof IncreaseOpponentLifeCostForTargetingControlledPermanentEffect taxEffect)) {
                        continue;
                    }
                    for (UUID tid : allTargetIds) {
                        Permanent targetPerm = gameQueryService.findPermanentById(gameData, tid);
                        if (targetPerm != null) {
                            UUID targetController = gameQueryService.findPermanentController(gameData, tid);
                            if (controllerId.equals(targetController)
                                    && predicateEvaluationService.matchesPermanentPredicate(
                                    targetPerm, taxEffect.predicate(),
                                    FilterContext.of(gameData).withSourcePermanentSnapshot(perm))) {
                                tax += taxEffect.amount();
                                break;
                            }
                        }
                    }
                }
            }
        }
        return tax;
    }

    /**
     * Net generic-mana adjustment for a spell's chosen targets, including both targeting taxes and
     * reductions granted by permanents being targeted.
     */
    public int getTargetingSpellCostModifier(GameData gameData, UUID casterId, UUID targetId,
                                             List<UUID> targetIds) {
        return getTargetingSubtypeTax(gameData, casterId, targetId, targetIds, false)
                - getTargetingControlledPermanentReduction(gameData, casterId, targetId, targetIds);
    }

    private int getTargetingControlledPermanentReduction(GameData gameData, UUID casterId, UUID targetId,
                                                         List<UUID> targetIds) {
        Set<UUID> allTargetIds = new HashSet<>();
        if (targetId != null) allTargetIds.add(targetId);
        if (targetIds != null) allTargetIds.addAll(targetIds);
        if (allTargetIds.isEmpty()) return 0;

        int reduction = 0;
        for (UUID controllerId : gameData.orderedPlayerIds) {
            if (controllerId.equals(casterId)) continue;
            List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
            if (battlefield == null) continue;
            for (Permanent source : battlefield) {
                for (CardEffect effect : source.getCard().getEffects(EffectSlot.STATIC)) {
                    if (!(effect instanceof ReduceOpponentCostForTargetingControlledPermanentEffect reduceEffect)) {
                        continue;
                    }
                    for (UUID target : allTargetIds) {
                        Permanent targetPermanent = gameQueryService.findPermanentById(gameData, target);
                        if (targetPermanent == null) continue;
                        UUID targetController = gameQueryService.findPermanentController(gameData, target);
                        if (controllerId.equals(targetController)
                                && predicateEvaluationService.matchesPermanentPredicate(
                                targetPermanent, reduceEffect.predicate(),
                                FilterContext.of(gameData).withSourcePermanentSnapshot(source))) {
                            reduction += reduceEffect.amount();
                            break;
                        }
                    }
                }
            }
        }
        return reduction;
    }

    /**
     * Extra generic mana imposed by {@link IncreaseCostOfSpellsTargetingThisSpellEffect} on the
     * cards of the targeted stack entries (e.g. Kaervek's Torch taxes spells that target it {2}).
     * Symmetric — applies to both players — and to spells only, never activated abilities.
     */
    public int getTargetingStackEntryTax(GameData gameData, UUID targetId, List<UUID> targetIds) {
        Set<UUID> allTargetIds = new HashSet<>();
        if (targetId != null) allTargetIds.add(targetId);
        if (targetIds != null) allTargetIds.addAll(targetIds);

        int tax = 0;
        for (UUID tid : allTargetIds) {
            StackEntry entry = gameQueryService.findStackEntryByCardId(gameData, tid);
            if (entry == null || entry.getCard() == null) continue;
            for (CardEffect effect : entry.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof IncreaseCostOfSpellsTargetingThisSpellEffect taxEffect) {
                    tax += taxEffect.amount();
                }
            }
        }
        return tax;
    }

    /**
     * Extra generic mana required to activate an activated ability of {@code sourcePermanent},
     * summed over every {@link ActivatedAbilityCostIncreasingEffect} on any battlefield whose
     * predicate matches the source (e.g. Gloom taxes white enchantments' abilities {3} more).
     * Symmetric — applies regardless of who controls the source or the taxing permanent.
     */
    public int getActivatedAbilityActivationTax(GameData gameData, Permanent sourcePermanent) {
        int tax = 0;
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof ActivatedAbilityCostIncreasingEffect taxEffect
                            && predicateEvaluationService.matchesPermanentPredicate(
                                    sourcePermanent, taxEffect.affectedPermanents(),
                                    FilterContext.of(gameData)
                                            .withSourceCardId(perm.getOriginalCard().getId())
                                            .withSourceControllerId(pid))) {
                        tax += taxEffect.additionalGenericCost();
                    }
                }
            }
        }
        return tax;
    }

    /**
     * Generic mana removed from an activated ability's cost by controller-scoped reductions on the
     * activating player's battlefield. Symmetric reductions are collected by
     * {@link #getActivatedAbilityActivationCostReduction(GameData, Permanent, ActivatedAbility)}.
     * Only the generic portion of the ability's own mana cost is reducible; additional costs are
     * handled separately by their respective payment rules.
     */
    public int getActivatedAbilityCostReduction(GameData gameData, UUID activatingPlayerId,
                                                Permanent sourcePermanent, ActivatedAbility ability) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(activatingPlayerId);
        if (battlefield == null) return 0;

        int reduction = 0;
        for (Permanent permanent : battlefield) {
            for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof ActivatedAbilityCostReducingEffect reducer
                        && !reducer.appliesSymmetrically()
                        && reducer.appliesTo(ability)
                        && predicateEvaluationService.matchesPermanentPredicate(
                                sourcePermanent, reducer.affectedPermanents(),
                                FilterContext.of(gameData)
                                        .withSourceCardId(permanent.getOriginalCard().getId())
                                        .withSourceControllerId(activatingPlayerId))) {
                    reduction += reducer.genericCostReduction();
                }
            }
        }
        return reduction;
    }

    /**
     * Generic mana removed from the activation cost of {@code sourcePermanent}'s activated ability,
     * summed over every matching reduction effect on every battlefield. Symmetric — applies
     * regardless of who controls the source or the reducing permanent.
     */
    public int getActivatedAbilityActivationCostReduction(GameData gameData, Permanent sourcePermanent) {
        return getActivatedAbilityActivationCostReduction(gameData, sourcePermanent, null);
    }

    /**
     * Generic mana removed from cycling abilities activated by {@code activatingPlayerId}.
     */
    public int getCyclingAbilityCostReduction(GameData gameData, UUID activatingPlayerId) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(activatingPlayerId);
        if (battlefield == null) return 0;

        int reduction = 0;
        for (Permanent permanent : battlefield) {
            for (CardEffect effect : permanent.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof CyclingCostReducingEffect reducer) {
                    reduction += reducer.genericCostReduction();
                }
            }
        }
        return reduction;
    }

    public int getActivatedAbilityActivationCostReduction(GameData gameData, Permanent sourcePermanent,
                                                          ActivatedAbility ability) {
        int reduction = 0;
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof ActivatedAbilityCostReducingEffect reducingEffect
                            && reducingEffect.appliesSymmetrically()
                            && (ability == null || reducingEffect.appliesTo(ability))
                            && predicateEvaluationService.matchesPermanentPredicate(
                                    sourcePermanent, reducingEffect.affectedPermanents(),
                                    FilterContext.of(gameData)
                                            .withSourceCardId(perm.getOriginalCard().getId())
                                            .withSourceControllerId(pid))) {
                        reduction += reducingEffect.genericCostReduction();
                    }
                }
            }
        }
        return reduction;
    }

    /**
     * Extra sacrifice-of-matching-permanent requirement imposed by battlefield taxes such as
     * Drought: one sacrifice per matching mana symbol in {@code cost}, summed across every
     * {@link AdditionalSacrificePerManaSymbolTaxEffect} on any battlefield. Symmetric.
     * {@code forSpell}/{@code forAbility} select which tax flags apply.
     */
    public ImposedSacrificeRequirement getImposedSacrificeRequirement(
            GameData gameData, ManaCost cost, boolean forSpell, boolean forAbility) {
        if (cost == null) {
            return ImposedSacrificeRequirement.none();
        }
        int total = 0;
        PermanentPredicate filter = null;
        String description = null;
        for (UUID pid : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(pid);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (!(effect instanceof AdditionalSacrificePerManaSymbolTaxEffect tax)) continue;
                    if (forSpell && !tax.taxesSpells()) continue;
                    if (forAbility && !tax.taxesActivatedAbilities()) continue;
                    int symbols = cost.countColorSymbols(tax.color());
                    if (symbols <= 0) continue;
                    total += symbols;
                    if (filter == null) {
                        filter = tax.sacrificeFilter();
                        description = tax.description();
                    }
                }
            }
        }
        if (total <= 0 || filter == null) {
            return ImposedSacrificeRequirement.none();
        }
        return new ImposedSacrificeRequirement(total, filter, description != null ? description : "a permanent");
    }

    /** Spell-cast convenience for {@link #getImposedSacrificeRequirement}. */
    public ImposedSacrificeRequirement getImposedSacrificeRequirementForSpell(GameData gameData, Card spell) {
        return getImposedSacrificeRequirement(gameData, spell.getParsedManaCost(), true, false);
    }

    /** Ability-activation convenience for {@link #getImposedSacrificeRequirement}. */
    public ImposedSacrificeRequirement getImposedSacrificeRequirementForAbility(GameData gameData, String abilityManaCost) {
        if (abilityManaCost == null || abilityManaCost.isBlank()) {
            return ImposedSacrificeRequirement.none();
        }
        return getImposedSacrificeRequirement(gameData, new ManaCost(abilityManaCost), false, true);
    }

    /**
     * True when the player controls enough permanents to pay every imposed per-symbol sacrifice
     * tax for casting {@code card} (in addition to any SPELL-slot additional costs).
     */
    public boolean canPayImposedSacrificeTax(GameData gameData, UUID playerId, Card card) {
        ImposedSacrificeRequirement req = getImposedSacrificeRequirementForSpell(gameData, card);
        if (req.isEmpty()) return true;
        List<Permanent> battlefield = gameData.playerBattlefields.getOrDefault(playerId, List.of());
        long matching = battlefield.stream()
                .filter(p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, req.filter()))
                .count();
        return matching >= req.count();
    }

    /**
     * Battlefield-imposed additional sacrifice cost: sacrifice {@code count} permanents matching
     * {@code filter} (e.g. Drought's "Sacrifice a Swamp" per black mana symbol).
     */
    public record ImposedSacrificeRequirement(int count, PermanentPredicate filter, String description) {
        public static ImposedSacrificeRequirement none() {
            return new ImposedSacrificeRequirement(0, null, null);
        }

        public boolean isEmpty() {
            return count <= 0 || filter == null;
        }
    }

    /**
     * Generic mana removed from the cost of activating an activated ability of {@code graveyardCard}
     * from {@code activatingPlayerId}'s graveyard, summed over every
     * {@link GraveyardActivatedAbilityCostReducingEffect} that player controls whose card predicate
     * matches the card (e.g. Embalmer's Tools makes creature cards' graveyard abilities cost {1} less).
     * Controller-scoped — "in your graveyard" benefits only the effect's own controller, and the
     * activating player is always the graveyard's owner, so only their battlefield is scanned.
     */
    public int getGraveyardActivatedAbilityCostReduction(GameData gameData, UUID activatingPlayerId, Card graveyardCard) {
        List<Permanent> battlefield = gameData.playerBattlefields.get(activatingPlayerId);
        if (battlefield == null) return 0;
        int reduction = 0;
        for (Permanent perm : battlefield) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof GraveyardActivatedAbilityCostReducingEffect reducer
                        && predicateEvaluationService.matchesCardPredicate(
                                graveyardCard, reducer.affectedGraveyardCards(), null)) {
                    reduction += reducer.genericCostReduction();
                }
            }
        }
        return reduction;
    }

    public boolean hasAlternativeZeroCostFromBattlefield(GameData gameData, UUID playerId, Card card) {
        return hasAlternativeZeroCostFromBattlefield(gameData, playerId, card, true);
    }

    /**
     * Legacy boolean overload of {@link #hasAlternativeZeroCostFromBattlefield(GameData, UUID, Card, Zone)}.
     * {@code false} represents an exile cast; use the {@link Zone} overload for other source zones.
     */
    public boolean hasAlternativeZeroCostFromBattlefield(GameData gameData, UUID playerId, Card card, boolean fromHand) {
        return hasAlternativeZeroCostFromBattlefield(gameData, playerId, card,
                fromHand ? Zone.HAND : Zone.EXILE);
    }

    public boolean hasAlternativeZeroCostFromBattlefield(GameData gameData, UUID playerId, Card card,
                                                         Zone sourceZone) {
        return findFreeCastSource(gameData, playerId, card, sourceZone) != null;
    }

    /**
     * True when a battlefield permanent offers the shared-color discard alternative for the spell.
     * The source is symmetric: the effect is available to every spell controller.
     */
    public boolean hasSharedColorDiscardAlternativeCostFromBattlefield(GameData gameData, UUID playerId, Card card) {
        if (card.getColors() == null || card.getColors().isEmpty()) {
            return false;
        }
        for (UUID controllerId : gameData.orderedPlayerIds) {
            List<Permanent> battlefield = gameData.playerBattlefields.get(controllerId);
            if (battlefield == null) continue;
            for (Permanent permanent : battlefield) {
                if (permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(SharedColorDiscardAlternativeCostEffect.class::isInstance)) {
                    return true;
                }
            }
        }
        return false;
    }

    /**
     * True when the player has a card in hand other than the spell that shares a color with it.
     */
    public boolean canPaySharedColorDiscardAlternativeCostFromBattlefield(GameData gameData, UUID playerId, Card card) {
        if (!hasSharedColorDiscardAlternativeCostFromBattlefield(gameData, playerId, card)) {
            return false;
        }
        List<Card> hand = gameData.playerHands.getOrDefault(playerId, List.of());
        for (Card candidate : hand) {
            if (candidate.getId().equals(card.getId())) {
                continue;
            }
            if (candidate.getColors() != null && candidate.getColors().stream().anyMatch(card.getColors()::contains)) {
                return true;
            }
        }
        return false;
    }

    /**
     * Validates the selected pre-removal hand index and returns its index in the current hand.
     * This works both before and after the spell itself has been removed from the hand.
     */
    public int validateSharedColorDiscardAlternativeCost(GameData gameData, UUID playerId, Card card,
                                                         Integer discardHandCardIndex, int spellCardIndex) {
        if (!hasSharedColorDiscardAlternativeCostFromBattlefield(gameData, playerId, card)) {
            throw new IllegalStateException("Shared-color discard alternative cost is not available");
        }
        List<Card> hand = gameData.playerHands.get(playerId);
        if (discardHandCardIndex == null || hand == null) {
            throw new IllegalStateException("Must discard a card sharing a color with " + card.getName());
        }
        boolean spellStillInHand = spellCardIndex >= 0 && spellCardIndex < hand.size()
                && hand.get(spellCardIndex).getId().equals(card.getId());
        int effectiveIndex = !spellStillInHand && discardHandCardIndex > spellCardIndex
                ? discardHandCardIndex - 1 : discardHandCardIndex;
        int selectedIndex = spellStillInHand ? discardHandCardIndex : effectiveIndex;
        if (selectedIndex < 0 || selectedIndex >= hand.size()) {
            throw new IllegalStateException("Must discard a card sharing a color with " + card.getName());
        }
        Card selected = hand.get(selectedIndex);
        if (selected.getId().equals(card.getId())
                || selected.getColors() == null
                || selected.getColors().stream().noneMatch(card.getColors()::contains)) {
            throw new IllegalStateException("Discarded card must share a color with " + card.getName());
        }
        return selectedIndex;
    }

    /**
     * Applies a battlefield "you may pay {0}" alternative cost to the card being cast and, for a
     * once-each-turn source (As Foretold), records that the source has been used this turn so it
     * offers no further free cast until the next turn. Returns whether a free cast applied.
     * Non-mutating callers (playability previews, validation) must use
     * {@link #hasAlternativeZeroCostFromBattlefield} instead.
     */
    public boolean consumeFreeCastFromBattlefield(GameData gameData, UUID playerId, Card card) {
        return consumeFreeCastFromBattlefield(gameData, playerId, card, true);
    }

    /**
     * Legacy boolean overload of {@link #consumeFreeCastFromBattlefield(GameData, UUID, Card, Zone)}.
     * {@code false} represents an exile cast; use the {@link Zone} overload for other source zones.
     */
    public boolean consumeFreeCastFromBattlefield(GameData gameData, UUID playerId, Card card, boolean fromHand) {
        return consumeFreeCastFromBattlefield(gameData, playerId, card,
                fromHand ? Zone.HAND : Zone.EXILE);
    }

    public boolean consumeFreeCastFromBattlefield(GameData gameData, UUID playerId, Card card, Zone sourceZone) {
        FreeCastSource source = findFreeCastSource(gameData, playerId, card, sourceZone);
        if (source == null) return false;
        if (source.effect().oncePerTurn()) {
            gameData.freeCastPermanentUsedThisTurn.add(source.permanent().getId());
        }
        return true;
    }

    private record FreeCastSource(Permanent permanent, AlternativeCostForSpellsEffect effect) {
    }

    /**
     * A permanent whose {@link AlternativeCostForSpellsEffect} offers the player a zero
     * alternative cost currently applicable to {@code card}: the filter matches, any counter-based
     * mana-value cap is satisfied, and a once-each-turn source has not yet been used this turn. An
     * unlimited source (e.g. Rooftop Storm) is preferred over a once-each-turn source (As Foretold)
     * so the limited use is not spent while a free one is available. A hand-only source (Omniscience)
     * is skipped entirely when the spell is not being cast from hand. The player's emblems are
     * consulted first; permanents are then searched across every battlefield, but an opponent's
     * source only counts when it applies to all players (Aluren).
     */
    private FreeCastSource findFreeCastSource(GameData gameData, UUID playerId, Card card, Zone sourceZone) {
        FreeCastSource emblemSource = findEmblemFreeCastSource(gameData, playerId, card, sourceZone);
        if (emblemSource != null) return emblemSource;

        FreeCastSource oncePerTurnFallback = null;
        for (UUID ownerId : gameData.orderedPlayerIds) {
            List<Permanent> bf = gameData.playerBattlefields.get(ownerId);
            if (bf == null) continue;
            for (Permanent perm : bf) {
                for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                    if (effect instanceof AlternativeCostForSpellsEffect altCost
                            && (altCost.appliesToAllPlayers() || ownerId.equals(playerId))
                            && (!altCost.controllerTurnOnly() || playerId.equals(gameData.activePlayerId))
                            && new ManaCost(altCost.manaCostFor(card.getManaValue())).getManaValue() == 0
                            && (sourceZone == Zone.HAND || !altCost.fromHandOnly())
                            && (altCost.allowedZones() == null || altCost.allowedZones().contains(sourceZone))
                            && predicateEvaluationService.matchesCardPredicate(card, altCost.filter(), null)
                            && manaValueCapSatisfied(perm, card, altCost)
                            && !(altCost.oncePerTurn() && gameData.freeCastPermanentUsedThisTurn.contains(perm.getId()))) {
                        if (!altCost.oncePerTurn()) {
                            return new FreeCastSource(perm, altCost);
                        }
                        if (oncePerTurnFallback == null) {
                            oncePerTurnFallback = new FreeCastSource(perm, altCost);
                        }
                    }
                }
            }
        }
        return oncePerTurnFallback;
    }

    /**
     * The emblem counterpart of {@link #findFreeCastSource}: an emblem the player has whose
     * {@link AlternativeCostForSpellsEffect} offers a zero alternative cost for {@code card}
     * ("You may cast spells from your hand without paying their mana costs." — Tamiyo, Field
     * Researcher's −7). An emblem is not a permanent, so it carries no counters and cannot be
     * "used this turn"; the counter-capped and once-each-turn variants are therefore skipped rather
     * than silently treated as unlimited. The returned source has a null permanent — every caller
     * only consults {@code effect()}, and {@code consumeFreeCastFromBattlefield} touches the
     * permanent only on the once-each-turn path this can never take.
     */
    private FreeCastSource findEmblemFreeCastSource(GameData gameData, UUID playerId, Card card, Zone sourceZone) {
        for (Emblem emblem : List.copyOf(gameData.emblems)) {
            if (!playerId.equals(emblem.controllerId())) continue;
            for (CardEffect effect : emblem.staticEffects()) {
                if (effect instanceof AlternativeCostForSpellsEffect altCost
                        && altCost.manaValueCapCounter() == null
                        && !altCost.oncePerTurn()
                        && new ManaCost(altCost.manaCostFor(card.getManaValue())).getManaValue() == 0
                        && (sourceZone == Zone.HAND || !altCost.fromHandOnly())
                        && (altCost.allowedZones() == null || altCost.allowedZones().contains(sourceZone))
                        && predicateEvaluationService.matchesCardPredicate(card, altCost.filter(), null)) {
                    return new FreeCastSource(null, altCost);
                }
            }
        }
        return null;
    }

    private boolean manaValueCapSatisfied(Permanent perm, Card card, AlternativeCostForSpellsEffect altCost) {
        if (altCost.manaValueCapCounter() == null) return true;
        return card.getManaValue() <= perm.getCounterCount(altCost.manaValueCapCounter());
    }

    /**
     * Returns true if any permanent the player controls provides a non-zero alternative mana cost
     * for the given card AND the player's mana pool can pay that alternative cost (plus any modifiers).
     */
    public boolean canAffordAlternativeCostFromBattlefield(GameData gameData, UUID playerId, Card card, ManaPool pool, int additionalCost) {
        return findAffordableAlternativeCostFromBattlefield(gameData, playerId, card, pool, additionalCost) != null;
    }

    /**
     * Returns the mana cost string of an affordable non-zero alternative cost from the battlefield,
     * or null if none exists or none is affordable.
     */
    public String findAffordableAlternativeCostFromBattlefield(GameData gameData, UUID playerId, Card card, ManaPool pool, int additionalCost) {
        List<Permanent> bf = gameData.playerBattlefields.get(playerId);
        if (bf == null) return null;
        for (Permanent perm : bf) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof AlternativeCostForSpellsEffect altCost
                        && predicateEvaluationService.matchesCardPredicate(card, altCost.filter(), null)) {
                    String alternativeCostString = altCost.manaCostFor(card.getManaValue());
                    ManaCost alternativeManaCost = applyColoredManaCostReductions(
                            gameData, playerId, card, new ManaCost(alternativeCostString));
                    if (alternativeManaCost.getManaValue() > 0 && alternativeManaCost.canPay(pool, additionalCost)) {
                        return alternativeCostString;
                    }
                }
            }
        }
        return null;
    }

    /**
     * Returns true if the card's {@link AlternateHandCast} casting option (e.g. Demon of Death's
     * Gate) exists and all of its costs (life, sacrifices, taps, mana) are currently payable.
     */
    public boolean canPayAlternateHandCast(GameData gameData, UUID playerId, Card card) {
        var altCastOpt = card.getCastingOption(AlternateHandCast.class);
        if (altCastOpt.isEmpty()) {
            var bestowCast = card.getCastingOption(BestowCast.class);
            if (bestowCast.isEmpty()) return false;
            return bestowCast.get().getCost(ManaCastingCost.class)
                    .map(cost -> applyColoredManaCostReductions(gameData, playerId, card,
                            new ManaCost(cost.manaCost())).canPay(
                            gameData.playerManaPools.get(playerId), getCastCostModifier(gameData, playerId, card)))
                    .orElse(false);
        }
        AlternateHandCast altCast = altCastOpt.get();
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);

        // Prowl: the alternate cost is only available if the caster dealt combat damage to a
        // player this turn with a creature of the required subtype.
        if (!altCast.prowlDamageSubtypes().isEmpty() && !prowlConditionMet(gameData, playerId, altCast.prowlDamageSubtypes())) {
            return false;
        }

        // General availability gate (e.g. Qasali Ambusher's "if a creature is attacking you and you
        // control a Forest and a Plains").
        if (altCast.availabilityCondition() != null
                && !conditionEvaluationService.isMet(gameData, altCast.availabilityCondition(),
                        ConditionContext.forCasting(playerId))) {
            return false;
        }

        var lifeCost = altCast.getCost(LifeCastingCost.class);
        if (lifeCost.isPresent() && gameData.getLife(playerId) < lifeCost.get().amount()) return false;

        var sacCost = altCast.getCost(SacrificePermanentsCost.class);
        if (sacCost.isPresent()) {
            if (battlefield == null) return false;
            long matchingCount = battlefield.stream()
                    .filter(p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, sacCost.get().filter()))
                    .count();
            if (matchingCount < sacCost.get().count()) return false;
        }

        var tapCost = altCast.getCost(TapUntappedPermanentsCost.class);
        if (tapCost.isPresent()) {
            if (battlefield == null) return false;
            long matchingCount = battlefield.stream()
                    .filter(p -> !p.isTapped() && predicateEvaluationService.matchesPermanentPredicate(gameData, p, tapCost.get().filter()))
                    .count();
            if (matchingCount < tapCost.get().count()) return false;
        }

        var returnCost = altCast.getCost(ReturnPermanentsCost.class);
        if (returnCost.isPresent()) {
            if (battlefield == null) return false;
            long matchingCount = battlefield.stream()
                    .filter(p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, returnCost.get().filter()))
                    .count();
            if (matchingCount < returnCost.get().count()) return false;
        }

        var exileHandCost = altCast.getCost(ExileCardsFromHandCastingCost.class);
        if (exileHandCost.isPresent()) {
            List<Card> hand = gameData.playerHands.get(playerId);
            if (hand == null) return false;
            long matchingCount = hand.stream()
                    .filter(c -> c != card)
                    .filter(c -> exileHandCost.get().predicate() == null
                            || predicateEvaluationService.matchesCardPredicate(
                                    c, exileHandCost.get().predicate(), c.getId()))
                    .count();
            if (matchingCount < exileHandCost.get().count()) return false;
        }

        var revealHandCost = altCast.getCost(RevealCardsFromHandCastingCost.class);
        if (revealHandCost.isPresent() && !revealHandCost.get().revealEntireHand()) {
            List<Card> hand = gameData.playerHands.get(playerId);
            if (hand == null) return false;
            boolean hasMatchingCard = hand.stream()
                    .filter(c -> c != card)
                    .anyMatch(c -> revealHandCost.get().predicate() == null
                            || predicateEvaluationService.matchesCardPredicate(
                                    c, revealHandCost.get().predicate(), c.getId()));
            if (!hasMatchingCard) return false;
        }

        var exileGraveyardCost = altCast.getCost(ExileTopCardsFromGraveyardCastingCost.class);
        if (exileGraveyardCost.isPresent()) {
            List<Card> graveyard = gameData.playerGraveyards.get(playerId);
            if (graveyard == null) return false;
            long matchingCount = graveyard.stream()
                    .filter(c -> exileGraveyardCost.get().predicate() == null
                            || predicateEvaluationService.matchesCardPredicate(
                                    c, exileGraveyardCost.get().predicate(), c.getId()))
                    .count();
            if (matchingCount < exileGraveyardCost.get().count()) return false;
        }

        var manaCost = altCast.getCost(ManaCastingCost.class);
        if (manaCost.isPresent()) {
            ManaPool pool = gameData.playerManaPools.get(playerId);
            ManaCost cost = applyColoredManaCostReductions(gameData, playerId, card,
                    new ManaCost(manaCost.get().manaCost()));
            if (altCast.reduceManaBySacrificedManaCost() && sacCost.isPresent() && battlefield != null
                    && sacCost.get().count() == 1) {
                return battlefield.stream()
                        .filter(p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, sacCost.get().filter()))
                        .map(this::manaCostOf)
                        .anyMatch(reduction -> cost.canPayAfterReduction(pool, reduction));
            }
            // Emerge: optimistically reduce by the highest mana value among sacrificeable permanents.
            int emergeReduction = 0;
            if (altCast.reduceManaBySacrificedManaValue() && sacCost.isPresent() && battlefield != null) {
                emergeReduction = battlefield.stream()
                        .filter(p -> predicateEvaluationService.matchesPermanentPredicate(gameData, p, sacCost.get().filter()))
                        .mapToInt(p -> p.getCard().getManaValue())
                        .max()
                        .orElse(0);
            }
            if (!cost.canPay(pool, -emergeReduction)) return false;
        }

        return true;
    }

    private ManaCost manaCostOf(Permanent permanent) {
        String manaCost = permanent.getCard().getManaCost();
        return new ManaCost(manaCost == null ? "{0}" : manaCost);
    }

    /**
     * Prowl (CR 702.75): true if {@code playerId} dealt combat damage to a player this turn with a
     * creature of any of the given subtypes (a Changeling creature counts as every subtype).
     */
    public boolean prowlConditionMet(GameData gameData, UUID playerId, Set<CardSubtype> subtypes) {
        Set<CardSubtype> dealt = gameData.combatDamageToPlayerControllerSubtypesThisTurn
                .getOrDefault(playerId, Set.of());
        return subtypes.stream().anyMatch(dealt::contains)
                || gameData.controllersDealtCombatDamageWithChangelingThisTurn.contains(playerId);
    }

    /**
     * Computes the actual cost reduction for spells that cost less when targeting a
     * permanent matching a predicate (e.g. Ajani's Response targeting a tapped creature),
     * a controlled permanent matching a predicate (e.g. Savage Stomp targeting a Dinosaur),
     * or a spell on the stack matching a predicate.
     * Returns the reduction amount if the first target matches, 0 otherwise.
     */
    public int computeTargetBasedCostReduction(GameData gameData, UUID playerId, Card card, List<UUID> targetIds) {
        if (targetIds.isEmpty()) {
            return 0;
        }

        int enchantedPlayerReduction = gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                .mapToInt(permanent -> {
                    UUID enchantedPlayer = permanent.getAttachedTo();
                    if (enchantedPlayer == null || !gameData.playerIds.contains(enchantedPlayer)
                            || !targetIds.contains(enchantedPlayer)) {
                        return 0;
                    }
                    return permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                            .filter(ReduceOwnCastCostIfTargetingEnchantedPlayerEffect.class::isInstance)
                            .mapToInt(effect -> ((ReduceOwnCastCostIfTargetingEnchantedPlayerEffect) effect).amount())
                            .sum();
                })
                .sum();
        if (enchantedPlayerReduction != 0) {
            return enchantedPlayerReduction;
        }

        List<PerTargetCastCostReductionEffect> perTargetEffects = new ArrayList<>();
        card.getEffects(EffectSlot.STATIC).stream()
                .filter(PerTargetCastCostReductionEffect.class::isInstance)
                .map(PerTargetCastCostReductionEffect.class::cast)
                .forEach(perTargetEffects::add);
        gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                .flatMap(permanent -> permanent.getCard().getEffects(EffectSlot.STATIC).stream())
                .filter(PerTargetCastCostReductionEffect.class::isInstance)
                .map(PerTargetCastCostReductionEffect.class::cast)
                .forEach(perTargetEffects::add);

        int perTargetReduction = perTargetEffects.stream()
                .mapToInt(effect -> (int) targetIds.stream()
                        .map(targetId -> targetId == null
                                ? null : gameQueryService.findPermanentById(gameData, targetId))
                        .filter(java.util.Objects::nonNull)
                        .filter(permanent -> predicateEvaluationService.matchesPermanentPredicate(
                                gameData, permanent, effect.predicate()))
                        .count() * effect.amount())
                .sum();
        if (perTargetReduction != 0) {
            return perTargetReduction;
        }

        UUID firstTargetId = targetIds.getFirst();
        Permanent firstTarget = gameQueryService.findPermanentById(gameData, firstTargetId);
        if (firstTarget != null) {
            ReduceOwnCastCostIfTargetingPermanentEffect permanentEffect = card.getEffects(EffectSlot.STATIC).stream()
                    .filter(ReduceOwnCastCostIfTargetingPermanentEffect.class::isInstance)
                    .map(ReduceOwnCastCostIfTargetingPermanentEffect.class::cast)
                    .findFirst().orElse(null);
            if (permanentEffect == null) {
                return 0;
            }
            if (permanentEffect.controlledByCaster()
                    && !playerId.equals(gameQueryService.findPermanentController(gameData, firstTargetId))) {
                return 0;
            }
            if (predicateEvaluationService.matchesPermanentPredicate(gameData, firstTarget, permanentEffect.predicate())) {
                return permanentEffect.amount();
            }
            return 0;
        }

        Card firstTargetCard = gameQueryService.findCardInGraveyardById(gameData, firstTargetId);
        if (firstTargetCard != null) {
            GraveyardCardTargetCostReductionEffect graveyardEffect = card.getEffects(EffectSlot.STATIC).stream()
                    .filter(GraveyardCardTargetCostReductionEffect.class::isInstance)
                    .map(GraveyardCardTargetCostReductionEffect.class::cast)
                    .findFirst().orElse(null);
            if (graveyardEffect != null
                    && predicateEvaluationService.matchesCardPredicate(
                    firstTargetCard, graveyardEffect.predicate(), card.getId(), gameData,
                    gameQueryService.findGraveyardOwnerById(gameData, firstTargetId))) {
                return graveyardEffect.amount();
            }
            return 0;
        }

        StackEntry firstTargetSpell = gameQueryService.findStackEntryByCardId(gameData, firstTargetId);
        if (firstTargetSpell == null) {
            return 0;
        }

        ReduceOwnCastCostIfTargetingStackEntryEffect stackEffect = card.getEffects(EffectSlot.STATIC).stream()
                .filter(ReduceOwnCastCostIfTargetingStackEntryEffect.class::isInstance)
                .map(ReduceOwnCastCostIfTargetingStackEntryEffect.class::cast)
                .findFirst().orElse(null);
        if (stackEffect != null
                && targetLegalityService.matchesStackEntryPredicate(
                        gameData, firstTargetSpell, stackEffect.predicate(), playerId)) {
            return stackEffect.amount();
        }
        return 0;
    }

    public boolean hasTargetBasedCastCostReduction(Card card) {
        return card.getEffects(EffectSlot.STATIC).stream()
                .anyMatch(e -> e instanceof ReduceOwnCastCostIfTargetingPermanentEffect
                        || e instanceof ReduceOwnCastCostIfTargetingStackEntryEffect
                        || e instanceof PerTargetCastCostReductionEffect
                        || e instanceof GraveyardCardTargetCostReductionEffect);
    }

    public boolean hasPerTargetCastCostReduction(GameData gameData, UUID playerId, Card card) {
        return card.getEffects(EffectSlot.STATIC).stream()
                .anyMatch(PerTargetCastCostReductionEffect.class::isInstance)
                || gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                .flatMap(permanent -> permanent.getCard().getEffects(EffectSlot.STATIC).stream())
                .anyMatch(PerTargetCastCostReductionEffect.class::isInstance);
    }

    public boolean hasEnchantedPlayerCastCostReduction(GameData gameData, UUID playerId) {
        return gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                .anyMatch(permanent -> permanent.getAttachedTo() != null
                        && gameData.playerIds.contains(permanent.getAttachedTo())
                        && permanent.getCard().getEffects(EffectSlot.STATIC).stream()
                        .anyMatch(ReduceOwnCastCostIfTargetingEnchantedPlayerEffect.class::isInstance));
    }

    public int getAttackPaymentPerCreature(GameData gameData, UUID attackingPlayerId) {
        UUID defenderId = gameQueryService.getOpponentId(gameData, attackingPlayerId);
        return getAttackPaymentPerCreature(gameData, attackingPlayerId, defenderId);
    }

    public int getAttackPaymentPerCreature(GameData gameData, UUID attackingPlayerId, UUID attackTargetId) {
        UUID defenderId = gameQueryService.getOpponentId(gameData, attackingPlayerId);
        List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(defenderId);
        if (defenderBattlefield == null) return 0;

        boolean attackingPlaneswalker = defenderBattlefield.stream()
                .filter(perm -> perm.getId().equals(attackTargetId))
                .findFirst()
                .map(perm -> gameQueryService.isPlaneswalker(gameData, perm))
                .orElse(false);
        int totalTax = 0;
        for (Permanent perm : defenderBattlefield) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof RequirePaymentToAttackEffect tax
                        && (!attackingPlaneswalker || tax.protectsPlaneswalkers())
                        && (tax.activeCondition() == null || conditionEvaluationService.isMet(
                                gameData, tax.activeCondition(), ConditionContext.forPermanent(perm, defenderId)))) {
                    totalTax += amountEvaluationService.evaluate(gameData, tax.amountPerAttacker(),
                            AmountContext.forStaticEffect(perm, defenderId));
                }
            }
        }
        synchronized (gameData.floatingEffects) {
            for (var floatingEffect : gameData.floatingEffects) {
                if (floatingEffect.effect() instanceof GlobalAttackCostEffect tax) {
                    totalTax += tax.attackCostPerCreature();
                }
            }
        }
        return totalTax;
    }

    public List<ManaColor> getPhyrexianAttackPaymentsPerCreature(GameData gameData, UUID attackingPlayerId) {
        UUID defenderId = gameQueryService.getOpponentId(gameData, attackingPlayerId);
        List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(defenderId);
        if (defenderBattlefield == null) return List.of();

        List<ManaColor> payments = new ArrayList<>();
        for (Permanent perm : defenderBattlefield) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof RequirePhyrexianPaymentToAttackEffect tax) {
                    payments.add(tax.color());
                }
            }
        }
        return payments;
    }

    public boolean controlsPermanent(GameData gameData, UUID playerId, PermanentPredicate predicate) {
        return support.controlsPermanent(gameData, playerId, predicate);
    }

    public boolean battlefieldHasPermanentMatching(GameData gameData, PermanentPredicate predicate) {
        return support.battlefieldHasPermanentMatching(gameData, predicate);
    }

    public boolean stackHasMatchingSpell(GameData gameData, StackEntryPredicate predicate) {
        return support.stackHasMatchingSpell(gameData, predicate);
    }

    public boolean stackHasMatchingSpell(GameData gameData, UUID controllerId, StackEntryPredicate predicate) {
        return gameData.stack.stream().anyMatch(entry -> targetLegalityService.matchesStackEntryPredicate(
                gameData, entry, predicate, controllerId));
    }

    /**
     * Returns true if the flashback option's {@link TapUntappedPermanentsCost} (e.g. Group Project's
     * "tap three untapped creatures you control") is currently payable. Used for the playable-card
     * previews of flashback options that have no mana cost.
     */
    public boolean canPayFlashbackTapCost(GameData gameData, UUID playerId, FlashbackCast flashback) {
        var tapCost = flashback.getCost(TapUntappedPermanentsCost.class);
        if (tapCost.isEmpty()) {
            return false;
        }
        List<Permanent> battlefield = gameData.playerBattlefields.get(playerId);
        if (battlefield == null) {
            return false;
        }
        long matchingCount = battlefield.stream()
                .filter(p -> !p.isTapped() && predicateEvaluationService.matchesPermanentPredicate(p,
                        tapCost.get().filter(), FilterContext.of(gameData).withSourceControllerId(playerId)))
                .count();
        return matchingCount >= tapCost.get().count();
    }

    /**
     * CR 601.2b/601.2f: can the player currently satisfy every non-mana additional cast cost on
     * the card? Thin delegate to {@link AdditionalSpellCostService#satisfiable} — the engine's
     * single satisfiability query — so the playable-card previews and the AI's move generation
     * can never disagree with cast-time validation. Pure query; never mutates state.
     */
    public boolean canPayAdditionalSpellCosts(GameData gameData, UUID playerId, Card card) {
        return additionalSpellCostService.satisfiable(gameData, playerId, card)
                && canPayImposedSacrificeTax(gameData, playerId, card);
    }

    /** Checks additional costs for a spell being cast from a graveyard zone. */
    public boolean canPayAdditionalSpellCostsFromGraveyard(GameData gameData, UUID playerId, Card card) {
        if (!additionalSpellCostService.satisfiableForGraveyardCast(gameData, playerId, card)
                || !canPayImposedSacrificeTax(gameData, playerId, card)) {
            return false;
        }
        return card.getCastingOption(GraveyardCast.class)
                .map(GraveyardCast::costs)
                .orElse(List.of())
                .stream()
                .filter(RemoveCountersFromControlledCreaturesCastingCost.class::isInstance)
                .map(RemoveCountersFromControlledCreaturesCastingCost.class::cast)
                .allMatch(cost -> totalMatchingCounters(gameData, playerId, cost) >= cost.count());
    }

    private int totalMatchingCounters(GameData gameData, UUID playerId,
                                      RemoveCountersFromControlledCreaturesCastingCost cost) {
        return gameData.playerBattlefields.getOrDefault(playerId, List.of()).stream()
                .filter(p -> gameQueryService.isCreature(gameData, p))
                .mapToInt(p -> switch (cost.counterType()) {
                    case PLUS_ONE_PLUS_ONE -> p.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE);
                    case MINUS_ONE_MINUS_ONE -> p.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE);
                    case CHARGE -> p.getCounterCount(CounterType.CHARGE);
                    case ANY -> p.getCounterCount(CounterType.MINUS_ONE_MINUS_ONE)
                            + p.getCounterCount(CounterType.PLUS_ONE_PLUS_ONE);
                    default -> 0;
                })
                .sum();
    }

    /** Checks non-mana costs on a card's own graveyard-casting option. */
    public boolean canPayGraveyardCastCosts(GameData gameData, UUID playerId, GraveyardCast graveyardCast) {
        var lifeCost = graveyardCast.getCost(LifeCastingCost.class);
        if (lifeCost.isPresent() && gameData.getLife(playerId) < lifeCost.get().amount()) {
            return false;
        }
        var discardCost = graveyardCast.getCost(DiscardCardCastingCost.class);
        return discardCost.isEmpty() || !gameData.playerHands.getOrDefault(playerId, List.of()).isEmpty();
    }

    /** Returns the maximum generic mana reduction currently available from delve, if present. */
    public int maximumDelveReduction(GameData gameData, UUID playerId, Card card,
                                     int effectiveXValue, int additionalGenericCost) {
        if (additionalSpellCostService.peek(card).delveCost() == null) {
            return 0;
        }
        ManaCost manaCost = card.getParsedManaCost();
        if (manaCost == null) {
            return 0;
        }
        int genericDemand = manaCost.getGenericCost()
                + (manaCost.hasX() ? effectiveXValue * manaCost.getXSymbolCount() : 0)
                + additionalGenericCost;
        int graveyardSize = gameData.playerGraveyards.getOrDefault(playerId, List.of()).size();
        return Math.min(graveyardSize, Math.max(0, genericDemand));
    }

    /** @see AdditionalSpellCostService#validDiscardCostIndices */
    public List<Integer> validDiscardCostIndices(GameData gameData, UUID playerId, Card card) {
        return additionalSpellCostService.validDiscardCostIndices(gameData, playerId, card);
    }

    /** True when the card carries any non-mana additional cast cost. */
    public boolean hasAdditionalSpellCosts(Card card) {
        return additionalSpellCostService.peek(card).any();
    }
}
