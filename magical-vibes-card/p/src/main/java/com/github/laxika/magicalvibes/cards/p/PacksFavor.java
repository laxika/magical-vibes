package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;

@CardRegistration(set = "GRN", collectorNumber = "139")
public class PacksFavor extends Card {

    public PacksFavor() {
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(3, 3));
    }
}
