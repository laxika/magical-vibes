package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.TargetManaValue;
import com.github.laxika.magicalvibes.model.filter.PermanentAnyOfPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentIsEnchantmentPredicate;

import java.util.List;

/**
 * Destroys the primary target, then deals damage equal to its mana value to one creature in a
 * separate target group. The mana value is read before destruction, while the damage is dealt by
 * the resolving spell.
 *
 * @param damageTargetGroup target-group index containing the creature that receives the damage
 */
public record DestroyTargetPermanentThenDealManaValueDamageToTargetCreatureEffect(int damageTargetGroup)
        implements RemovalEffect, DamageDealingEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent(), new PermanentAnyOfPredicate(List.of(
                new PermanentIsArtifactPredicate(), new PermanentIsEnchantmentPredicate())));
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.DESTROY;
    }

    @Override
    public DynamicAmount damageAmount() {
        return new TargetManaValue();
    }

    @Override
    public boolean canDamageCreatures() {
        return true;
    }

    @Override
    public boolean canDamagePlayers() {
        return false;
    }
}
