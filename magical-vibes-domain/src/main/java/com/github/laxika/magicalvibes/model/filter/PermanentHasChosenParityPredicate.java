package com.github.laxika.magicalvibes.model.filter;

/**
 * "With mana value of the chosen quality", "with power of the chosen quality" after "choose odd or even" in the same
 * resolution ({@code ChooseManaValueParityAtResolutionEffect}); nothing while none is chosen.
 *
 * @param of which of the permanent's numbers has that parity
 */
public record PermanentHasChosenParityPredicate(Quality of) implements PermanentPredicate {

    public enum Quality {
        MANA_VALUE,
        POWER
    }
}
