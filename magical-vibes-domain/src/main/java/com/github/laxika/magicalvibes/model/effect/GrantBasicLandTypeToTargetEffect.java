package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * When resolved, changes the basic land type of the target land.
 *
 * <p>When {@code replacing} is {@code false} (the default), the chosen basic land type is added
 * "in addition to its other types" and the land also gains the intrinsic mana ability of that
 * type (Navigator's Compass, Aquitect's Will).
 *
 * <p>When {@code replacing} is {@code true}, the target land <em>becomes</em> the chosen basic
 * land type, losing its other land types and mana ability per MTG rule 305.7 (Tideshaper Mystic).
 * Only {@link EffectDuration#UNTIL_END_OF_TURN} is supported for the replacing form.
 *
 * <p>If {@code fixedSubtype} is {@code null}, the controller is prompted to choose a basic
 * land type; otherwise that specific type is applied without a prompt.
 *
 * @param duration     how long the granted/overriding type lasts
 * @param fixedSubtype the specific basic land type, or {@code null} to prompt for a choice
 * @param replacing    {@code true} to replace the land's types (Tideshaper Mystic),
 *                     {@code false} to add "in addition to its other types"
 */
public record GrantBasicLandTypeToTargetEffect(EffectDuration duration, CardSubtype fixedSubtype, boolean replacing) implements CardEffect {

    public GrantBasicLandTypeToTargetEffect(EffectDuration duration) {
        this(duration, null, false);
    }

    public GrantBasicLandTypeToTargetEffect(EffectDuration duration, CardSubtype fixedSubtype) {
        this(duration, fixedSubtype, false);
    }

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
