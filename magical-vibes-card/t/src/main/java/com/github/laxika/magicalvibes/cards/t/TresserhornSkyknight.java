package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.PreventDamageToSelfFromCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentHasKeywordPredicate;

@CardRegistration(set = "CSP", collectorNumber = "73")
public class TresserhornSkyknight extends Card {

    public TresserhornSkyknight() {
        addEffect(EffectSlot.STATIC,
                new PreventDamageToSelfFromCreaturesEffect(new PermanentHasKeywordPredicate(Keyword.FIRST_STRIKE)));
    }
}
