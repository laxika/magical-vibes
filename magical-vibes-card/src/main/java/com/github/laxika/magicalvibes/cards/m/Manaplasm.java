package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostSelfByCastSpellManaValueEffect;

@CardRegistration(set = "ALA", collectorNumber = "138")
public class Manaplasm extends Card {

    public Manaplasm() {
        // Whenever you cast a spell, this creature gets +X/+X until end of turn,
        // where X is that spell's mana value.
        addEffect(EffectSlot.ON_CONTROLLER_CASTS_SPELL,
                new BoostSelfByCastSpellManaValueEffect(null));
    }
}
