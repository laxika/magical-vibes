package com.github.laxika.magicalvibes.cards.c;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDamageToBlockedAttackersOnDeathEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "NPH", collectorNumber = "5")
public class CathedralMembrane extends Card {

    public CathedralMembrane() {
        addEffect(EffectSlot.ON_DEATH, new DealDamageToBlockedAttackersOnDeathEffect(6));
    }
}
