package com.github.laxika.magicalvibes.cards.l;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.SacrificeOtherCreatureOrDamageEffect;

@CardRegistration(set = "TSP", collectorNumber = "113")
public class LiegeOfThePit extends Card {

    public LiegeOfThePit() {
        addMorph("{B}{B}{B}{B}");
        addEffect(EffectSlot.UPKEEP_TRIGGERED, new SacrificeOtherCreatureOrDamageEffect(7));
    }
}
