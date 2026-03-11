package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Gains control of target permanent permanently (indefinite duration).
 * Optionally grants a subtype to the stolen permanent (e.g. "It becomes a Vampire
 * in addition to its other types").
 *
 * @param grantedSubtype if non-null, this subtype is permanently added to the stolen permanent
 */
public record GainControlOfTargetPermanentEffect(CardSubtype grantedSubtype) implements CardEffect {

    public GainControlOfTargetPermanentEffect() {
        this(null);
    }

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
