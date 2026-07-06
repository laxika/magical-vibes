package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.amount.DynamicAmount;
import com.github.laxika.magicalvibes.model.amount.Fixed;

/**
 * Exile {@code count} cards from the top of your library. Grants play permission for those cards
 * (of any type) until the end of your next turn.
 * <p>
 * Used by cards like Elemental Mascot ("exile the top card of your library. You may play that card
 * until the end of your next turn."). The {@code count} is a {@link DynamicAmount} so relational
 * wordings — e.g. Archaic's Agony's "equal to the excess damage dealt this way" — pass an
 * {@code EventValue} that reads the excess damage the preceding damage effect stored on the entry.
 */
public record ExileTopCardsMayPlayUntilNextTurnEffect(DynamicAmount count) implements CardEffect {

    public ExileTopCardsMayPlayUntilNextTurnEffect(int count) {
        this(new Fixed(count));
    }
}
