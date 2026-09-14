package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/** Exiles cards from one or more libraries' bottoms and tracks them with the source permanent. */
public record ExileBottomCardsToSourceEffect(DynamicAmount count, boolean faceDown,
                                             LibraryScope scope) implements CardEffect {

    /** Face-up exile from the controller's library. */
    public ExileBottomCardsToSourceEffect(DynamicAmount count) {
        this(count, false, LibraryScope.CONTROLLER);
    }

    public ExileBottomCardsToSourceEffect(int count, boolean faceDown, LibraryScope scope) {
        this(new Fixed(count), faceDown, scope);
    }
}
