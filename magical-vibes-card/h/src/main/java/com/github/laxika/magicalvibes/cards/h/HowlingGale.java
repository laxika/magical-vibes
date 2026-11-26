package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

@CardRegistration(set = "ODY", collectorNumber = "244")
public class HowlingGale extends Card {

    public HowlingGale() {
        addSpellEffect(DealDamageEffect.toEachCreature(1,
                new PermanentHasKeywordPredicate(Keyword.FLYING)).andTo(Subjects.eachPlayer()));
        addCastingOption(new FlashbackCast("{1}{G}"));
    }
}
