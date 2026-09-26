package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Static marker effect: "You may play lands from the top of your library."
 * The permission applies to the source permanent's controller and uses the normal land-play
 * timing and per-turn land-play allowance. When {@code filter} is non-null, only matching lands
 * may be played.
 */
public record PlayLandsFromTopOfLibraryEffect(CardPredicate filter) implements CardEffect {

    public PlayLandsFromTopOfLibraryEffect() {
        this(null);
    }
}
