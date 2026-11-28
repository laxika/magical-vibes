package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CantHaveOrGainKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantNonAbilityEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "BNG", collectorNumber = "4")
public class ArchetypeOfCourage extends Card {

    public ArchetypeOfCourage() {
        addStaticEffect(new GrantKeywordEffect(Keyword.FIRST_STRIKE, Subjects.creaturesYouControl()));
        addStaticEffect(new RemoveKeywordEffect(Keyword.FIRST_STRIKE, Subjects.creaturesYourOpponentsControl()));
        addStaticEffect(new GrantNonAbilityEffectEffect(
                new CantHaveOrGainKeywordEffect(Keyword.FIRST_STRIKE), Subjects.creaturesYourOpponentsControl()));
    }
}
