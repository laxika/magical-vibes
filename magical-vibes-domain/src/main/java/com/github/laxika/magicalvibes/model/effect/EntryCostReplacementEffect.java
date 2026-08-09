package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

/**
 * An optional cost paid while a permanent is entering the battlefield; failure to pay puts the
 * permanent into its owner's graveyard instead.
 */
public interface EntryCostReplacementEffect extends ReplacementEffect {

    enum Kind {
        SACRIFICE_PERMANENT,
        DISCARD_CARD
    }

    Kind kind();

    int count();

    String description();

    default PermanentPredicate permanentFilter() {
        return null;
    }

    default CardPredicate cardFilter() {
        return null;
    }
}
