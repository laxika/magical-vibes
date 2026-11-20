package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.StepKind;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.amount.Scaled;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentTruePredicate;

@CardRegistration(set = "MMQ", collectorNumber = "42")
public class Renounce extends Card {

    public Renounce() {
        addSpellEffect(SacrificePermanentsEffect.anyNumber(new PermanentTruePredicate(), Subjects.you()));
        addSpellEffect(new GainLifeEffect(new Scaled(new AmountThisWay(StepKind.SACRIFICE), 2)));
    }
}
