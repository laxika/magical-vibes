package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardNamedPredicate;
import com.github.laxika.magicalvibes.model.filter.CardNotPredicate;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Burning-Rune Demon's ETB search: find exactly two differently named cards other than
 * Burning-Rune Demon, reveal them, and let an opponent choose which goes to hand; the other goes
 * to the controller's graveyard.
 */
public record BurningRuneDemonEffect(CardPredicate filter) implements CardEffect {

    public BurningRuneDemonEffect() {
        this(new CardNotPredicate(new CardNamedPredicate("Burning-Rune Demon")));
    }
}
