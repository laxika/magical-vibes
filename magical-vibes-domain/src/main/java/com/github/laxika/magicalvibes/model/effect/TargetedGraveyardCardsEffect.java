package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Describes an effect that targets several cards in one graveyard and moves them into a library.
 * The selected cards are carried by {@code StackEntry.targetCardIds}; the destination-specific
 * handler resolves the movement.
 */
public interface TargetedGraveyardCardsEffect extends CardEffect {

    /** Maximum number of cards that may be targeted; zero means any number. */
    int maxTargets();

    /** Restriction on the targeted cards, or {@code null} for any card. */
    CardPredicate filter();

    /** Graveyard scope named by the effect. */
    default GraveyardSearchScope source() {
        return GraveyardSearchScope.CONTROLLERS_GRAVEYARD;
    }

    /** Whether the effect targets a graveyard other than the controller's own. */
    default boolean fromOtherGraveyards() {
        return source() != GraveyardSearchScope.CONTROLLERS_GRAVEYARD;
    }
}
