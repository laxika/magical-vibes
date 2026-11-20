package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.model.EquipActivatedAbility;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.StaticBoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "ARB", collectorNumber = "65")
@CardRegistration(set = "DDH", collectorNumber = "28")
@CardRegistration(set = "C13", collectorNumber = "178")
@CardRegistration(set = "AFC", collectorNumber = "180")
@CardRegistration(set = "PIP", collectorNumber = "211")
@CardRegistration(set = "PIP", collectorNumber = "739")
@CardRegistration(set = "FIC", collectorNumber = "320")
@CardRegistration(set = "C17", collectorNumber = "162")
public class BehemothSledge extends Card {

    public BehemothSledge() {
        addStaticEffect(new StaticBoostEffect(2, 2, Subjects.attachedCreature()));
        addStaticEffect(new GrantKeywordEffect(Keyword.TRAMPLE, Subjects.attachedCreature()));
        addStaticEffect(new GrantKeywordEffect(Keyword.LIFELINK, Subjects.attachedCreature()));
        addActivatedAbility(new EquipActivatedAbility("{3}"));
    }
}
