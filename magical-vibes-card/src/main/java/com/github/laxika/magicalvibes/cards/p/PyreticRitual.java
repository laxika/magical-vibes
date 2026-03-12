package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.ManaColor;
import com.github.laxika.magicalvibes.model.effect.AwardManaEffect;

@CardRegistration(set = "M11", collectorNumber = "153")
public class PyreticRitual extends Card {

    public PyreticRitual() {
        addEffect(EffectSlot.SPELL, new AwardManaEffect(ManaColor.RED, 3));
    }
}
