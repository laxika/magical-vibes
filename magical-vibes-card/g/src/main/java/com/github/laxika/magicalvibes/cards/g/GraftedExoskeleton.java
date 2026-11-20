package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeSelfEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "SOM", collectorNumber = "162")
@CardRegistration(set = "TD2", collectorNumber = "74")
@CardRegistration(set = "ONC", collectorNumber = "132")
public class GraftedExoskeleton extends Card {

    public GraftedExoskeleton() {
        addStaticEffect(new StaticBoostEffect(2, 2, Subjects.attachedCreature()));
        addStaticEffect(new GrantKeywordEffect(Keyword.INFECT, Subjects.attachedCreature()));
        addTrigger(Triggers.becomesUnattachedFromPermanent(), SacrificeSelfEffect.ofTriggeringPermanent());
        addActivatedAbility(new EquipActivatedAbility("{2}"));
    }
}
