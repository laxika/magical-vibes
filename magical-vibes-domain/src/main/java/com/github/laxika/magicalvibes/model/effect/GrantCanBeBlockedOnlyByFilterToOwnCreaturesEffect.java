package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * One-shot spell/ability effect: every creature the resolving player controls that matches
 * {@link #creatureFilter} can be blocked only by blockers matching {@link #blockerPredicate}
 * until end of turn. Used by Dread Charge ("black creatures you control can't be blocked this turn
 * except by black creatures").
 *
 * <p>This modifies blocking rules without granting an ability. Matching is evaluated at blocking
 * time, including creatures that enter, change color, or change controllers after resolution.
 * The restriction is stored on {@code GameData} and expires at cleanup.
 *
 * @param creatureFilter selects which of the controller's creatures gain the restriction; {@code null} = all
 * @param blockerPredicate the only blockers that may block the affected creatures
 * @param allowedBlockersDescription human-readable description of the allowed blockers (for logs/UI)
 */
public record GrantCanBeBlockedOnlyByFilterToOwnCreaturesEffect(
        PermanentPredicate creatureFilter,
        PermanentPredicate blockerPredicate,
        String allowedBlockersDescription
) implements CardEffect {
}
