package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.UUID;

/**
 * Increases the activation cost of activated abilities of permanents matching {@code predicate}
 * by {@code amount} generic mana (static). The two-argument constructor is symmetric and taxes
 * every player's matching permanent; the optional flags support opponent-only and non-mana-only
 * taxes such as Tithe Taker.
 */
public record IncreaseActivatedAbilityCostEffect(PermanentPredicate predicate, int amount,
                                                 boolean opponentsOnly, boolean nonManaOnly)
        implements ActivatedAbilityCostIncreasingEffect {

    public IncreaseActivatedAbilityCostEffect(PermanentPredicate predicate, int amount) {
        this(predicate, amount, false, false);
    }

    public static IncreaseActivatedAbilityCostEffect opponentNonMana(PermanentPredicate predicate, int amount) {
        return new IncreaseActivatedAbilityCostEffect(predicate, amount, true, true);
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
                && (!nonManaOnly || !manaAbility);
    }
}
