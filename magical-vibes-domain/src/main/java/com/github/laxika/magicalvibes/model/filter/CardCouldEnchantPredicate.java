package com.github.laxika.magicalvibes.model.filter;

import com.github.laxika.magicalvibes.model.effect.PermanentSubject;

import java.util.Objects;

/**
 * Matches an Aura card that could enchant one of the permanents {@code permanents} names: "search your library for an
 * Aura card that could enchant that creature" (Sovereigns of Lost Alara, Auratouched Mage). The Aura's enchant ability
 * and the effects that would make it illegal to attach (protection, "can't be enchanted") are checked against each
 * permanent as it is, or as it last existed on the battlefield (CR 303.4a, 608.2h, 702.16c); hexproof and shroud don't
 * matter, as nothing is targeted. The subject needs the resolving entry, so the search binds the predicate to the cards
 * that match as it begins; unbound it matches nothing.
 */
public record CardCouldEnchantPredicate(PermanentSubject permanents) implements CardPredicate {

    public CardCouldEnchantPredicate {
        Objects.requireNonNull(permanents, "permanents");
    }
}
