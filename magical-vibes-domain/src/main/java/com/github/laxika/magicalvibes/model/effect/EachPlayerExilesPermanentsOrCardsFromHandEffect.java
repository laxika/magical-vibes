package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;

/**
 * "Each player exiles X permanents they control and/or cards from their hand" (Descent into
 * Madness). Each affected player makes a single mixed-zone selection over their own battlefield
 * and hand; the exiles happen simultaneously once every player has chosen (CR 101.4), so no
 * player sees another's picks take effect before making their own.
 *
 * <p>{@code amount} is evaluated once at resolution against the source permanent — Descent into
 * Madness passes {@code CountersOnSource(CounterType.DESPAIR)}, read after the counter for this
 * trigger has already been placed by the preceding step of its {@link SequenceEffect}.
 *
 * <p>The choice is mandatory: a player who controls/holds fewer than X objects exiles all of
 * them.
 */
public record EachPlayerExilesPermanentsOrCardsFromHandEffect(DynamicAmount amount,
                                                               boolean opponentsOnly)
        implements CardEffect {

    public EachPlayerExilesPermanentsOrCardsFromHandEffect(DynamicAmount amount) {
        this(amount, false);
    }

    public static EachPlayerExilesPermanentsOrCardsFromHandEffect opponents(DynamicAmount amount) {
        return new EachPlayerExilesPermanentsOrCardsFromHandEffect(amount, true);
    }
}
