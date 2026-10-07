package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Shuffle up to {@code maxTargets} target cards matching {@code filter} from the controller's
 * graveyard into their library. Multi-target selection at ETB/cast time or activation time
 * (choose 0 to decline). Sibling of {@link ReturnTargetCardsFromGraveyardToHandEffect}.
 *
 * <p>When {@code optional} is set the shuffle is a "you may" choice made as the effect resolves,
 * after the targets were chosen; accepting shuffles the library even with no targets chosen
 * (Cathartic Parting).
 */
public record ShuffleTargetCardsFromControllerGraveyardIntoLibraryEffect(
        CardPredicate filter,
        int maxTargets,
        boolean optional,
        boolean shufflesWithoutTargets
) implements TargetedGraveyardCardsEffect {

    public ShuffleTargetCardsFromControllerGraveyardIntoLibraryEffect(CardPredicate filter, int maxTargets) {
        this(filter, maxTargets, false, false);
    }

    /** "You may shuffle up to {@code maxTargets} target cards from your graveyard into your library." */
    public static ShuffleTargetCardsFromControllerGraveyardIntoLibraryEffect optional(
            CardPredicate filter, int maxTargets) {
        return new ShuffleTargetCardsFromControllerGraveyardIntoLibraryEffect(filter, maxTargets, true, false);
    }

    /** Sentinel for "any number of target cards" (capped only by the graveyard contents). */
    public static final int ANY_NUMBER = 0;

    public ShuffleTargetCardsFromControllerGraveyardIntoLibraryEffect(CardPredicate filter) {
        this(filter, ANY_NUMBER);
    }
}
