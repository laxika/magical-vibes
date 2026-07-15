package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * One-shot, non-targeting effect that permanently modifies the source permanent: it adds
 * {@code addedSubtype} "in addition to its other types" and sets its base power and toughness
 * to {@code power}/{@code toughness}. Both changes last indefinitely (not until end of turn) —
 * the subtype is stored in {@code grantedSubtypes} and the base P/T uses the permanent base
 * override fields, participating in CR 613.7 layer-7b ordering via a fresh timestamp.
 *
 * <p>When {@code requiredSubtype} is non-null the effect only applies if the source already has
 * that subtype (an intervening "if" checked at resolution); otherwise it does nothing. This models
 * Figure of Destiny's level-up chain ("If this creature is a Spirit, it becomes a Kithkin Spirit
 * Warrior with base power and toughness 4/4").
 *
 * @param power           the base power to set on the source
 * @param toughness       the base toughness to set on the source
 * @param addedSubtype    the subtype to add to the source (additive, survives turn resets)
 * @param requiredSubtype if non-null, the effect only applies when the source already has this
 *                        subtype; {@code null} for an unconditional change
 */
public record BecomeCreatureTypeWithBasePowerToughnessEffect(int power, int toughness,
                                                             CardSubtype addedSubtype,
                                                             CardSubtype requiredSubtype) implements CardEffect {

    public BecomeCreatureTypeWithBasePowerToughnessEffect(int power, int toughness, CardSubtype addedSubtype) {
        this(power, toughness, addedSubtype, null);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.NONE;
    }
}
