package com.github.laxika.magicalvibes.cards.w;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.TimeTravelEffect;

@CardRegistration(set = "WHO", collectorNumber = "62")
public class WibblyWobblyTimeyWimey extends Card {

    public WibblyWobblyTimeyWimey() {
        addEffect(EffectSlot.SPELL, new TimeTravelEffect(1));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
    }
}
