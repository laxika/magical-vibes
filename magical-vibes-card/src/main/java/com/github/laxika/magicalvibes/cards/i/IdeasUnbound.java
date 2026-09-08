package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.RegisterDiscardAtNextEndStepEffect;

@CardRegistration(set = "SOK", collectorNumber = "40")
public class IdeasUnbound extends Card {

    public IdeasUnbound() {
        addEffect(EffectSlot.SPELL, new DrawCardEffect(3));
        addEffect(EffectSlot.SPELL, new RegisterDiscardAtNextEndStepEffect(3));
    }
}
