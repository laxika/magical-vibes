package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.Keyword;
import com.github.laxika.magicalvibes.model.effect.ControlsSubtypeConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.GrantKeywordEffect;
import com.github.laxika.magicalvibes.model.effect.GrantScope;

@CardRegistration(set = "ISD", collectorNumber = "3")
public class AngelicOverseer extends Card {

    public AngelicOverseer() {
        addEffect(EffectSlot.STATIC, new ControlsSubtypeConditionalEffect(
                CardSubtype.HUMAN,
                new GrantKeywordEffect(Keyword.HEXPROOF, GrantScope.SELF)
        ));
        addEffect(EffectSlot.STATIC, new ControlsSubtypeConditionalEffect(
                CardSubtype.HUMAN,
                new GrantKeywordEffect(Keyword.INDESTRUCTIBLE, GrantScope.SELF)
        ));
    }
}
