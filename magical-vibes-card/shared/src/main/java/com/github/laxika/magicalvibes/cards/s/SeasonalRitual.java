package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.AwardAnyColorManaEffect;

public class SeasonalRitual extends Card {

    public SeasonalRitual() {
        addEffect(EffectSlot.SPELL, new AwardAnyColorManaEffect());
    }
}
