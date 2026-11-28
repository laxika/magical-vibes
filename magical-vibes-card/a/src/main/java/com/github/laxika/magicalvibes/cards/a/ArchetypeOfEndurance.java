package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CantHaveOrGainKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantNonAbilityEffectEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.RemoveKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;

@CardRegistration(set = "BNG", collectorNumber = "116")
public class ArchetypeOfEndurance extends Card {

    public ArchetypeOfEndurance() {
        addStaticEffect(new GrantKeywordEffect(Keyword.HEXPROOF, Subjects.creaturesYouControl()));
        addStaticEffect(new RemoveKeywordEffect(Keyword.HEXPROOF, Subjects.creaturesYourOpponentsControl()));
        addStaticEffect(new GrantNonAbilityEffectEffect(
                new CantHaveOrGainKeywordEffect(Keyword.HEXPROOF), Subjects.creaturesYourOpponentsControl()));
    }
}
