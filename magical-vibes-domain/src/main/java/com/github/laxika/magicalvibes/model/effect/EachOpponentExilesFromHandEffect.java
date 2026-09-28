package com.github.laxika.magicalvibes.model.effect;

/**
 * Each opponent exiles {@code amount} cards from their hand (their choice). Non-targeting.
 * Opponents with fewer than {@code amount} cards exile their entire hand. Uses the shared
 * {@code EXILE_FROM_HAND_CHOICE} interaction (Nicol Bolas, God-Pharaoh +1).
 *
 * @param returnOnSourceLeave whether source-tracked cards return to their owners' hands when the
 *                            source permanent leaves
 */
public record EachOpponentExilesFromHandEffect(int amount,
                                               boolean grantPlayPermissionToChooser,
                                               int exilePlayOpponentTax,
                                               boolean landsEnterTapped,
                                               boolean returnOnSourceLeave) implements CardEffect {

    public EachOpponentExilesFromHandEffect(int amount) {
        this(amount, false, 0, false, false);
    }

    /** Each opponent's chosen card returns to its owner's hand when the source leaves. */
    public static EachOpponentExilesFromHandEffect withReturnOnSourceLeave(int amount) {
        return new EachOpponentExilesFromHandEffect(amount, false, 0, false, true);
    }

    /** Each opponent may play their chosen exiled card for as long as it remains exiled. */
    public static EachOpponentExilesFromHandEffect withPlayPermission(int amount,
                                                                       int exilePlayOpponentTax,
                                                                       boolean landsEnterTapped) {
        return new EachOpponentExilesFromHandEffect(amount, true, exilePlayOpponentTax,
                landsEnterTapped, false);
    }
}
