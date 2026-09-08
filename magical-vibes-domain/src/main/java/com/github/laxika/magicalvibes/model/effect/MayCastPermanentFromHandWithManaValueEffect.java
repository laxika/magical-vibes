package com.github.laxika.magicalvibes.model.effect;

/**
 * Offers a permanent spell from hand whose mana value matches the resolution-time threshold.
 *
 * <p>The {@code scryIfDeclined} form is used only on the final offered card so declining every
 * eligible card resumes the original ability with its scry fallback.</p>
 */
public record MayCastPermanentFromHandWithManaValueEffect(boolean scryIfDeclined) implements CardEffect {

    public MayCastPermanentFromHandWithManaValueEffect() {
        this(false);
    }
}
