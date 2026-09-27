package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;

@CardRegistration(set = "MH2", collectorNumber = "82")
public class DiscerningTaste extends Card {

    public DiscerningTaste() {
        addEffect(EffectSlot.SPELL,
                LookAtTopCardsEffect.chooseOneToHandRestToGraveyardGainLifeEqualToGreatestPower(4));
    }
}
