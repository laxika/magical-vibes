package com.github.laxika.magicalvibes.cards.p;

import com.github.laxika.magicalvibes.cards.CardRegistration;
import com.github.laxika.magicalvibes.model.Card;
import com.github.laxika.magicalvibes.model.EffectSlot;
import com.github.laxika.magicalvibes.model.effect.BoostAllCreaturesEffect;
import com.github.laxika.magicalvibes.model.filter.PermanentIsAttackingPredicate;


@CardRegistration(set = "AKH", collectorNumber = "147")
public class PursueGlory extends Card {

    public PursueGlory() {
        // Attacking creatures get +2/+0 until end of turn.
        addEffect(EffectSlot.SPELL, new BoostAllCreaturesEffect(2, 0, new PermanentIsAttackingPredicate()));

        // Cycling {2} ({2}, Discard this card: Draw a card.) — discard cost is intrinsic.
        addCycling("{2}");
    }
}
