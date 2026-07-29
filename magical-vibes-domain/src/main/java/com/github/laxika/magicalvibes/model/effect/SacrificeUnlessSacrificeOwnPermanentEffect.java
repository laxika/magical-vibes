package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * "Sacrifice this permanent unless you sacrifice a [permanent matching the filter]."
 * The source's controller chooses; declining (or controlling nothing that matches)
 * sacrifices the source. Used by Sacred Mesa (sacrifice a Pegasus).
 *
 * @param filter      which permanents you control may be sacrificed instead
 * @param description how the alternative reads in prompts and the game log (e.g. "a Pegasus")
 */
public record SacrificeUnlessSacrificeOwnPermanentEffect(PermanentPredicate filter, String description)
        implements CardEffect {
}
