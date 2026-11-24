package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.effect.AddTokenCreationEffect.TokenKind;

import java.util.Objects;

/**
 * Static replacement: "if [tokens of a kind] would be created under your control, that many [replacement tokens] are
 * created instead". Divine Visitation replaces every creature token ({@link TokenKind#creature()}) with a 4/4 Angel;
 * Fisher's Talent a Fish token with a Shark from class level 2 and a Shark with an Octopus from level 3
 * ({@link TokenKind#withAnySubtype}). The shared token-creation pipeline keeps event-level modifiers such as tapped,
 * attacking, initial counters and delayed exile when applying the replacement.
 *
 * @param replaces           the tokens it replaces
 * @param replacementToken   the token created in place of each replaced token
 * @param minimumSourceLevel the class level the source needs for the replacement to apply (0: any)
 */
public record ReplaceTokenSubtypeCreationEffect(
        TokenKind replaces,
        CreateTokenEffect replacementToken,
        int minimumSourceLevel
) implements CardEffect {

    public ReplaceTokenSubtypeCreationEffect {
        Objects.requireNonNull(replaces, "replaces");
        Objects.requireNonNull(replacementToken, "replacementToken");
        if (minimumSourceLevel < 0) {
            throw new IllegalArgumentException("minimumSourceLevel must not be negative");
        }
    }

    /** "If you would create a [subtype] token, create [replacement] instead", from class level {@code level}. */
    public ReplaceTokenSubtypeCreationEffect(CardSubtype sourceSubtype, CreateTokenEffect replacementToken,
                                             int minimumSourceLevel) {
        this(TokenKind.withAnySubtype(sourceSubtype), replacementToken, minimumSourceLevel);
    }

    public ReplaceTokenSubtypeCreationEffect(CardSubtype sourceSubtype,
                                             CreateTokenEffect replacementToken) {
        this(sourceSubtype, replacementToken, 0);
    }

    /** "If one or more creature tokens would be created under your control, that many [token] are created instead." */
    public static ReplaceTokenSubtypeCreationEffect creatureTokens(CreateTokenEffect replacementToken) {
        return new ReplaceTokenSubtypeCreationEffect(TokenKind.creature(), replacementToken, 0);
    }
}
