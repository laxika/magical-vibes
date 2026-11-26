package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "SNC", collectorNumber = "158")
public class TakeToTheStreets extends Card {

    public TakeToTheStreets() {
        var citizen = new PermanentHasSubtypePredicate(CardSubtype.CITIZEN);
        addSpellEffect(BoostEffect.creaturesYouControl(2, 2));
        addSpellEffect(BoostEffect.creaturesYouControl(1, 1, citizen));
        addSpellEffect(new GrantKeywordEffect(Keyword.VIGILANCE, Subjects.otherCreaturesYouControl(), citizen));
    }
}
