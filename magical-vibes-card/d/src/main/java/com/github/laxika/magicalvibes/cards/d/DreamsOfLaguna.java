package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.FlashbackCast;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SurveilEffect;

@CardRegistration(set = "FIN", collectorNumber = "50")
public class DreamsOfLaguna extends Card {

    public DreamsOfLaguna() {
        addEffect(EffectSlot.SPELL, new SurveilEffect(1));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(1));
        addCastingOption(new FlashbackCast("{3}{U}"));
    }
}
