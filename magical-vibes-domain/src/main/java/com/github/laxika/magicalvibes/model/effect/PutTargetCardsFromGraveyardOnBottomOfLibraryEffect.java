package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Put up to {@code maxTargets} target cards from your graveyard on the bottom of your library in
 * the order supplied on the stack entry.
 */
public record PutTargetCardsFromGraveyardOnBottomOfLibraryEffect(
        CardPredicate filter,
        int maxTargets
) implements TargetedGraveyardCardsEffect {

    public PutTargetCardsFromGraveyardOnBottomOfLibraryEffect(CardPredicate filter) {
        this(filter, 0);
    }
}
