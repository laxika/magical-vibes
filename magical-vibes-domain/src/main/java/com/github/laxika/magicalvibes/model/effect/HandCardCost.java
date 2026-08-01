package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

/**
 * A cost paid by choosing card(s) from the payer's hand and moving them to another zone.
 *
 * <p>The activation path treats every implementation identically — it collects the legal hand
 * indices, suspends activation on a hand-card choice when the payer has not picked yet, and pays
 * once a card is chosen — so a new "choose N cards from hand" cost only has to describe which
 * cards are legal and where they go.
 */
public interface HandCardCost extends CostEffect {

    /**
     * Optional predicate the chosen cards must match, or {@code null} when any card is legal.
     */
    CardPredicate predicate();

    /**
     * Human-readable label for the card quality (e.g. "land", "nonland"), used in UI prompts and
     * error messages. May be {@code null}.
     */
    String label();

    /**
     * Number of cards that must be paid.
     */
    int count();

    /**
     * When true, a chosen card's mana value must equal the ability's chosen X
     * (Knollspine Invocation "Discard a card with mana value X").
     */
    default boolean manaValueEqualsX() {
        return false;
    }

    /**
     * When true, snapshot the paid card's mana value into the stack entry's {@code xValue}
     * (Mercurial Chemister "damage equal to the discarded card's mana value").
     */
    default boolean trackManaValue() {
        return false;
    }

    /**
     * When true, every card paid for this cost must share the same name (Sphinx of the Chimes).
     * Only meaningful with {@link #count()} &gt; 1.
     */
    default boolean sameName() {
        return false;
    }

    /**
     * True when paid cards are exiled rather than put into their owner's graveyard. Exiling is not
     * a discard, so it fires no discard triggers.
     */
    default boolean exilesPaidCards() {
        return false;
    }

    /**
     * The verb used in prompts and the game log ("discard" / "exile").
     */
    default String payVerb() {
        return exilesPaidCards() ? "exile" : "discard";
    }
}
