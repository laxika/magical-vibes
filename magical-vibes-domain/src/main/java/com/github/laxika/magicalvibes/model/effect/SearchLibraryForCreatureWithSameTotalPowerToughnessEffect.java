package com.github.laxika.magicalvibes.model.effect;

/**
 * Searches the controller's library for a creature card whose power plus toughness equals the
 * entering creature's current total power and toughness, then puts it onto the battlefield.
 *
 * @param powerAtTrigger      last-known power captured when the triggering creature entered
 * @param toughnessAtTrigger  last-known toughness captured when the triggering creature entered
 */
public record SearchLibraryForCreatureWithSameTotalPowerToughnessEffect(
        Integer powerAtTrigger,
        Integer toughnessAtTrigger
) implements CardEffect {

    public SearchLibraryForCreatureWithSameTotalPowerToughnessEffect() {
        this(null, null);
    }
}
