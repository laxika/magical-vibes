package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

/**
 * Gain control of every permanent matching {@code predicate} across all battlefields (Karrthus,
 * Tyrant of Jund's "gain control of all Dragons"; Tibalt, the Fiend-Blooded's "gain control of all
 * creatures until end of turn").
 *
 * <p>Non-targeted mass control gain. At resolution the controller gains control of each matching
 * permanent they do not already control, via a per-permanent {@link GainControlOfTargetEffect}
 * floating effect on the standard layer-2 control machinery — mirroring
 * {@link GainControlOfAllPermanentsTargetPlayerControlsEffect}, but selected by predicate rather than
 * by a target player.
 *
 * <p>Optional {@code thenEffects} resolve for every permanent gained after all control changes have
 * been applied. They are useful for riders such as untapping and granting haste to exactly the
 * permanents seized by the effect; the rider effects must resolve synchronously and use their
 * target as the stack entry's target.
 *
 * @param predicate narrows which permanents (any controller) are seized
 * @param duration  how long the control gain lasts
 * @param thenEffects ordered rider effects applied to each seized permanent
 */
public record GainControlOfAllPermanentsMatchingEffect(PermanentPredicate predicate, ControlDuration duration,
                                                        List<CardEffect> thenEffects)
        implements CardEffect {

    public GainControlOfAllPermanentsMatchingEffect {
        thenEffects = List.copyOf(thenEffects);
    }

    public GainControlOfAllPermanentsMatchingEffect(PermanentPredicate predicate) {
        this(predicate, ControlDuration.PERMANENT);
    }

    public GainControlOfAllPermanentsMatchingEffect(PermanentPredicate predicate, ControlDuration duration) {
        this(predicate, duration, List.of());
    }

    public GainControlOfAllPermanentsMatchingEffect(PermanentPredicate predicate, ControlDuration duration,
                                                     CardEffect... thenEffects) {
        this(predicate, duration, List.of(thenEffects));
    }
}
