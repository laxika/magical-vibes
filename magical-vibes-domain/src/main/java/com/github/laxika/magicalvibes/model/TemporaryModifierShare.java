package com.github.laxika.magicalvibes.model;

import java.util.EnumSet;
import java.util.Set;

/**
 * The part of a permanent's "until end of turn" power/toughness modifiers and granted keywords
 * that was created while one particular player was the active player. Runed Terror's sequential
 * phases give every player a cleanup step each turn cycle, and such an effect ends only at the
 * cleanup step of the player who was active when it began (Runed Terror ruling, CR 514.2), so the
 * cleanup step keeps the shares of the other players.
 *
 * @param power     the power modifier contributed during that player's phases
 * @param toughness the toughness modifier contributed during that player's phases
 * @param keywords  the keywords granted during that player's phases
 */
public record TemporaryModifierShare(int power, int toughness, Set<Keyword> keywords) {

    public TemporaryModifierShare {
        keywords = keywords.isEmpty() ? Set.of() : Set.copyOf(keywords);
    }

    /** Returns the combination of this share and {@code other}. */
    public TemporaryModifierShare plus(TemporaryModifierShare other) {
        Set<Keyword> combined = EnumSet.noneOf(Keyword.class);
        combined.addAll(keywords);
        combined.addAll(other.keywords);
        return new TemporaryModifierShare(power + other.power, toughness + other.toughness, combined);
    }
}
