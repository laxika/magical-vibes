package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.TravelThroughCaradhrasEffect;

@CardRegistration(set = "LTC", collectorNumber = "44")
@CardRegistration(set = "LTC", collectorNumber = "127")
public class TravelThroughCaradhras extends Card {

    public TravelThroughCaradhras() {
        addEffect(EffectSlot.SPELL, new TravelThroughCaradhrasEffect());
        addEffect(EffectSlot.SPELL, new ExileSpellEffect());
    }
}
