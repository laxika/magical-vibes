package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Exile this permanent and a permanent you both own and control named {@code partnerName}, then
 * meld them into this card's back face (the meld result). The one-argument constructor retains
 * the creature-partner form used by the original meld cards.
 *
 * <p>Intervening-if ownership/control is checked by the wrapping {@code ConditionalEffect};
 * this effect re-validates at resolution and no-ops if the partner or source is gone / not owned.
 */
public record MeldWithNamedCreatureEffect(String partnerName, PermanentPredicate partnerPredicate,
                                          boolean entersTappedAndAttacking)
        implements CardEffect {

    public MeldWithNamedCreatureEffect(String partnerName) {
        this(partnerName, new PermanentIsCreaturePredicate(), false);
    }

    public MeldWithNamedCreatureEffect(String partnerName, PermanentPredicate partnerPredicate) {
        this(partnerName, partnerPredicate, false);
    }

    public MeldWithNamedCreatureEffect(String partnerName, boolean entersTappedAndAttacking) {
        this(partnerName, new PermanentIsCreaturePredicate(), entersTappedAndAttacking);
    }
}
