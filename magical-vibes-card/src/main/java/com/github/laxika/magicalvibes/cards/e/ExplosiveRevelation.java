package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealUntilNonlandToHandRestToBottomDealManaValueDamageEffect;

@CardRegistration(set = "ROE", collectorNumber = "143")
public class ExplosiveRevelation extends Card {

    public ExplosiveRevelation() {
        addEffect(EffectSlot.SPELL, new RevealUntilNonlandToHandRestToBottomDealManaValueDamageEffect());
    }
}
