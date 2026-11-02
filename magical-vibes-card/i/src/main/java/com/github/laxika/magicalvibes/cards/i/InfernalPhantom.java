package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.BoostSelfEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DSK", collectorNumber = "141")
public class InfernalPhantom extends Card {

    public InfernalPhantom() {
        BoostSelfEffect eerie = new BoostSelfEffect(2, 0);
        addTrigger(Triggers.enters(Triggers.ENCHANTMENT, ObjectRelation.ANOTHER_YOU_CONTROL), eerie);
        addTrigger(Triggers.youFullyUnlockRoom(), eerie);
        addTrigger(Triggers.dies(ObjectRelation.THIS), new DealDamageToAnyTargetEffect(new SourcePower()));
    }
}
