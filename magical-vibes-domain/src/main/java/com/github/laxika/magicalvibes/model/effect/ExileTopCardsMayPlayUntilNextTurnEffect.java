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
 * @param freeCastIfCastFromGraveyard whether cards exiled by this effect may be played without
 *                                    paying their mana costs when the spell was cast from a graveyard
 * @param duringOwnersNextTurnOnly whether the permission is worded "during your next turn" rather
 *                                 than "until the end of your next turn", so it cannot be used
 *                                 on the current turn or the turns before the owner's next one
 */
public record ExileTopCardsMayPlayUntilNextTurnEffect(
        DynamicAmount count,
        boolean useTriggeringPermanentController,
        boolean freeCastIfCastFromGraveyard,
        boolean duringOwnersNextTurnOnly
) implements CardEffect, CombatDamageAmountAwareEffect {

    @Override
    public DynamicAmount combatDamageAmount() {
        return count;
    }

    public ExileTopCardsMayPlayUntilNextTurnEffect(DynamicAmount count) {
        this(count, false, false, false);
    }

    public ExileTopCardsMayPlayUntilNextTurnEffect(DynamicAmount count,
                                                    boolean useTriggeringPermanentController) {
        this(count, useTriggeringPermanentController, false, false);
    }

    public ExileTopCardsMayPlayUntilNextTurnEffect(int count) {
        this(new Fixed(count));
    }

    public ExileTopCardsMayPlayUntilNextTurnEffect(int count,
                                                    boolean useTriggeringPermanentController,
                                                    boolean freeCastIfCastFromGraveyard) {
        this(new Fixed(count), useTriggeringPermanentController, freeCastIfCastFromGraveyard, false);
    }

    public static ExileTopCardsMayPlayUntilNextTurnEffect duringOwnersNextTurn(int count) {
        return new ExileTopCardsMayPlayUntilNextTurnEffect(new Fixed(count), false, false, true);
    }

    public static ExileTopCardsMayPlayUntilNextTurnEffect forTriggeringPermanentController(
            DynamicAmount count) {
        return new ExileTopCardsMayPlayUntilNextTurnEffect(count, true);
    }
}
