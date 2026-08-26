package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.DiscardCardTypeCost;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;

@CardRegistration(set = "FIN", collectorNumber = "143")
public class LaughingMad extends Card {

    public LaughingMad() {
        addEffect(EffectSlot.SPELL, new DiscardCardTypeCost(null, null));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
        addCastingOption(new FlashbackCast("{3}{R}"));
    }
}
