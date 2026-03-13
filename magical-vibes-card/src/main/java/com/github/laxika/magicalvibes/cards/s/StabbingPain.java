package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostTargetCreatureEffect;
import com.github.laxika.magicalvibes.model.effect.TapTargetPermanentEffect;

@CardRegistration(set = "M11", collectorNumber = "118")
public class StabbingPain extends Card {

    public StabbingPain() {
        addEffect(EffectSlot.SPELL, new BoostTargetCreatureEffect(-1, -1));
        addEffect(EffectSlot.SPELL, new TapTargetPermanentEffect());
    }
}
