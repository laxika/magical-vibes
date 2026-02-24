package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.CardType;
import com.github.laxika.magicalvibes.model.Keyword;

public record ReturnCardWithKeywordFromGraveyardToHandEffect(
        CardType cardType,
        Keyword keyword
) implements CardEffect {

    @Override
    public boolean canTargetGraveyard() {
        return true;
    }
}
