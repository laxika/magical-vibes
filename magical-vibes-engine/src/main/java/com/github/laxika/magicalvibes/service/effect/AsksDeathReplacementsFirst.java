package com.github.laxika.magicalvibes.service.effect;

/**
 * Marks an effect handler that changes nothing before its first attempt to remove a permanent
 * from the battlefield, on every path. While it resolves as a stack object's effect,
 * {@code EffectResolutionService} lets a creature it would kill that has competing death
 * replacements, one of them keeping the card (CR 616.1), be asked about before anything happens:
 * the handler stops, its controller chooses, and the handler is resolved again with that choice,
 * so the creature leaves inside the effect's own event. A handler that changes the game before its
 * first removal attempt must not carry this mark, since its earlier changes would happen twice.
 */
public interface AsksDeathReplacementsFirst {
}
