package com.github.laxika.magicalvibes.cards.m;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "4ED", collectorNumber = "36")
public class Morale extends Card {

    public Morale() {
        // Attacking creatures get +1/+1 until end of turn.
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(1, 1, new PermanentIsAttackingPredicate()));
    }
}
