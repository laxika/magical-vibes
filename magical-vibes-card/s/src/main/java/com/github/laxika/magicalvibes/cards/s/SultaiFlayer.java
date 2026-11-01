package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.TriggeringPermanentConditionalEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentToughnessAtLeastPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "KTK", collectorNumber = "152")
@CardRegistration(set = "IMA", collectorNumber = "186")
public class SultaiFlayer extends Card {

    private static final TriggeringPermanentConditionalEffect DEATH_TRIGGER =
            new TriggeringPermanentConditionalEffect(
                    new PermanentToughnessAtLeastPredicate(4), new GainLifeEffect(4));

    public SultaiFlayer() {
        addTrigger(Triggers.creatureDies(ObjectRelation.YOU_CONTROL), DEATH_TRIGGER);
    }
}
