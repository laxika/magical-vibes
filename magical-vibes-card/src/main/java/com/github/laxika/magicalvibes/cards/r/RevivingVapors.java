package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;

@CardRegistration(set = "INV", collectorNumber = "265")
public class RevivingVapors extends Card {

    public RevivingVapors() {
        addEffect(EffectSlot.SPELL,
                LookAtTopCardsEffect.chooseOneToHandRestToGraveyardGainLifeEqualToManaValue(3));
    }
}
