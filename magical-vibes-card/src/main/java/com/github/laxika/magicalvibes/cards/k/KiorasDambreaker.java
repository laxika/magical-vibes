package com.github.laxika.magicalvibes.cards.k;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;

@CardRegistration(set = "WAR", collectorNumber = "58")
public class KiorasDambreaker extends Card {

    public KiorasDambreaker() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new ProliferateEffect());
    }
}
