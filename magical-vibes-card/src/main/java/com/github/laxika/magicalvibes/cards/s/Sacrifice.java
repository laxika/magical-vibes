package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

@CardRegistration(set = "SUM", collectorNumber = "126")
public class Sacrifice extends Card {

    public Sacrifice() {
        addEffect(EffectSlot.SPELL, new SacrificeCreatureCost(true));
        addEffect(EffectSlot.SPELL, new AwardManaEffect(ManaColor.BLACK, new XValue()));
    }
}
