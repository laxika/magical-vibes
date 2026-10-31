package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Ability;
import com.github.laxika.magicalvibes.model.filter.PermanentPredicate;

import java.util.List;

/**
 * Replacement behavior for a permanent that may become a copy as it is turned face up.
 * Additional abilities are copy exceptions retained by the resulting copy.
 */
public record TurnFaceUpCopyEffect(PermanentPredicate filter,
                                   List<Ability> additionalAbilities)
        implements ReplacementEffect {

    public TurnFaceUpCopyEffect(PermanentPredicate filter) {
        this(filter, List.of());
    }
}
