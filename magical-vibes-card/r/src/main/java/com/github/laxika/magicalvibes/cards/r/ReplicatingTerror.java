package com.github.laxika.magicalvibes.cards.r;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.EachOpponentSacrificesNontokenCreatureConjuresDuplicatesEffect;

@CardRegistration(set = "YDSK", collectorNumber = "10")
public class ReplicatingTerror extends Card {

    public ReplicatingTerror() {
        addEffect(EffectSlot.SPELL, new EachOpponentSacrificesNontokenCreatureConjuresDuplicatesEffect());
    }
}
