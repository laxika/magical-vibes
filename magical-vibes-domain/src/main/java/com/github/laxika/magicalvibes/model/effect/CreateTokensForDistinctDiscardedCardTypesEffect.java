package com.github.laxika.magicalvibes.model.effect;

/** Creates one copy of the supplied token for each distinct card type among discarded cards. */
public record CreateTokensForDistinctDiscardedCardTypesEffect(CreateTokenEffect tokenTemplate)
        implements InlineDiscardFollowUpEffect {

    public CreateTokensForDistinctDiscardedCardTypesEffect {
        if (tokenTemplate == null) {
            throw new IllegalArgumentException("A token template is required");
        }
    }
}
