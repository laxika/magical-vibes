package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardUnlessPaysEffect;

@CardRegistration(set = "PCY", collectorNumber = "45")
@CardRegistration(set = "WOT", collectorNumber = "25")
@CardRegistration(set = "WOT", collectorNumber = "71")
@CardRegistration(set = "WOT", collectorNumber = "91")
@CardRegistration(set = "FCA", collectorNumber = "31")
public class RhysticStudy extends Card {

    public RhysticStudy() {
        addEffect(EffectSlot.ON_OPPONENT_CASTS_SPELL, new DrawCardUnlessPaysEffect(1, 1));
    }
}
