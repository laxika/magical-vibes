package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.UUID;

/**
 * Increases the activation cost of activated abilities of permanents matching {@code predicate}
 * by {@code amount} generic mana (static). The two-argument constructor is symmetric and taxes
 * every player's matching permanent; the optional flags support opponent-only, non-mana-only,
 * and loyalty-ability-only taxes such as Tithe Taker and Eidolon of Obstruction.
 */
public record IncreaseActivatedAbilityCostEffect(PermanentPredicate predicate, int amount,
                                                 boolean opponentsOnly, boolean nonManaOnly,
                                                 boolean loyaltyAbilitiesOnly)
        implements ActivatedAbilityCostIncreasingEffect {

    public IncreaseActivatedAbilityCostEffect(PermanentPredicate predicate, int amount) {
        this(predicate, amount, false, false, false);
    }

    public IncreaseActivatedAbilityCostEffect(PermanentPredicate predicate, int amount,
                                              boolean opponentsOnly, boolean nonManaOnly) {
        this(predicate, amount, opponentsOnly, nonManaOnly, false);
    }

    public static IncreaseActivatedAbilityCostEffect opponentNonMana(PermanentPredicate predicate, int amount) {
        return new IncreaseActivatedAbilityCostEffect(predicate, amount, true, true, false);
    }

    public static IncreaseActivatedAbilityCostEffect opponentLoyalty(PermanentPredicate predicate, int amount) {
        return new IncreaseActivatedAbilityCostEffect(predicate, amount, true, false, true);
    }

    @Override
    public PermanentPredicate affectedPermanents() {
        return predicate;
    }

    @Override
    public int additionalGenericCost() {
        return amount;
    }

    @Override
    public boolean appliesTo(ActivatedAbility ability, boolean manaAbility,
                              UUID activatingPlayerId, UUID sourceControllerId) {
        return (!opponentsOnly || (activatingPlayerId != null && sourceControllerId != null
                && !activatingPlayerId.equals(sourceControllerId)))
                && (!nonManaOnly || !manaAbility)
                && (!loyaltyAbilitiesOnly || (ability != null && ability.getLoyaltyCost() != null));
    }
}
