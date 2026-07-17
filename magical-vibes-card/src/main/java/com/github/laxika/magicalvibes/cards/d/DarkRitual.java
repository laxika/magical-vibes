package com.github.laxika.magicalvibes.cards.d;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

@CardRegistration(set = "5ED", collectorNumber = "153")
public class DarkRitual extends Card {

    public DarkRitual() {
        addEffect(EffectSlot.SPELL, new AwardManaEffect(ManaColor.BLACK, 3));
    }
}
