package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ForetellCast;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ScryEffect;

@CardRegistration(set = "KHM", collectorNumber = "46")
public class BeholdTheMultiverse extends Card {

    public BeholdTheMultiverse() {
        addEffect(EffectSlot.SPELL, new ScryEffect(2));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(2));
        addCastingOption(new ForetellCast("{1}{U}"));
    }
}
