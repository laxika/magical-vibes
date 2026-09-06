package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;

@CardRegistration(set = "DTK", collectorNumber = "75")
public class SightBeyondSight extends Card {

    public SightBeyondSight() {
        addEffect(EffectSlot.SPELL,
                LookAtTopCardsEffect.chooseOneToHandRestOnBottom(new Fixed(2)));
    }
}
