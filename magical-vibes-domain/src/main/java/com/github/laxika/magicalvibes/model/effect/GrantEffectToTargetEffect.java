package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.EffectSlot;

/**
 * When resolved, grants the targeted permanent a triggered ability for the given slot.
 * Duration {@link EffectDuration#PERMANENT} stores it on
 * {@link com.github.laxika.magicalvibes.model.Permanent#addPersistentTriggeredEffect} (survives
 * end-of-turn cleanup). Duration {@link EffectDuration#UNTIL_END_OF_TURN} uses
 * {@link com.github.laxika.magicalvibes.model.Permanent#addTemporaryTriggeredEffect} (cleared by
 * {@code resetModifiers()}).
 *
 * <p>Example: Balduvian Shaman grants {@link EffectSlot#UPKEEP_TRIGGERED} +
 * {@link CumulativeUpkeepEffect} indefinitely.
 *
 * @param slot           the trigger slot to grant
 * @param grantedEffect  the effect to fire when the trigger condition is met
 * @param duration       how long the grant lasts
 */
public record GrantEffectToTargetEffect(
        EffectSlot slot,
        CardEffect grantedEffect,
        EffectDuration duration
) implements CardEffect {

    /** Indefinite grant (Balduvian Shaman). */
    public GrantEffectToTargetEffect(EffectSlot slot, CardEffect grantedEffect) {
        this(slot, grantedEffect, EffectDuration.PERMANENT);
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetCategory.PERMANENT);
    }
}
