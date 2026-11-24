package com.github.laxika.magicalvibes.model.effect;

/**
 * "Create a [token] for each card type among cards discarded this way": one copy of the token for each distinct card
 * type among the cards the most recent {@code StepKind.DISCARD} step of the resolution discarded (Occult Epiphany).
 */
public record CreateTokensForDistinctDiscardedCardTypesEffect(CreateTokenEffect tokenTemplate)
        implements CardEffect {

    public CreateTokensForDistinctDiscardedCardTypesEffect {
        if (tokenTemplate == null) {
            throw new IllegalArgumentException("A token template is required");
        }
    }
}
