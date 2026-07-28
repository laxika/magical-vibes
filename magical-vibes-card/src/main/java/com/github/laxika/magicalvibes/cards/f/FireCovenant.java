package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.model.effect.PayXLifeCost;

@CardRegistration(set = "ICE", collectorNumber = "289")
public class FireCovenant extends Card {

    public FireCovenant() {
        // As an additional cost to cast this spell, pay X life.
        addEffect(EffectSlot.SPELL, new PayXLifeCost());
        // Fire Covenant deals X damage divided as you choose among any number of target creatures.
        addEffect(EffectSlot.SPELL, DealDividedDamageEffect.xAmongTargetCreatures());
    }
}
