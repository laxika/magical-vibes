package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Causes permanents matching the given scope to lose all abilities.
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
 * @param scope    which permanents are affected (ENCHANTED_CREATURE, TARGET, etc.)
 * @param filter   optional additional permanent filter
 * @param duration how long the effect lasts
 */
public record LosesAllAbilitiesEffect(GrantScope scope, PermanentPredicate filter,
                                      Duration duration) implements CardEffect {

    /** Convenience constructor defaulting to {@link Duration#CONTINUOUS}. */
    public LosesAllAbilitiesEffect(GrantScope scope) {
        this(scope, null, Duration.STATIC);
    }

    public LosesAllAbilitiesEffect(GrantScope scope, Duration duration) {
        this(scope, null, duration);
    }

    public LosesAllAbilitiesEffect(GrantScope scope, PermanentPredicate filter) {
        this(scope, filter, Duration.STATIC);
    }

    @Override
    public TargetSpec targetSpec() {
        return switch (scope) {
            case TARGET_PLAYERS_CREATURES -> TargetSpec.benign(TargetPredicates.player());
            case TARGET -> TargetSpec.benign(TargetPredicates.permanent(), filter);
            case SELF -> new TargetSpec(null, false, null, true, 1);
            default -> TargetSpec.NONE;
        };
    }
}
