package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;

public record ReturnCardFromGraveyardToHandEffect(
        CardType cardType,
        boolean targetGraveyard
) implements CardEffect {

    /** Any card type, targets graveyard (current behavior) */
    public ReturnCardFromGraveyardToHandEffect() {
        this(null, true);
    }

    /** Specific card type */
    public ReturnCardFromGraveyardToHandEffect(CardType cardType) {
        this(cardType, cardType == CardType.CREATURE);
    }

    @Override
    public boolean canTargetGraveyard() {
        return targetGraveyard;
    }
}
