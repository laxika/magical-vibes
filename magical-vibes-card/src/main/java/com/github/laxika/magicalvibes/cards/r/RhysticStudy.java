package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardUnlessPaysEffect;

@CardRegistration(set = "PCY", collectorNumber = "45")
public class RhysticStudy extends Card {

    public RhysticStudy() {
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL, new DrawCardUnlessPaysEffect(1, 1));
    }
}
