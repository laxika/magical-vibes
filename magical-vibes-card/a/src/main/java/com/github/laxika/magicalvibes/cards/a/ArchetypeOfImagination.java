package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CantHaveOrGainKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "BNG", collectorNumber = "32")
@CardRegistration(set = "DSC", collectorNumber = "111")
@CardRegistration(set = "C18", collectorNumber = "81")
public class ArchetypeOfImagination extends Card {

    public ArchetypeOfImagination() {
        addStaticEffect(new GrantKeywordEffect(Keyword.FLYING, Subjects.creaturesYouControl()));
        addStaticEffect(new RemoveKeywordEffect(Keyword.FLYING, Subjects.creaturesYourOpponentsControl()));
        addStaticEffect(new GrantEffectEffect(
                new CantHaveOrGainKeywordEffect(Keyword.FLYING), Subjects.creaturesYourOpponentsControl()));
    }
}
