package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MadnessCast;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayManaEffect;
import com.github.laxika.magicalvibes.model.effect.MayPayPayer;
import com.github.laxika.magicalvibes.model.effect.SacrificePermanentsEffect;
import com.github.laxika.magicalvibes.model.effect.Subjects;
import com.github.laxika.magicalvibes.model.filter.PermanentIsCreaturePredicate;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PLC", collectorNumber = "65")
public class BrainGorgers extends Card {

    public BrainGorgers() {
        addTrigger(Triggers.youCastThisSpell(), MayPayManaEffect.anyPlayerMay(MayPayPayer.ANY_PLAYER,
                new SacrificePermanentsEffect(1, new PermanentIsCreaturePredicate(), Subjects.acceptedPlayers()),
                "Sacrifice a creature? If a player does, Brain Gorgers is countered.",
                CounterSpellEffect.triggeringSpell(), null).payingAsEachAccepts());
        addCastingOption(new MadnessCast("{1}{B}"));
    }
}
