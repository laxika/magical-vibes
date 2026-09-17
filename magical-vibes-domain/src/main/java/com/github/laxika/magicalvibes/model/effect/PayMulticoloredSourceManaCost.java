package com.github.laxika.magicalvibes.model.effect;

/**
 * Activated-ability cost for one mana from a source that could produce at least two colors.
 *
 * <p>This is the playtest {@code {Z}} symbol used by Experiment Five. The mana itself remains
 * ordinary mana; the engine keeps the source-capability provenance as a tag until payment.</p>
 */
public record PayMulticoloredSourceManaCost() implements CostEffect {
}
