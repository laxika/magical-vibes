package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealDividedDamageEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M10", collectorNumber = "136")
@CardRegistration(set = "M11", collectorNumber = "138")
@CardRegistration(set = "5ED", collectorNumber = "227")
public class Fireball extends Card {

    public Fireball() {
        setAdditionalCostPerExtraTarget(1);
        target(1, 99).addEffect(EffectSlot.SPELL, DealDividedDamageEffect.xDividedEvenly());
    }
}
