package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/** Static replacement effect that exiles a creature of the configured subtype you control instead of letting it die. */
public record ExileOwnCreaturesOfSubtypeInsteadOfDyingEffect(CardSubtype subtype) implements CardEffect {
}
