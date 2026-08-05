package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsArtifactPredicate;

/**
 * Destroys the targeted artifact (it can be regenerated), then that artifact deals damage equal to
 * its mana value to the source permanent of this ability. The artifact is the damage source
 * (CR 608.2h — its last known information is used), so its colour, name and "can't deal damage"
 * restrictions apply. The damage happens whether or not the destruction actually succeeded.
 * Used by Goblin Tinkerer.
 */
public record DestroyTargetArtifactDealManaValueDamageToSourceEffect() implements RemovalEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.permanent(), new PermanentIsArtifactPredicate());
    }

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.DESTROY;
    }
}
