package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.effect.ToxicEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONE", collectorNumber = "193")
public class VenomousBrutalizer extends Card {

    public VenomousBrutalizer() {
        addStaticEffect(new ToxicEffect(3));
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                UnlessPaysEffect.youMayPay("{1}{G}", new ProliferateEffect(new Fixed(1))));
    }
}
