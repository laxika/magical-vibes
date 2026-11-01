package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongControlled;
import com.github.laxika.magicalvibes.model.effect.ConjureCardToBattlefieldEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YDFT", collectorNumber = "19")
public class MitoticUltimus extends Card {

    public MitoticUltimus() {
        addStaticEffect(new ReduceOwnCastCostEffect(new GreatestPowerAmongControlled()));
        addTrigger(Triggers.dies(ObjectRelation.THIS), SequenceEffect.of(
                new ConjureCardToBattlefieldEffect("M11", "185"),
                new ConjureCardToBattlefieldEffect("M11", "185")));
    }
}
