package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DiscardEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "FDN", collectorNumber = "55")
public class ArbiterOfWoe extends Card {

    public ArbiterOfWoe() {
        addCastingCost(new SacrificeCreatureCost());
        addTrigger(Triggers.enters(ObjectRelation.THIS), SequenceEffect.of(
                new DiscardEffect(1, Subjects.eachOpponent()),
                new LoseLifeEffect(2, Subjects.eachOpponent()),
                new DrawCardEffect(1),
                new GainLifeEffect(2)
        ));
    }
}
