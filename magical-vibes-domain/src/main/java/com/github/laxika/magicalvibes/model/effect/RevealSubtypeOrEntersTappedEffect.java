package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.condition.Condition;

import java.util.Set;

/**
 * As-enters replacement effect: "As this permanent enters, you may reveal a [subtype] card from
 * your hand. If you don't, this permanent enters tapped." (Lorwyn dual lands, e.g. Ancient
 * Amphitheater — reveal a Giant card.) Multiple subtypes are accepted when the card text says
 * "an [subtype] or [subtype] card."
 * <p>
 * Placed in {@link com.github.laxika.magicalvibes.model.EffectSlot#STATIC}. Handled during
 * battlefield entry by {@code BattlefieldEntryService}: if the controller has no card with the
 * subtype in hand the permanent enters tapped automatically; otherwise the controller is prompted
 * (a "you may" choice) to reveal — declining taps the permanent.
 * <p>
 * When {@code entersUntappedIf} is non-null ("enters tapped unless you revealed a [subtype] card
 * this way or you control a [subtype]", Fortified Beachhead), the reveal is still offered whenever
 * a matching card is in hand, but the permanent stays untapped when that condition holds even if
 * nothing is revealed.
 *
 * @param subtypes         subtypes of the card that may be revealed
 * @param entersUntappedIf condition that keeps the permanent untapped without a reveal, or
 *                         {@code null} when only a reveal keeps it untapped
 */
public record RevealSubtypeOrEntersTappedEffect(Set<CardSubtype> subtypes, Condition entersUntappedIf)
        implements CardEffect {

    public RevealSubtypeOrEntersTappedEffect {
        subtypes = Set.copyOf(subtypes);
    }

    public RevealSubtypeOrEntersTappedEffect(Set<CardSubtype> subtypes) {
        this(subtypes, null);
    }

    public RevealSubtypeOrEntersTappedEffect(CardSubtype subtype) {
        this(Set.of(subtype), null);
    }

    public RevealSubtypeOrEntersTappedEffect(CardSubtype subtype, Condition entersUntappedIf) {
        this(Set.of(subtype), entersUntappedIf);
    }
}
