package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.OpponentEffectsCantCauseSacrificeEffect;

@CardRegistration(set = "AVR", collectorNumber = "210")
public class SigardaHostOfHerons extends Card {

    public SigardaHostOfHerons() {
        // Flying and hexproof come from the Scryfall-loaded keywords.
        addEffect(EffectSlot.STATIC, new OpponentEffectsCantCauseSacrificeEffect());
    }
}
