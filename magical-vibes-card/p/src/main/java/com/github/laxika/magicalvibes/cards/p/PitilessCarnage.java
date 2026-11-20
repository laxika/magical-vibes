package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.AlternateHandCast;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.ManaCastingCost;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

import java.util.List;

@CardRegistration(set = "OTJ", collectorNumber = "98")
public class PitilessCarnage extends Card {

    public PitilessCarnage() {
        addSpellEffect(SacrificePermanentsEffect.anyNumber(new PermanentTruePredicate(), Subjects.you()));
        addSpellEffect(new DrawCardEffect(new AmountThisWay(StepKind.SACRIFICE)));

        // Plot {1}{B}{B}
        addCastingOption(new AlternateHandCast(List.of(new ManaCastingCost("{1}{B}{B}"))));
    }
}
