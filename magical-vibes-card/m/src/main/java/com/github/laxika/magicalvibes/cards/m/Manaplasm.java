package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.model.effect.BoostEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.amount.TargetSpellManaValue;
import com.github.laxika.magicalvibes.model.trigger.Triggers;

@CardRegistration(set = "ALA", collectorNumber = "138")
public class Manaplasm extends Card {

    public Manaplasm() {
        // Whenever you cast a spell, this creature gets +X/+X until end of turn,
        // where X is that spell's mana value.
        addTrigger(Triggers.youCastSpell().anySpell(),
                BoostEffect.self(new TargetSpellManaValue(), new TargetSpellManaValue()));
    }
}
