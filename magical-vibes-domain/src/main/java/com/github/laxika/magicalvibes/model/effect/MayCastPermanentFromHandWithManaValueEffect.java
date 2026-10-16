package com.github.laxika.magicalvibes.model.effect;

/**
 * Offers a permanent spell from hand whose mana value matches the resolution-time threshold.
 *
 * <p>The {@code scryIfDeclined} form is used only on the final offered card so declining every
 * eligible card resumes the original ability with its scry fallback. {@code backFace} offers the
 * back face of a modal double-faced card, which is cast with that face's characteristics.</p>
 */
public record MayCastPermanentFromHandWithManaValueEffect(boolean scryIfDeclined, boolean backFace)
        implements CardEffect {

    public MayCastPermanentFromHandWithManaValueEffect(boolean scryIfDeclined) {
        this(scryIfDeclined, false);
    }

    public MayCastPermanentFromHandWithManaValueEffect() {
        this(false, false);
    }
}
