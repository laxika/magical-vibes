package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Creates a token copy of a permanent chosen from among permanents the controller controls. */
public record CreateTokenCopyOfChosenPermanentYouControlEffect(
        PermanentPredicate filter,
        boolean markSourceOncePerTurnOnAccept,
        boolean accepted
) implements CardEffect {

    public CreateTokenCopyOfChosenPermanentYouControlEffect() {
        this(null, false, false);
    }

    public CreateTokenCopyOfChosenPermanentYouControlEffect(PermanentPredicate filter) {
        this(filter, false, false);
    }

    public CreateTokenCopyOfChosenPermanentYouControlEffect(PermanentPredicate filter,
                                                             boolean markSourceOncePerTurnOnAccept) {
        this(filter, markSourceOncePerTurnOnAccept, false);
    }

    public CreateTokenCopyOfChosenPermanentYouControlEffect asAccepted() {
        return new CreateTokenCopyOfChosenPermanentYouControlEffect(
                filter, markSourceOncePerTurnOnAccept, true);
    }
}
