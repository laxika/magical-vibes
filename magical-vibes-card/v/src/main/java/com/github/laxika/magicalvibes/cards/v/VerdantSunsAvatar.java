package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.amount.TriggeringPermanentToughness;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "XLN", collectorNumber = "213")
@CardRegistration(set = "LCC", collectorNumber = "262")
@CardRegistration(set = "C21", collectorNumber = "208")
@CardRegistration(set = "FDC", collectorNumber = "239")
public class VerdantSunsAvatar extends Card {

    public VerdantSunsAvatar() {
        // Whenever this creature or another creature you control enters, you gain life equal to that creature's
        // toughness, read as the ability resolves (CR 608.2h).
        addTrigger(Triggers.enters(Triggers.CREATURE, ObjectRelation.THIS_OR_ANOTHER_YOU_CONTROL),
                new GainLifeEffect(new TriggeringPermanentToughness()));
    }
}
