package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.GainLifeEqualToDyingCreatureToughnessEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DIS", collectorNumber = "16")
public class ProperBurial extends Card {

    public ProperBurial() {
        // Whenever a creature you control dies, you gain life equal to that creature's toughness.
        addTrigger(Triggers.creatureDies(ObjectRelation.YOU_CONTROL),
                new GainLifeEqualToDyingCreatureToughnessEffect());
    }
}
