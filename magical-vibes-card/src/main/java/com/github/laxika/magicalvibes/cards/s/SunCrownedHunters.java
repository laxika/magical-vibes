package com.github.laxika.magicalvibes.cards.s;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToTargetOpponentOrPlaneswalkerEffect;

@CardRegistration(set = "XLN", collectorNumber = "164")
public class SunCrownedHunters extends Card {

    public SunCrownedHunters() {
        // Enrage — Whenever this creature is dealt damage, it deals 3 damage to target opponent or planeswalker.
        addEffect(EffectSlot.ON_DEALT_DAMAGE, new DealDamageToTargetOpponentOrPlaneswalkerEffect(3));
    }
}
