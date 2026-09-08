package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Additively grants a creature subtype until end of turn.
 *
 * <p>The affected permanents are captured when the effect resolves. The normal effect handler
 * records the grant as a temporary continuous effect so it participates in the layered subtype
 * system and preserves the permanents' existing creature types.</p>
 *
 * @param subtype the creature subtype to grant
 * @param scope the permanent or player scope receiving the subtype
 */
public record GrantSubtypeUntilEndOfTurnEffect(CardSubtype subtype, GrantScope scope) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return switch (scope) {
            case TARGET_PLAYERS_CREATURES -> TargetSpec.benign(TargetPredicates.player());
            case OWN_CREATURES -> TargetSpec.NONE;
            case SELF -> new TargetSpec(null, false, null, true, 1);
            default -> TargetSpec.benign(TargetPredicates.creature());
        };
    }
}
