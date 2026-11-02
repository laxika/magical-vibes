package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ReturnPermanentControlledByPlayerToHandAndPerpetuallyBecomeAngelEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YSNC", collectorNumber = "3")
public class SkylineSavior extends Card {

    public SkylineSavior() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ReturnPermanentControlledByPlayerToHandAndPerpetuallyBecomeAngelEffect(
                        new PermanentTruePredicate(), "permanent"));
    }
}
