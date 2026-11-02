package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.ProliferateEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONE", collectorNumber = "201")
public class EzuriStalkerOfSpheres extends Card {

    public EzuriStalkerOfSpheres() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new MayPayManaEffect("{3}", new ProliferateEffect(new Fixed(2)),
                        "Pay {3} to proliferate twice?"));
        addTrigger(Triggers.youProliferate(), new DrawCardEffect());
    }
}
