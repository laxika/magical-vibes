package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCommanderPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONC", collectorNumber = "8")
@CardRegistration(set = "ONC", collectorNumber = "46")
@CardRegistration(set = "FDC", collectorNumber = "41")
public class NornsChoirmaster extends Card {

    public NornsChoirmaster() {
        // Whenever a commander you control enters or attacks, proliferate.
        addTrigger(Triggers.enters(null, ObjectRelation.ANOTHER_YOU_CONTROL),
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsCommanderPredicate(), new ProliferateEffect()));
        addEffect(EffectSlot.ON_ALLY_CREATURE_ATTACKS,
                new TriggeringPermanentConditionalEffect(
                        new PermanentIsCommanderPredicate(), new ProliferateEffect()));
    }
}
