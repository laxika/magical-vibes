package com.github.laxika.magicalvibes.service.effect;

/**
 * Marks an effect handler whose first coin-flip call comes before anything else it does, on every
 * path. While it resolves as a stack object's effect, {@code EffectResolutionService} lets a flip of
 * that call Krark's Thumb replaces, whose physical results disagree, stop the handler so the flipping
 * player can choose which results to ignore; the handler is then resolved again and reads the same
 * flips back with the kept results. Only the handler's first coin-flip call can stop it, so later
 * flips (and flips of effects it resolves afterwards) keep the heads-preferring default. A handler that
 * changes the game before its first flip must not carry this mark, since its earlier changes would
 * happen twice; one that changes the game between flips uses {@link ChoosesCoinFlipResultsAtCheckpoints}.
 */
public interface ChoosesCoinFlipResultsFirst {
}
