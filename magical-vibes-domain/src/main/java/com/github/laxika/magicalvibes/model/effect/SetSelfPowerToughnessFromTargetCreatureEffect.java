package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Sets the <em>source</em> permanent's power and toughness until end of turn from the target
 * creature's current toughness and power (CR 613, layer 7b).
 *
 * <p>Sworn Defender: "This creature's power becomes the toughness of target creature blocking or
 * being blocked by this creature minus 1 until end of turn, and its toughness becomes 1 plus the
 * power of that creature until end of turn." The values are locked in when the ability resolves —
 * later changes to the target's P/T (or its leaving the battlefield) do not update them — so the
 * handler reads the target once and installs a plain until-end-of-turn 7b setter on the source.
 *
 * @param powerFromTargetToughnessOffset added to the target's toughness to get the source's power
 * @param toughnessFromTargetPowerOffset added to the target's power to get the source's toughness
 * @param targetPredicate                narrows the legal target (e.g. "blocking or blocked by this creature")
 */
public record SetSelfPowerToughnessFromTargetCreatureEffect(
        int powerFromTargetToughnessOffset,
        int toughnessFromTargetPowerOffset,
        PermanentPredicate targetPredicate
) implements CardEffect {

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.CREATURE, targetPredicate);
    }
}
