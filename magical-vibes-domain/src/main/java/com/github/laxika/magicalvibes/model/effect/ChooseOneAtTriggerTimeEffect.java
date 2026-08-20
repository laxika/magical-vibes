package com.github.laxika.magicalvibes.model.effect;

/**
 * A modal choice made as a triggered ability is put on the stack rather than while its source
 * spell is being cast.
 */
public record ChooseOneAtTriggerTimeEffect(ChooseOneEffect choice) implements CardEffect {
}
