package com.github.laxika.magicalvibes.cards.g;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.MadnessCast;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;

@CardRegistration(set = "INR", collectorNumber = "111")
public class GisasBidding extends Card {

    public GisasBidding() {
        // Create two 2/2 black Zombie creature tokens.
        addEffect(EffectSlot.SPELL, CreateTokenEffect.blackZombie(2));

        // Madness {2}{B}
        addCastingOption(new MadnessCast("{2}{B}"));
    }
}
