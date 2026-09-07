package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * One-shot effect that grants a subtype to the targeted creature
 * ("That creature becomes a [subtype] in addition to its other types").
 * Permanent grants are added to the permanent's {@code grantedSubtypes}; source-linked grants
 * are represented by a floating layer effect.
 *
 * @param subtype the subtype to grant to the target creature
 * @param duration how long the subtype grant lasts
 */
public record GrantSubtypeToTargetCreatureEffect(CardSubtype subtype, EffectDuration duration)
        implements CardEffect {

    public GrantSubtypeToTargetCreatureEffect(CardSubtype subtype) {
        this(subtype, EffectDuration.PERMANENT);
    }

    @Override
    public TargetSpec targetSpec() {
        // The validator enforces only a battlefield permanent (PERMANENT); the creature restriction
        // is the card's own target filter, so this preserves the old canTargetPermanent boolean.
        return TargetSpec.benign(TargetPredicates.permanent());
    }
}
