package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.CardsInHandAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.QuantumRiddlerDrawReplacementEffect;

import java.util.List;

@CardRegistration(set = "EOE", collectorNumber = "72")
public class QuantumRiddler extends Card {

    public QuantumRiddler() {
        addEffect(EffectSlot.ON_ENTER_BATTLEFIELD, new DrawCardEffect());
        addEffect(EffectSlot.STATIC, new ConditionalEffect(
                new CardsInHandAtMost(1), new QuantumRiddlerDrawReplacementEffect()));
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{1}{U}"))));
    }
}
