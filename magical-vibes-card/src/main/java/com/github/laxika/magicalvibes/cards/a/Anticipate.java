package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;

@CardRegistration(set = "M19", collectorNumber = "44")
@CardRegistration(set = "M20", collectorNumber = "45")
public class Anticipate extends Card {

    public Anticipate() {
        addEffect(EffectSlot.SPELL, LookAtTopCardsEffect.chooseOneToHandRestOnBottom(new Fixed(3)));
    }
}
