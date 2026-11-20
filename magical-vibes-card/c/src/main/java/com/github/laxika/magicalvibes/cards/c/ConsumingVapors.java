package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.AmountThisWay;
import com.github.laxika.magicalvibes.model.effect.GainLifeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;

@CardRegistration(set = "ROE", collectorNumber = "101")
@CardRegistration(set = "AFC", collectorNumber = "96")
@CardRegistration(set = "C17", collectorNumber = "105")
public class ConsumingVapors extends Card {

    public ConsumingVapors() {
        addSpellEffect(new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(), Subjects.targetPlayer()));
        addSpellEffect(new GainLifeEffect(AmountThisWay.sacrificedToughness()));
    }
}
