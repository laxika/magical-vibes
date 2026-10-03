package com.github.laxika.magicalvibes.cards.o;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.CreateTokensForDistinctDiscardedCardTypesEffect;
import com.github.laxika.magicalvibes.model.effect.DrawDiscardThenEffect;

@CardRegistration(set = "VOC", collectorNumber = "52")
public class OccultEpiphany extends Card {

    public OccultEpiphany() {
        addEffect(EffectSlot.SPELL, new DrawDiscardThenEffect(new XValue(),
                new CreateTokensForDistinctDiscardedCardTypesEffect(CreateTokenEffect.whiteSpirit(1))));
    }
}
