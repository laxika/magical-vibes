package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;
import com.github.laxika.magicalvibes.model.filter.PermanentNotPredicate;

@CardRegistration(set = "ISD", collectorNumber = "161")
public class RollingTemblor extends Card {

    public RollingTemblor() {
        addEffect(EffectSlot.SPELL, new MassDamageEffect(2, false, false, new PermanentNotPredicate(new PermanentHasKeywordPredicate(Keyword.FLYING))));
        addCastingOption(new FlashbackCast("{4}{R}{R}"));
    }
}
