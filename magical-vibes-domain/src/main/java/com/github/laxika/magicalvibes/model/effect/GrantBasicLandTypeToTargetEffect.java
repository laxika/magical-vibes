package com.github.laxika.magicalvibes.model.effect;

import java.util.List;

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
 * The replacing form supports {@link EffectDuration#UNTIL_END_OF_TURN} (Tideshaper Mystic) and
 * {@link EffectDuration#UNTIL_CONTROLLERS_NEXT_UNTAP_STEP} (Orcish Farmer).
 *
 * <p>If {@code fixedSubtype} is {@code null}, the controller is prompted to choose a basic
 * land type from {@code allowedTypes}; an empty list offers all five basic land types. Otherwise
 * that specific type is applied without a prompt.
 *
 * @param duration     how long the granted/overriding type lasts
 * @param fixedSubtype the specific basic land type, or {@code null} to prompt for a choice
 * @param replacing    {@code true} to replace the land's types (Tideshaper Mystic),
 *                     {@code false} to add "in addition to its other types"
 * @param allowedTypes the basic land types offered when {@code fixedSubtype} is {@code null};
 *                     an empty list offers all five
 */
public record GrantBasicLandTypeToTargetEffect(EffectDuration duration, CardSubtype fixedSubtype,
                                               boolean replacing, List<CardSubtype> allowedTypes) implements CardEffect {

    public GrantBasicLandTypeToTargetEffect {
        allowedTypes = allowedTypes == null ? List.of() : List.copyOf(allowedTypes);
    }

    public GrantBasicLandTypeToTargetEffect(EffectDuration duration) {
        this(duration, null, false, List.of());
    }

    public GrantBasicLandTypeToTargetEffect(EffectDuration duration, CardSubtype fixedSubtype) {
        this(duration, fixedSubtype, false, List.of());
    }

    public GrantBasicLandTypeToTargetEffect(EffectDuration duration, CardSubtype fixedSubtype, boolean replacing) {
        this(duration, fixedSubtype, replacing, List.of());
    }

    @Override
    public TargetSpec targetSpec() {
        return TargetSpec.benign(TargetPredicates.land());
    }
}
