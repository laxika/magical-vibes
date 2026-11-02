package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "EOE", collectorNumber = "77")
public class StarbreachWhale extends Card {

    public StarbreachWhale() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new SurveilEffect(2));
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{1}{U}"))));
    }
}
