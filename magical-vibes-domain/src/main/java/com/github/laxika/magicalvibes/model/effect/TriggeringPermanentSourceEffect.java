package com.github.laxika.magicalvibes.model.effect;

/**
 * Capability for an effect whose damage source is the permanent that caused the trigger rather
 * than the permanent carrying the triggered ability.
 */
public interface TriggeringPermanentSourceEffect extends CardEffect {

    default boolean sourceIsTriggeringPermanent() {
        return true;
    }
}
