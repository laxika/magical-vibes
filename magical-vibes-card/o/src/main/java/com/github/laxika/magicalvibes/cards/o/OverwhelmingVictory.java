package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.EventValue;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TLE", collectorNumber = "123")
@CardRegistration(set = "TLE", collectorNumber = "196")
public class OverwhelmingVictory extends Card {

    public OverwhelmingVictory() {
        target(TargetFilters.creature())
                .addSpellEffect(DealDamageEffect.toTargetCreature(5))
                .addSpellEffect(BoostEffect.creaturesYouControl(new EventValue(), new Fixed(0)))
                .addSpellEffect(new GrantKeywordEffect(Keyword.TRAMPLE, Subjects.otherCreaturesYouControl()));
    }
}
