package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Causes permanents matching the given subject to lose all abilities.
 * Keywords, activated abilities, triggered abilities, and static abilities of the
 * affected permanent are suppressed.
 * <p>
 * With {@link Duration#CONTINUOUS}: static effect used by auras like Deep Freeze.
 * Resolved by {@code LosesAllAbilitiesEffectHandler} in {@code staticfx}.
 * <p>
 * Resolved (Merfolk Trickster, Retched Wretch's return) by the normalfx {@code LosesAllAbilitiesEffectHandler}: a
 * floating layer-6 effect on each affected permanent with the stated duration (CR 611.2a), applied in timestamp order
 * (CR 613.1f, 613.7), so an ability granted after it stays; source-linked durations last while the source remains.
 *
 * @param subject    which permanents are affected (ENCHANTED_CREATURE, TARGET, etc.)
 * @param filter   optional additional permanent filter
 * @param duration how long the effect lasts
 */
public record LosesAllAbilitiesEffect(PermanentSubject subject, PermanentPredicate filter,
                                      Duration duration) implements CardEffect {

    /** Convenience constructor defaulting to {@link Duration#CONTINUOUS}. */
    public LosesAllAbilitiesEffect(PermanentSubject subject) {
        this(subject, null, Duration.STATIC);
    }

    public LosesAllAbilitiesEffect(PermanentSubject subject, Duration duration) {
        this(subject, null, duration);
    }

    public LosesAllAbilitiesEffect(PermanentSubject subject, PermanentPredicate filter) {
        this(subject, filter, Duration.STATIC);
    }

    @Override
    public TargetSpec targetSpec() {
        return switch (subject) {
            case PermanentSubject.ControlledBy set when set.declaresPlayerTarget() ->
                    TargetSpec.benign(TargetPredicates.player());
            case PermanentSubject.Target ignored -> TargetSpec.benign(TargetPredicates.permanent(), filter);
            case PermanentSubject.Source ignored -> new TargetSpec(null, false, null, true, 1);
            default -> TargetSpec.NONE;
        };
    }
}
