package com.github.laxika.magicalvibes.cards.i;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.XValue;
import com.github.laxika.magicalvibes.model.condition.Kicked;
import com.github.laxika.magicalvibes.model.condition.RepeatedAdditionalCostPaid;
import com.github.laxika.magicalvibes.model.effect.ConditionalEffect;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.KickerEffect;
import com.github.laxika.magicalvibes.model.effect.RepeatableAdditionalManaCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

import java.util.List;

@CardRegistration(set = "APC", collectorNumber = "63")
public class Illuminate extends Card {

    public Illuminate() {
        addStaticEffect(new KickerEffect("{2}{R}"));
        addCastingCost(new RepeatableAdditionalManaCost(List.of("{3}{U}"), true, 1));

        target(TargetFilters.creature())
                .addSpellEffect(DealDamageEffect.toTargetCreature(new XValue()))
                .addSpellEffect(new ConditionalEffect(new Kicked(),
                        DealDamageEffect.to(Subjects.controllerOf(Subjects.target()), new XValue())));
        addSpellEffect(new ConditionalEffect(new RepeatedAdditionalCostPaid("{3}{U}"),
                new DrawCardEffect(new XValue())));
    }
}
