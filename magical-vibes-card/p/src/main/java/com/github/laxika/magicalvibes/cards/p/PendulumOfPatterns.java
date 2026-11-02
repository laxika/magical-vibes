package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.ActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfCost;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "M19", collectorNumber = "288")
@CardRegistration(set = "KLR", collectorNumber = "261")
public class PendulumOfPatterns extends Card {

    public PendulumOfPatterns() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(3));
        addActivatedAbility(new ActivatedAbility(
                true,
                "{5}",
                List.of(new SacrificeSelfCost(), new DrawCardEffect(1)),
                "{5}, {T}, Sacrifice this artifact: Draw a card."
        ));
    }
}
