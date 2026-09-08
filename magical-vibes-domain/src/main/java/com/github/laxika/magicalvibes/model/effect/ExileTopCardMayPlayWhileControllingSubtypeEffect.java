package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/** Exiles the top card of the controller's library and lets them play it while they control a permanent with the subtype. */
public record ExileTopCardMayPlayWhileControllingSubtypeEffect(CardSubtype subtype) implements CardEffect {
}
