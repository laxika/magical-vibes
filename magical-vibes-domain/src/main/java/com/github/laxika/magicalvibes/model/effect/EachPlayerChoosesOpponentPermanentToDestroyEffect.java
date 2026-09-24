package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Each player chooses up to one qualifying permanent controlled by an opponent, then all chosen permanents are destroyed. */
public record EachPlayerChoosesOpponentPermanentToDestroyEffect(PermanentPredicate filter)
        implements CardEffect {
}
