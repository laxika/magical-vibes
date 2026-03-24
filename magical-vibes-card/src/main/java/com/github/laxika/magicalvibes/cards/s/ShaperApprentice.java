package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ControlsAnotherSubtypeConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "XLN", collectorNumber = "75")
public class ShaperApprentice extends Card {

    public ShaperApprentice() {
        addEffect(EffectSlot.STATIC, new ControlsAnotherSubtypeConditionalEffect(
                CardSubtype.MERFOLK,
                new GrantKeywordEffect(Keyword.FLYING, GrantScope.SELF)
        ));
    }
}
