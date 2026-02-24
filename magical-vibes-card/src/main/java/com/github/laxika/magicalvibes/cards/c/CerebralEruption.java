package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardDealManaValueDamageEffect;

@CardRegistration(set = "SOM", collectorNumber = "86")
public class CerebralEruption extends Card {

    public CerebralEruption() {
        addEffect(EffectSlot.SPELL, new RevealTopCardDealManaValueDamageEffect(true, true, true));
    }
}
