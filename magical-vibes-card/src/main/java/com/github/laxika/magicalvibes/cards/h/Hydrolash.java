package com.github.laxika.magicalvibes.cards.h;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.effect.DrawCardEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "ORI", collectorNumber = "59")
public class Hydrolash extends Card {

    public Hydrolash() {
        // Both sides' attacking creatures are affected; the set is fixed at resolution.
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(-2, 0, new PermanentIsAttackingPredicate()));
        addEffect(EffectSlot.SPELL, new DrawCardEffect());
    }
}
