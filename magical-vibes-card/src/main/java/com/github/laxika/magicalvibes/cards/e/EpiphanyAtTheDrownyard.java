package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.amount.Fixed;
import com.github.laxika.magicalvibes.model.amount.Sum;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.RevealTopCardsAndSeparateEffect;

@CardRegistration(set = "SOI", collectorNumber = "59")
public class EpiphanyAtTheDrownyard extends Card {

    public EpiphanyAtTheDrownyard() {
        addEffect(EffectSlot.SPELL, new RevealTopCardsAndSeparateEffect(
                new Sum(new XValue(), new Fixed(1)), true));
    }
}
