package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.CreateTokenCopyOfSourceEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyReduceCreatureCastCostInHandEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.TriggerOptions;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YBLB", collectorNumber = "17")
public class FountainportCharmer extends Card {

    public FountainportCharmer() {
        addStaticEffect(new KickerEffect("{2}", false));
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                TriggerOptions.DEFAULT.withInterveningIf(new Kicked()),
                new CreateTokenCopyOfSourceEffect(false, 1, null, null, false, 1, 1));
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new PerpetuallyReduceCreatureCastCostInHandEffect());
    }
}
