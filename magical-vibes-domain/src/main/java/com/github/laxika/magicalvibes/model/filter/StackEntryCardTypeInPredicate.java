package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.CardType;

import java.util.Set;

/**
 * Matches stack entries whose card has at least one of the given card types. For a spell that is
 * the spell's own type; for an activated or triggered ability it is the type of the ability's
 * <em>source</em> card, which is what "an activated ability from an artifact source" asks about
 * (Brown Ouphe). Combine with {@link StackEntryNotPredicate} for "non-[type]" restrictions.
 */
public record StackEntryCardTypeInPredicate(Set<CardType> cardTypes) implements StackEntryPredicate {
}
