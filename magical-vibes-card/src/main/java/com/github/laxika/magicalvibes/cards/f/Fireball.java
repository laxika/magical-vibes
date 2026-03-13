package com.github.laxika.magicalvibes.cards.f;

import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.DealXDamageDividedEvenlyAmongTargetsEffect;
import com.github.laxika.magicalvibes.cards.CardRegistration;

@CardRegistration(set = "M10", collectorNumber = "136")
@CardRegistration(set = "M11", collectorNumber = "138")
public class Fireball extends Card {

    public Fireball() {
        setMinTargets(1);
        setMaxTargets(99);
        setAdditionalCostPerExtraTarget(1);
        addEffect(EffectSlot.SPELL, new DealXDamageDividedEvenlyAmongTargetsEffect());
    }
}
