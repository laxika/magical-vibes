package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.CascadeEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WHO", collectorNumber = "88")
@CardRegistration(set = "WHO", collectorNumber = "693")
@CardRegistration(set = "WHO", collectorNumber = "387")
@CardRegistration(set = "WHO", collectorNumber = "978")
@CardRegistration(set = "MSC", collectorNumber = "165")
@CardRegistration(set = "MSC", collectorNumber = "363")
public class IntoTheTimeVortex extends Card {

    public IntoTheTimeVortex() {
        addTrigger(Triggers.youCastThisSpell(), new CascadeEffect());
    }
}
