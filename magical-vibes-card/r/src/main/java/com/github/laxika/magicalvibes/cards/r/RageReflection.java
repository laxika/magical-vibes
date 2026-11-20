package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "SHM", collectorNumber = "104")
@CardRegistration(set = "DPA", collectorNumber = "52")
@CardRegistration(set = "2XM", collectorNumber = "139")
public class RageReflection extends Card {

    public RageReflection() {
        addStaticEffect(new GrantKeywordEffect(Keyword.DOUBLE_STRIKE, Subjects.creaturesYouControl()));
    }
}
