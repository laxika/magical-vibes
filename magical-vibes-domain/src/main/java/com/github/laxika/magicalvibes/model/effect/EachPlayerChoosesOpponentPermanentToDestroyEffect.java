package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Each player chooses a qualifying permanent controlled by an opponent, then all chosen permanents are destroyed.
 * Each choice is "up to one" unless {@code mandatory} is set, in which case the chooser must pick one whenever a
 * qualifying permanent exists.
 */
public record EachPlayerChoosesOpponentPermanentToDestroyEffect(
        PermanentPredicate filter,
        boolean startWithNextOpponent,
        boolean opponentsOfSourceController,
        boolean mandatory
) implements CardEffect {

    public EachPlayerChoosesOpponentPermanentToDestroyEffect(PermanentPredicate filter, boolean startWithNextOpponent,
                                                             boolean opponentsOfSourceController) {
        this(filter, startWithNextOpponent, opponentsOfSourceController, false);
    }

    public EachPlayerChoosesOpponentPermanentToDestroyEffect(PermanentPredicate filter, boolean startWithNextOpponent) {
        this(filter, startWithNextOpponent, false, false);
    }

    public EachPlayerChoosesOpponentPermanentToDestroyEffect(PermanentPredicate filter) {
        this(filter, false, false);
    }
}
