package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealUntilNonlandBottomThenDealManaValueDamageEffect;

@CardRegistration(set = "ONS", collectorNumber = "201")
@CardRegistration(set = "PC2", collectorNumber = "41")
public class ErraticExplosion extends Card {

    public ErraticExplosion() {
        addEffect(EffectSlot.SPELL, new RevealUntilNonlandBottomThenDealManaValueDamageEffect());
    }
}
