package com.github.laxika.magicalvibes.cards.y;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ETBDoubleTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

@CardRegistration(set = "M20", collectorNumber = "220")
public class YarokTheDesecrated extends Card {

    public YarokTheDesecrated() {
        addEffect(EffectSlot.STATIC, new ETBDoubleTriggerEffect(new CardTruePredicate(), false));
    }
}
