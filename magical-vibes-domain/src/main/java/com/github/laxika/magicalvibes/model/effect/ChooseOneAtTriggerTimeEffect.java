package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * A modal choice made as a triggered ability is put on the stack rather than while its source
 * spell is being cast. The optional maximum supports triggered abilities such as "choose up to X"
 * where X is evaluated as the trigger is put on the stack.
 */
public record ChooseOneAtTriggerTimeEffect(ChooseOneEffect choice, DynamicAmount maximumChoices)
        implements TriggeredModalEffect {

    public ChooseOneAtTriggerTimeEffect(ChooseOneEffect choice) {
        this(choice, null);
    }
}
