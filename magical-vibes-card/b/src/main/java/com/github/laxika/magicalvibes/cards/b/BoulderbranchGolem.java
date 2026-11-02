package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardColor;
import com.github.laxika.magicalvibes.model.amount.SourcePower;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "BRO", collectorNumber = "197")
public class BoulderbranchGolem extends Card {

    public BoulderbranchGolem() {
        addPrototype("{3}{G}", CardColor.GREEN, 3, 3);

        addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(new SourcePower()));
    }
}
