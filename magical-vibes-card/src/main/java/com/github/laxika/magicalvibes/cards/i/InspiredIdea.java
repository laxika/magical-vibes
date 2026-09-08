package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.condition.CastForAlternateCost;
import com.github.laxika.magicalvibes.model.condition.NotCondition;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.ReduceControllerMaxHandSizeEffect;

@CardRegistration(set = "VOW", collectorNumber = "64")
public class InspiredIdea extends Card {

    public InspiredIdea() {
        addCastingOption(AlternateHandCast.cleave("{3}{U}{U}", null));
        addEffect(EffectSlot.SPELL, new DrawCardEffect(3));
        addEffect(EffectSlot.SPELL, new ConditionalEffect(
                new NotCondition(new CastForAlternateCost()),
                new ReduceControllerMaxHandSizeEffect(3)));
    }
}
