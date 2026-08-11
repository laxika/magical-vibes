package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

@CardRegistration(set = "ODY", collectorNumber = "244")
public class HowlingGale extends Card {

    public HowlingGale() {
        addEffect(EffectSlot.SPELL, new MassDamageEffect(1, false, true,
                new PermanentHasKeywordPredicate(Keyword.FLYING)));
        addCastingOption(new FlashbackCast("{1}{G}"));
    }
}
