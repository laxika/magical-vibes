package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "5DN", collectorNumber = "126")
public class GraftedWargear extends Card {

    public GraftedWargear() {
        addStaticEffect(new StaticBoostEffect(3, 2, Subjects.attachedCreature()));
        addTrigger(Triggers.becomesUnattachedFromPermanent(), SacrificeSelfEffect.ofTriggeringPermanent());
        addActivatedAbility(new EquipActivatedAbility("{0}"));
    }
}
