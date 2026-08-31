package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/** Non-targeting choice to remove one counter from a matching permanent the controller controls. */
public record RemoveCounterFromChosenOwnPermanentEffect(PermanentPredicate permanentFilter)
        implements CardEffect {
}
