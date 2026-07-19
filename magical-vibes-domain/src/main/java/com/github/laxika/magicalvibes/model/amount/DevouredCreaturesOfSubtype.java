package com.github.laxika.magicalvibes.model.amount;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * The number of creatures of the given subtype the source permanent devoured (CR 702.82) as it
 * entered the battlefield. Reads the cards snapshotted by {@code Permanent.recordDevouredCreature}
 * on the stack entry's source permanent — "twice the number of Goblins it devoured" (Voracious
 * Dragon), wrapped in {@code Scaled(..., 2)} for the "twice" multiplier.
 */
public record DevouredCreaturesOfSubtype(CardSubtype subtype) implements DynamicAmount {
}
