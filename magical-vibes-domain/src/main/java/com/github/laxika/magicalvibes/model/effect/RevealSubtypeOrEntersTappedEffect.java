package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * As-enters replacement effect: "As this permanent enters, you may reveal a [subtype] card from
 * your hand. If you don't, this permanent enters tapped." (Lorwyn dual lands, e.g. Ancient
 * Amphitheater — reveal a Giant card.)
 * <p>
 * Placed in {@link com.github.laxika.magicalvibes.model.EffectSlot#STATIC}. Handled during
 * battlefield entry by {@code BattlefieldEntryService}: if the controller has no card with the
 * subtype in hand the permanent enters tapped automatically; otherwise the controller is prompted
 * (a "you may" choice) to reveal — declining taps the permanent.
 */
public record RevealSubtypeOrEntersTappedEffect(CardSubtype subtype) implements CardEffect {
}
