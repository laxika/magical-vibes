package com.github.laxika.magicalvibes.cards.q;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.condition.CardsInHandAtMost;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.QuantumRiddlerDrawReplacementEffect;
import com.github.laxika.magicalvibes.model.trigger.ObjectRelation;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

import java.util.List;

@CardRegistration(set = "EOE", collectorNumber = "72")
public class QuantumRiddler extends Card {

    public QuantumRiddler() {
        addTrigger(Triggers.enters(ObjectRelation.THIS), new DrawCardEffect());
        addStaticEffect(new ConditionalEffect(
                new CardsInHandAtMost(1), new QuantumRiddlerDrawReplacementEffect()));
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{1}{U}"))));
    }
}
