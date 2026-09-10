package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/** Exiles cards from the controller's library and lets them play them while they control a permanent with the subtype. */
public record ExileTopCardMayPlayWhileControllingSubtypeEffect(
        CardSubtype subtype,
        int count,
        boolean faceDown
) implements CardEffect {

    public ExileTopCardMayPlayWhileControllingSubtypeEffect(CardSubtype subtype) {
        this(subtype, 1, false);
    }
}
