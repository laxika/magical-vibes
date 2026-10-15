package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.effect.ETBDoubleTriggerEffect;
import com.github.laxika.magicalvibes.model.filter.CardTruePredicate;

import java.util.List;

@CardRegistration(set = "EOE", collectorNumber = "78")
public class StarfieldVocalist extends Card {

    public StarfieldVocalist() {
        addEffect(EffectSlot.STATIC, new ETBDoubleTriggerEffect(new CardTruePredicate(), false));
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{1}{U}"))));
    }
}
