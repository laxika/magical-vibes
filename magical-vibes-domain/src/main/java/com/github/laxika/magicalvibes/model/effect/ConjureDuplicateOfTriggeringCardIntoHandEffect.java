package com.github.laxika.magicalvibes.model.effect;

/**
 * Conjures a duplicate of the card that caused this trigger (a spell cast or a land played) into its controller's
 * hand. When {@code discardAtNextEndStep} is set, that exact duplicate is discarded at the beginning of the
 * controller's next end step.
 */
public record ConjureDuplicateOfTriggeringCardIntoHandEffect(boolean discardAtNextEndStep) implements CardEffect {

    public ConjureDuplicateOfTriggeringCardIntoHandEffect() {
        this(false);
    }
}
