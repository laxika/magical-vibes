package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.GraveyardCardThreshold;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.ReturnToHandEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BLB", collectorNumber = "236")
public class TidecallerMentor extends Card {

    public TidecallerMentor() {
        target(TargetFilters.nonlandPermanent(), 0, 1)
                .addTrigger(Triggers.enters(ObjectRelation.THIS), new ConditionalEffect(
                        new GraveyardCardThreshold(7, null),
                        ReturnToHandEffect.target()));
    }
}
