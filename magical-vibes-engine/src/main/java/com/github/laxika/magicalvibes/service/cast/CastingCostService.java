package com.github.laxika.magicalvibes.service.cast;

import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.LifeCastingCost;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.ManaCost;
import com.github.laxika.magicalvibes.model.ManaPool;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.SacrificePermanentsCost;
import com.github.laxika.magicalvibes.model.ReturnPermanentsCost;
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TapUntappedPermanentsCost;
import com.github.laxika.magicalvibes.model.effect.ActivatedAbilityCostIncreasingEffect;
import com.github.laxika.magicalvibes.model.effect.AlternativeCostForSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.GraveyardActivatedAbilityCostReducingEffect;
import com.github.laxika.magicalvibes.model.effect.IncreaseOpponentCostForTargetingControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostIfTargetingControlledPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostIfTargetingPermanentEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostIfTargetingStackEntryEffect;
import com.github.laxika.magicalvibes.model.effect.RequirePaymentToAttackEffect;
import com.github.laxika.magicalvibes.model.effect.RequirePhyrexianPaymentToAttackEffect;
import com.github.laxika.magicalvibes.model.filter.FilterContext;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.StackEntryPredicate;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.ConditionContext;
import com.github.laxika.magicalvibes.service.effect.ConditionEvaluationService;
import com.github.laxika.magicalvibes.service.effect.cost.AdditionalSpellCostService;
import com.github.laxika.magicalvibes.service.filter.PredicateEvaluationService;
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
 * <p>Both the view side (playable-card previews in {@code GameBroadcastService}) and the
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

    /**
     * All cost-modifying static effects currently on the battlefield that could affect spells
     * cast by one player, pre-collected in a single pass so per-card evaluation doesn't re-scan
     * all permanents.
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
        return getCastCostModifier(gameData, playerId, card, buildCostModifierSnapshot(gameData, playerId));
    }

    public int getCastCostModifier(GameData gameData, UUID playerId, Card card, CostModifierSnapshot snapshot) {
        CostModificationContext context = new CostModificationContext(gameData, playerId, card);
        int delta = 0;
        for (CardEffect effect : card.getEffects(EffectSlot.STATIC)) {
            CostModificationHandlerBean handler = costModificationHandlerRegistry.getSpellSelfHandler(effect);
            if (handler != null) {
                delta += handler.modifyCost(context, effect, CostModificationSource.SPELL_ITSELF);
            }
        }
        for (CollectedCostModifier modifier : snapshot.modifiers()) {
            delta += modifier.handler().modifyCost(context, modifier.effect(), modifier.source());
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
                        for (UUID tid : allTargetIds) {
                            Permanent targetPerm = gameQueryService.findPermanentById(gameData, tid);
                            if (targetPerm != null) {
                                UUID targetController = gameQueryService.findPermanentController(gameData, tid);
                                if (controllerId.equals(targetController)
                                        && predicateEvaluationService.matchesPermanentPredicate(
                                                gameData, targetPerm, taxEffect.predicate())) {
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
                                    gameData, sourcePermanent, taxEffect.affectedPermanents())) {
                        tax += taxEffect.additionalGenericCost();
                    }
                }
            }
        }
        return tax;
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
        return findFreeCastSource(gameData, playerId, card) != null;
    }

    /**
     * Applies a battlefield "you may pay {0}" alternative cost to the card being cast and, for a
     * once-each-turn source (As Foretold), records that the source has been used this turn so it
     * offers no further free cast until the next turn. Returns whether a free cast applied.
     * Non-mutating callers (playability previews, validation) must use
     * {@link #hasAlternativeZeroCostFromBattlefield} instead.
     */
    public boolean consumeFreeCastFromBattlefield(GameData gameData, UUID playerId, Card card) {
        FreeCastSource source = findFreeCastSource(gameData, playerId, card);
        if (source == null) return false;
        if (source.effect().oncePerTurn()) {
            gameData.freeCastPermanentUsedThisTurn.add(source.permanent().getId());
        }
        return true;
    }

    private record FreeCastSource(Permanent permanent, AlternativeCostForSpellsEffect effect) {
    }

    /**
     * A permanent the player controls whose {@link AlternativeCostForSpellsEffect} offers a zero
     * alternative cost currently applicable to {@code card}: the filter matches, any counter-based
     * mana-value cap is satisfied, and a once-each-turn source has not yet been used this turn. An
     * unlimited source (e.g. Rooftop Storm) is preferred over a once-each-turn source (As Foretold)
     * so the limited use is not spent while a free one is available.
     */
    private FreeCastSource findFreeCastSource(GameData gameData, UUID playerId, Card card) {
        List<Permanent> bf = gameData.playerBattlefields.get(playerId);
        if (bf == null) return null;
        FreeCastSource oncePerTurnFallback = null;
        for (Permanent perm : bf) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof AlternativeCostForSpellsEffect altCost
                        && new ManaCost(altCost.manaCost()).getManaValue() == 0
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
        return oncePerTurnFallback;
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
                    ManaCost alternativeManaCost = new ManaCost(altCost.manaCost());
                    if (alternativeManaCost.getManaValue() > 0 && alternativeManaCost.canPay(pool, additionalCost)) {
                        return altCost.manaCost();
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
        if (altCastOpt.isEmpty()) return false;
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

        var manaCost = altCast.getCost(ManaCastingCost.class);
        if (manaCost.isPresent()) {
            ManaPool pool = gameData.playerManaPools.get(playerId);
            ManaCost cost = new ManaCost(manaCost.get().manaCost());
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

        UUID firstTargetId = targetIds.getFirst();
        Permanent firstTarget = gameQueryService.findPermanentById(gameData, firstTargetId);
        if (firstTarget != null) {
            ReduceOwnCastCostIfTargetingPermanentEffect generalEffect = card.getEffects(EffectSlot.STATIC).stream()
                    .filter(ReduceOwnCastCostIfTargetingPermanentEffect.class::isInstance)
                    .map(ReduceOwnCastCostIfTargetingPermanentEffect.class::cast)
                    .findFirst().orElse(null);
            if (generalEffect != null
                    && predicateEvaluationService.matchesPermanentPredicate(gameData, firstTarget, generalEffect.predicate())) {
                return generalEffect.amount();
            }

            ReduceOwnCastCostIfTargetingControlledPermanentEffect controlledEffect = card.getEffects(EffectSlot.STATIC).stream()
                    .filter(ReduceOwnCastCostIfTargetingControlledPermanentEffect.class::isInstance)
                    .map(ReduceOwnCastCostIfTargetingControlledPermanentEffect.class::cast)
                    .findFirst().orElse(null);
            if (controlledEffect == null) {
                return 0;
            }

            UUID targetController = gameQueryService.findPermanentController(gameData, firstTargetId);
            if (playerId.equals(targetController)
                    && predicateEvaluationService.matchesPermanentPredicate(gameData, firstTarget, controlledEffect.predicate())) {
                return controlledEffect.amount();
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
                && predicateEvaluationService.matchesStackEntryPredicate(firstTargetSpell, stackEffect.predicate(), null)) {
            return stackEffect.amount();
        }
        return 0;
    }

    public boolean hasTargetBasedCastCostReduction(Card card) {
        return card.getEffects(EffectSlot.STATIC).stream()
                .anyMatch(e -> e instanceof ReduceOwnCastCostIfTargetingPermanentEffect
                        || e instanceof ReduceOwnCastCostIfTargetingControlledPermanentEffect
                        || e instanceof ReduceOwnCastCostIfTargetingStackEntryEffect);
    }

    public int getAttackPaymentPerCreature(GameData gameData, UUID attackingPlayerId) {
        UUID defenderId = gameQueryService.getOpponentId(gameData, attackingPlayerId);
        List<Permanent> defenderBattlefield = gameData.playerBattlefields.get(defenderId);
        if (defenderBattlefield == null) return 0;

        int totalTax = 0;
        for (Permanent perm : defenderBattlefield) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof RequirePaymentToAttackEffect tax) {
                    totalTax += tax.amountPerAttacker();
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
        return additionalSpellCostService.satisfiable(gameData, playerId, card);
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
