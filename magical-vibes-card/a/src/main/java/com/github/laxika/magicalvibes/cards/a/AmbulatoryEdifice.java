package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.QueueReflexiveAbilityEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ONE", collectorNumber = "79")
public class AmbulatoryEdifice extends Card {

    public AmbulatoryEdifice() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new MayPayManaEffect("{0}", 2,
                        new QueueReflexiveAbilityEffect(new BoostTargetCreatureEffect(-1, -1)),
                        "Pay 2 life to give target creature -1/-1 until end of turn?"));
    }
}
