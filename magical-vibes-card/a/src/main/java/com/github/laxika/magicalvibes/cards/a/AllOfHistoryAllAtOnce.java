package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.StormEffect;
import com.github.laxika.magicalvibes.model.effect.TimeTravelEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "WHO", collectorNumber = "34")
@CardRegistration(set = "WHO", collectorNumber = "352")
@CardRegistration(set = "WHO", collectorNumber = "639")
@CardRegistration(set = "WHO", collectorNumber = "943")
public class AllOfHistoryAllAtOnce extends Card {

    public AllOfHistoryAllAtOnce() {
        addSpellEffect(new TimeTravelEffect(1));
        addTrigger(Triggers.youCastThisSpell(), new StormEffect());
    }
}
