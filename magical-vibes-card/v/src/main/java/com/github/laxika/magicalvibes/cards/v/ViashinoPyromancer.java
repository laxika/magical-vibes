package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "M19", collectorNumber = "166")
public class ViashinoPyromancer extends Card {

    public ViashinoPyromancer() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), DealDamageEffect.toTargetPlayerOrPlaneswalker(2));
    }
}
