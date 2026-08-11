package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;

@CardRegistration(set = "M20", collectorNumber = "56")
public class DrawnFromDreams extends Card {

    public DrawnFromDreams() {
        addEffect(EffectSlot.SPELL, LookAtTopCardsEffect.chooseNToHandRestOnBottomRandom(
                new Fixed(7), 2));
    }
}
