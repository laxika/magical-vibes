package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileFromHandToImprintEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.PutImprintedCreatureOntoBattlefieldEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "5DN", collectorNumber = "157")
public class SummonersEgg extends Card {

    public SummonersEgg() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD,
                new MayEffect(ExileFromHandToImprintEffect.faceDown(null, "a card"),
                        "You may exile a card from your hand face down."));
        addTrigger(Triggers.dies(ObjectRelation.THIS), new PutImprintedCreatureOntoBattlefieldEffect());
    }
}
