package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.BlightEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.LoseLifeEffect;
import com.github.laxika.magicalvibes.model.effect.MayEffect;
import com.github.laxika.magicalvibes.model.effect.SequenceEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ECL", collectorNumber = "90")
public class BlightedBlackthorn extends Card {

    public BlightedBlackthorn() {
        MayEffect blight = new MayEffect(
                new BlightEffect(2, SequenceEffect.of(new DrawCardEffect(1), new LoseLifeEffect(1))),
                "Blight 2?");
        addTrigger(Triggers.enters(ObjectRelation.THIS), blight);
        addTrigger(Triggers.attacks(), blight);
    }
}
