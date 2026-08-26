package com.github.laxika.magicalvibes.model.effect;

/**
 * Static marker for an effect that makes spells controlled by the source permanent's controller
 * unable to be countered.
 */
public record ControllerSpellsCantBeCounteredEffect(boolean noncreatureOnly) implements CardEffect {

    public ControllerSpellsCantBeCounteredEffect() {
        this(false);
    }
}
