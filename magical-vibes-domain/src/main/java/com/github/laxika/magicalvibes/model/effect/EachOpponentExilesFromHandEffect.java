package com.github.laxika.magicalvibes.model.effect;

/**
 * Each opponent exiles {@code amount} cards from their hand (their choice). Non-targeting.
 * Opponents with fewer than {@code amount} cards exile their entire hand. Uses the shared
 * {@code EXILE_FROM_HAND_CHOICE} interaction (Nicol Bolas, God-Pharaoh +1).
 */
public record EachOpponentExilesFromHandEffect(int amount,
                                               boolean grantPlayPermissionToChooser,
                                               int exilePlayOpponentTax,
                                               boolean landsEnterTapped) implements CardEffect {

    public EachOpponentExilesFromHandEffect(int amount) {
        this(amount, false, 0, false);
    }

    /** Each opponent may play their chosen exiled card for as long as it remains exiled. */
    public static EachOpponentExilesFromHandEffect withPlayPermission(int amount,
                                                                       int exilePlayOpponentTax,
                                                                       boolean landsEnterTapped) {
        return new EachOpponentExilesFromHandEffect(amount, true, exilePlayOpponentTax,
                landsEnterTapped);
    }
}
