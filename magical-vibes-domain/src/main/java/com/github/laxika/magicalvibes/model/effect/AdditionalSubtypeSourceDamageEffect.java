package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.Set;

/**
 * Static replacement effect: a creature of one of the given subtypes controlled by this
 * permanent's controller deals that much damage plus {@code amount} instead.
 */
public record AdditionalSubtypeSourceDamageEffect(int amount, Set<CardSubtype> subtypes)
        implements SubtypeSourceDamageBonusEffect {

    public AdditionalSubtypeSourceDamageEffect {
        subtypes = Set.copyOf(subtypes);
    }
}
