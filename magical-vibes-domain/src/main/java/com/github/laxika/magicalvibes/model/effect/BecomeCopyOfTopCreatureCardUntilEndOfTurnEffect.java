package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

import java.util.Set;

/**
 * Causes the source permanent to become a copy of the top creature card of its controller's
 * library until end of turn. {@code additionalKeywords} are baked into the copy as copy
 * exceptions ("except it has flying"), so they are copiable and survive a later Clone of this
 * permanent.
 */
public record BecomeCopyOfTopCreatureCardUntilEndOfTurnEffect(Set<Keyword> additionalKeywords)
        implements CardEffect {

    public BecomeCopyOfTopCreatureCardUntilEndOfTurnEffect() {
        this(Set.of());
    }
}
