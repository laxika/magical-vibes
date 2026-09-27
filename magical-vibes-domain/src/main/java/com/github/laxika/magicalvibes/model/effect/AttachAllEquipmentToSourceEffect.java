package com.github.laxika.magicalvibes.model.effect;

/**
 * Attaches every Equipment that can legally be attached to the source permanent. When
 * {@code controlledOnly} is true, only Equipment controlled by the effect controller is used.
 */
public record AttachAllEquipmentToSourceEffect(boolean controlledOnly) implements CardEffect {

    public AttachAllEquipmentToSourceEffect() {
        this(false);
    }
}
