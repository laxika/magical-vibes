package com.github.laxika.magicalvibes.cards.e;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DestroyAllCreaturesSharingSacrificedCreatureTypeEffect;
import com.github.laxika.magicalvibes.model.effect.SacrificeCreatureCost;

@CardRegistration(set = "ONS", collectorNumber = "142")
public class EndemicPlague extends Card {

    public EndemicPlague() {
        addEffect(EffectSlot.SPELL, new SacrificeCreatureCost());
        addEffect(EffectSlot.SPELL, new DestroyAllCreaturesSharingSacrificedCreatureTypeEffect());
    }
}
