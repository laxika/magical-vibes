package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * Effect: creatures lose all creature types. The
 * {@link GrantScope} selects who is affected: {@link GrantScope#TARGET} strips the single
 * targeted creature (e.g. Amoeboid Changeling's second ability), while
 * {@link GrantScope#TARGET_PLAYERS_CREATURES} strips every creature the targeted player
 * controls (e.g. Ego Erasure). In a static slot, the effect continuously replaces creature
 * subtypes for matching permanents. The one-shot path sets
 * {@code Permanent.losesAllCreatureTypesUntilEndOfTurn}, which makes every creature subtype
 * (base, transient, granted) read as absent and nullifies the Changeling keyword's type grant.
 * The inverse "gains all creature types" is {@code GrantKeywordEffect(Keyword.CHANGELING, scope)}.
 */
public record LoseAllCreatureTypesEffect(GrantScope scope, PermanentPredicate filter) implements CardEffect {

    /** Single targeted creature loses all creature types (Amoeboid Changeling). */
    public LoseAllCreatureTypesEffect() {
        this(GrantScope.TARGET, null);
    }

    public LoseAllCreatureTypesEffect(GrantScope scope) {
        this(scope, null);
    }

    @Override
    public TargetSpec targetSpec() {
        if (scope == GrantScope.TARGET) {
            return TargetSpec.benign(TargetPredicates.permanent());
        }
        if (scope == GrantScope.TARGET_PLAYERS_CREATURES) {
            return TargetSpec.benign(TargetPredicates.player());
        }
        return TargetSpec.NONE;
    }
}
