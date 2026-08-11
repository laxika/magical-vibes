package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentMaxManaValuePredicate;

/**
 * Exiles target creature with mana value 2 or less, other same-name creatures controlled by its
 * controller, and same-name cards from that player's hand and graveyard.
 *
 * <p>The card supplies the opponent-control restriction through its target filter.</p>
 *
 * <p>Used by: Legion's End</p>
 */
public record ExileTargetCreatureAndSameNameFromBattlefieldHandAndGraveyardEffect()
        implements RemovalEffect {

    @Override
    public RemovalKind removalKind() {
        return RemovalKind.EXILE;
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.harmful(TargetPredicates.creature(), new PermanentMaxManaValuePredicate(2));
    }
}
