package com.github.laxika.magicalvibes.cards.t;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;

@CardRegistration(set = "M10", collectorNumber = "160")
@CardRegistration(set = "M13", collectorNumber = "152")
@CardRegistration(set = "UDS", collectorNumber = "98")
@CardRegistration(set = "M19", collectorNumber = "165")
@CardRegistration(set = "KTK", collectorNumber = "124")
public class TrumpetBlast extends Card {

    public TrumpetBlast() {
        // Attacking creatures get +2/+0 until end of turn.
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(2, 0, new PermanentIsAttackingPredicate()));
    }
}
