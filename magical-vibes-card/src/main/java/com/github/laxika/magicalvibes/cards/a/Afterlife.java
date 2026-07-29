package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CreateTokenEffect;
import com.github.laxika.magicalvibes.model.effect.DestroyTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "MIR", collectorNumber = "1")
public class Afterlife extends Card {

    public Afterlife() {
        // Destroy target creature. It can't be regenerated. Its controller
        // creates a 1/1 white Spirit creature token with flying.
        target(TargetFilters.creature()).addEffect(EffectSlot.SPELL,
                new DestroyTargetPermanentEffect(true, CreateTokenEffect.whiteSpirit(1)));
    }
}
