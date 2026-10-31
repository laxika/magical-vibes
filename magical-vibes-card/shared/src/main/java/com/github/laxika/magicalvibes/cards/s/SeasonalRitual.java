package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;

public class SeasonalRitual extends Card {

    public SeasonalRitual() {
        addSpellEffect(new AwardAnyColorManaEffect());
    }
}
