package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.GraveyardSearchScope;

import java.util.Collections;
import java.util.List;

/** Describes independently optional, single-card target groups in graveyards. */
public interface IndependentlyTargetedGraveyardCardsEffect extends CardEffect {

    /** The filters for the target groups, in the order in which they are announced. */
    List<CardPredicate> targetFilters();

    /** Human-readable target descriptions corresponding to {@link #targetFilters()}. */
    List<String> targetDescriptions();

    /** The minimum number of cards required for each target group. */
    default List<Integer> minimumTargetCounts() {
        return java.util.Collections.nCopies(targetFilters().size(), 0);
    }

    /** The graveyard scope for each target group; defaults to the controller's graveyard. */
    default List<GraveyardSearchScope> targetScopes() {
        return Collections.nCopies(targetFilters().size(), GraveyardSearchScope.CONTROLLERS_GRAVEYARD);
    }

    /** Whether each group is available only when the source spell was kicked. */
    default List<Boolean> targetGroupsOnlyWhenKicked() {
        return Collections.nCopies(targetFilters().size(), false);
    }

    /** Whether cards chosen for different target groups must be different cards. */
    default boolean requiresDistinctTargets() {
        return false;
    }
}
