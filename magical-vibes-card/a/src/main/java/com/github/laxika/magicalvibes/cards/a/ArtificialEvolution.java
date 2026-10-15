package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.ChangeColorTextEffect;

@CardRegistration(set = "ONS", collectorNumber = "67")
public class ArtificialEvolution extends Card {

    public ArtificialEvolution() {
        addEffect(EffectSlot.SPELL, ChangeColorTextEffect.creatureTypes(true));
    }
}
