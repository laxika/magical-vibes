package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MadnessCast;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "INR", collectorNumber = "140")
@CardRegistration(set = "INR", collectorNumber = "393")
public class AlchemistsGreeting extends Card {

    public AlchemistsGreeting() {
        // Alchemist's Greeting deals 4 damage to target creature.
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEffect(4));

        // Madness {1}{R}
        addCastingOption(new MadnessCast("{1}{R}"));
    }
}
