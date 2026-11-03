package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FDN", collectorNumber = "28")
@CardRegistration(set = "FDC", collectorNumber = "56")
public class VanguardSeraph extends Card {

    public VanguardSeraph() {
        // Whenever you gain life for the first time each turn, surveil 1.
        addTrigger(Triggers.youGainLife().firstTimeEachTurn(), new SurveilEffect(1));
    }
}
