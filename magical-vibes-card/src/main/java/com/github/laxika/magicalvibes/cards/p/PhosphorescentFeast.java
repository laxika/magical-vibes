package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.amount.ColorManaSymbolsInHand;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;

@CardRegistration(set = "EVE", collectorNumber = "72")
public class PhosphorescentFeast extends Card {

    public PhosphorescentFeast() {
        addEffect(EffectSlot.SPELL, new GainLifeEffect(
                new Scaled(new ColorManaSymbolsInHand(ManaColor.GREEN), 2)));
    }
}
