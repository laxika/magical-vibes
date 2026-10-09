package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Each player chooses up to one qualifying permanent controlled by an opponent, then all chosen permanents are destroyed. */
public record EachPlayerChoosesOpponentPermanentToDestroyEffect(
        PermanentPredicate filter,
        boolean startWithNextOpponent,
        boolean opponentsOfSourceController
) implements CardEffect {

    public EachPlayerChoosesOpponentPermanentToDestroyEffect(PermanentPredicate filter, boolean startWithNextOpponent) {
        this(filter, startWithNextOpponent, false);
    }

    public EachPlayerChoosesOpponentPermanentToDestroyEffect(PermanentPredicate filter) {
        this(filter, false, false);
    }
}
