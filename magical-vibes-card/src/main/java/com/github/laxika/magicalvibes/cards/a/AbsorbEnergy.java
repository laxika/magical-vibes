package com.github.laxika.magicalvibes.cards.a;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.CounterSpellEffect;
import com.github.laxika.magicalvibes.model.effect.PerpetuallyReduceCostForHandCardsSharingTargetSpellEffect;

@CardRegistration(set = "YMID", collectorNumber = "12")
public class AbsorbEnergy extends Card {

    public AbsorbEnergy() {
        addEffect(EffectSlot.SPELL, new PerpetuallyReduceCostForHandCardsSharingTargetSpellEffect());
        addEffect(EffectSlot.SPELL, new CounterSpellEffect());
    }
}
