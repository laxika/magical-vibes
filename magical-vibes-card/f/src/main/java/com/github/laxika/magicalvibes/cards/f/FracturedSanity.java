package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.MillEffect;
import com.github.laxika.magicalvibes.model.effect.MillRecipient;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "MH2", collectorNumber = "44")
public class FracturedSanity extends Card {

    public FracturedSanity() {
        addSpellEffect(new MillEffect(14, MillRecipient.EACH_OPPONENT));
        addTrigger(Triggers.youCycleThisCard(), new MillEffect(4, MillRecipient.EACH_OPPONENT));
        addCycling("{1}{U}");
    }
}
