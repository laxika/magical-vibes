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
 * With {@link Duration#UNTIL_END_OF_TURN}: one-shot effect that sets a temporary flag
 * on the permanent, cleared by {@link com.github.laxika.magicalvibes.model.Permanent#resetModifiers()}.
 * Used by cards like Merfolk Trickster. Resolved by {@code KeywordGrantResolutionService}.
 * Source-linked durations create a floating layer effect that lasts while the source remains on
 * the battlefield.
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
