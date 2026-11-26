package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "BRO", collectorNumber = "154")
public class TyrantOfKherRidges extends Card {

    public TyrantOfKherRidges() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), DealDamageEffect.toAnyTarget(4));
        addActivatedAbility(new ActivatedAbility(false, "{R}", List.of(BoostEffect.self(1, 0)),
                "{R}: This creature gets +1/+0 until end of turn."));
    }
}
