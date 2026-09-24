package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.GraveyardSearchScope;
import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Describes an as-enters replacement that exiles matching cards from the controller's graveyard.
 * The entry pipeline uses the bounds to create the choice and tracks the selected cards with the
 * entering permanent.
 */
public interface AsEntersGraveyardExileEffect extends ReplacementEffect {

    CardPredicate filter();

    /** Which graveyards supply the cards offered by this entry replacement. */
    default GraveyardSearchScope graveyardScope() {
        return GraveyardSearchScope.CONTROLLERS_GRAVEYARD;
    }

    int minimumCards();

    int maximumCards();
}
