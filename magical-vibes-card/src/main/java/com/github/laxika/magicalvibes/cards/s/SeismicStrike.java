package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect;

@CardRegistration(set = "M10", collectorNumber = "154")
public class SeismicStrike extends Card {

    public SeismicStrike() {
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect(CardSubtype.MOUNTAIN));
    }
}
