package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.MassDamageEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

@CardRegistration(set = "CHK", collectorNumber = "209")
public class GaleForce extends Card {

    public GaleForce() {
        addEffect(EffectSlot.SPELL, new MassDamageEffect(new Fixed(5), false, false,
                new PermanentHasKeywordPredicate(Keyword.FLYING)));
    }
}
