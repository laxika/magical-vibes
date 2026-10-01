package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

import java.util.Objects;

/**
 * Static replacement marker that changes a created token with one subtype into a supplied token
 * profile. The shared token-creation pipeline preserves event-level modifiers such as tapped,
 * attacking, initial counters, and delayed exile when applying the replacement.
 */
public record ReplaceTokenSubtypeCreationEffect(
        CardSubtype sourceSubtype,
        CreateTokenEffect replacementToken,
        int minimumSourceLevel
) implements CardEffect {

    public ReplaceTokenSubtypeCreationEffect {
        Objects.requireNonNull(sourceSubtype, "sourceSubtype");
        Objects.requireNonNull(replacementToken, "replacementToken");
        if (minimumSourceLevel < 0) {
            throw new IllegalArgumentException("minimumSourceLevel must not be negative");
        }
    }

    public ReplaceTokenSubtypeCreationEffect(CardSubtype sourceSubtype,
                                             CreateTokenEffect replacementToken) {
        this(sourceSubtype, replacementToken, 0);
    }
}
