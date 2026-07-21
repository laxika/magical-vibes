package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Capability for a static effect that lets its controller cast spells matching {@link #filter()}
 * from their graveyard for their normal mana costs (Abandoned Sarcophagus). Read by
 * {@code CastingPermissionService} / graveyard cast paths without branching on the concrete effect.
 */
public interface CastSpellsFromGraveyardPermission extends CardEffect {

    /** Spells matching this predicate may be cast from the controller's graveyard. */
    CardPredicate filter();
}
