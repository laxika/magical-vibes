package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ExileInstantSorceryCardsInsteadOfGraveyardEffect;

@CardRegistration(set = "RTR", collectorNumber = "214")
@CardRegistration(set = "GK1", collectorNumber = "111")
public class DryadMilitant extends Card {

    public DryadMilitant() {
        addEffect(EffectSlot.STATIC, new ExileInstantSorceryCardsInsteadOfGraveyardEffect());
    }
}
