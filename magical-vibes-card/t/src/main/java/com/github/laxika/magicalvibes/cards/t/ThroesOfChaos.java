package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Retrace;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MH1", collectorNumber = "150")
@CardRegistration(set = "WHO", collectorNumber = "818")
@CardRegistration(set = "WHO", collectorNumber = "227")
@CardRegistration(set = "MB2", collectorNumber = "62")
@CardRegistration(set = "SOC", collectorNumber = "257")
@CardRegistration(set = "AFC", collectorNumber = "146")
public class ThroesOfChaos extends Card {

    public ThroesOfChaos() {
        addTrigger(Triggers.youCastThisSpell(), new CascadeEffect());
        addCastingOption(new Retrace());
    }
}
