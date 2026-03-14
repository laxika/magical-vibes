package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "M10", collectorNumber = "160")
public class TrumpetBlast extends Card {

    public TrumpetBlast() {
        // Attacking creatures get +2/+0 until end of turn.
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(2, 0, new PermanentIsAttackingPredicate()));
    }
}
