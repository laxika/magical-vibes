package com.github.laxika.magicalvibes.cards.v;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DiscardXCardsCost;
import com.github.laxika.magicalvibes.model.effect.ExileTargetPermanentEffect;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "TOR", collectorNumber = "21")
public class VengefulDreams extends Card {

    public VengefulDreams() {
        addEffect(EffectSlot.SPELL, new DiscardXCardsCost());
        targetExactlyX(TargetFilters.attackingCreature(), 100)
                .addEffect(EffectSlot.SPELL, new ExileTargetPermanentEffect());
    }
}
