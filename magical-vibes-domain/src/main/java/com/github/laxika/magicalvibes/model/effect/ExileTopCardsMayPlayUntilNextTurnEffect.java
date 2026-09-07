package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Exile {@code count} cards from the top of the controller's library. Grants play permission for
 * those cards (of any type) until the end of that player's next turn.
 * <p>
 * Used by cards like Elemental Mascot ("exile the top card of your library. You may play that card
 * until the end of your next turn."). The {@code count} is a {@link DynamicAmount} so relational
 * wordings — e.g. Archaic's Agony's "equal to the excess damage dealt this way" — pass an
 * {@code EventValue} that reads the excess damage the preceding damage effect stored on the entry.
 *
 * @param useTriggeringPermanentController whether to use the controller captured for the
 *                                         triggering permanent instead of the stack entry's
 *                                         controller; used by global damage triggers whose
 *                                         wording refers to the damaged creature's controller
 */
public record ExileTopCardsMayPlayUntilNextTurnEffect(
        DynamicAmount count,
        boolean useTriggeringPermanentController
) implements CardEffect {

    public ExileTopCardsMayPlayUntilNextTurnEffect(DynamicAmount count) {
        this(count, false);
    }

    public ExileTopCardsMayPlayUntilNextTurnEffect(int count) {
        this(new Fixed(count));
    }

    public static ExileTopCardsMayPlayUntilNextTurnEffect forTriggeringPermanentController(
            DynamicAmount count) {
        return new ExileTopCardsMayPlayUntilNextTurnEffect(count, true);
    }
}
