package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Card;

/**
 * Continues a paid top-of-library cast after the resolution-time mana payment has succeeded.
 */
public record CastTopCardFromLibraryForManaEffect(Card cardToCast, String manaCost) implements CardEffect {
}
