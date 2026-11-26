package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "DMU", collectorNumber = "20")
public class HeroicCharge extends Card {

    public HeroicCharge() {
        addStaticEffect(new KickerEffect("{1}{R}"));
        addSpellEffect(BoostEffect.creaturesYouControl(2, 1));
        addSpellEffect(new ConditionalEffect(new Kicked(),
                new GrantKeywordEffect(Keyword.TRAMPLE, Subjects.otherCreaturesYouControl())));
    }
}
