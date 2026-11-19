package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Exiles cards from the bottoms of the libraries of {@code player} and tracks them with the source permanent.
 * The subject must not declare a target.
 */
public record ExileBottomCardsToSourceEffect(DynamicAmount count, boolean faceDown,
                                             PlayerSubject player) implements CardEffect {

    public ExileBottomCardsToSourceEffect {
        if (player.declaresTarget()) {
            throw new IllegalArgumentException("ExileBottomCardsToSourceEffect declares no target");
        }
    }

    /** Face-up exile from the controller's library. */
    public ExileBottomCardsToSourceEffect(DynamicAmount count) {
        this(count, false, Subjects.you());
    }

    public ExileBottomCardsToSourceEffect(int count, boolean faceDown, PlayerSubject player) {
        this(new Fixed(count), faceDown, player);
    }
}
