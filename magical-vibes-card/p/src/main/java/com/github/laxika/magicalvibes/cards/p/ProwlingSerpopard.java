package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CantBeCounteredEffect;
import com.github.laxika.magicalvibes.model.effect.ControllerCreatureSpellsCantBeCounteredEffect;

@CardRegistration(set = "AKH", collectorNumber = "180")
@CardRegistration(set = "AKR", collectorNumber = "209")
public class ProwlingSerpopard extends Card {

    public ProwlingSerpopard() {
        addStaticEffect(new CantBeCounteredEffect());
        addStaticEffect(new ControllerCreatureSpellsCantBeCounteredEffect());
    }
}
