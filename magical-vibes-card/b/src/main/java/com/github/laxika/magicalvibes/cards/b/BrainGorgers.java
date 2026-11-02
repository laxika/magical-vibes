package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.MadnessCast;
import com.github.laxika.magicalvibes.model.effect.AnyPlayerMaySacrificeCreatureToCounterSpellEffect;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "PLC", collectorNumber = "65")
public class BrainGorgers extends Card {

    public BrainGorgers() {
        addTrigger(Triggers.youCastThisSpell(), new AnyPlayerMaySacrificeCreatureToCounterSpellEffect());
        addCastingOption(new MadnessCast("{1}{B}"));
    }
}
