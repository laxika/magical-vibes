package com.github.laxika.magicalvibes.model.effect;

/**
 * Gives each creature controlled by the effect's controller every "hexproof from [quality]" variant
 * (CR 702.11d) currently held by a creature that controller controls, until end of turn. Plain
 * hexproof is not included; it is shared as the ordinary keyword.
 *
 * <p>The variant set is snapshotted by the normal-effect handler when this resolves (Odric,
 * Lunarch Marshal).
 */
public record GrantHexproofVariantsSharedByOwnCreaturesUntilEndOfTurnEffect() implements CardEffect {
}
