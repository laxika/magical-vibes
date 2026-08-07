package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

import java.util.List;

/**
 * Choose a card name (excluding cards of the given types) and a number greater than 0, then the
 * target player reveals their library. If that library contains exactly the chosen number of cards
 * with the chosen name, the source deals {@code damage} damage to that player. Then that player
 * shuffles their library — whether or not the guess was right.
 *
 * <p>The name and the number are two chained prompts to the controller; both are made on
 * resolution, before the library is revealed.
 *
 * <p>Used by: Mindblaze ({@code damage = 8}, excluding lands).
 */
public record ChooseNameAndNumberRevealLibraryDamageEffect(List<CardType> excludedTypes, int damage)
        implements DamageDealingEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.player());
    }

    @Override
    public DynamicAmount damageAmount() {
        return new Fixed(damage);
    }

    @Override
    public boolean canDamageCreatures() {
        return false;
    }

    @Override
    public boolean canDamagePlayers() {
        return true;
    }
}
