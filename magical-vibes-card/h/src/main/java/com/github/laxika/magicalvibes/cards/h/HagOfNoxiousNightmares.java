package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasSubtypePredicate;

@CardRegistration(set = "HBG", collectorNumber = "H22")
public class HagOfNoxiousNightmares extends Card {

    public HagOfNoxiousNightmares() {
        // Warlocks you control have menace.
        addStaticEffect(new GrantKeywordEffect(
                Keyword.MENACE, Subjects.creaturesYouControl(),
                new PermanentHasSubtypePredicate(CardSubtype.WARLOCK)));
    }
}
