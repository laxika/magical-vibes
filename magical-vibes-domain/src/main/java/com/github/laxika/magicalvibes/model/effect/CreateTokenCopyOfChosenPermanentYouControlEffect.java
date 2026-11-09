package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Creates a token copy of a permanent chosen from among permanents the controller controls. A "do this only once each
 * turn" around it is the ability's {@code TriggerLimit.DO_ONLY_ONCE_EACH_TURN} (Calix, Guided by Fate), not the effect's.
 */
public record CreateTokenCopyOfChosenPermanentYouControlEffect(PermanentPredicate filter) implements CardEffect {

    public CreateTokenCopyOfChosenPermanentYouControlEffect() {
        this(null);
    }
}
