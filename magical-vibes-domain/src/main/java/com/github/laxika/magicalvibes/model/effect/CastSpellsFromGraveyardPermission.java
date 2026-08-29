package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CastingCost;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

import java.util.List;

/**
 * Capability for a static effect that lets its controller cast spells matching {@link #filter()}
 * from their graveyard for their normal mana costs (Abandoned Sarcophagus). Read by
 * {@code CastingPermissionService} / graveyard cast paths without branching on the concrete effect.
 */
public interface CastSpellsFromGraveyardPermission extends CardEffect {

    /** Spells matching this predicate may be cast from the controller's graveyard. */
    CardPredicate filter();

    /** Additional costs paid when using this permission. */
    default List<CastingCost> additionalCosts() {
        return List.of();
    }

    /** True when the permission applies only during its controller's turn. */
    default boolean onlyDuringControllerTurn() {
        return false;
    }

    /**
     * True if the permission is limited to one spell during each of the controller's own turns
     * (Gisa and Geralf). False grants an unlimited, any-turn permission (Abandoned Sarcophagus).
     */
    default boolean oncePerControllerTurn() {
        return false;
    }
}
