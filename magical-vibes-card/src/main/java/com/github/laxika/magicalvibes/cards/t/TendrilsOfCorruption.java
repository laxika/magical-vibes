package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.CardSubtype;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect;

@CardRegistration(set = "M10", collectorNumber = "114")
public class TendrilsOfCorruption extends Card {

    public TendrilsOfCorruption() {
        addEffect(EffectSlot.SPELL, new DealDamageToTargetCreatureEqualToControlledSubtypeCountEffect(CardSubtype.SWAMP, true));
    }
}
