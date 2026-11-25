package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MadnessCast;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentCost;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.effect.UnlessPaysEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PLC", collectorNumber = "65")
public class BrainGorgers extends Card {

    public BrainGorgers() {
        addTrigger(Triggers.youCastThisSpell(), UnlessPaysEffect.mayPay(Subjects.eachPlayer(),
                new SacrificePermanentCost(new PermanentIsCreaturePredicate(), "creature", false),
                CounterSpellEffect.triggeringSpell()));
        addCastingOption(new MadnessCast("{1}{B}"));
    }
}
