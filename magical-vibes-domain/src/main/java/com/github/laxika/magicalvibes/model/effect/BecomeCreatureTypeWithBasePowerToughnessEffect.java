package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * One-shot, non-targeting effect that permanently modifies the source permanent: it adds
 * {@code addedSubtype} "in addition to its other types" and, when supplied, sets its base power and toughness
 * to {@code power}/{@code toughness}. Both changes last indefinitely (not until end of turn) —
 * the subtype is stored in {@code grantedSubtypes} and the base P/T uses the permanent base
 * override fields, participating in CR 613.7 layer-7b ordering via a fresh timestamp.
 *
 * <p>When {@code requiredSubtype} is non-null the effect only applies if the source already has
 * that subtype (an intervening "if" checked at resolution); otherwise it does nothing. This models
 * Figure of Destiny's level-up chain ("If this creature is a Spirit, it becomes a Kithkin Spirit
 * Warrior with base power and toughness 4/4").
 *
 * @param power           the base power to set on the source, or {@code null} to leave it unchanged
 * @param toughness       the base toughness to set on the source, or {@code null} to leave it unchanged
 * @param addedSubtype    the subtype to add to the source (additive, survives turn resets)
 * @param requiredSubtype if non-null, the effect only applies when the source already has this
 *                        subtype; {@code null} for an unconditional change
 * @param replacesGrantedSubtypes whether to replace the persistent subtypes granted by earlier
 *                                instances of this effect before adding {@code addedSubtype}
 * @param grantsProtectionFromOpponents whether to also grant protection from the ability
 *                                      controller's opponents
 * @param replacedSubtype the printed subtype to remove when changing the source's subtype line,
 *                        or {@code null} to leave printed subtypes unchanged
 */
public record BecomeCreatureTypeWithBasePowerToughnessEffect(Integer power, Integer toughness,
                                                             CardSubtype addedSubtype,
                                                             CardSubtype requiredSubtype,
                                                             boolean replacesGrantedSubtypes,
                                                             boolean grantsProtectionFromOpponents,
                                                             CardSubtype replacedSubtype) implements CardEffect {

    public BecomeCreatureTypeWithBasePowerToughnessEffect(int power, int toughness, CardSubtype addedSubtype) {
        this(power, toughness, addedSubtype, null, false, false, null);
    }

    public BecomeCreatureTypeWithBasePowerToughnessEffect(int power, int toughness,
                                                          CardSubtype addedSubtype,
                                                          CardSubtype requiredSubtype) {
        this(power, toughness, addedSubtype, requiredSubtype, false, false, null);
    }

    public BecomeCreatureTypeWithBasePowerToughnessEffect(int power, int toughness,
                                                          CardSubtype addedSubtype,
                                                          CardSubtype requiredSubtype,
                                                          boolean replacesGrantedSubtypes) {
        this(power, toughness, addedSubtype, requiredSubtype, replacesGrantedSubtypes, false, null);
    }

    public BecomeCreatureTypeWithBasePowerToughnessEffect(int power, int toughness,
                                                          CardSubtype addedSubtype,
                                                          CardSubtype requiredSubtype,
                                                          boolean replacesGrantedSubtypes,
                                                          boolean grantsProtectionFromOpponents) {
        this(power, toughness, addedSubtype, requiredSubtype, replacesGrantedSubtypes,
                grantsProtectionFromOpponents, null);
    }

    /** Creates a subtype change that retains the source's current base power and toughness. */
    public static BecomeCreatureTypeWithBasePowerToughnessEffect replacingSubtype(
            CardSubtype addedSubtype, CardSubtype requiredSubtype, CardSubtype replacedSubtype) {
        return new BecomeCreatureTypeWithBasePowerToughnessEffect(null, null, addedSubtype,
                requiredSubtype, true, false, replacedSubtype);
    }

    /** Creates a base-P/T-setting subtype change that replaces one printed subtype. */
    public static BecomeCreatureTypeWithBasePowerToughnessEffect replacingSubtype(
            int power, int toughness, CardSubtype addedSubtype, CardSubtype requiredSubtype,
            CardSubtype replacedSubtype) {
        return new BecomeCreatureTypeWithBasePowerToughnessEffect(power, toughness, addedSubtype,
                requiredSubtype, true, false, replacedSubtype);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.NONE;
    }
}
