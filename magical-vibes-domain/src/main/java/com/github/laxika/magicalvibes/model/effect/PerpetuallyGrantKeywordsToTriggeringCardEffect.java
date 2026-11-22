package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

import java.util.Set;

/**
 * Perpetually grants keywords to the card that caused the surrounding trigger, or to the cards {@code cards} names
 * ("that card perpetually gains menace" after a seek).
 *
 * @param keywords the keywords
 * @param cards    the cards; {@code null} for the triggering card
 */
public record PerpetuallyGrantKeywordsToTriggeringCardEffect(Set<Keyword> keywords, CardSubject cards)
        implements CardEffect {

    /** The triggering card gains them. */
    public PerpetuallyGrantKeywordsToTriggeringCardEffect(Set<Keyword> keywords) {
        this(keywords, null);
    }

    /** The cards {@code cards} names gain them. */
    public static PerpetuallyGrantKeywordsToTriggeringCardEffect toCards(CardSubject cards, Keyword... keywords) {
        return new PerpetuallyGrantKeywordsToTriggeringCardEffect(Set.of(keywords), cards);
    }

    public PerpetuallyGrantKeywordsToTriggeringCardEffect {
        if (keywords == null || keywords.isEmpty()) {
            throw new IllegalArgumentException("At least one keyword is required");
        }
        keywords = Set.copyOf(keywords);
    }
}
