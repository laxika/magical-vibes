package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Land-tap trigger: when a matching land taps for mana of the source permanent's chosen color,
 * add one additional mana of that color.
 *
 * <p>The default form only watches lands tapped by the source's controller. The symmetric form
 * watches every player's matching lands, and {@code landFilter} can restrict which lands match.</p>
 */
public record AddExtraManaOfChosenColorOnLandTapEffect(
        boolean controllerOnly,
        PermanentPredicate landFilter
) implements CardEffect {

    public AddExtraManaOfChosenColorOnLandTapEffect() {
        this(true, null);
    }

    public AddExtraManaOfChosenColorOnLandTapEffect(boolean controllerOnly) {
        this(controllerOnly, null);
    }
}
