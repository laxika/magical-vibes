package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.CantBlockEffect;
import com.github.laxika.magicalvibes.model.effect.ControlsAnotherSubtypeConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "XLN", collectorNumber = "147")
public class HeadstrongBrute extends Card {

    public HeadstrongBrute() {
        addEffect(EffectSlot.STATIC, new CantBlockEffect());
        addEffect(EffectSlot.STATIC, new ControlsAnotherSubtypeConditionalEffect(
                CardSubtype.PIRATE,
                new GrantKeywordEffect(Keyword.MENACE, GrantScope.SELF)
        ));
    }
}
