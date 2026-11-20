package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * "You may play / cast that card without paying its mana cost for as long as it remains exiled": each card
 * {@link #cards()} names that is in exile gets a play permission for the effect's controller, without paying its mana
 * cost, that ends as the card leaves exile (CR 400.7). A card the subject finds in another zone gets none. Spelljack's
 * and Kheru Spellsnatcher's "If that spell is countered this way, exile it instead … You may play it" is
 * {@code CounterSpellEffect(EXILE)} and this over {@code Subjects.cardsThisWay(StepKind.COUNTER_SPELL)}.
 *
 * @param cards the cards that may be played
 */
public record AllowPlayCardsWithoutPayingManaCostWhileExiledEffect(CardSubject cards) implements CardEffect {

    public AllowPlayCardsWithoutPayingManaCostWhileExiledEffect {
        Objects.requireNonNull(cards, "cards");
    }
}
