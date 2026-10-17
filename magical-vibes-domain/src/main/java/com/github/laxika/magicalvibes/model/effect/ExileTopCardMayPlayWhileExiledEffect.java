package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.condition.Condition;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Exiles the top card of the controller's library and lets them play it for as long as it
 * remains exiled. An optional condition can restrict when that permission is active, and an
 * optional spell filter restricts which cards the permission covers (for example, non-land cards
 * when the permission only allows casting the card).
 */
public record ExileTopCardMayPlayWhileExiledEffect(Condition permissionCondition,
                                                  boolean faceDown, boolean anyManaType,
                                                  CardPredicate spellFilter)
        implements CardEffect {

    public ExileTopCardMayPlayWhileExiledEffect(Condition permissionCondition,
                                                boolean faceDown, boolean anyManaType) {
        this(permissionCondition, faceDown, anyManaType, null);
    }

    public ExileTopCardMayPlayWhileExiledEffect() {
        this(null, false, false);
    }

    public ExileTopCardMayPlayWhileExiledEffect(Condition permissionCondition) {
        this(permissionCondition, false, false);
    }

    public ExileTopCardMayPlayWhileExiledEffect(boolean faceDown, boolean anyManaType) {
        this(null, faceDown, anyManaType);
    }
}
