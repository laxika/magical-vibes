package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToAnyTargetEffect;
import com.github.laxika.magicalvibes.model.effect.DiscardRandomCardCost;

@CardRegistration(set = "TOR", collectorNumber = "115")
public class SonicSeizure extends Card {

    public SonicSeizure() {
        addEffect(EffectSlot.SPELL, new DiscardRandomCardCost());
        addEffect(EffectSlot.SPELL, new DealDamageToAnyTargetEffect(3));
    }
}
