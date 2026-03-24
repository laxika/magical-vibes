package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardSubtype;

/**
 * Exile target permanent, then immediately return it to the battlefield under its owner's control.
 * Optionally, if the exiled permanent had the specified subtype, a bonus effect is applied
 * (e.g. draw a card if it was a Pirate).
 *
 * <p>Used by Siren's Ruse, Cloudshift, Essence Flux, and similar instant-speed flicker spells.</p>
 */
public record ExileTargetPermanentAndReturnImmediatelyEffect(
        CardSubtype bonusSubtype,
        CardEffect bonusEffect
) implements CardEffect {

    /**
     * Plain flicker with no conditional bonus.
     */
    public ExileTargetPermanentAndReturnImmediatelyEffect() {
        this(null, null);
    }

    @Override
    public boolean canTargetPermanent() {
        return true;
    }
}
