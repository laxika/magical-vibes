package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "ODY", collectorNumber = "226")
public class VolcanicSpray extends Card {

    public VolcanicSpray() {
        addSpellEffect(DealDamageEffect.toEachCreature(1,
                new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING)))
                .andTo(Subjects.eachPlayer()));
        addCastingOption(new FlashbackCast("{1}{R}"));
    }
}
