package com.github.laxika.magicalvibes.ai;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.GameData;
import com.github.laxika.magicalvibes.model.Permanent;
import com.github.laxika.magicalvibes.model.SpellTarget;
import com.github.laxika.magicalvibes.model.effect.CardEffect;
import com.github.laxika.magicalvibes.model.effect.ChooseOneEffect;
import com.github.laxika.magicalvibes.model.effect.CreatureBoostEffect;
import com.github.laxika.magicalvibes.model.effect.RemovalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentAllOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPowerAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicateTargetFilter;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessAtLeastPredicate;
import com.github.laxika.magicalvibes.model.filter.TargetFilter;
import com.github.laxika.magicalvibes.service.battlefield.GameQueryService;
import com.github.laxika.magicalvibes.service.effect.AmountContext;
import com.github.laxika.magicalvibes.service.effect.AmountEvaluationService;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/**
 * Detects the "pump an undersized creature so a size-gated removal can kill it" line —
 * e.g. Giant Growth a 2/2 into a 5/5 so {@code Smite the Monstrous} (power 4+) becomes legal.
 *
 * <p>Beneficial pumps normally never aim at the opponent; this is the narrow exception where
 * buffing the opponent's creature is instrumentally correct.
 */
public final class SizeGatedRemovalPump {

    private SizeGatedRemovalPump() {}

    /**
     * Opponent creatures that are currently illegal for every size-gated removal the AI can
     * follow up with, but would become legal after {@code pumpCard}'s targeted P/T boost.
     */
    public static List<Permanent> findEnabledOpponentCreatures(
            GameData gameData,
            Card pumpCard,
            UUID aiPlayerId,
            UUID opponentId,
            GameQueryService gameQueryService,
            AmountEvaluationService amountEvaluationService) {
        int powerBoost = 0;
        int toughnessBoost = 0;
        AmountContext ctx = AmountContext.forEstimation(aiPlayerId);
        for (CardEffect effect : pumpCard.getEffects(EffectSlot.SPELL)) {
            if (effect instanceof CreatureBoostEffect boost) {
                powerBoost += amountEvaluationService.evaluate(gameData, boost.powerBoost(), ctx);
                toughnessBoost += amountEvaluationService.evaluate(gameData, boost.toughnessBoost(), ctx);
            }
        }
        if (powerBoost <= 0 && toughnessBoost <= 0) {
            return List.of();
        }

        List<SizeGate> gates = collectFollowUpSizeGates(gameData, pumpCard, aiPlayerId);
        if (gates.isEmpty()) {
            return List.of();
        }

        List<Permanent> enabled = new ArrayList<>();
        for (Permanent opp : gameData.playerBattlefields.getOrDefault(opponentId, List.of())) {
            if (!gameQueryService.isCreature(gameData, opp)) {
                continue;
            }
            int power = gameQueryService.getEffectivePower(gameData, opp);
            int toughness = gameQueryService.getEffectiveToughness(gameData, opp);
            int pumpedPower = power + powerBoost;
            int pumpedToughness = toughness + toughnessBoost;
            for (SizeGate gate : gates) {
                if (!gate.matches(power, toughness) && gate.matches(pumpedPower, pumpedToughness)) {
                    enabled.add(opp);
                    break;
                }
            }
        }
        return enabled;
    }

    private static List<SizeGate> collectFollowUpSizeGates(GameData gameData, Card pumpCard, UUID aiPlayerId) {
        List<SizeGate> gates = new ArrayList<>();
        for (Card handCard : gameData.playerHands.getOrDefault(aiPlayerId, List.of())) {
            if (handCard.getId().equals(pumpCard.getId())) {
                continue;
            }
            collectSizeGatesFromCard(handCard, gates);
        }
        for (Permanent permanent : gameData.playerBattlefields.getOrDefault(aiPlayerId, List.of())) {
            for (ActivatedAbility ability : permanent.getCard().getActivatedAbilities()) {
                if (!abilityHasRemoval(ability)) {
                    continue;
                }
                collectSizeGatesFromFilter(ability.getTargetFilter(), gates);
            }
        }
        return gates;
    }

    private static void collectSizeGatesFromCard(Card card, List<SizeGate> gates) {
        boolean hasRemoval = false;
        for (CardEffect effect : card.getEffects(EffectSlot.SPELL)) {
            if (effect instanceof RemovalEffect) {
                hasRemoval = true;
            }
            if (effect instanceof ChooseOneEffect chooseOne) {
                for (ChooseOneEffect.ChooseOneOption option : chooseOne.options()) {
                    boolean optionRemoves = option.effects().stream().anyMatch(RemovalEffect.class::isInstance);
                    if (!optionRemoves) {
                        continue;
                    }
                    collectSizeGatesFromFilter(option.targetFilter(), gates);
                    if (option.targetFilters() != null) {
                        for (TargetFilter filter : option.targetFilters()) {
                            collectSizeGatesFromFilter(filter, gates);
                        }
                    }
                }
            }
        }
        if (hasRemoval) {
            collectSizeGatesFromFilter(card.getTargetFilter(), gates);
            for (SpellTarget spellTarget : card.getSpellTargets()) {
                collectSizeGatesFromFilter(spellTarget.getFilter(), gates);
            }
        }
    }

    private static boolean abilityHasRemoval(ActivatedAbility ability) {
        return ability.getEffects().stream().anyMatch(RemovalEffect.class::isInstance);
    }

    private static void collectSizeGatesFromFilter(TargetFilter filter, List<SizeGate> gates) {
        if (filter instanceof PermanentPredicateTargetFilter predicateFilter) {
            collectSizeGatesFromPredicate(predicateFilter.predicate(), gates);
        }
    }

    private static void collectSizeGatesFromPredicate(PermanentPredicate predicate, List<SizeGate> gates) {
        switch (predicate) {
            case PermanentPowerAtLeastPredicate power -> gates.add(SizeGate.powerAtLeast(power.minPower()));
            case PermanentToughnessAtLeastPredicate toughness ->
                    gates.add(SizeGate.toughnessAtLeast(toughness.minToughness()));
            case PermanentAllOfPredicate all -> {
                for (PermanentPredicate nested : all.predicates()) {
                    collectSizeGatesFromPredicate(nested, gates);
                }
            }
            case PermanentAnyOfPredicate any -> {
                for (PermanentPredicate nested : any.predicates()) {
                    collectSizeGatesFromPredicate(nested, gates);
                }
            }
            default -> { }
        }
    }

    private record SizeGate(Integer minPower, Integer minToughness) {
        static SizeGate powerAtLeast(int minPower) {
            return new SizeGate(minPower, null);
        }

        static SizeGate toughnessAtLeast(int minToughness) {
            return new SizeGate(null, minToughness);
        }

        boolean matches(int power, int toughness) {
            if (minPower != null && power < minPower) {
                return false;
            }
            if (minToughness != null && toughness < minToughness) {
                return false;
            }
            return minPower != null || minToughness != null;
        }
    }
}
