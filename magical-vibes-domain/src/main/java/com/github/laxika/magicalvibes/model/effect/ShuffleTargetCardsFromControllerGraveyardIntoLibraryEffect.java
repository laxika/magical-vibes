package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * Shuffle up to {@code maxTargets} target cards matching {@code filter} from the controller's
 * graveyard into their library. Multi-target selection at ETB/cast time (choose 0 to decline —
 * covers "you may"). Sibling of {@link ReturnTargetCardsFromGraveyardToHandEffect}.
 */
public record ShuffleTargetCardsFromControllerGraveyardIntoLibraryEffect(
        CardPredicate filter,
        int maxTargets
) implements CardEffect {
}
