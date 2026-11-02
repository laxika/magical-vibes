package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.ExileTargetCardFromGraveyardMayPlayWhileExiledEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceNonHandSpellCastCostEffect;
import com.github.laxika.magicalvibes.model.filter.CardIsPermanentPredicate;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "OTC", collectorNumber = "33")
@CardRegistration(set = "OTC", collectorNumber = "69")
public class SavvyTrader extends Card {

    public SavvyTrader() {
        addTrigger(Triggers.enters(ObjectRelation.THIS),
                new ExileTargetCardFromGraveyardMayPlayWhileExiledEffect(
                        new CardIsPermanentPredicate(), true));
        addStaticEffect(new ReduceNonHandSpellCastCostEffect(1));
    }
}
