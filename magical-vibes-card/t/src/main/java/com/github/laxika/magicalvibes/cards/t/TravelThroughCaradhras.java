package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileSpellEffect;
import com.github.laxika.magicalvibes.model.effect.TravelThroughCaradhrasEffect;

@CardRegistration(set = "LTC", collectorNumber = "44")
@CardRegistration(set = "LTC", collectorNumber = "127")
@CardRegistration(set = "LTC", collectorNumber = "446")
public class TravelThroughCaradhras extends Card {

    public TravelThroughCaradhras() {
        addSpellEffect(new TravelThroughCaradhrasEffect());
        addSpellEffect(new ExileSpellEffect());
    }
}
