package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.condition.Condition;

/**
 * Exiles the top card of the controller's library and lets them play it for as long as it
 * remains exiled. An optional condition can restrict when that permission is active.
 */
public record ExileTopCardMayPlayWhileExiledEffect(Condition permissionCondition) implements CardEffect {

    public ExileTopCardMayPlayWhileExiledEffect() {
        this(null);
    }
}
