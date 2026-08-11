package com.github.laxika.magicalvibes.cards.b;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.DelveCost;
import com.github.laxika.magicalvibes.model.filter.TargetFilters;

@CardRegistration(set = "KTK", collectorNumber = "130")
public class BecomeImmense extends Card {

    public BecomeImmense() {
        target(TargetFilters.creature())
                .addEffect(EffectSlot.SPELL, new DelveCost())
                .addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(6, 6));
    }
}
