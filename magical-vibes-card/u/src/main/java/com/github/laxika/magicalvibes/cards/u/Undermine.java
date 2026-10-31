package com.github.laxika.magicalvibes.cards.u;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.TargetSpellControllerLosesLifeEffect;

@CardRegistration(set = "INV", collectorNumber = "282")
@CardRegistration(set = "DDH", collectorNumber = "62")
@CardRegistration(set = "SCD", collectorNumber = "254")
public class Undermine extends Card {

    public Undermine() {
        addSpellEffect(new CounterSpellEffect());
        addSpellEffect(new TargetSpellControllerLosesLifeEffect(3));
    }
}
