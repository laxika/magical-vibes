package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.PartySize;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.effect.LookAtTopCardsEffect;

@CardRegistration(set = "ZNR", collectorNumber = "81")
public class SkyclavePlunder extends Card {

    public SkyclavePlunder() {
        addEffect(EffectSlot.SPELL,
                LookAtTopCardsEffect.chooseNToHandRestOnBottomRandom(
                        new Sum(new Fixed(3), new PartySize()), 3));
    }
}
