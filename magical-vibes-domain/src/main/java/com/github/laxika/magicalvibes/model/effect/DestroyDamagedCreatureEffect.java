package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Marker for "Whenever this creature deals damage to a creature, destroy that creature"
 * (Cruel Deceiver's granted ability). Registered in
 * {@link com.github.laxika.magicalvibes.model.EffectSlot#ON_ALLY_CREATURE_DEALS_DAMAGE_TO_CREATURE}
 * and self-scoped: it only fires when the permanent holding it is the damage source.
 *
 * <p>Expanded at trigger-collection time into a {@link DestroyTargetPermanentEffect} stack entry
 * whose target is the damaged creature, so it is never resolved directly. An optional filter
 * restricts which damaged creatures qualify.
 */
public record DestroyDamagedCreatureEffect(PermanentPredicate damagedCreatureFilter)
        implements DamagedCreatureTriggerEffect {

    public DestroyDamagedCreatureEffect() {
        this(null);
    }

    @Override
    public CardEffect triggeredEffect() {
        return new DestroyTargetPermanentEffect();
    }
}
