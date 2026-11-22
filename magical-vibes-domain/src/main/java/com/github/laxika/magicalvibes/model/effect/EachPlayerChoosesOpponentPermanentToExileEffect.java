package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Each player chooses up to one qualifying permanent your opponents control, then all chosen permanents are exiled at once. */
public record EachPlayerChoosesOpponentPermanentToExileEffect(PermanentPredicate filter)
        implements CardEffect {
}
