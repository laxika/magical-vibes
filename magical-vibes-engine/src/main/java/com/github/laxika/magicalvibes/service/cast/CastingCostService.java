package com.github.laxika.magicalvibes.service.cast;

import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
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
import com.github.laxika.magicalvibes.model.StackEntry;
import com.github.laxika.magicalvibes.model.TapUntappedPermanentsCost;
import com.github.laxika.magicalvibes.model.effect.AlternativeCostForSpellsEffect;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ExileCardFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ExileNCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.ExileXCardsFromGraveyardCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeArtifactCost;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
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

    public boolean hasAlternativeZeroCostFromBattlefield(GameData gameData, UUID playerId, Card card) {
        List<Permanent> bf = gameData.playerBattlefields.get(playerId);
        if (bf == null) return false;
        for (Permanent perm : bf) {
            for (CardEffect effect : perm.getCard().getEffects(EffectSlot.STATIC)) {
                if (effect instanceof AlternativeCostForSpellsEffect altCost
                        && new ManaCost(altCost.manaCost()).getManaValue() == 0
                        && predicateEvaluationService.matchesCardPredicate(card, altCost.filter(), null)) {
                    return true;
                }
            }
        }
        return false;
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

        var manaCost = altCast.getCost(ManaCastingCost.class);
        if (manaCost.isPresent()) {
            ManaPool pool = gameData.playerManaPools.get(playerId);
            ManaCost cost = new ManaCost(manaCost.get().manaCost());
            if (!cost.canPay(pool, 0)) return false;
        }

        return true;
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
     * CR 601.2b/601.2f: can the player currently satisfy every non-mana additional cost baked into
     * the card's SPELL effects that must be paid from a zone other than the mana pool — sacrifice
     * costs (creature / artifact / filtered-permanent) and graveyard-exile costs (a single card, a
     * fixed count, or all-cards-for-X)? Pure query over the player's battlefield and graveyard;
     * never mutates state.
     *
     * <p>This is the single source of truth for the "additional costs are payable" gate that
     * {@code SpellCastingService.playCard} enforces card-by-card at cast time. Callers that offer a
     * spell before the player commits to it (the playable-card previews and the AI's move
     * generation) consult it so they never advertise a spell whose additional costs can't be paid.
     * It deliberately does NOT re-check the mana affordability, targeting, or ExileN 601.2b gates
     * that {@code GameBroadcastService.isCardPlayable} already covers (ExileN is included here too
     * so the query stands alone as a complete additional-cost check).
     */
    public boolean canPayAdditionalSpellCosts(GameData gameData, UUID playerId, Card card) {
        List<Permanent> battlefield = gameData.playerBattlefields.getOrDefault(playerId, List.of());
        List<Card> graveyard = gameData.playerGraveyards.getOrDefault(playerId, List.of());
        for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
            switch (effect) {
                case SacrificeCreatureCost ignored -> {
                    if (battlefield.stream().noneMatch(p -> gameQueryService.isCreature(gameData, p))) return false;
                }
                case SacrificeArtifactCost ignored -> {
                    if (battlefield.stream().noneMatch(p -> gameQueryService.isArtifact(gameData, p))) return false;
                }
                case SacrificePermanentCost sacCost -> {
                    if (battlefield.stream().noneMatch(p ->
                            predicateEvaluationService.matchesPermanentPredicate(gameData, p, sacCost.filter()))) return false;
                }
                case ExileNCardsFromGraveyardCost cost -> {
                    long matchingCount = graveyard.stream()
                            .filter(c -> cost.requiredType() == null || c.hasType(cost.requiredType()))
                            .count();
                    if (matchingCount < cost.count()) return false;
                }
                case ExileCardFromGraveyardCost cost -> {
                    if (graveyard.stream().noneMatch(c -> cost.requiredType() == null || c.hasType(cost.requiredType()))) return false;
                }
                case ExileXCardsFromGraveyardCost ignored -> {
                    if (graveyard.isEmpty()) return false;
                }
                default -> { }
            }
        }
        return true;
    }
}
