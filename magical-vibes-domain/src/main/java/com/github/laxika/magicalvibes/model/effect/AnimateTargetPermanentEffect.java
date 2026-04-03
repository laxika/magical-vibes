package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;

import java.util.List;
import java.util.Set;

/**
 * Makes a target permanent into a creature with base power and toughness permanently
 * (no "until end of turn" duration). The permanent retains its other types.
 * Optionally grants subtypes (persistent) and keywords.
 * Used by Tezzeret, Agent of Bolas -1, Waker of the Wilds, and similar effects.
 */
public record AnimateTargetPermanentEffect(int power, int toughness,
                                           List<CardSubtype> grantedSubtypes,
                                           Set<Keyword> grantedKeywords) implements CardEffect {

    /** Simple constructor for effects that only set base P/T (e.g. Tezzeret). */
    public AnimateTargetPermanentEffect(int power, int toughness) {
        this(power, toughness, List.of(), Set.of());
    }

    @Override
    public boolean canTargetPermanent() { return true; }
}
