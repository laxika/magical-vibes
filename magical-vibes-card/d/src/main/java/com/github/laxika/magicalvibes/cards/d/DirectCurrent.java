package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.JumpStartCast;
import com.github.laxika.magicalvibes.model.effect.DealDamageEffect;

@CardRegistration(set = "GRN", collectorNumber = "96")
public class DirectCurrent extends Card {

    public DirectCurrent() {
        addSpellEffect(DealDamageEffect.toAnyTarget(2));
        addCastingOption(new JumpStartCast());
    }
}
