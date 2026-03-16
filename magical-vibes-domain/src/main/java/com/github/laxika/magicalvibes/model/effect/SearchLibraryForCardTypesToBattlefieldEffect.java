package com.github.laxika.magicalvibes.model.effect;

import com.github.laxika.magicalvibes.model.filter.CardPredicate;

public record SearchLibraryForCardTypesToBattlefieldEffect(CardPredicate filter,
                                                           boolean entersTapped, int maxCount) implements CardEffect {

    public SearchLibraryForCardTypesToBattlefieldEffect(CardPredicate filter,
                                                        boolean entersTapped) {
        this(filter, entersTapped, 1);
    }
}
