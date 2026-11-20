package com.github.laxika.magicalvibes.model.effect;

import java.util.Objects;

/**
 * Each opponent exiles {@code amount} cards from their hand (their choice). Non-targeting.
 * Opponents with fewer than {@code amount} cards exile their entire hand. Uses the shared
 * {@code EXILE_FROM_HAND_CHOICE} interaction (Nicol Bolas, God-Pharaoh +1). With another
 * {@code players}, "each player exiles two cards from their hand" ({@code eachPlayer()}, Mind Swords) or
 * "exile a card from your hand" ({@code you()}, Ego Drain): the named players are asked in APNAP order
 * (CR 101.4), a player with an empty hand is skipped. Without a play permission the cards are exiled
 * with the source permanent when there is one (Azor's Gateway's "cards exiled with Azor's Gateway").
 *
 * @param returnOnSourceLeave whether source-tracked cards return to their owners' hands when the
 *                            source permanent leaves
 * @param players             who exiles; {@code eachOpponent()} by default, never a target
 */
public record EachOpponentExilesFromHandEffect(int amount,
                                               boolean grantPlayPermissionToChooser,
                                               int exilePlayOpponentTax,
                                               boolean landsEnterTapped,
                                               boolean returnOnSourceLeave,
                                               PlayerSubject players) implements CardEffect {

    public EachOpponentExilesFromHandEffect {
        Objects.requireNonNull(players, "players");
        if (players.declaresTarget()) {
            throw new IllegalArgumentException("The hand exile names its players without a target: " + players);
        }
    }

    public EachOpponentExilesFromHandEffect(int amount, boolean grantPlayPermissionToChooser,
                                            int exilePlayOpponentTax, boolean landsEnterTapped,
                                            boolean returnOnSourceLeave) {
        this(amount, grantPlayPermissionToChooser, exilePlayOpponentTax, landsEnterTapped, returnOnSourceLeave,
                Subjects.eachOpponent());
    }

    /** "[Players] exile {@code amount} cards from their hand": {@code eachPlayer()}, {@code you()}. */
    public static EachOpponentExilesFromHandEffect byPlayers(PlayerSubject players, int amount) {
        return new EachOpponentExilesFromHandEffect(amount, false, 0, false, false, players);
    }

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
