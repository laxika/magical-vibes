package com.github.laxika.magicalvibes.cards.n;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;
import com.github.laxika.magicalvibes.model.effect.OpponentControlsSubtypeConditionalEffect;

@CardRegistration(set = "ISD", collectorNumber = "153")
public class NightRevelers extends Card {

    public NightRevelers() {
        addEffect(EffectSlot.STATIC, new OpponentControlsSubtypeConditionalEffect(
                CardSubtype.HUMAN,
                new GrantKeywordEffect(Keyword.HASTE, GrantScope.SELF)
        ));
    }
}
