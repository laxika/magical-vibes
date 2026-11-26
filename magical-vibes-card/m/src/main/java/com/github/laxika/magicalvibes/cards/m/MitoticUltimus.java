package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.GreatestPowerAmongControlled;
import com.github.laxika.magicalvibes.model.effect.ConjureCardsEffect;
import com.github.laxika.magicalvibes.model.effect.ConjureDestination;
import com.github.laxika.magicalvibes.model.effect.ReduceOwnCastCostEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "YDFT", collectorNumber = "19")
public class MitoticUltimus extends Card {

    public MitoticUltimus() {
        addStaticEffect(new ReduceOwnCastCostEffect(new GreatestPowerAmongControlled()));
        addTrigger(Triggers.dies(ObjectRelation.THIS),
                ConjureCardsEffect.named("M11", "185", ConjureDestination.ontoBattlefield()).times(2));
    }
}
