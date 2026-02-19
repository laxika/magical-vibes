package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect;

@CardRegistration(set = "10E", collectorNumber = "238")
public class SpittingEarth extends Card {

    public SpittingEarth() {
        setNeedsTarget(true);
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect(CardSubtype.MOUNTAIN));
    }
}
