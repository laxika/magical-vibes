package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.Keyword;

import java.util.Objects;
import java.util.Set;

/**
 * "It perpetually gains [keywords]": the cards {@code cards} names gain them ("that card perpetually gains menace" after
 * a seek, {@code Subjects.cardsOf(Subjects.triggeringSpell())} for "when you cast a creature spell, it perpetually gains
 * haste", {@code Subjects.cardsOf(Subjects.triggering())} for "whenever another creature enters, it perpetually gains
 * haste"), each only while it is still the object named (CR 400.7).
 *
 * @param keywords the keywords
 * @param cards    the cards
 */
public record PerpetuallyGrantKeywordsToTriggeringCardEffect(Set<Keyword> keywords, CardSubject cards)
        implements CardEffect {

    /** The cards {@code cards} names gain them. */
    public static PerpetuallyGrantKeywordsToTriggeringCardEffect toCards(CardSubject cards, Keyword... keywords) {
        return new PerpetuallyGrantKeywordsToTriggeringCardEffect(Set.of(keywords), cards);
    }

    public PerpetuallyGrantKeywordsToTriggeringCardEffect {
        if (keywords == null || keywords.isEmpty()) {
            throw new IllegalArgumentException("At least one keyword is required");
        }
        keywords = Set.copyOf(keywords);
        Objects.requireNonNull(cards, "cards");
    }
}
