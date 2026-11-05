package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.condition.ColorSpentToCast;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.CantBeBlockedEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "DIS", collectorNumber = "2")
@CardRegistration(set = "GK2", collectorNumber = "2")
@CardRegistration(set = "C13", collectorNumber = "6")
public class AzoriusHerald extends Card {

    public AzoriusHerald() {
        addStaticEffect(new CantBeBlockedEffect());
        addTrigger(Triggers.enters(ObjectRelation.THIS), new GainLifeEffect(4));
        addTrigger(Triggers.enters(ObjectRelation.THIS), new ConditionalEffect(
                new NotCondition(new ColorSpentToCast(ManaColor.BLUE)), new SacrificeSelfEffect()));
    }
}
